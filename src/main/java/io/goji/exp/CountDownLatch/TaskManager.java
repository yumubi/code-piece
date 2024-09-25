package io.goji.exp.CountDownLatch;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.List;
import java.util.ArrayList;

// 主线程去调用 10 个子线程查询任务(返回 true/false), 在回调中统计结果,
//我想在得到两个 true 时主线程立即返回 true, 不管其他的子线程时已提交到线程池还是未提交到线程池, 都不需要了
// 下面的代码没有实现这个功能 todo
public class TaskManager {

    public static boolean executeTasks() throws InterruptedException {
        ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(10);
        List<CompletableFuture<Boolean>> futures = new ArrayList<>();
        AtomicInteger trueCount = new AtomicInteger(0);
        CountDownLatch latch = new CountDownLatch(1);  // 用于主线程同步

        long startTime = System.currentTimeMillis();  // 记录开始时间

        for (int i = 0; i < 10; i++) {
            int taskId = i + 1;
            CompletableFuture<Boolean> future = CompletableFuture.supplyAsync(() -> {
                while (!Thread.currentThread().isInterrupted()) {
                    try {
                        // 模拟耗时操作
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        // 当任务收到中断信号时立即退出
                        System.out.println("Task " + taskId + " was interrupted.");
                        Thread.currentThread().interrupt();  // 重新设置中断状态
                        return false;
                    }

                    // 检查中断状态
                    if (Thread.currentThread().isInterrupted()) {
                        System.out.println("Task " + taskId + " was interrupted before completing.");
                        return false;
                    }

                    // 模拟任务结果
                    boolean result = Math.random() > 0.5;
                    System.out.println("Task " + taskId + " returned: " + result);
                    return result;
                }
                return false;
            }, executor);

            // 处理回调
            future.thenAccept(result -> {
                if (result && trueCount.incrementAndGet() >= 2) {
                    // 当两个任务返回 true 时，停止所有其他任务
                    System.out.println("Two true results reached. Stopping remaining tasks.");
                    executor.shutdownNow();  // 立即中断所有线程
                    latch.countDown();       // 释放主线程阻塞
                }
            });

            futures.add(future);
        }

        // 等待两个任务返回 true，主线程立即返回
        latch.await();

        // 记录主线程返回时间
        long endTime = System.currentTimeMillis();
        System.out.println("Main thread is returning after two true results. Time elapsed: " + (endTime - startTime) + " ms");

        // 获取两个 true 后的最终结果
        boolean isTrue = trueCount.get() >= 2;

        // 确保线程池被关闭
        executor.shutdown();

        return isTrue;
    }

    public static void main(String[] args) throws InterruptedException {
        boolean result = executeTasks();
        System.out.println("Final result: " + result);
    }
}
