package io.goji.jav.locks;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class ProducerConsumerExample {
    private static final int CAPACITY = 10; // 队列容量
    private static Queue<Integer> queue = new LinkedList<>(); // 共享队列
    private static ReentrantLock lock = new ReentrantLock(); // 可重入锁
    private static Condition notFull = lock.newCondition(); // 非满条件
    private static Condition notEmpty = lock.newCondition(); // 非空条件

    public static void main(String[] args) {
        Thread producerThread = new Thread(new Producer()); // 创建生产者线程
        Thread consumerThread = new Thread(new Consumer()); // 创建消费者线程
        producerThread.start(); // 启动生产者线程
        consumerThread.start(); // 启动消费者线程
    }

    static class Producer implements Runnable {
        @Override
        public void run() {
            for (int i = 0; i < 20; i++) {
                lock.lock(); // 获取锁
                try {
                    while (queue.size() == CAPACITY) {
                        // 队列已满，等待非满条件
                        notFull.await();
                    }
                    queue.offer(i); // 将项目放入队列
                    System.out.println("生产者生产: " + i);
                    // 通知消费者队列非空
                    notEmpty.signal();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    lock.unlock(); // 释放锁
                }
            }
        }
    }

    static class Consumer implements Runnable {
        @Override
        public void run() {
            for (int i = 0; i < 20; i++) {
                lock.lock(); // 获取锁
                try {
                    while (queue.isEmpty()) {
                        // 队列为空，等待非空条件
                        notEmpty.await();
                    }
                    int item = queue.poll(); // 从队列中取出项目
                    System.out.println("消费者消费: " + item);
                    // 通知生产者队列非满
                    notFull.signal();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    lock.unlock(); // 释放锁
                }
            }
        }
    }
}
