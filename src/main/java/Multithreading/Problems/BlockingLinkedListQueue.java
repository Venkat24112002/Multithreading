package Multithreading.Problems;

import java.util.LinkedList;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class BlockingLinkedListQueue<T> {
    private int capacity;
    private LinkedList<T> list;
    private Lock lock;
    private Condition notEmpty;
    private Condition notFull;

    public BlockingLinkedListQueue(int capacity){
        list = new LinkedList<>();
        lock = new ReentrantLock();
        notEmpty = lock.newCondition();
        notFull = lock.newCondition();
        capacity = capacity;
    }

    public void enqueue(T value) throws InterruptedException {
        lock.lock();
        try {
            while(list.size() == capacity){
                notFull.await();
            }
            list.addLast(value);
            notEmpty.signal();
        } catch (Exception e){

        } finally {
            lock.unlock();
        }
    }

    public T deque() {
        lock.lock();
        T value = null;
        try {
            while (list.size() == 0) {
                notEmpty.await();
            }
            value = list.removeFirst();
            notFull.signal();
        } catch (Exception e) {

        } finally {
            lock.unlock();
            return value;
        }
    }

    public int size(){
        lock.lock();
        try {
            return list.size();
        } finally {
            lock.unlock();
        }
    }

}




//// The blocking queue class
//class BlockingQueue<T> {
//    T[] array;
//    Object lock = new Object();
//    int size = 0;
//    int capacity;
//    int head = 0;
//    int tail = 0;
//    @SuppressWarnings("unchecked")
//    public BlockingQueue(int capacity) {
//        // The casting results in a warning
//        array = (T[]) new Object[capacity];
//        this.capacity = capacity;
//    }
//    public void enqueue(T item) throws InterruptedException {
//        // synchronized means only 1 thread can invoke the euqueue/dequeue
//        // method. Hence, when we test for size variable, no other thread
//        // is modifying it
//        synchronized (lock) {
//            while (size == capacity) {
//                lock.wait();
//            } // If the current size of the queue == capacity
//            // then we know we'll need to block the caller of the method.
//            // The loop's predicate would become false, as soon as,
//            // another thread performs a dequeue.
//
//            // reset tail to the beginning if the tail is already
//            // at the end of the backing array
//            if (tail == capacity) {
//                tail = 0;
//            }
//            array[tail] = item; // place the item in the array
//            size++;
//            tail++;
//            // don't forget to notify any other threads waiting on
//            // a change in value of size. There might be consumers (waiting to dequeue)
//            // waiting for the queue to have at least one element..
//            // If no thread is waiting, then the signal will simply go
//            // unnoticed and be ignored, which wouldn't affect the correct working of our class. This would be an instance of missed signal that we have talked about earlier.
//            lock.notifyAll();
//        }
//    }
//    public T dequeue() throws InterruptedException {
//        T item = null;
//        synchronized (lock) {
//            while (size == 0) { // block if there's nothing to dequeue
//                lock.wait();
//            }
//            if (head == capacity) { // We need to reset head of the queue back to zero in-case it's pointing past the end of the array.
//                head = 0;
//            }
//
//            // store the reference to the object being dequeued
//            // and overwrite with null
//            item = array[head];
//            array[head] = null;
//            head++;
//            size--;
//
//            // don't forget to call notify, there might be another thread
//            // blocked in the enqueue method.
//            lock.notifyAll();
//        }
//        return item;
//    }
//}
//
//class Demonstration {
//    public static void main( String args[] ) throws Exception{
//        final BlockingQueue<Integer> q = new BlockingQueue<Integer>(5); // size 5
//        Thread t1 = new Thread(new Runnable() {
//            @Override
//            public void run() {
//                try {
//                    for (int i = 0; i < 50; i++) {
//                        q.enqueue(new Integer(i));
//                        System.out.println("enqueued " + i);
//                    }
//                } catch (InterruptedException ie) {
//                }
//            }
//        });
//        Thread t2 = new Thread(new Runnable() {
//            @Override
//            public void run() {
//                try {
//                    for (int i = 0; i < 25; i++) {
//                        System.out.println("Thread 2 dequeued: " + q.dequeue());
//                    }
//                } catch (InterruptedException ie) {
//                }
//            }
//        });
//        Thread t3 = new Thread(new Runnable() {
//            @Override
//            public void run() {
//                try {
//                    for (int i = 0; i < 25; i++) {
//                        System.out.println("Thread 3 dequeued: " + q.dequeue());
//                    }
//                } catch (InterruptedException ie) {
//                }
//            }
//        });
//        t1.start();
//        Thread.sleep(4000);
//        t2.start();
//        t2.join();
//        t3.start();
//        t1.join();
//        t3.join();
//    }
//}
