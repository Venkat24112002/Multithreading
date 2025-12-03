package Multithreading.Problems;

/*
 *  Congress Bathroom Problem
 *  -------------------------
 *  There is a bathroom in Congress shared by two political parties:
 *  Democrats and Republicans.
 *
 *  Rules:
 *    1. The bathroom has only 3 stalls, so at most 3 people can be inside at once.
 *    2. Only one party may use the bathroom at a time.
 *       - If Democrats are inside, all Republicans must wait.
 *       - If Republicans are inside, all Democrats must wait.
 *    3. If the bathroom becomes empty and there are people waiting from the
 *       opposite party, they get priority to enter next.
 *    4. No starvation:
 *       - Neither party should be blocked forever if members are waiting.
 */

import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;



public class Bathroom {

    enum Party{
        NONE,
        DEMOCRAT,
        REPUBLICAN
    }


    private Party inUseBy = Party.NONE;
    private int democratsWaiting = 0;
    private int republicansWaiting = 0;
    private int currentCount = 0;

    private final Semaphore bathroomSemaphore = new Semaphore(3);
    private final Lock lock = new ReentrantLock(true);

    //gates
    private final Semaphore democratGate = new Semaphore(0);
    private final Semaphore republicGate = new Semaphore(0);

    public void democratUseBathroom() throws InterruptedException {

        lock.lock();
        democratsWaiting++;

        //if bathroom is empty or democrats are using it proceed
        if(inUseBy == Party.NONE ){
            inUseBy = Party.DEMOCRAT;
            democratsWaiting--;
            lock.unlock();
        } else {
            lock.unlock();
            democratGate.acquire();
            lock.lock();
            democratsWaiting--;
            lock.unlock();
        }

        bathroomSemaphore.acquire();

        lock.lock();
        currentCount++;
        lock.unlock();

        useBathroom("Democrat");

        lock.lock();
        currentCount--;
        bathroomSemaphore.release();

        if(currentCount == 0){
            inUseBy = Party.NONE;
            if(republicansWaiting > 0){
                inUseBy = Party.REPUBLICAN;
                int toRelease = Math.min(republicansWaiting,3);
                republicGate.release(toRelease);
            } else if (democratsWaiting > 0) {
                inUseBy = Party.DEMOCRAT;
                int toRelease = Math.min(democratsWaiting, 3);
                democratGate.release(toRelease);
            }
        }
        lock.unlock();

    }


    private void useBathroom(String party) {
        System.out.println(party + " using bathroom. Count: " + currentCount);
        try { Thread.sleep(1000); } catch (InterruptedException e) {}
        System.out.println(party + " leaving bathroom.");
    }

    public void republicanUseBathroom() throws InterruptedException {

        lock.lock();
        republicansWaiting++;

        if (inUseBy == Party.NONE) {
            inUseBy = Party.REPUBLICAN;
            republicansWaiting--;
            lock.unlock();
        } else {
            lock.unlock();
            republicGate.acquire();
            lock.lock();
            republicansWaiting--;
            lock.unlock();
        }

        bathroomSemaphore.acquire();
        lock.lock();
        currentCount++;
        lock.unlock();
        useBathroom("Republic");

        lock.lock();
        currentCount--;
        bathroomSemaphore.release();

        if(currentCount == 0){
            inUseBy = Party.NONE;
            if(democratsWaiting > 0){
                democratGate.release(Math.min(3,democratsWaiting));
            } else if (republicansWaiting > 0){
                republicGate.release(Math.min(3,republicansWaiting));
            }
        }
        lock.unlock();
    }

