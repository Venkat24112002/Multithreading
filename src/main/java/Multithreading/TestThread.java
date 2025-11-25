package Multithreading;

public class TestThread extends Thread{

    @Override
    public void run() {
        System.out.println(Thread.currentThread().getName());
        for(int i=0;i<5;i++){
            System.out.println("hiii");
        }
    }

    public static void main(String args[]){
        System.out.println("main" + Thread.currentThread().getName());
        TestThread t1 = new TestThread();
        t1.start();
    }
}
