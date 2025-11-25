package Multithreading;

public class TestRunnable implements Runnable{

    @Override
    public void run() {
        System.out.println(Thread.currentThread().getName());
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {

        }
    }

    public static void main(String args[]) throws InterruptedException {
        System.out.println("main" + Thread.currentThread().getName());
        Thread t1 = new Thread(new TestRunnable());
        System.out.println(Thread.currentThread().getState());
        t1.start();
        Thread.sleep(200);
        System.out.println(t1.getState());
        System.out.println(Thread.currentThread().getState());
        t1.join();
        System.out.println(t1.getState());
    }
}
