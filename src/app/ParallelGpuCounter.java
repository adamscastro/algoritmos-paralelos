package app;

import org.jocl.*;
import java.nio.charset.StandardCharsets;
import java.nio.IntBuffer;

import static org.jocl.CL.*;

/**
 * ParallelGpuCounter.java
 *
 * Versão ajustada para contar a ocorrência de uma palavra (word) dentro de um texto (text)
 * usando OpenCL em GPU, fazendo comparação case-insensitive. 
 * Converte o texto e a palavra em lowercase antes de mandar para a GPU.
 */
public class ParallelGpuCounter {
    // Variáveis estáticas para manter contexto, fila, programa e kernel em memória
    private static cl_context        context        = null;
    private static cl_command_queue  commandQueue   = null;
    private static cl_program        program        = null;
    private static cl_kernel         kernel         = null;
    private static boolean           initialized    = false;

    // Fonte do kernel em OpenCL: compara byte a byte, contando apenas palavras inteiras
    private static final String programSource =
        "__kernel void count_occurrences(__global const char* text,\n" +
        "                                 const int text_len,\n" +
        "                                 __global const char* word,\n" +
        "                                 const int word_len,\n" +
        "                                 __global int* result) {\n" +
        "    int i = get_global_id(0);\n" +
        "    // Só processar até (text_len - word_len)\n" +
        "    if (i + word_len > text_len) return;\n" +
        "\n" +
        "    // Delimitador antes\n" +
        "    if (i > 0) {\n" +
        "        char c = text[i - 1];\n" +
        "        if ((c >= 'a' && c <= 'z') || (c >= '0' && c <= '9')) {\n" +
        "            return;\n" +
        "        }\n" +
        "    }\n" +
        "\n" +
        "    // Delimitador depois\n" +
        "    int endPos = i + word_len;\n" +
        "    if (endPos < text_len) {\n" +
        "        char c = text[endPos];\n" +
        "        if ((c >= 'a' && c <= 'z') || (c >= '0' && c <= '9')) {\n" +
        "            return;\n" +
        "        }\n" +
        "    }\n" +
        "\n" +
        "    // Compara byte a byte (tanto text[] quanto word[] já estão em lowercase)\n" +
        "    for (int j = 0; j < word_len; j++) {\n" +
        "        if (text[i + j] != word[j]) {\n" +
        "            return;\n" +
        "        }\n" +
        "    }\n" +
        "\n" +
        "    // Se bateu, incrementa contador\n" +
        "    atomic_inc(result);\n" +
        "}";

    /**
     * Inicializa o ambiente OpenCL (contexto, fila, programa e kernel) apenas uma vez.
     */
    private static void initOpenCL() {
        if (initialized) return;
        initialized = true;

        CL.setExceptionsEnabled(true);

        // 1) Obter plataformas
        int[] numPlatformsArray = new int[1];
        clGetPlatformIDs(0, null, numPlatformsArray);
        cl_platform_id[] platforms = new cl_platform_id[numPlatformsArray[0]];
        clGetPlatformIDs(platforms.length, platforms, null);
        cl_platform_id platform = platforms[0];

        // 2) Obter dispositivo GPU
        cl_device_id[] devices = new cl_device_id[1];
        clGetDeviceIDs(platform, CL_DEVICE_TYPE_GPU, 1, devices, null);
        cl_device_id device = devices[0];

        // 3) Criar contexto
        context = clCreateContext(null, 1, new cl_device_id[]{device}, null, null, null);

        // 4) Criar fila de comando
        commandQueue = clCreateCommandQueueWithProperties(context, device, null, null);

        // 5) Criar e compilar programa
        program = clCreateProgramWithSource(context, 1, new String[]{programSource}, null, null);
        clBuildProgram(program, 0, null, null, null, null);

        // 6) Criar kernel
        kernel = clCreateKernel(program, "count_occurrences", null);
    }

