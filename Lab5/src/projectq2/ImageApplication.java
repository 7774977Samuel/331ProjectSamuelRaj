package projectq2;


import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicIntegerArray;
import javax.imageio.ImageIO;

public class ImageApplication {
    final static int l_max = 255;
    final static int number_of_loops = 3;
    final static int[] num_thread = {1, 2, 4, 8, 16};

    public static void main(String[] args) {
        String fileName1 = "c:/image/Rain_Tree.jpg";
        String fileName2 = "c:/image/Wr";

        colourImage img = new colourImage();
        imageReadWrite.readJpgImage(fileName1, img);

        System.out.println("Single-thread implementation:");
        for (int i = 0; i < number_of_loops; i++) {
            colourImage output = new colourImage();
            output.height = img.height;
            output.width = img.width;
            output.pixels = new short[img.height][img.width][3];

            long startTime = System.currentTimeMillis();
            equalizeHistogram(img, output);
            long endTime = System.currentTimeMillis();

            System.out.println("Run " + (i + 1) + ": " + (endTime - startTime) + " ms");
            imageReadWrite.writeJpgImage(output, fileName2 + "_single" + i + ".jpg");
        }

        for (int threads : num_thread) {
            System.out.println("\nMulti-thread implementation with " + threads + " threads (Design i-a, shared atomic histogram):");
            long totalTime = 0;
            for (int i = 0; i < number_of_loops; i++) {
                colourImage output = new colourImage();
                output.height = img.height;
                output.width = img.width;
                output.pixels = new short[img.height][img.width][3];

                long startTime = System.currentTimeMillis();
                equalizeHistogramMultiThreadSharedAtomic(img, output, threads);
                long endTime = System.currentTimeMillis();

                System.out.println("Run " + (i + 1) + ": " + (endTime - startTime) + " ms");
                totalTime += (endTime - startTime);
                imageReadWrite.writeJpgImage(output, fileName2 + "_multi_shared_atomic_" + threads + "_" + i + ".jpg");
            }
            System.out.println("Average time: " + (totalTime / number_of_loops) + " ms");
        }

        for (int threads : num_thread) {
            System.out.println("\nMulti-thread implementation with " + threads + " threads (Design i-b, alternative access pattern):");
            long totalTime = 0;
            for (int i = 0; i < number_of_loops; i++) {
                colourImage output = new colourImage();
                output.height = img.height;
                output.width = img.width;
                output.pixels = new short[img.height][img.width][3];

                long startTime = System.currentTimeMillis();
                equalizeHistogramMultiThreadSharedAtomicAlt(img, output, threads);
                long endTime = System.currentTimeMillis();

                System.out.println("Run " + (i + 1) + ": " + (endTime - startTime) + " ms");
                totalTime += (endTime - startTime);
                imageReadWrite.writeJpgImage(output, fileName2 + "_multi_shared_atomic_alt_" + threads + "_" + i + ".jpg");
            }
            System.out.println("Average time: " + (totalTime / number_of_loops) + " ms");
        }

        for (int threads : num_thread) {
            System.out.println("\nMulti-thread implementation with " + threads + " threads (Design ii, sub-histograms):");
            long totalTime = 0;
            for (int i = 0; i < number_of_loops; i++) {
                colourImage output = new colourImage();
                output.height = img.height;
                output.width = img.width;
                output.pixels = new short[img.height][img.width][3];

                long startTime = System.currentTimeMillis();
                equalizeHistogramMultiThreadSubHistograms(img, output, threads);
                long endTime = System.currentTimeMillis();

                System.out.println("Run " + (i + 1) + ": " + (endTime - startTime) + " ms");
                totalTime += (endTime - startTime);
                imageReadWrite.writeJpgImage(output, fileName2 + "_multi_sub_hist_" + threads + "_" + i + ".jpg");
            }
            System.out.println("Average time: " + (totalTime / number_of_loops) + " ms");
        }
    }

    // Single-thread implementation (already explained)
    public static void equalizeHistogram(colourImage input, colourImage output) {
        int size = input.height * input.width;
        for (int c = 0; c < 3; c++) {
            int[] histogram = new int[256];
            int[] cumulativeHist = new int[256];

            for (int i = 0; i < input.height; i++) {
                for (int j = 0; j < input.width; j++) {
                    int val = input.pixels[i][j][c];
                    histogram[val]++;
                }
            }
            cumulativeHist[0] = histogram[0];
            for (int i = 1; i <= l_max; i++) {
                cumulativeHist[i] = cumulativeHist[i - 1] + histogram[i];
            }
            for (int i = 0; i <= l_max; i++) {
                cumulativeHist[i] = (cumulativeHist[i] * l_max) / size;
            }
            for (int i = 0; i < input.height; i++) {
                for (int j = 0; j < input.width; j++) {
                    output.pixels[i][j][c] = (short) cumulativeHist[input.pixels[i][j][c]];
                }
            }
        }
    }

