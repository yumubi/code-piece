package io.goji.exp.blockingqueue;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;

sealed interface TakeResult<T> {
    record GotValue<T>(T value) implements TakeResult<T> {}
    record NoValue<T>() implements TakeResult<T> {}
}

public final class Fibonacci {
    private Fibonacci() {}

    static void fibonacci(
        int n,
        BlockingQueue<TakeResult<Integer>> queue
    ) throws InterruptedException {
        int x = 0;
        int y = 1;

        for (int i = 0; i < n; i++) {
            queue.put(new TakeResult.GotValue<>(x));
            int temp = x;
            x = y;
            y = temp + x;
        }
        queue.put(new TakeResult.NoValue<>());
    }

    public static void main(String[] args)
        throws InterruptedException {
        try (var executor =
                Executors.newVirtualThreadPerTaskExecutor()) {
            var queue =
                new ArrayBlockingQueue<TakeResult<Integer>>(10);
            executor.submit(() -> {
                fibonacci(queue.remainingCapacity(), queue);
                return null;
            });

            while (queue.take() instanceof
                    TakeResult.GotValue<Integer> gotValue) {
                System.out.println(gotValue.value());
            }
        }
    }
}
