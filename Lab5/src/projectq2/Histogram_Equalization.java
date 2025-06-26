package projectq2;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import java.util.concurrent.atomic.AtomicIntegerArray;

public class Histogram_Equalization {
	 // single-threaded histogram equalization method
    public static class SingleThreaded {
        public static BufferedImage equalizehistogram(BufferedImage image) {
            int width = image.getWidth();
            int height= image.getHeight();
            int size = width *height;
            // arrays to store pixel values for each color channel
            int[] redPixels = new int[size];
            int[] greenPixels= new int[size];
            int[]bluePixels = new int[size];
           // separate the image into red, green, and blue channels
            int index = 0;
            for (int y= 0; y < height; y++) {
                for (int x = 0;x < width; x++){
                    int rgb = image.getRGB(x, y);
                    redPixels [index] =(rgb >>16) & 0xFF;
                    greenPixels[index]= (rgb >> 8)& 0xFF;
                    bluePixels[index] =rgb & 0xFF;
                    index++;
                }
            }
            int[] equalizedred= equalizechannel(redPixels);// apply equalization to each channel
            int[] equalizedgreen= equalizechannel(greenPixels);
            int[]equalizedblue =equalizechannel(bluePixels);
            // create new image with equalized values
            BufferedImage result =new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            index = 0;
            for (int y = 0; y < height; y++){
                for (int x = 0; x < width; x++) {
                    int rgb = (equalizedred[index]<< 16) |(equalizedgreen[index]<< 8)| equalizedblue[index];
                    result.setRGB(x, y,rgb);
                    index++;
                }
            }
            return result;
        }
        // method to perform histogram equalization on a single channel
        private static int[] equalizechannel(int[] pixels) {
            int[] histogram = new int[256];
            int[]cumulativehistogram =new int[256];
            int[] output =new int[pixels.length];

            for (int pixel : pixels) {
                // calculate histogram
                histogram[pixel]++;
            }
             // compute cumulative histogram
            cumulativehistogram[0] =histogram[0];
            for (int i = 1; i < 256;i++){
                cumulativehistogram[i] = cumulativehistogram[i - 1]+ histogram[i];
            }// normalize cumulative histogram
            for (int i= 0; i < 256; i++) {
                cumulativehistogram[i] =(cumulativehistogram[i] *255) /pixels.length;
            }// map old pixel values to new ones
            for (int i =0; i < pixels.length; i++) {
                output[i]= cumulativehistogram[pixels[i]];
            }
            return output;
        }
    }
    // runnable class for building shared histogram in parallel
    static class Shared_histogram_runnable implements Runnable{
        private final int[] pixels;
        private final int start, end;
        private final AtomicIntegerArray shared_histogram;

        public Shared_histogram_runnable(int[] pixels, int start,int end, AtomicIntegerArray sharedHistogram) {
            this.pixels =pixels;
            this.start= start;
            this.end = end;
            this.shared_histogram= sharedHistogram;
        }

