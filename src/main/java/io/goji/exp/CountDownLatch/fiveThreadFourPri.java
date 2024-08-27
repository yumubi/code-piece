package io.goji.exp.CountDownLatch;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

// 5 个线程，前 4 个执行完后才执行第 5 个
public class fiveThreadFourPri {

    public static void main(String[] args) throws InterruptedException {
//        ExecutorService executorService = Executors.newFixedThreadPool(5);
//        // 提交前 4 个任务给线程池
//        for (int i = 1; i <= 4; i++) {
//            executorService.execute(() -> {
//                // 执行任务逻辑
//                System.out.println("Thread " + Thread.currentThread().getId() + " is running.");
//            });
//        }
//        // 等待所有任务执行完成
//        executorService.shutdown();
//        executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
//
//        // 执行第 5 个任务
//        System.out.println("Thread " + Thread.currentThread().getId() + " is running.");

        // By CountDownLatch
        ExecutorService executorService = Executors.newFixedThreadPool(5);
        // 用于等待前 4 个任务执行完成
        CountDownLatch countDownLatch = new CountDownLatch(4);
        // 提交前 4 个任务给线程池
        for (int i = 1; i <= 4; i++) {
            executorService.execute(() -> {
                // 执行任务逻辑
                System.out.println("Thread " + Thread.currentThread().getId() + " is running.");
                // 任务执行完成，countDownLatch 减 1
                countDownLatch.countDown();
            });
        }
        // 等待前 4 个任务执行完成
        countDownLatch.await();
        // 执行第 5 个任务
        System.out.println("Thread " + Thread.currentThread().getId() + " is running.");
        executorService.shutdown();

    }

}
