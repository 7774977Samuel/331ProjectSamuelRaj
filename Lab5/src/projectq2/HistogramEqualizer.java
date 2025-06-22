package projectq2;

import java.util.Arrays;

public class HistogramEqualizer {
    public static final int LEVELS = 256;

    // ================= SINGLE-THREADED VERSION =====================
    public static void equalizeSingleThread(colourImage img) {
        long startTime = System.nanoTime();

        for (int channel = 0; channel < 3; channel++) {
            int[] hist = new int[LEVELS];
            int[] cumHist = new int[LEVELS];
            int width = img.width, height = img.height;
            int size = width * height;

            // Step 1: Compute histogram
            for (int y = 0; y < height; y++)
                for (int x = 0; x < width; x++)
                    hist[img.pixels[y][x][channel]]++;

            // Step 2: Cumulative histogram
            cumHist[0] = hist[0];
            for (int i = 1; i < LEVELS; i++)
                cumHist[i] = cumHist[i - 1] + hist[i];

            for (int i = 0; i < LEVELS; i++)
                cumHist[i] = (cumHist[i] * (LEVELS - 1)) / size;

            // Step 3: Map the pixels
            for (int y = 0; y < height; y++)
                for (int x = 0; x < width; x++)
                    img.pixels[y][x][channel] = (short) cumHist[img.pixels[y][x][channel]];
        }

        long endTime = System.nanoTime();
        System.out.printf("Single-threaded Equalization Time: %.2f ms%n", (endTime - startTime) / 1e6);
    }

    // ================= MULTITHREADED VERSION =====================
    public static void equalizeMultiThread(colourImage img, int numThreads, int method) throws InterruptedException {
        long startTime = System.nanoTime();

        int height = img.height;
        HistogramThread[] threads = new HistogramThread[numThreads];
        Thread[] t = new Thread[numThreads];
        int blockSize = height / numThreads;

        for (int i = 0; i < numThreads; i++) {
            int startRow = i * blockSize;
            int endRow = (i == numThreads - 1) ? height : startRow + blockSize;
            threads[i] = new HistogramThread(img, startRow, endRow, method, numThreads, i);
            t[i] = new Thread(threads[i]);
            t[i].start();
        }

        for (int i = 0; i < numThreads; i++)
            t[i].join();

        long endTime = System.nanoTime();
        System.out.printf("Multithreaded Equalization (method %d, %d threads): %.2f ms%n",
                method, numThreads, (endTime - startTime) / 1e6);
    }
}
