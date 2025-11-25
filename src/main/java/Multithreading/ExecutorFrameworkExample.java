package Multithreading;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ExecutorFrameworkExample {
    public static int factorial(int i){
        return i;
    }

    public static void main(String []args){

//        ExecutorService executorService = Executors.newFixedThreadPool(9);

//        List<Future<Integer>> futures = new ArrayList<>();
//        for(int i=0;i < 10; i++){
//
//            int finalI1 = i;
//            Future<Integer> future = executorService.submit(() -> {
//                return finalI1;
//            });
//            futures.add(future);
//        }
//        for(Future<Integer> f : futures){
//            try{
//                int x = f.get();
//            } catch(Exception e){
//
//            }
//        }
//
//


//        Future<Integer> future = executorService.submit(() -> {
//            return 32;
//        });
//        try{
//            System.out.println(future.get());
//        } catch(Exception e){
//
//        }


        // Invoke All method
//        Callable<Integer> c1 = () -> {return 1;};
//        Callable<Integer> c2 = () -> {return 2;};
//        Callable<Integer> c3 = () -> {return 3;};
//
//        List<Callable<Integer>> list = Arrays.asList(c1,c2,c3);
//
//        try{
//            List<Future<Integer>> result =executorService.invokeAll(list);
//        } catch (Exception e){
//
//        }

        //Invoke Any method (it will return the data type not future
//        try {
//            Integer res = executorService.invokeAny(list);
//        } catch (InterruptedException e) {
//            throw new RuntimeException(e);
//        } catch (ExecutionException e) {
//            throw new RuntimeException(e);
//        }

        //  executorService.shutdown();

        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(3);
        //schedule
        scheduler.schedule(()-> System.out.println("hii"),
                2,
                TimeUnit.SECONDS);

        //schedule at fixed rate
        scheduler.scheduleAtFixedRate(()-> System.out.println("hii"),
                2,
                2,
                TimeUnit.SECONDS);
        //scheduler.shutdown(); this will onyl print once because after period it will add into internal queue
        scheduler.schedule(()-> scheduler.shutdown(),10,TimeUnit.SECONDS);


        //schuler with fixed delay - see gpt for diff(Main diff is after runnable task in finsihed then delay starts)
        scheduler.scheduleWithFixedDelay(()-> System.out.println("hii"),
                2,
                2,
                TimeUnit.SECONDS);


//        try{
//            executorService.awaitTermination(1, TimeUnit.MILLISECONDS);
//        } catch( Exception e){
//            throw new RuntimeException(e);
//        }
    }


}
