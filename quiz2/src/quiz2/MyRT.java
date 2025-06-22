package quiz2;

public class MyRT extends RealtimeThread {

	int threadNum;
	
	public MyRT(int num, int priority) {
		this.threadNum = num;
		this.setPriority(priority);
	}

	public void run() {
		System.out.println("Thread#" + threadNum + " Starting");
		while(!this.isInterrupted()) {
			System.out.println("Thread#" + threadNum + " Working");
			//Perform an operation which take 200ms
			some200msOperation();
		}
		System.out.println("Thread#" + threadNum + " Ending");
	}

public static void main(String[] args) throws InterruptedException {
	
	Thread.currentThread().setPriority(20);
	MyRT rt1 = new MyRT(1, 11);
	rt1.start();
	Thread.sleep(1000);
	MyRT rt2 = new MyRT(2, 12);
	rt2.start();
	Thread.sleep(1000);
	MyRT rt3 = new MyRT(3, 13);
	rt3.start();
	Thread.sleep(1000);
	rt1.interrupt();
	rt2.interrupt();
	rt3.interrupt();
}
}
