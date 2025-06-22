package lab5;

public class ThreadTest {
    public static void main(String[] args) throws InterruptedException {
        Thread t = new Thread(new MyRunnable());
        t.start();

        System.out.println("Main Thread sleeping for 5s...");
        Thread.sleep(5000);

        System.out.println("Interrupting the thread!");
        t.interrupt();
    }
}

