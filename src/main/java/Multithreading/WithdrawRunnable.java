package Multithreading;

public class WithdrawRunnable implements Runnable{

    private BankBalance bankBalance;
    private int amount;

    public WithdrawRunnable(BankBalance bankBalance, int amount) {
        this.bankBalance = bankBalance;
        this.amount = amount;
    }

    @Override
    public void run(){
        bankBalance.withdraw(amount);
    }

}
