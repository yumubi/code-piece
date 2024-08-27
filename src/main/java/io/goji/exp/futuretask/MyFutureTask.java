package io.goji.exp.futuretask;

import java.util.concurrent.locks.LockSupport;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

public class MyFutureTask<V> {
    private volatile int state;
    private static final int NEW = 0;
    private static final int COMPLETING = 1;
    private static final int NORMAL = 2;
    private static final int EXCEPTIONAL = 3;

    private static final AtomicReferenceFieldUpdater<MyFutureTask, WaitNode> WAITERS =
            AtomicReferenceFieldUpdater.newUpdater(MyFutureTask.class, WaitNode.class, "waiters");
    private static final AtomicIntegerFieldUpdater<MyFutureTask> STATE =
            AtomicIntegerFieldUpdater.newUpdater(MyFutureTask.class, "state");

    private volatile WaitNode waiters;

    static final class WaitNode {
        volatile Thread thread;
        volatile WaitNode next;

        WaitNode() {
            thread = Thread.currentThread();
        }
    }

    private int awaitDone(boolean timed, long nanos) throws InterruptedException {
        long startTime = 0L;
        WaitNode q = null;
        boolean queued = false;
        for (;;) {
            int s = state;
            if (s > COMPLETING) {
                if (q != null)
                    q.thread = null;
                return s;
            }
            else if (s == COMPLETING)
                Thread.yield();
            else if (Thread.interrupted()) {
                removeWaiter(q);
                throw new InterruptedException();
            }
            else if (q == null) {
                if (timed && nanos <= 0L)
                    return s;
                q = new WaitNode();
            }
            else if (!queued)
                queued = WAITERS.weakCompareAndSet(this, q.next = waiters, q);
            else if (timed) {
                final long parkNanos;
                if (startTime == 0L) {
                    startTime = System.nanoTime();
                    if (startTime == 0L)
                        startTime = 1L;
                    parkNanos = nanos;
                } else {
                    long elapsed = System.nanoTime() - startTime;
                    if (elapsed >= nanos) {
                        removeWaiter(q);
                        return state;
                    }
                    parkNanos = nanos - elapsed;
                }
                if (state < COMPLETING)
                    LockSupport.parkNanos(this, parkNanos);
            }
            else
                LockSupport.park(this);
        }
    }

    private void removeWaiter(WaitNode node) {
        if (node != null) {
            node.thread = null;
            for (;;) {
                WaitNode pred = null, q = waiters, s;
                while (q != null) {
                    s = q.next;
                    if (q.thread != null)
                        pred = q;
                    else if (pred != null)
                        pred.next = s;
                    else if (!WAITERS.compareAndSet(this, q, s))
                        break;
                    q = s;
                }
                break;
            }
        }
    }

    // Simulate task completion
    public void complete() {
        state = NORMAL;
    }

    // Simulate task starting to complete
    public void startCompleting() {
        state = COMPLETING;
    }

    public static void main(String[] args) {
        MyFutureTask<Integer> task = new MyFutureTask<>();

        // Start a thread that will wait on the task
        Thread waiterThread = new Thread(() -> {
            try {
                System.out.println("Awaiting done...");
                int result = task.awaitDone(true, 1000000000L); // 1 second timeout
                System.out.println("Await done with state: " + result);
            } catch (InterruptedException e) {
                System.out.println("Interrupted");
            }
        });

        waiterThread.start();

        // Simulate task state changes
        try {
            Thread.sleep(200); // Ensure the waiter thread starts waiting
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Task starts completing
        System.out.println("Task is completing");
        task.startCompleting();

        try {
            Thread.sleep(200); // Simulate some delay
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Task completed
        System.out.println("Task completed");
        task.complete();

        try {
            waiterThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
