package io.goji.exp.locks;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

final class SafeCounter {
    private final Map<String, Integer> v;
    private final ReentrantLock lock;

    public SafeCounter() {
        this.v = new HashMap<>();
        this.lock = new ReentrantLock();
    }
    void inc(String key) {
        lock.lock();
        try {
            v.put(key, v.getOrDefault(key, 0) + 1);
        } finally {
            lock.unlock();
        }
    }

    int value(String key) {
        lock.lock();
        try {
            return v.getOrDefault(key, 0);
        } finally {
            lock.unlock();
        }
    }
}

public final class Mutex {
    private Mutex() {}

    public static void main(String[] args)
            throws InterruptedException {
        var c = new SafeCounter();
        for (int i = 0; i < 1000; i++) {
            Thread.startVirtualThread(
                    () -> c.inc("somekey")
            );
        }

        Thread.sleep(Duration.ofSeconds(1));
        System.out.println(c.value("somekey"));
    }
}
