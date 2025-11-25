package Multithreading;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class ReadWriteLockExample {

    public int count = 0;

    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    private final Lock readLock = lock.readLock();
    private final Lock writeLock = lock.writeLock();

    public void increment() {
        writeLock.lock();
        try {
            count++;
            Thread.sleep(50);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            writeLock.unlock();
        }
    }

    public int getCount() {
        readLock.lock();
        try {
            return count;
        } finally {
            readLock.unlock();
        }
    }

    public static void main(String []args) {

        ReadWriteLockExample counter = new ReadWriteLockExample();

        Runnable read = new Runnable() {
            @Override
            public void run() {
                for(int i=0;i<10;i++){
                    System.out.println(Thread.currentThread().getName() + "reading " + counter.getCount());
                }
            }
        };

        Runnable write = new Runnable() {
            @Override
            public void run() {
                for(int i=0;i<10;i++){
                    counter.increment();
                    System.out.println(Thread.currentThread().getName() + "incremented");
                }
            }
        };

        Thread readThread1 = new Thread(read,"t1");
        Thread readThread2 = new Thread(read, "t2");
        Thread writeThread = new Thread(write, "t3");

        writeThread.start();
        readThread1.start();
        readThread2.start();

    }

}
