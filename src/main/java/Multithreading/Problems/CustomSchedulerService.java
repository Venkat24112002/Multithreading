package Multithreading.Problems;

import java.util.PriorityQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

class ScheduledTask {
    public Runnable runnable;
    public Long scheduledTime;
    public int taskType;
    public Long period;
    public Long delay;
    public TimeUnit timeUnit;

    public ScheduledTask(Runnable task,long scheduledTime, int taskType, Long period, Long delay, TimeUnit unit){
        this.runnable = task;
        this.scheduledTime = scheduledTime;
        this.taskType = taskType;
        this.period = period;
        this.delay = delay;
        this.timeUnit = unit;
    }
}


public class CustomSchedulerService {
    private  PriorityQueue<ScheduledTask> taskQueue;
    private Lock lock = new ReentrantLock();
    private Condition newTaskAdded = lock.newCondition();
    private final ExecutorService workerExecutor;

    public CustomSchedulerService(int workerThreads) {
        workerExecutor = Executors.newFixedThreadPool(workerThreads);
        this.taskQueue = new PriorityQueue<>((a,b) -> Long.compare(a.scheduledTime,b.scheduledTime));
    }

    public void start(){
        long timeToSleep = 0;
        while(true){
            lock.lock();
            try {
                while(taskQueue.isEmpty()){
                    newTaskAdded.await();
                }
                while(!taskQueue.isEmpty()){
                    timeToSleep = taskQueue.peek().scheduledTime - System.currentTimeMillis();
                    if(timeToSleep <= 0){
                        break;
                    }
                    newTaskAdded.await(timeToSleep, TimeUnit.MILLISECONDS);
                }
                ScheduledTask task = taskQueue.poll();
                long newSchuledTime = 0;
                switch ( task.taskType){
                    case 1 :
                        workerExecutor.submit(task.runnable);
                        break;
                    case 2 :
                        newSchuledTime = System.currentTimeMillis() + task.timeUnit.toMillis(task.period);
                        workerExecutor.submit(task.runnable);
                        task.scheduledTime = newSchuledTime;
                        taskQueue.add(task);
                        newTaskAdded.signalAll();  // ✅ wake up scheduler
                        break;
                    case 3 :
                        Future<?> f = workerExecutor.submit(task.runnable);
                        f.get();
                        newSchuledTime = System.currentTimeMillis() + task.timeUnit.toMillis(task.delay);
                        task.scheduledTime = newSchuledTime;
                        taskQueue.add(task);
                        newTaskAdded.signalAll();  // ✅ wake up scheduler
                        break;
                }
            } catch(Exception e){
                e.printStackTrace();
            } finally {
                lock.unlock();
            }
        }

    }

    public void schedule(Runnable task, long delay, TimeUnit unit){
        lock.lock();
        try {
            long scheduledTime = System.currentTimeMillis() + unit.toMillis(delay);
            ScheduledTask task1 = new ScheduledTask(task,scheduledTime,1,null,null,unit);
            taskQueue.add(task1);
            newTaskAdded.signalAll();
        } catch (Exception e){
            System.out.println(e.getStackTrace());
        } finally {
            lock.unlock();
        }
    }

    public void scheduleAtFixedRate(Runnable task, long initialDelay, long period, TimeUnit unit){
        lock.lock();
        try {
            long scheduledTime = System.currentTimeMillis() + unit.toMillis(initialDelay);
            ScheduledTask task1 = new ScheduledTask(task,scheduledTime,2,period,null,unit);
            taskQueue.add(task1);
            newTaskAdded.signalAll();
        } catch (Exception e){
            System.out.println(e.getStackTrace());
        } finally {
            lock.unlock();
        }
    }

    public void scheduleWithFixedDelay(Runnable command, long initialDelay, long delay, TimeUnit unit) {
        lock.lock();
        try {
            long scheduledTime = System.currentTimeMillis() + unit.toMillis(initialDelay);
            ScheduledTask task = new ScheduledTask(command, scheduledTime, 3, null, delay, unit);
            taskQueue.add(task);
            newTaskAdded.signalAll();
        } catch (Exception e) {
            System.out.println("some thing wrong in scheduling task type 3");
            e.printStackTrace();
        } finally {
            lock.unlock();
        }
    }

    public static void main(String[] args) {
        CustomSchedulerService schedulerService = new CustomSchedulerService(10);
        Runnable task1 = getRunnableTask("Task1");
        schedulerService.schedule(task1, 1, TimeUnit.SECONDS);
        Runnable task2 = getRunnableTask("Task2");
        schedulerService.scheduleAtFixedRate(task2,1, 2, TimeUnit.SECONDS);
        Runnable task3 = getRunnableTask("Task3");
        schedulerService.scheduleWithFixedDelay(task3,1,2,TimeUnit.SECONDS);
        Runnable task4 = getRunnableTask("Task4");
        schedulerService.scheduleAtFixedRate(task4,1, 2, TimeUnit.SECONDS);
        schedulerService.start();
    }

    private static Runnable getRunnableTask(String s) {
        return () -> {
            System.out.println(s + " started at " + System.currentTimeMillis() / 1000);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println(s + " ended at " + System.currentTimeMillis() / 1000);
        };
    }
}