        @Override
        public void run() {// increment shared histogram for assigned chunk
            for (int i = start; i < end; i++) {
                shared_histogram.incrementAndGet(pixels[i]);
            }
        }
    }
    // multithreaded histogram equalization using shared histogram
    public static class Multithread {
        public static BufferedImage equalizeHistogram(BufferedImage image, int numThreads) {
            int width = image.getWidth();
            int height = image.getHeight();
         // process each color channel in parallel
            int[] redResult = processChannel(extractchannel(image,0), numThreads);
            int[] greenResult= processChannel(extractchannel(image, 1),numThreads);
            int[]blueResult = processChannel(extractchannel(image, 2), numThreads);
         // construct result image
            BufferedImage result =new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            int index = 0;
            for (int y = 0;y < height; y++) {
                for (int x = 0; x < width; x++) {
                    int rgb = (redResult[index] <<16) | (greenResult[index] << 8) | blueResult[index];
                    result.setRGB(x, y, rgb);
                    index++;
                }
            }
            return result;
        }
        // processes one channel using multiple threads with shared histogram
        private static int[] processChannel(int[] pixels,int numThreads){
            AtomicIntegerArray sharedhistogram =new AtomicIntegerArray(256);
            Thread[] threads = new Thread[numThreads];
            int chunkSize= pixels.length /numThreads;
         
            // create and start threads
            for (int t = 0; t < numThreads; t++) {
                int start = t * chunkSize;
                int end = (t == numThreads - 1) ? pixels.length: (t + 1) *chunkSize;
                threads[t] = new Thread(
                    new Shared_histogram_runnable(pixels, start, end,sharedhistogram),
                    "Shared histogram thread-" + t
                );
                threads[t].start();
            } // wait for threads to finish
            for (Thread thread :threads) {
                try {
                    thread.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            // compute cumulative histogram
            int[] cumulativehist =new int[256];
            cumulativehist[0] = sharedhistogram.get(0);
            for (int i = 1;i < 256; i++) {
                cumulativehist[i] =cumulativehist[i - 1] + sharedhistogram.get(i);
            }
         // normalize cumulative histogram
            for (int i = 0; i < 256; i++) {
                cumulativehist[i] = (cumulativehist[i] * 255) /pixels.length;
            }
            // apply new pixel values
            int[] output = new int[pixels.length];
            for (int i = 0;i < pixels.length; i++) {
                output[i] = cumulativehist[pixels[i]];
            }
            return output;
        }
     // extracts one channel from the image (0 = red, 1 = green, 2 = blue)
        private static int[] extractchannel(BufferedImage image, int channel) {
            int width = image.getWidth();
            int height = image.getHeight();
            int[] channelData = new int[width * height];

            int index = 0;
            for (int y = 0;y < height; y++) {
                for (int x = 0; x < width; x++) {
                    int rgb = image.getRGB(x, y);
                    switch (channel) {
                        case 0:channelData[index] =(rgb >> 16) &0xFF; break;
                        case 1: channelData[index]= (rgb >> 8) & 0xFF;break;
                        case 2: channelData[index]= rgb & 0xFF;break;
                    }
                    index++;
                }
            }
            return channelData;
        }
    }
    // task for creating a local histogram in sub-histogram approach
    static class Sub_Histogram_Task implements Runnable {
        private final int[] pixels;
        private final int start, end;
        private final int[] localHistogram;

        public Sub_Histogram_Task(int[] pixels,int start, int end, int[] localHistogram) {
            this.pixels = pixels;
            this.start= start;
            this.end = end;
            this.localHistogram =localHistogram;
        }

        @Override
        public void run() {// fill local histogram
            for (int i = start; i < end;i++) {
                localHistogram[pixels[i]]++;
            }
        }
    }
 // multithreaded histogram equalization using sub histograms
    public static class Multithread_subhistograms {
        public static BufferedImage equalizeHistogram(BufferedImage image, int numThreads) {
            int width = image.getWidth();
            int height= image.getHeight();

            // process all channels using sub-histogram method
            int[]redResult = process_channel_Subhistograms(extractChannel(image, 0), numThreads);
            int[] greenResult= process_channel_Subhistograms(extractChannel(image, 1), numThreads);
            int[] blueResult =process_channel_Subhistograms(extractChannel(image, 2),numThreads);
            
            // create final output image
            BufferedImage result= new BufferedImage(width, height,BufferedImage.TYPE_INT_RGB);
            int index = 0;
            for (int y = 0;y < height; y++) {
                for (int x = 0; x < width;x++) {
                    int rgb =(redResult[index] << 16) |(greenResult[index] << 8) |blueResult[index];
                    result.setRGB(x, y, rgb);
                    index++;
                }
            }
            return result;
        }
     // process pixels using separate histogram per thread
        private static int[] process_channel_Subhistograms(int[] pixels, int numThreads) {
            Thread[] threads= new Thread[numThreads];
            int[][] subHistograms =new int[numThreads][256];
            int chunkSize = pixels.length/ numThreads;
            
            // start all threads to compute local histograms
            for (int t = 0; t < numThreads; t++) {
                int start = t *chunkSize;
                int end = (t== numThreads - 1) ? pixels.length : (t + 1) * chunkSize;
                threads[t] =new Thread(new Sub_Histogram_Task(pixels,start, end, subHistograms[t]),"HistThread-" + t);
                threads[t].start();
            }
            // wait for all threads to finish
            for (Thread thread : threads) {
                try {
                    thread.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            int[] final_histogram = new int[256];
            for (int i = 0; i < 256; i++) {
                for (int t = 0; t < numThreads;t++){
                    final_histogram[i]+= subHistograms[t][i];
                }
            }
            // compute and normalize cumulative histogram
            int[] cumulativeHist =new int[256];
            cumulativeHist[0] = final_histogram[0];
            for (int i = 1;i < 256; i++) {
                cumulativeHist[i] =cumulativeHist[i - 1] + final_histogram[i];
            }
            for (int i = 0;i < 256; i++) {
                cumulativeHist[i]= (cumulativeHist[i] *255)/ pixels.length;
            }
            int[] output =new int[pixels.length];
            for (int i = 0;i < pixels.length; i++) {
                output[i]= cumulativeHist[pixels[i]];
            }
            return output;
        }
        // extract one color channel from image

        private static int[] extractChannel(BufferedImage image, int channel) {
            int width = image.getWidth();
            int height = image.getHeight();
            int[] Cdata = new int[width * height];

            int index = 0;
            for (int y = 0; y < height;y++) {
                for (int x= 0; x < width; x++) {
                    int rgb =image.getRGB(x, y);
                    switch (channel) {
                        case 0: Cdata[index]= (rgb >> 16)& 0xFF; break;
                        case 1: Cdata[index] = (rgb >>8) & 0xFF;break;
                        case 2:Cdata[index] = rgb & 0xFF; break;
                    }
                    index++;
                }
            }
            return Cdata;
        }
    }
    // main method for testing sub-histogram equalization
    public static void main(String[] args){
        try {
            BufferedImage input= ImageIO.read(new File("/C:/Users/samun/Documents/GitHub/331ProjectSamuelRa/Lab5/image/Rain_Tree.jpg"));
            BufferedImage output =Multithread_subhistograms.equalizeHistogram(input,4);
            ImageIO.write(output, "jpg", new File("/C:/Users/samun/Documents/GitHub/331ProjectSamuelRa/Lab5/image/Rain_Tree_equalized.jpg"));
            System.out.println("equalized image saved as Rain_Tree_equalized.jpg");
        } catch (IOException e){
            System.err.println("Error" +e.getMessage());
        }
    }
    
}