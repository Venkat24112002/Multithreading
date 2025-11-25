package Multithreading;

public class Main {

    public static void main(String args[]){
        BankBalance bankBalance = new BankBalance();

        Runnable Task = new Runnable() {
            @Override
            public void run() {
                bankBalance.withdraw(50);
            }
        };
        // creating threads using the above object task
        Thread t1 = new Thread(Task, "T1");
        Thread t2 = new Thread(Task, "T2");

        t1.start();
        t2.start();

        //creating threads using withrunnable object
        Thread t3 = new Thread(new WithdrawRunnable(bankBalance,30), "t3");
        Thread t5 = new Thread(new WithdrawRunnable(bankBalance,60), "t5");

        try {
            Thread.sleep(3000);
        } catch (Exception e){

        }

        t3.start();
        t5.start();

    }
}
