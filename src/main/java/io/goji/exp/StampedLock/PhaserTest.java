package io.goji.exp.StampedLock;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Phaser;

public class PhaserTest {
    public static void main(String[] args) {
        Phaser phaser = new Phaser(50);
        Runnable r = () -> {
            System.out.println("phase-0");
            phaser.arriveAndAwaitAdvance();
            System.out.println("phase-1");
            phaser.arriveAndAwaitAdvance();
            System.out.println("phase-2");
            phaser.arriveAndDeregister();
        };

        ExecutorService executor = Executors.newFixedThreadPool(50);
        for (int i = 0; i < 50; i++) {
            executor.submit(r);
        }
    }
}
