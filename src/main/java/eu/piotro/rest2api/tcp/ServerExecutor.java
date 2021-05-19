package eu.piotro.rest2api.tcp;

import java.util.HashSet;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Logger;

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
     * @param maxQueueSize maximum number of waiting {@link Runnable}
     */
    public ServerExecutor(int maxThreads, int minKeepThreads, int inactiveWorkerTimeout, int maxQueueSize){
        this.maxThreads = maxThreads;
        this.minKeepThreads = minKeepThreads;
        this.inactiveWorkerTimeout = inactiveWorkerTimeout;
        this.maxQueueSize = maxQueueSize;

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
            if(taskQueue.size() >= maxQueueSize)
                throw new RejectedExecutionException("Maximum size of queue reached");
            if (!taskQueue.offer(r))
                throw new RejectedExecutionException("Cannot add to queue");
            adjustWorkers();
            newTask.signal();
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
        if(runningWorkersCnt.get() == workers.size() && workers.size() < maxThreads){
            logger.fine("Starting new Worker " + "SEWorker"+workerId);
            Worker newWorker = new Worker();
            Thread workerThread = new Thread(newWorker, "SEWorker"+workerId++);
            workerThread.start();
            workers.add(newWorker);
        }
    }

    private final int maxThreads;
    private final int minKeepThreads;
    private final int inactiveWorkerTimeout;
    private final int maxQueueSize;
    private final Condition newTask;
    private int workerId = 0;
    private final AtomicInteger runningWorkersCnt = new AtomicInteger(0);
    private final Logger logger = Logger.getLogger(ServerExecutor.class.getName());

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
                            logger.fine(Thread.currentThread().getName() + "awaiting" + runningWorkersCnt);
                            newTask.await(); //Fixes checking if newTask in main loop  (Consuming 100% CPU) now waiting for signal in addition to queue take. Await releases lock and waits to reacquire it after receiving signal
                        }

                        runningTask = taskQueue.take();
                        logger.fine(Thread.currentThread().getName() + " executing task " + runningTask);
                    } finally {
                        lock.unlock();
                    }

                    if (runningTask != null) {
                        runningWorkersCnt.incrementAndGet();
                        inactiveTimeoutTask.cancel(false);
                        runningTask.run();
                        inactiveTimeoutTask = scheduledExecutorService.schedule(new InactiveTimeoutRunnable(), inactiveWorkerTimeout, TimeUnit.SECONDS);
                        runningWorkersCnt.decrementAndGet();

                    }
                    runningTask = null;
                }
            } catch (InterruptedException ignored){ }
            workers.remove(this);
            logger.fine("Removed worker " + Thread.currentThread().getName() + ". " + workers.size() + " workers left");
        }

        private Runnable runningTask;
        private Thread currentThread;

        private class InactiveTimeoutRunnable implements Runnable {
            @Override
            public void run() {
                if(workers.size() > minKeepThreads) {
                    Worker.this.currentThread.interrupt();
                }
            }
        }
    }
}
