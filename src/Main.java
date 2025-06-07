import java.io.FileWriter;
import java.nio.file.*;
import java.util.*;

public class Main {

    public static void main(String[] args) throws Exception {
        Files.createDirectories(Path.of("resultados"));

        if (!Files.exists(Path.of("resultados/resultados.csv"))) {
            try (FileWriter fw = new FileWriter("resultados/resultados.csv", true)) {
                fw.write("Arquivo,Metodo,Threads,Ocorrencias,TempoMs\n");
            }
        }

        if (args.length >= 2) {
            String textoOriginal = Files.readString(Path.of(args[0]));
            String texto = TextCleaner.clean(textoOriginal);
            Map<String, Long> medias = new LinkedHashMap<>();
            Map<String, List<Long>> detalhes = new LinkedHashMap<>();
            runAllAndSave(args[0], texto, args[1], true, medias, detalhes);
        } else {
            String pasta = "amostras";
            String palavra = "the";
            int repeticoes = 3;

            try (DirectoryStream<Path> stream = Files.newDirectoryStream(Path.of(pasta), "*.txt")) {
                for (Path arquivo : stream) {
                    String textoOriginal = Files.readString(arquivo);
                    String txt = TextCleaner.clean(textoOriginal);
                    for (int i = 0; i < repeticoes; i++) {
                        Map<String, Long> medias = new LinkedHashMap<>();
                        Map<String, List<Long>> detalhes = new LinkedHashMap<>();
                        runAllAndSave(arquivo.toString(), txt, palavra, false, medias, detalhes);
                    }
                }
            }

            javax.swing.SwingUtilities.invokeLater(() -> {
                GuiMain gui = new GuiMain();
                gui.setVisible(true);
            });
        }
    }

    public static List<String> runAllAndSave(
            String arquivo,
            String text,
            String word,
            boolean verbose,
            Map<String, Long> mediasOut,
            Map<String, List<Long>> detalhesOut) throws Exception {

        List<String> logs = new ArrayList<>();
        FileWriter fw = new FileWriter("resultados/resultados.csv", true);
        int repeticoes = 3;

        // SerialCPU
        List<Long> serialTempos = new ArrayList<>();
        for (int i = 1; i <= repeticoes; i++) {
            long start = System.nanoTime();
            int count = SerialCounter.countWord(text, word);
            long time = (System.nanoTime() - start) / 1_000_000;
            serialTempos.add(time);
            String log = String.format("SerialCPU [amostra %d]: %d ocorrências em %d ms\n", i, count, time);
            if (verbose) System.out.println(log);
            logs.add(log);
            fw.write(String.format("%s,SerialCPU,1,%d,%d\n", arquivo, count, time));
        }
        long avgSerial = serialTempos.stream().mapToLong(Long::longValue).sum() / repeticoes;
        mediasOut.put("SerialCPU", avgSerial);
        detalhesOut.put("SerialCPU", serialTempos);

        // ParallelCPU
        int[] threadCounts = {1, 2, 4, 8, 12, 16, 24};
        for (int t : threadCounts) {
            List<Long> tempos = new ArrayList<>();
            for (int i = 1; i <= repeticoes; i++) {
                long start = System.nanoTime();
                int count = ParallelCpuCounter.countWord(text, word, t);
                long time = (System.nanoTime() - start) / 1_000_000;
                tempos.add(time);
                String log = String.format("ParallelCPU-%dT [amostra %d]: %d ocorrências em %d ms\n", t, i, count, time);
                if (verbose) System.out.println(log);
                logs.add(log);
                fw.write(String.format("%s,ParallelCPU,%d,%d,%d\n", arquivo, t, count, time));
            }
            String key = "ParallelCPU-" + t + "T";
            long avg = tempos.stream().mapToLong(Long::longValue).sum() / repeticoes;
            mediasOut.put(key, avg);
            detalhesOut.put(key, tempos);
        }

        // ParallelGPU
        List<Long> gpuTempos = new ArrayList<>();
        for (int i = 1; i <= repeticoes; i++) {
            long start = System.nanoTime();
            int count = ParallelGpuCounter.countWordGPU(text, word);
            long time = (System.nanoTime() - start) / 1_000_000;
            gpuTempos.add(time);
            String log = String.format("ParallelGPU [amostra %d]: %d ocorrências em %d ms\n", i, count, time);
            if (verbose) System.out.println(log);
            logs.add(log);
            fw.write(String.format("%s,ParallelGPU,1,%d,%d\n", arquivo, count, time));
        }
        long avgGpu = gpuTempos.stream().mapToLong(Long::longValue).sum() / repeticoes;
        mediasOut.put("ParallelGPU", avgGpu);
        detalhesOut.put("ParallelGPU", gpuTempos);

        fw.close();
        return logs;
    }
}
