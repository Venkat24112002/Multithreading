package Multithreading;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class BankBalance {
    private int balance = 1000;

    private Lock lock = new ReentrantLock();
    //This is intrinsic lock using synchronized key word
//    public synchronized void withdraw(int amount){
//        System.out.println(Thread.currentThread().getName() + "Withdrawing" + amount);
//        if(balance >= amount) {
//            System.out.println(Thread.currentThread().getName() + "Proceeding" + amount);
//            try{
//                Thread.sleep(3000);
//            } catch(Exception e){
//
//            }
//            balance -= amount;
//        }
//        else {
//            System.out.println("Insufficient balance");
//        }
//        System.out.println(Thread.currentThread().getName() + "current balance" + balance);
//    }

    // this is explicit locks
    public void withdraw(int amount){
        System.out.println(Thread.currentThread().getName() + "Withdrawing" + amount);
        try {
            if(lock.tryLock(1000, TimeUnit.MILLISECONDS)){
                if(balance >= amount) {
                    System.out.println(Thread.currentThread().getName() + "Proceeding" + amount);
                    try{
                        Thread.sleep(3000);
                    } catch(Exception e){
                        //this catch of is because of sleep
                        // we need to ensure if this is called we need to interrupt the thread
                        Thread.currentThread().interrupt();
                    }
                    finally {
                        balance -= amount;
                        System.out.println(Thread.currentThread().getName() + "current balance" + balance);
                        lock.unlock();
                    }
                }
            }
            else {
                System.out.println(Thread.currentThread().getName() + "someone locked skipping this");
            }
        }catch (Exception e){
            Thread.currentThread().interrupt();
        }

        if(Thread.currentThread().isInterrupted()){
            System.out.println(Thread.currentThread().getName() + "got interrupted");
        }
    }
}
