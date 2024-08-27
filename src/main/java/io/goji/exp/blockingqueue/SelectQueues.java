package io.goji.exp.blockingqueue;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

public final class SelectQueues {
    private SelectQueues() {}

    static void fibonacci(BlockingQueue<Integer> queue,
                          AtomicBoolean quit) {
        int x = 0;
        int y = 1;

        while (!quit.get()) {
            if (queue.offer(x)) {
                int temp = x;
                x = y;
                y = temp + x;
            }
        }

        System.out.println("quit");
    }

    public static void main(String[] args) throws InterruptedException {
        var queue = new ArrayBlockingQueue<Integer>(1);
        var quit = new AtomicBoolean(false);

        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            executor.submit(() -> {
                for (int i = 0; i < 10; i++) {
                    System.out.println(queue.take());
                }
                quit.set(true);
                return null;
            });

            fibonacci(queue, quit);
        }
    }
}
