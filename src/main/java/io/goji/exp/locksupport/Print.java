package io.goji.exp.locksupport;

import java.util.concurrent.locks.LockSupport;

public class Print {
    public static void main(String[] args) throws InterruptedException {
        Thread t1 = new Thread(() -> {
            while (true) {
                //try { Thread.sleep(1000); } catch (InterruptedException e) { }
                // 当没有许可时，当前线程暂停运行；有许可时，用掉这个许可，当前线程恢复运行
                LockSupport.park();
                System.out.println("1");
            }
        });
        Thread t2 = new Thread(() -> {
            while (true) {
                System.out.println("2");
                // 给线程 t1 发放『许可』（多次连续调用 unpark 只会发放一个『许可』）
                LockSupport.unpark(t1);
                try { Thread.sleep(500); } catch (InterruptedException e) { }
            }
        });
        t1.start();
        t2.start();
    }

}
