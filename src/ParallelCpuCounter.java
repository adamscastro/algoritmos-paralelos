

import java.util.concurrent.*;
import java.util.*;

public class ParallelCpuCounter {
    public static int countWord(String text, String word, int numThreads)
            throws InterruptedException, ExecutionException {
        if (text == null || word == null || numThreads <= 0) {
            return 0;
        }

        String[] words = text.split("\\W+");
        int totalTokens = words.length;
        if (totalTokens == 0) {
            return 0;
        }

        numThreads = Math.min(numThreads, totalTokens);

        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        List<Future<Integer>> results = new ArrayList<>(numThreads);

        int chunkSize = totalTokens / numThreads;

        for (int i = 0; i < numThreads; i++) {
            int start = i * chunkSize;
            int end = (i == numThreads - 1) ? totalTokens : (i + 1) * chunkSize;
            final int s = start, e = end;

            results.add(executor.submit(() -> {
                int localCount = 0;
                for (int j = s; j < e; j++) {
                    if (words[j].equalsIgnoreCase(word)) {
                        localCount++;
                    }
                }
                return localCount;
            }));
        }

        executor.shutdown();

        int total = 0;
        for (Future<Integer> future : results) {
            total += future.get();
        }
        return total;
    }
}
