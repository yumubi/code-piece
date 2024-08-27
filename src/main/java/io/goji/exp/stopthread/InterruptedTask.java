package io.goji.exp.stopthread;


public class InterruptedTask implements Runnable{

    @Override
    public void run() {

        Thread currentThread = Thread.currentThread();
        while (true){
            if(currentThread.isInterrupted()){
                break;
            }

            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                currentThread.interrupt();
                e.printStackTrace();
            }
        }
    }
}
