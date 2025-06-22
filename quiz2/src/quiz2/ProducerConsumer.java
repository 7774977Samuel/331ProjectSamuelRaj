package quiz2;

class Message {
    private String msg;
    
    public Message(String str){
        this.msg=str;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String str) {
        this.msg=str;
    }

}


class Consumer implements Runnable{
    
    private Message msg;
    
    public Consumer(Message m){
        msg=m;
    }

    @Override
    public void run() {
        String name = Thread.currentThread().getName();
        System.out.println(name+" is now running...");
        synchronized (msg) {
            try{
                System.out.println(name+" Inside the C.S, about to wait @time :"+System.currentTimeMillis());
                 msg.wait();
            }catch(InterruptedException e){
                e.printStackTrace();
            }
            System.out.println(name+" thread got resumed at~ :"+System.currentTimeMillis());
            
            System.out.println(name+" Message retrieved: "+msg.getMsg());
        }
    }

}


class Producer implements Runnable {

    private Message msg;
    
    public Producer(Message msg) {
        this.msg = msg;
    }

    @Override
    public void run() {
        String name = Thread.currentThread().getName();
        System.out.println(name+" is now running...");
        
        try {
         
       	
            synchronized (msg) {
                System.out.println(name+" inside the C.S about to notify@time: "+System.currentTimeMillis());
                msg.setMsg(" Producer Set Message");
                msg.notify();
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        
            System.out.println(name+" end: "+System.currentTimeMillis());
    }// run

}

public class ProducerConsumer {

    public static void main(String[] args) {
        Message msg = new Message("Initial Message");

        Consumer consumer = new Consumer(msg);
        Thread c= new Thread(consumer,"Consumer"); c.start();
        
        Producer producer = new Producer(msg);
        Thread p=new Thread(producer, "Producer");p.start();
        System.out.println("All the threads are started");
       
        
        try
        {
        	c.join();
        	p.join();           
        }
        catch (InterruptedException e)
        {
              e.printStackTrace();
        }
        finally{
               System.out.println(">>>>>>>>>>>>>>>>>>>> END OF PROGRAM EXECUTION <<<<<<<<<<<<<<<<<<");
        }
        
       
        
    }

}
