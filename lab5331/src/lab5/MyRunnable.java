package lab5;

public class MyRunnable implements Runnable {
    public void run() {
        System.out.println("Hello from my thread!");

        try {
            // Sleep for 15 seconds
            Thread.sleep(15 * 1000);
        } catch (InterruptedException e) {
            System.out.println("Thread was interrupted during sleep.");
            return; // Exit early on interrupt
        }

        System.out.println("Woke up from sleep!");
    }
}
