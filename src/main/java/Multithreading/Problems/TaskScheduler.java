package Multithreading.Problems;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

class Node{
    Node next;
    Runnable task;

    public Node(Runnable task){
        this.task = task;
        next = null;
    }
}

public class TaskScheduler {

    private Node head;
    private Node tail;
    private Lock lock;
    private Condition tasksAvailable;
    private Condition allComplete;
    private Thread[] workerThreads;
    Boolean isShutdown;
    private int activeTaskCount;


    public TaskScheduler(int num){
        lock = new ReentrantLock();
        tasksAvailable = lock.newCondition();
        allComplete = lock.newCondition();
        isShutdown = false;
        activeTaskCount = 0;
        workerThreads = new Thread[num];

        for(int i=0;i<num;i++){
            workerThreads[i] = new Thread(() -> {

                while(!isShutdown){
                    Runnable task = null;

                    lock.lock();
                    try {
                        while (head == null && !isShutdown) {
                            tasksAvailable.await();
                        }
                        if (isShutdown) {
                            break;
                        }
                        task = head.task;
                        head = head.next;
                        if(head == null){
                            tail = null;
                        }
                    } catch(Exception e){
                        Thread.currentThread().interrupt();
                        break;
                    } finally {
                        lock.unlock();
                    }

                    try{
                        task.run();
                    } catch (Exception e){
                        System.err.println("Task failed: " + e.getMessage());
                        e.printStackTrace();
                    }

                    lock.lock();
                    try {
                        activeTaskCount --;

                        if(activeTaskCount == 0){
                            allComplete.signalAll();
                        }
                    } finally {
                        lock.unlock();
                    }
                }
            }, "Worker-" + i);
            workerThreads[i].start();
        }
    }


    public void schedule(Runnable task){
        lock.lock();
        try{
            Node newNode = new Node(task);
            if(tail == null){
                head = tail = newNode;
            } else {
                tail.next = newNode;
                tail = newNode;
            }

            activeTaskCount++;

            tasksAvailable.signal();
        } finally {
            lock.unlock();
        }
    }

    //added interrupted exception because waitUntil is being called main thread
    public void waitUntilComplete () throws InterruptedException {
        lock.lock();
        try{
            while(activeTaskCount > 0){
                allComplete.await();
            }
        } catch(Exception e){
            Thread.currentThread().interrupt();
        } // if we dont to throw the cheked expections
        finally {
            lock.unlock();
        }
    }

    public void shutdown(){
        lock.lock();
        try{
            isShutdown = true;
            tasksAvailable.signalAll();
        } finally {
            lock.unlock();
        }
    }

    public static void main(String []args){
        TaskScheduler scheduler = new TaskScheduler(4);

        for(int i=0;i<30;i++){
            int taskId = i;
            scheduler.schedule(()-> {
                System.out.println("Task " + taskId + " started on " +
                        Thread.currentThread().getName());

                try {
                    Thread.sleep(1000); // Simulate work
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                System.out.println("Task " + taskId + " completed");
            });
        }

        System.out.println("\nMain: All tasks scheduled. Now waiting for completion...");
       try {
           scheduler.waitUntilComplete();
       } catch (Exception e){
           scheduler.shutdown();
       }
//        if(Thread.currentThread().isInterrupted()){
//
//        }

    }

}
