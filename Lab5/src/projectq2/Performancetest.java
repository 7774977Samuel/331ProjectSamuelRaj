package projectq2;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public class Performancetest {
    private static final int NUM_ITERATIONS = 3;// number of times each test is repeated for averaging 
    public static void main(String[] args){
        try {
            BufferedImage image= ImageIO.read(new File("/C:/Users/samun/Documents/GitHub/331ProjectSamuelRa/Lab5/image/Rain_Tree.jpg"));
            System.out.println("Histogram testing results:");
         // test single-threaded equalization and print time
            long singlethread_time =test_single(image);
            System.out.printf("Single threaded: %.2f ms\n", singlethread_time /1000000.0);
            
            int[] threadcount= {2,4, 8,16};// different thread counts to test

            // test multithreaded shared histogram with different thread counts        
            System.out.println("\nShared histogram:");
            for (int num_threads: threadcount) {
                long multithread_time = testMultithreaded_shared(image,num_threads);
                double speedup= (double) singlethread_time/ multithread_time;
                System.out.printf("%d threads: %.2f ms (speedup: %.2fx)\n", 
                    num_threads, multithread_time / 000000.0, speedup);
            }
   
            System.out.println("Sub Histograms:");
         // test multithreaded sub-histogram approach with different thread counts
            for (int numThreads: threadcount){
                long multithread_Time = testMultithreaded_subhistograms(image, numThreads);
                double speedup = (double) singlethread_time /multithread_Time;
                System.out.printf("%d threads: %.2f ms (speedup %.2fx)\n", 
                    numThreads, multithread_Time /1000000.0, speedup);
            }

            System.out.println("Equalized image saved as Rain_Tree_new.jpg");
        } catch (IOException e){
            // print error if image cannot be loaded
            System.err.println("Error loading image:" + e.getMessage());
        }
    }
 //  method to test single-threaded performance   
    private static long test_single(BufferedImage image) {
        long totalTime = 0;
        BufferedImage outputimage = null;
        for (int i = 0; i < NUM_ITERATIONS;i++) {
            long startTime =System.nanoTime();
            outputimage= Histogram_Equalization.SingleThreaded.equalizehistogram(image);
            long endtime = System.nanoTime();
            totalTime +=(endtime - startTime);
        }
        try {
        	// save the output image
            ImageIO.write(outputimage,"jpg", new File("/C:/Users/samun/Documents/GitHub/331ProjectSamuelRa/Lab5/image/Rain_Tree_new.jpg"));
            System.out.println("Equalized image saved as Rain_Tree_new.jpg");
        } catch (IOException e){
            System.err.println("Error saving image: "+ e.getMessage());
        }
        return totalTime / NUM_ITERATIONS;
    }
 // helper method to test shared histogram multithreaded performance
    private static long testMultithreaded_shared(BufferedImage image, int numThreads) {
        long totaltime= 0;
        for (int i = 0; i < NUM_ITERATIONS; i++) {
            long starttime = System.nanoTime();
            Histogram_Equalization.Multithread.equalizeHistogram(image, numThreads);
            long endtime = System.nanoTime();
            totaltime+= (endtime -starttime);
        }
        return totaltime / NUM_ITERATIONS;
    }
 // method to test subhistogram multithreaded performance  
    private static long testMultithreaded_subhistograms(BufferedImage image, int numThreads) {
        long totaltime = 0;
        for (int i = 0;i < NUM_ITERATIONS; i++){
            long startTime = System.nanoTime();
            Histogram_Equalization.Multithread_subhistograms.equalizeHistogram(image, numThreads);
            long endtime = System.nanoTime();
            totaltime += (endtime -startTime);
        }
        return totaltime /NUM_ITERATIONS;
    }
}