package eu.piotro.test2api.tcp;

import java.util.HashSet;
import java.util.concurrent.*;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Custom {@link Executor} for socket server.
 *
 * @author Piotro
 * @since 0.1
 */
public class ServerExecutor implements Executor {

    private final BlockingQueue<Runnable> taskQueue;
    private final HashSet<Worker> workers;
    private final ReentrantLock lock;

    /**
     * Creates ServerExecutor
     * @param maxThreads Max number of threads allowed to run in parallel
     * @param minKeepThreads Number of threads ({@link Worker}) that are always kept, even when they are idle and independently of inactiveTimeout.
     * @param inactiveWorkerTimeout Time in seconds after which idle threads ({@link Worker}) above {@code minKeepThreads} limit would be stopped.
     */
    public ServerExecutor(int maxThreads, int minKeepThreads, int inactiveWorkerTimeout){
        this.maxThreads = maxThreads;
        this.minKeepThreads = minKeepThreads;
        this.inactiveWorkerTimeout = inactiveWorkerTimeout;

        taskQueue = new LinkedBlockingQueue<>();
        workers = new HashSet<>(maxThreads);
        lock = new ReentrantLock();
        newTask = lock.newCondition();
    }

    /**
     * Submit new {@link Runnable} to execute
     * @param r {@link Runnable} to be executed
     * @throws RejectedExecutionException When error while adding {@link Runnable} to queue
     */
    @Override
    public void execute(Runnable r) throws RejectedExecutionException {
        try {
            lock.lock();
            if (!taskQueue.offer(r))
                throw new RejectedExecutionException("Cannot add to queue");
            adjustWorkers();
            newTask.signal();
            System.out.println("Notified");
        } finally {
            lock.unlock();
        }
    }

//    public void shutdown(){
//        try{
//            lock.lock();
//            for(Worker w: workers){
//                w.currentThread.interrupt();
//                w.shutdown = true; //Set this in case this not interrupted Worker but only ConnectionHandler
//            }
//        } finally {
//            lock.unlock();
//        }
//    }

    private void adjustWorkers(){
        if(taskQueue.size() > 0 && workers.size() < maxThreads){
            boolean idle = false;
            for(Worker w: workers){
                if(w.runningTask == null){
                    idle = true;
                    break;
                }
            }

            if(!idle){
                System.out.println("Starting new Worker");
                Worker newWorker = new Worker();
                Thread workerThread = new Thread(newWorker, "SEWorker"+workerCnt++);
                workerThread.start();
                workers.add(newWorker);
            }
        }
    }

    private final int maxThreads;
    private final int minKeepThreads;
    private final int inactiveWorkerTimeout;
    private final Condition newTask;
    private int workerCnt = 0;

    private final ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);

    private class Worker implements Runnable {

        @Override
        public void run() {
            currentThread = Thread.currentThread();
            ScheduledFuture<?> inactiveTimeoutTask = scheduledExecutorService.schedule(new InactiveTimeoutRunnable(), 1, TimeUnit.SECONDS);
            try {
                while (!Thread.currentThread().isInterrupted()) {
                    try {
                        lock.lock();
                        while (taskQueue.size() <= 0) {
                            System.out.println("Awaiting");
                            newTask.await(); //Fixes checking if newTask in main loop  (Consuming 100% CPU) now waiting for signal in addition to queue take
                            System.out.println("Exiting await");
                        }

                        runningTask = taskQueue.take();
                    } finally {
                        lock.unlock();
                    }

                    if (runningTask != null) {
                        inactiveTimeoutTask.cancel(false);
                        runningTask.run();
                        inactiveTimeoutTask = scheduledExecutorService.schedule(new InactiveTimeoutRunnable(), inactiveWorkerTimeout, TimeUnit.SECONDS);
                    }
                    runningTask = null;
                }
            } catch (InterruptedException ignored){ }
            workers.remove(this);
            System.out.println("Removed WORKER "+Thread.currentThread().getName());
        }

        private Runnable runningTask;
        private Thread currentThread;

        private class InactiveTimeoutRunnable implements Runnable {
            @Override
            public void run() {
                if(workers.size() > minKeepThreads) {
                    System.out.println(Thread.currentThread().getName() + "Shutting down worker");
                    Worker.this.currentThread.interrupt();
                }
            }
        }
    }
}
