import org.jocl.*;
import java.nio.charset.StandardCharsets;
import java.nio.IntBuffer;

import static org.jocl.CL.*;

public class ParallelGpuCounter {
    private static cl_context context = null;
    private static cl_command_queue commandQueue = null;
    private static cl_program program = null;
    private static cl_kernel kernel = null;
    private static boolean initialized = false;

    private static final String programSource =
        "__kernel void count_occurrences(__global const char* text,\n" +
        "                                 const int text_len,\n" +
        "                                 __global const char* word,\n" +
        "                                 const int word_len,\n" +
        "                                 __global int* result) {\n" +
        "    int i = get_global_id(0);\n" +
        "    if (i + word_len > text_len) return;\n" +

        "    // Verifica delimitadores antes e depois da palavra\n" +
        "    char before = (i == 0) ? ' ' : text[i - 1];\n" +
        "    char after = (i + word_len < text_len) ? text[i + word_len] : ' ';\n" +
        "    if (((before >= 'a' && before <= 'z') || (before >= '0' && before <= '9')) ||\n" +
        "        ((after  >= 'a' && after  <= 'z') || (after  >= '0' && after  <= '9')))\n" +
        "        return;\n" +

        "    for (int j = 0; j < word_len; j++) {\n" +
        "        if (text[i + j] != word[j]) return;\n" +
        "    }\n" +

        "    atomic_inc(result);\n" +
        "}";

    private static void initOpenCL() {
        if (initialized) return;
        initialized = true;

        CL.setExceptionsEnabled(true);
        int[] numPlatformsArray = new int[1];
        clGetPlatformIDs(0, null, numPlatformsArray);
        cl_platform_id[] platforms = new cl_platform_id[numPlatformsArray[0]];
        clGetPlatformIDs(platforms.length, platforms, null);
        cl_platform_id platform = platforms[0];

        cl_device_id[] devices = new cl_device_id[1];
        clGetDeviceIDs(platform, CL_DEVICE_TYPE_GPU, 1, devices, null);
        cl_device_id device = devices[0];

        context = clCreateContext(null, 1, new cl_device_id[]{device}, null, null, null);
        commandQueue = clCreateCommandQueueWithProperties(context, device, null, null);
        program = clCreateProgramWithSource(context, 1, new String[]{programSource}, null, null);
        clBuildProgram(program, 0, null, null, null, null);
        kernel = clCreateKernel(program, "count_occurrences", null);
    }

    public static int countWordGPU(String text, String word) {
        initOpenCL();

        String textLower = text.toLowerCase();
        String wordLower = word.toLowerCase();

        byte[] textBytes = textLower.getBytes(StandardCharsets.UTF_8);
        byte[] wordBytes = wordLower.getBytes(StandardCharsets.UTF_8);
        int textLen = textBytes.length;
        int wordLen = wordBytes.length;

        if (textLen < wordLen || wordLen == 0) return 0;

        cl_mem memText = clCreateBuffer(context,
            CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR,
            Sizeof.cl_char * textLen, Pointer.to(textBytes), null);
        cl_mem memWord = clCreateBuffer(context,
            CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR,
            Sizeof.cl_char * wordLen, Pointer.to(wordBytes), null);
        cl_mem memResult = clCreateBuffer(context,
            CL_MEM_READ_WRITE, Sizeof.cl_int, null, null);

        int[] zero = new int[]{0};
        clEnqueueWriteBuffer(commandQueue, memResult, CL_TRUE, 0, Sizeof.cl_int, Pointer.to(zero), 0, null, null);

        clSetKernelArg(kernel, 0, Sizeof.cl_mem, Pointer.to(memText));
        clSetKernelArg(kernel, 1, Sizeof.cl_int, Pointer.to(new int[]{textLen}));
        clSetKernelArg(kernel, 2, Sizeof.cl_mem, Pointer.to(memWord));
        clSetKernelArg(kernel, 3, Sizeof.cl_int, Pointer.to(new int[]{wordLen}));
        clSetKernelArg(kernel, 4, Sizeof.cl_mem, Pointer.to(memResult));

        long[] globalWorkSize = new long[]{textLen - wordLen + 1};
        clEnqueueNDRangeKernel(commandQueue, kernel, 1, null, globalWorkSize, null, 0, null, null);
        clFinish(commandQueue);

        IntBuffer buffer = IntBuffer.allocate(1);
        clEnqueueReadBuffer(commandQueue, memResult, CL_TRUE, 0, Sizeof.cl_int, Pointer.to(buffer), 0, null, null);
        int count = buffer.get(0);

        clReleaseMemObject(memText);
        clReleaseMemObject(memWord);
        clReleaseMemObject(memResult);
        return count;
    }

    public static void cleanup() {
        if (!initialized) return;
        clReleaseKernel(kernel);
        clReleaseProgram(program);
        clReleaseCommandQueue(commandQueue);
        clReleaseContext(context);
        kernel = null;
        program = null;
        commandQueue = null;
        context = null;
        initialized = false;
    }
}
