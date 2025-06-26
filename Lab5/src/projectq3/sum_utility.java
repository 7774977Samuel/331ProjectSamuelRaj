package projectq3;

public class sum_utility {
 public static int sum_to(int n){
     int total =0;
     for (int i = 0; i <= n; i++) {
         total +=i;// add each number up to n
     }
     return total;// return final sum
 }
}

//Shareddata
class Shareddata {
 public int A1, A2,A3;// thread a results
 public int B1,B2, B3;//thread b result

 public boolean A1done =false;
 public boolean B2done = false;
 public boolean A2done= false;
 public boolean B3done = false;

 public final Object lock =new Object();// shared lock object for thread sync
}

//threadA
class ThreadA extends Thread{
 private Shareddata data; // store reference to shared data

 public ThreadA(Shareddata data) {
     this.data= data;
 }

 public void run() {
     data.A1 =sum_utility.sum_to(500);// compute a1
     synchronized (data.lock){
         data.A1done = true;// set a1 done
         data.lock.notifyAll();// notify waiting threads
     }
     synchronized (data.lock) {
         while (!data.B2done){
             try { data.lock.wait();} catch (InterruptedException e){}
         }
     }
     data.A2 = data.B2 + sum_utility.sum_to(300);// compute a2 based on b2
     synchronized (data.lock){
         data.A2done = true;//set a2 done
         data.lock.notifyAll();// notify waiting threads
     }

     synchronized (data.lock) {
         while (!data.B3done){
             try { data.lock.wait(); } catch(InterruptedException e) {}// wait for b3
         }
     }
     data.A3 = data.B3 +sum_utility.sum_to(400);// compute a3 based on b3
 }
}
//threadB
class ThreadB extends Thread{
 private Shareddata data;

 public ThreadB(Shareddata data) {
     this.data= data;//store shared data reference
 }
 public void run(){
     data.B1 = sum_utility.sum_to(250);// compute b1

     synchronized (data.lock) {
         while (!data.A1done){
             try { data.lock.wait(); } catch (InterruptedException e){}// wait for a1
         }
     }
     data.B2 = data.A1 + sum_utility.sum_to(200);// compute b2 using a1
     synchronized (data.lock) {
         data.B2done = true;// set b2 done
         data.lock.notifyAll();// notify waiting threads
     }

     synchronized (data.lock) {
         while (!data.A2done) {
             try { data.lock.wait(); } catch (InterruptedException e) {}
         }
     }
     data.B3 = data.A2 + sum_utility.sum_to(400);// compute b3 using a2
     synchronized (data.lock){
         data.B3done = true;// set b3 done
         data.lock.notifyAll();
     }
 }
}

//threadC
class ThreadC extends Thread{
 private Shareddata data;

 public ThreadC(Shareddata data) {
     this.data = data; // reference to shared data
 }
 public void run(){
     synchronized (data.lock) {
         while (!data.B3done) {
             try { data.lock.wait(); }catch (InterruptedException e){}// wait for b3
         }
     }
     int result= data.A2 + data.B3;// final result using a2 and b3
     System.out.println("[ A2 + B3 = " +result);//print result
 }
}

