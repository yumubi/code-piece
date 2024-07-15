package io.goji.exp.StampedLock;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.LongAccumulator;
import java.util.concurrent.locks.StampedLock;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        StampedLock lock = new StampedLock();
        Balance b = new Balance(10000);
        Runnable w = () -> {
            long stamp = lock.writeLock();
            b.setAmount(b.getAmount() + 1000);
            System.out.println("Write: " + b.getAmount());
            lock.unlockWrite(stamp);
        };
        Runnable r = () -> {
            long stamp = lock.tryOptimisticRead();
            if (!lock.validate(stamp)) {
                stamp = lock.readLock();
                try {
                    System.out.println("Read: " + b.getAmount());
                } finally {
                    lock.unlockRead(stamp);
                }
            } else {
                System.out.println("Optimistic read fails");
            }
        };


        ExecutorService executor = Executors.newFixedThreadPool(10);
        for (int i = 0; i < 50; i++) {
            executor.submit(w);
            executor.submit(r);
        }
        executor.shutdown();



        LongAccumulator balance2 = new LongAccumulator(Long::sum, 10000L);
        Runnable w2 = () -> balance2.accumulate(1000L);

        ExecutorService executor2 = Executors.newFixedThreadPool(50);
        for (int i = 0; i < 50; i++) {
            executor2.submit(w2);
        }

        executor2.shutdown();
        if (executor2.awaitTermination(1000L, TimeUnit.MILLISECONDS))
            System.out.println("Balance2: " + balance2.get());
        assert balance2.get() == 60000L;
    }
}
