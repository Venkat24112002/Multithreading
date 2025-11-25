package Multithreading;

class SharedResource{
    int data;
    boolean hasData;

    public synchronized void produce(int value) throws InterruptedException {

        while(hasData){
            wait();
        }
        data = value;
        hasData = true;
        notify();
    }

    public synchronized void consume() throws InterruptedException{

        while(!hasData){
            wait();
        }
        System.out.println("Data is" + data);
        hasData = false;
        notify();
    }
}

class Producer implements Runnable{
    SharedResource sharedResource;

    public Producer(SharedResource sharedResource){
        this.sharedResource = sharedResource;
    }

    @Override
    public void run(){
        for(int i=0;i<10;i++){
            try {
                sharedResource.produce(i);
            } catch (InterruptedException e) {

            }
        }
    }
}

class Consumer implements Runnable{
    SharedResource sharedResource;

    public Consumer(SharedResource sharedResource){
        this.sharedResource = sharedResource;
    }

    @Override
    public void run(){
        for(int i=0;i<10;i++){
            try {
                sharedResource.consume();
            } catch (InterruptedException e) {

            }
        }
    }
}

public class ThreadCommunicationExample {

    public static void main(String []args){
        SharedResource sharedResource = new SharedResource();
        Thread t1 = new Thread(new Producer(sharedResource), "t1");
        Thread t2 = new Thread(new Consumer(sharedResource), "t2");

        t1.start();
        t2.start();
    }
}
