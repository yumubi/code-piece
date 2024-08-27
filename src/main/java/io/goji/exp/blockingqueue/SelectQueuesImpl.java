package io.goji.exp.blockingqueue;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;

public final class SelectQueuesImpl {
    private SelectQueuesImpl() {}

    static void fibonacci(BlockingQueue<Integer> queue,
                          BlockingQueue<Integer> quit) {
        int x = 0;
        int y = 1;

        while (true) {
            if (queue.offer(x)) {
                int temp = x;
                x = y;
                y = temp + x;
            }
            if (quit.poll() != null) {
                System.out.println("quit");
                break;
            }
        }
    }

    public static void main(String[] args) {
        var queue = new ArrayBlockingQueue<Integer>(1);
        var quit = new ArrayBlockingQueue<Integer>(1);

        try (var executor =
                Executors.newVirtualThreadPerTaskExecutor()) {
            executor.submit(() -> {
                for (int i = 0; i < 10; i++) {
                    System.out.println(queue.take());
                }
                quit.put(0);
                return null;
            });

            fibonacci(queue, quit);
        }
    }
}
