package io.goji.exp.blockingqueue;

import java.util.concurrent.ArrayBlockingQueue;

public final class BufferedQueue {
    private BufferedQueue() {}

    public static void main(String[] args)
        throws InterruptedException {
        var queue = new ArrayBlockingQueue<Integer>(2);
        queue.put(1);
        queue.put(2);
        System.out.println(queue.take());
        System.out.println(queue.take());
    }
}