    // Design i-a: shared atomic histogram with row-based slicing
    public static void equalizeHistogramMultiThreadSharedAtomic(colourImage input, colourImage output, int numThreads) {
        int height = input.height;
        int width = input.width;
        int totalPixels = height * width;

        for (int c = 0; c < 3; c++) {
            AtomicIntegerArray sharedHist = new AtomicIntegerArray(256);
            Thread[] threads = new Thread[numThreads];
            int sliceHeight = height / numThreads;

            for (int t = 0; t < numThreads; t++) {
                int startRow = t * sliceHeight;
                int endRow = (t == numThreads - 1) ? height : (t + 1) * sliceHeight;

                threads[t] = new Thread(() -> {
                    for (int i = startRow; i < endRow; i++) {
                        for (int j = 0; j < width; j++) {
                            int val = input.pixels[i][j][c];
                            sharedHist.incrementAndGet(val);
                        }
                    }
                });
                threads[t].start();
            }

            for (Thread t : threads) {
                try {
                    t.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            int[] cumulativeHist = new int[256];
            cumulativeHist[0] = sharedHist.get(0);
            for (int i = 1; i <= l_max; i++) {
                cumulativeHist[i] = cumulativeHist[i - 1] + sharedHist.get(i);
            }
            for (int i = 0; i <= l_max; i++) {
                cumulativeHist[i] = (cumulativeHist[i] * l_max) / totalPixels;
            }
            for (int i = 0; i < height; i++) {
                for (int j = 0; j < width; j++) {
                    output.pixels[i][j][c] = (short) cumulativeHist[input.pixels[i][j][c]];
                }
            }
        }
    }

    // Design i-b: shared atomic histogram with column-based slicing (alternative memory access pattern)
    public static void equalizeHistogramMultiThreadSharedAtomicAlt(colourImage input, colourImage output, int numThreads) {
        int height = input.height;
        int width = input.width;
        int totalPixels = height * width;

        for (int c = 0; c < 3; c++) {
            AtomicIntegerArray sharedHist = new AtomicIntegerArray(256);
            Thread[] threads = new Thread[numThreads];
            int sliceWidth = width / numThreads;

            for (int t = 0; t < numThreads; t++) {
                int startCol = t * sliceWidth;
                int endCol = (t == numThreads - 1) ? width : (t + 1) * sliceWidth;

                threads[t] = new Thread(() -> {
                    for (int i = 0; i < height; i++) {
                        for (int j = startCol; j < endCol; j++) {
                            int val = input.pixels[i][j][c];
                            sharedHist.incrementAndGet(val);
                        }
                    }
                });
                threads[t].start();
            }

            for (Thread t : threads) {
                try {
                    t.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            int[] cumulativeHist = new int[256];
            cumulativeHist[0] = sharedHist.get(0);
            for (int i = 1; i <= l_max; i++) {
                cumulativeHist[i] = cumulativeHist[i - 1] + sharedHist.get(i);
            }
            for (int i = 0; i <= l_max; i++) {
                cumulativeHist[i] = (cumulativeHist[i] * l_max) / totalPixels;
            }
            for (int i = 0; i < height; i++) {
                for (int j = 0; j < width; j++) {
                    output.pixels[i][j][c] = (short) cumulativeHist[input.pixels[i][j][c]];
                }
            }
        }
    }

    // Design ii: per-thread private histograms merged after threads finish
    public static void equalizeHistogramMultiThreadSubHistograms(colourImage input, colourImage output, int numThreads) {
        int height = input.height;
        int width = input.width;
        int totalPixels = height * width;

        for (int c = 0; c < 3; c++) {
            int[][] subHistograms = new int[numThreads][256];
            Thread[] threads = new Thread[numThreads];
            int sliceHeight = height / numThreads;

            for (int t = 0; t < numThreads; t++) {
                final int threadIndex = t;
                int startRow = t * sliceHeight;
                int endRow = (t == numThreads - 1) ? height : (t + 1) * sliceHeight;

                threads[t] = new Thread(() -> {
                    int[] localHist = new int[256];
                    for (int i = startRow; i < endRow; i++) {
                        for (int j = 0; j < width; j++) {
                            int val = input.pixels[i][j][c];
                            localHist[val]++;
                        }
                    }
                    subHistograms[threadIndex] = localHist;
                });
                threads[t].start();
            }

            for (Thread t : threads) {
                try {
                    t.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            // Merge subHistograms into final histogram
            int[] histogram = new int[256];
            for (int i = 0; i < 256; i++) {
                for (int t = 0; t < numThreads; t++) {
                    histogram[i] += subHistograms[t][i];
                }
            }

            int[] cumulativeHist = new int[256];
            cumulativeHist[0] = histogram[0];
            for (int i = 1; i <= l_max; i++) {
                cumulativeHist[i] = cumulativeHist[i - 1] + histogram[i];
            }
            for (int i = 0; i <= l_max; i++) {
                cumulativeHist[i] = (cumulativeHist[i] * l_max) / totalPixels;
            }
            for (int i = 0; i < height; i++) {
                for (int j = 0; j < width; j++) {
                    output.pixels[i][j][c] = (short) cumulativeHist[input.pixels[i][j][c]];
                }
            }
        }
    }
}
