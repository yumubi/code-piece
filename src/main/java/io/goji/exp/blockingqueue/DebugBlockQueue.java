package io.goji.exp.blockingqueue;

import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class DebugBlockQueue {
    public static void main(String[] args) {
        BlockingQueue<Integer> bq = new LinkedBlockingQueue<Integer>(1);

        Producer p1 = new Producer(bq);
        p1.setName("producer01");
        Customer c1 = new Customer(bq);
        c1.setName("customer01");
        p1.start();
        c1.start();
    }


    static class Producer extends Thread {

        private BlockingQueue<Integer> bq;

        public Producer(BlockingQueue<Integer> bq) {
            this.bq = bq;
        }

        @Override
        public void run() {
            while (true) {
                try {
                    bq.put(produce());
                    Thread.sleep(0, 1); //去掉sleep 就会无法实现交替执行的效果
                    // 原因: Thread.sleep(0,1) 实际上是 sleep 了 1ms , bq.take()耗时小于 1ms，所以看起来是交替执行
                    //去掉 sleep 之后，bq.take()拿到数据比循环到下一个 produce()时要慢，所以看起来不是交替执行
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
        }

        private Integer produce() {
            Integer number = (new Random().nextInt(100));
            System.out.println(getName() + ":produced =====> " + number);
            return number;
        }
    }


    static class Customer extends Thread {

        private BlockingQueue<Integer> bq;

        public Customer(BlockingQueue<Integer> bq) {
            this.bq = bq;
        }

        @Override
        public void run() {
            while (true) {
                try {
                    consume();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        private void consume() throws InterruptedException {
            System.out.println(getName() + ":consumed:" + bq.take());
        }
    }

}
