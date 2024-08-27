package io.goji.exp.CountDownLatch;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class App {

    public static volatile int count;
    public static final ReentrantLock lock = new ReentrantLock();
    public static final Condition cond = lock.newCondition();

    public static void main(String[] args) {
        Runnable run = new Runnable() {
            @Override
            public void run() {
                for (; ; ) {
                    lock.lock();
                    cond.signalAll();
                    try {
                        if (count >= 100) break;
                        count++;
                        print(count);
                        cond.await();
                    } catch (InterruptedException ignored) {
                    } finally {
                        lock.unlock();
                    }
                }
            }
        };

        new Thread(run, "A").start();
        new Thread(run, "B").start();
    }

    public static void print(int i) {
        System.out.printf("%s - %d%n", Thread.currentThread().getName(), i);
    }
}