    /**
     * Conta, de forma case-insensitive, as ocorrências exatas de 'word' em 'text' usando GPU.
     * @param text  Texto completo (qualquer mistura de maiúsculas/minúsculas).
     * @param word  Palavra a ser contada (ex: "the", "THE", "ThE").
     * @return      Quantidade de vezes que 'word' aparece como palavra inteira (ignora case).
     */
    public static int countWordGPU(String text, String word) {
        // Inicializa OpenCL (apenas na primeira chamada)
        initOpenCL();

        // 1) Converte tudo para lowercase (para fazer comparação case-insensitive)
        String textLower = text.toLowerCase();
        String wordLower = word.toLowerCase();

        // 2) Converte para bytes UTF-8
        byte[] textBytes = textLower.getBytes(StandardCharsets.UTF_8);
        byte[] wordBytes = wordLower.getBytes(StandardCharsets.UTF_8);
        int textLen = textBytes.length;
        int wordLen = wordBytes.length;

        // 3) Cria buffers na GPU
        cl_mem memText = clCreateBuffer(
            context,
            CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR,
            Sizeof.cl_char * textLen,
            Pointer.to(textBytes),
            null
        );
        cl_mem memWord = clCreateBuffer(
            context,
            CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR,
            Sizeof.cl_char * wordLen,
            Pointer.to(wordBytes),
            null
        );
        cl_mem memResult = clCreateBuffer(
            context,
            CL_MEM_READ_WRITE,
            Sizeof.cl_int,
            null,
            null
        );

        // 4) Zera o buffer de resultado
        int[] zeroArray = new int[]{0};
        clEnqueueWriteBuffer(
            commandQueue,
            memResult,
            CL_TRUE,
            0,
            Sizeof.cl_int,
            Pointer.to(zeroArray),
            0,
            null,
            null
        );

        // 5) Ajusta argumentos do kernel
        clSetKernelArg(kernel, 0, Sizeof.cl_mem,    Pointer.to(memText));
        clSetKernelArg(kernel, 1, Sizeof.cl_int,    Pointer.to(new int[]{textLen}));
        clSetKernelArg(kernel, 2, Sizeof.cl_mem,    Pointer.to(memWord));
        clSetKernelArg(kernel, 3, Sizeof.cl_int,    Pointer.to(new int[]{wordLen}));
        clSetKernelArg(kernel, 4, Sizeof.cl_mem,    Pointer.to(memResult));

        // 6) Define o tamanho de trabalho correto
        int workSize = textLen - wordLen + 1;
        if (workSize <= 0) {
            // Palavra maior que texto
            clReleaseMemObject(memText);
            clReleaseMemObject(memWord);
            clReleaseMemObject(memResult);
            return 0;
        }
        long[] globalWorkSize = new long[]{ workSize };

        // 7) Executa o kernel
        clEnqueueNDRangeKernel(
            commandQueue,
            kernel,
            1,
            null,
            globalWorkSize,
            null,
            0,
            null,
            null
        );
        clFinish(commandQueue);

        // 8) Lê o resultado (quantas ocorrências foram contadas)
        IntBuffer intBuffer = IntBuffer.allocate(1);
        clEnqueueReadBuffer(
            commandQueue,
            memResult,
            CL_TRUE,
            0,
            Sizeof.cl_int,
            Pointer.to(intBuffer),
            0,
            null,
            null
        );
        int count = intBuffer.get(0);

        // 9) Libera buffers desta execução
        clReleaseMemObject(memText);
        clReleaseMemObject(memWord);
        clReleaseMemObject(memResult);

        return count;
    }

    /**
     * Libera todo o ambiente OpenCL (kernel, programa, fila e contexto).
     * Deve ser chamado uma única vez ao final da aplicação, se desejar limpar recursos.
     */
    public static void cleanup() {
        if (!initialized) {
            return;
        }
        clReleaseKernel(kernel);
        clReleaseProgram(program);
        clReleaseCommandQueue(commandQueue);
        clReleaseContext(context);

        kernel        = null;
        program       = null;
        commandQueue  = null;
        context       = null;
        initialized   = false;
    }
}
