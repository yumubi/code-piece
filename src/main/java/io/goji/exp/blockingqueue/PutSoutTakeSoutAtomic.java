package io.goji.exp.blockingqueue;

import java.io.IOException;
import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class PutSoutTakeSoutAtomic {

    public static void main(String[] args) throws IOException, InterruptedException {
        Lock lock = new ReentrantLock();
        BlockingQueue<Integer> bq = new LinkedBlockingQueue<Integer>(1);

        AtomicInteger seq = new AtomicInteger(0);

        Producer p1 = new Producer(bq, seq, lock);
        p1.setName("producer01");
        Customer c1 = new Customer(bq, seq, lock);
        c1.setName("customer01");
        p1.start();
        c1.start();
    }

    static class Producer extends Thread {
        AtomicInteger seq;
        Lock lock;
        private BlockingQueue<Integer> bq;

        public Producer(BlockingQueue<Integer> bq, AtomicInteger seq, Lock lock) {
            this.bq = bq;
            this.seq = seq;
            this.lock = lock;
        }

        @Override
        public void run() {
            while (true) {
                try {
                    if (bq.size() == 1) continue;
                    lock.lock();
                    bq.put(produce());
                    lock.unlock();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
        }

        private Integer produce() {
            Integer number = (new Random().nextInt(100));
            int sid = seq.addAndGet(1);
            System.out.println("seq:" + sid + getName() + ":produced =====> " + number);
            System.out.flush();
            return number;
        }
    }

    static class Customer extends Thread {
        AtomicInteger seq;
        private BlockingQueue<Integer> bq;
        Lock lock;

        public Customer(BlockingQueue<Integer> bq, AtomicInteger seq, Lock lock) {
            this.bq = bq;
            this.seq = seq;
            this.lock = lock;
        }

        @Override
        public void run() {
            while (true) {
                try {
                    if (bq.size() == 0) continue;
                    lock.lock();
                    consume();
                    lock.unlock();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        private void consume() throws InterruptedException {
            int tk = bq.take();
            int sid = seq.addAndGet(1);
            System.out.println("seq:" + sid + getName() + ":consumed:" + tk);
            System.out.flush();
        }
    }


}
