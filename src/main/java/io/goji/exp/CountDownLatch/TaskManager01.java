package io.goji.exp.CountDownLatch;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;

// 仍然没有达到效果 todo
public class TaskManager01 {


    public static boolean executeTasks() throws InterruptedException {
        ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(10);
        CountDownLatch latch = new CountDownLatch(2); // 主线程只等待 2 个 true
        AtomicInteger trueCount = new AtomicInteger(0); // 统计 true 的个数

        long startTime = System.currentTimeMillis();  // 记录开始时间

        for (int i = 0; i < 10; i++) {
            int taskId = i + 1;
            executor.submit(() -> {
                while (!Thread.currentThread().isInterrupted()) {
                    try {
                        // 模拟耗时操作
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();  // 重新设置中断状态
                        System.out.println("Task " + taskId + " was interrupted.");
                        return;  // 中断后退出任务
                    }

                    // 模拟任务结果
                    boolean result = Math.random() > 0.5;
                    System.out.println("Task " + taskId + " returned: " + result);

                    // 如果任务返回 true，计数器递减
                    if (result) {
                        trueCount.incrementAndGet();
                        latch.countDown(); // 计数器减少
                    }

                    return; // 任务结束
                }
            });
        }

        // 主线程等待计数器为 0（即等待 2 个子线程返回 true）
        latch.await();

        long endTime = System.currentTimeMillis();
        System.out.println("Main thread is returning after two true results. Time elapsed: " + (endTime - startTime) + " ms");

        // 确保线程池被关闭
        executor.shutdown();

        // 返回 true 的个数是否大于等于 2
        return trueCount.get() >= 2;
    }

    public static void main(String[] args) throws InterruptedException {
        boolean result = executeTasks();
        System.out.println("Final result: " + result);

    }

}
