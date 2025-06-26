package projectq3;

//MainApp
public class Mainapp{
 public static void main(String[] args) throws InterruptedException{
     for (int i = 0; i < 100; i++) {
         Shareddata data = new Shareddata();// shared data object for communication
         ThreadA a = new ThreadA(data);
         ThreadB b = new ThreadB(data);
         ThreadC c = new ThreadC(data);
         // start the threads
         a.start();
         b.start();
         c.start();
         //waiting for the threads to finish
         a.join();
         b.join();
         c.join();

         int expectedResult = 190500 + 270700; //A2 + B3 = 461200
         int actualResult = data.A2 + data.B3;// calculate actual result
         if (actualResult != expectedResult){
             System.out.println("Error, mismatch at iteration "+ i);// report error
             break;
         }
     }

     System.out.println("All iterations completed");//success message
 }
}