    public static void main(String[] args) throws InterruptedException {
        Bathroom bathroom = new Bathroom();

        Runnable democrat = () -> {
            try {
                bathroom.democratUseBathroom();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.err.println(Thread.currentThread().getName() + " interrupted");
            }
        };

        Runnable republican = () -> {
            try {
                bathroom.republicanUseBathroom();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.err.println(Thread.currentThread().getName() + " interrupted");
            }
        };

        System.out.println("=== Test 1: Balanced arrival ===");
        for (int i = 0; i < 5; i++) {
            new Thread(democrat, "Democrat-" + (i+1)).start();
            new Thread(republican, "Republican-" + (i+1)).start();
            Thread.sleep(100); // Stagger arrivals slightly
        }

//        Thread.sleep(8000);
//
//        System.out.println("\n=== Test 2: Democrats arrive first ===");
//        for (int i = 0; i < 7; i++) {
//            new Thread(democrat, "Democrat-" + (i+6)).start();
//        }
//        Thread.sleep(500);
//        for (int i = 0; i < 4; i++) {
//            new Thread(republican, "Republican-" + (i+6)).start();
//        }
//
//        Thread.sleep(8000);
//
//        System.out.println("\n=== Test 3: Republicans arrive first ===");
//        for (int i = 0; i < 6; i++) {
//            new Thread(republican, "Republican-" + (i+10)).start();
//        }
//        Thread.sleep(500);
//        for (int i = 0; i < 3; i++) {
//            new Thread(democrat, "Democrat-" + (i+13)).start();
//        }
    }
}


//public class CongressBathroom {
//
//    // Enum to represent which party is currently using the bathroom
//    private enum Party {
//        NONE, DEMOCRAT, REPUBLICAN
//    }
//
//    private Party inUseBy = Party.NONE;
//    private int currentCount = 0;
//    private final Semaphore bathroomSemaphore = new Semaphore(3); // Max 3 people inside
//    private final Lock lock = new ReentrantLock(true); // Fair lock to prevent starvation
//    private final Condition condition = lock.newCondition();
//
//    public void democratUseBathroom() throws InterruptedException {
//        lock.lock();
//        try {
//            while (inUseBy != Party.NONE && inUseBy != Party.DEMOCRAT) {
//                condition.await();
//            }
//            inUseBy = Party.DEMOCRAT;
//            bathroomSemaphore.acquire();
//            currentCount++;
//        } finally {
//            lock.unlock();
//        }
//
//        useBathroom("Democrat");
//
//        lock.lock();
//        try {
//            currentCount--;
//            bathroomSemaphore.release();
//            if (currentCount == 0) {
//                inUseBy = Party.NONE;
//                condition.signalAll();
//            }
//        } finally {
//            lock.unlock();
//        }
//    }
//
//    public void republicanUseBathroom() throws InterruptedException {
//        lock.lock();
//        try {
//            while (inUseBy != Party.NONE && inUseBy != Party.REPUBLICAN) {
//                condition.await();
//            }
//            inUseBy = Party.REPUBLICAN;
//            bathroomSemaphore.acquire();
//            currentCount++;
//        } finally {
//            lock.unlock();
//        }
//
//        useBathroom("Republican");
//
//        lock.lock();
//        try {
//            currentCount--;
//            bathroomSemaphore.release();
//            if (currentCount == 0) {
//                inUseBy = Party.NONE;
//                condition.signalAll();
//            }
//        } finally {
//            lock.unlock();
//        }
//    }
//
//    private void useBathroom(String party) {
//        System.out.println(party + " is using the bathroom. Current count: " + currentCount);
//        try {
//            Thread.sleep(1000); // Simulate bathroom use
//        } catch (InterruptedException e) {
//            Thread.currentThread().interrupt();
//        }
//        System.out.println(party + " is leaving the bathroom.");
//    }
//
//    // Test the class
//    public static void main(String[] args) {
//        CongressBathroom bathroom = new CongressBathroom();
//
//        Runnable democrat = () -> {
//            try {
//                bathroom.democratUseBathroom();
//            } catch (InterruptedException e) {
//                Thread.currentThread().interrupt();
//            }
//        };
//
//        Runnable republican = () -> {
//            try {
//                bathroom.republicanUseBathroom();
//            } catch (InterruptedException e) {
//                Thread.currentThread().interrupt();
//            }
//        };
//
//        // Launch some threads
//        for (int i = 0; i < 5; i++) {
//            new Thread(democrat).start();
//            new Thread(republican).start();
//        }
//    }
//}