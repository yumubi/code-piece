package io.goji.exp.cas;

import java.util.concurrent.TimeUnit;

public class NoUseCAS {

    private volatile int value = 0;

    public void add() {
        value++;
    }

    public int getValue() {
        return value;
    }

    public static void main(String[] args) {
        NoUseCAS noUseCAS = new NoUseCAS();
        for (int i = 0; i < 10; i++) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        TimeUnit.MILLISECONDS.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    noUseCAS.add();
                }
            }).start();
        }
        try {
            TimeUnit.SECONDS.sleep(2);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("最后结果：" + noUseCAS.getValue());
    }
}
