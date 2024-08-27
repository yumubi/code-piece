package io.goji.exp.blockingqueue;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;

public final class Queues {
    private Queues() {}

    static void sum(
        int[] s,
        int start,
        int end,
        BlockingQueue<Integer> queue
    ) throws InterruptedException {
        int sum = 0;
        for (int i = start; i < end; i++) {
            sum += s[i];
        }
        queue.put(sum);
    }

    public static void main(String[] args)
        throws InterruptedException {
        int[] s = { 7, 2, 8, -9, 4, 0 };
        try (var executor =
                Executors.newVirtualThreadPerTaskExecutor()) {
            var queue = new ArrayBlockingQueue<Integer>(1);
            executor.submit(() -> {
                sum(s, 0, s.length / 2, queue);
                return null;
            });
            executor.submit(() -> {
                sum(s, s.length / 2, s.length, queue);
                return null;
            });

            int x = queue.take();
            int y = queue.take();

            System.out.printf("%d %d %d\n", x, y, x + y);
        }
    }
}
