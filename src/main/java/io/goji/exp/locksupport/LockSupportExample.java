package io.goji.exp.locksupport;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.LockSupport;

public class LockSupportExample {
    public static void main(String[] args) throws InterruptedException {
        List<Integer> counters = new ArrayList<>();
        final int[] count = {0};
        Thread t1 = new Thread(() -> {
            LockSupport.park();
            while (true){
                try {
                    Thread.sleep(1_000L);
                    counters.add(count[0]);
                    count[0]++;
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    //throw new RuntimeException(e);
                }
            }
        });

        t1.start();


        Thread t2 = new Thread(() -> {
             try {
                 Thread.sleep(2_500L);
             }
             catch (InterruptedException e){
                 Thread.currentThread().interrupt();
             }
             LockSupport.unpark(t1);
        });

        t2.start();

        Thread.sleep(5_000L);
        t1.interrupt();

        System.out.println(counters);
    }
}
