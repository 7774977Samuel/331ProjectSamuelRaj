package projectq2;

import java.util.concurrent.atomic.AtomicIntegerArray;

public class HistogramThread implements Runnable {
    private final colourImage img;
    private final int startRow, endRow, method, threadId, numThreads;
    private static final int LEVELS = 256;
    private static AtomicIntegerArray[] sharedHistograms = null;
    private static int[][] localHistograms = null;

    public HistogramThread(colourImage img, int startRow, int endRow, int method, int numThreads, int threadId) {
        this.img = img;
        this.startRow = startRow;
        this.endRow = endRow;
        this.method = method;
        this.threadId = threadId;
        this.numThreads = numThreads;

        if (method == 1 && sharedHistograms == null) {
            sharedHistograms = new AtomicIntegerArray[3];
            for (int c = 0; c < 3; c++)
                sharedHistograms[c] = new AtomicIntegerArray(LEVELS);
        }

        if (method == 2 && localHistograms == null) {
            localHistograms = new int[numThreads * 3][LEVELS];
        }
    }

    @Override
    public void run() {
        if (method == 1) { // Shared atomic histogram (Figure 2a or 2b)
            for (int y = startRow; y < endRow; y++) {
                for (int x = 0; x < img.width; x++) {
                    for (int c = 0; c < 3; c++) {
                        int val = img.pixels[y][x][c];
                        sharedHistograms[c].incrementAndGet(val);
                    }
                }
            }
            // Synchronize once all threads finish
            if (threadId == 0) {
                applyEqualizationFromAtomic(sharedHistograms, img);
            }

        } else if (method == 2) { // Thread-local histograms
            for (int y = startRow; y < endRow; y++) {
                for (int x = 0; x < img.width; x++) {
                    for (int c = 0; c < 3; c++) {
                        int val = img.pixels[y][x][c];
                        localHistograms[threadId * 3 + c][val]++;
                    }
                }
            }

            // Wait for all threads before merging (thread 0 does merge)
            if (threadId == 0) {
                int[][] merged = new int[3][LEVELS];
                for (int t = 0; t < numThreads; t++) {
                    for (int c = 0; c < 3; c++) {
                        for (int i = 0; i < LEVELS; i++) {
                            merged[c][i] += localHistograms[t * 3 + c][i];
                        }
                    }
                }
                applyEqualizationFromArray(merged, img);
            }
        }
    }

    private void applyEqualizationFromAtomic(AtomicIntegerArray[] histograms, colourImage img) {
        int size = img.width * img.height;
        for (int c = 0; c < 3; c++) {
            int[] cum = new int[LEVELS];
            cum[0] = histograms[c].get(0);
            for (int i = 1; i < LEVELS; i++)
                cum[i] = cum[i - 1] + histograms[c].get(i);
            for (int i = 0; i < LEVELS; i++)
                cum[i] = (cum[i] * (LEVELS - 1)) / size;

            for (int y = 0; y < img.height; y++)
                for (int x = 0; x < img.width; x++)
                    img.pixels[y][x][c] = (short) cum[img.pixels[y][x][c]];
        }
    }

    private void applyEqualizationFromArray(int[][] histograms, colourImage img) {
        int size = img.width * img.height;
        for (int c = 0; c < 3; c++) {
            int[] cum = new int[LEVELS];
            cum[0] = histograms[c][0];
            for (int i = 1; i < LEVELS; i++)
                cum[i] = cum[i - 1] + histograms[c][i];
            for (int i = 0; i < LEVELS; i++)
                cum[i] = (cum[i] * (LEVELS - 1)) / size;

            for (int y = 0; y < img.height; y++)
                for (int x = 0; x < img.width; x++)
                    img.pixels[y][x][c] = (short) cum[img.pixels[y][x][c]];
        }
    }
}
