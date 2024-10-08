package io.goji.exp.promise;

import java.util.List;
import java.util.Spliterator;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Consumer;

/**
 * @author Grzegorz Piwowarek
 */
final class CompletionOrderSpliterator2<T> implements Spliterator<T> {

    private final int initialSize;
    private final BlockingQueue<CompletableFuture<T>> completed = new LinkedBlockingQueue<>();
    private int remaining;

    CompletionOrderSpliterator2(List<CompletableFuture<T>> futures) {
        this.initialSize = futures.size();
        this.remaining = initialSize;
        futures.forEach(f -> f.whenComplete((t, __) -> completed.add(f)));
    }

    @Override
    public boolean tryAdvance(Consumer<? super T> action) {
        if (remaining > 0) {
            nextCompleted().thenAccept(action).join();
            return true;
        } else {
            return false;
        }
    }

    private CompletableFuture<T> nextCompleted() {
        remaining--;
        try {
            return completed.take();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }

    @Override
    public Spliterator<T> trySplit() {
        return null;
    }

    @Override
    public long estimateSize() {
        return initialSize;
    }

    @Override
    public int characteristics() {
        return SIZED | IMMUTABLE | NONNULL;
    }
}

