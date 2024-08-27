package io.goji.exp.stopthread;


import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;

public class InterruptedTest {


    public static void main(String[] args){
        InterruptedTask interruptedTask = new InterruptedTask();
        Thread interruptedThread = new Thread(interruptedTask);
        interruptedThread.start();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        interruptedThread.interrupt();
    }



//    final Arena arena = Arena.ofShared();
//    final MemorySegment.Scope scope = arena.scope();
//    void loop() {
//        while(true) {
//            if (!scope.isAlive()) {
//                break;
//            }
//            // ...
//        }
//        System.out.println("end !");
//    }
//
//    void main() throws InterruptedException {
//        var thread = new Thread(this::loop);
//        thread.start();
//
//        Thread.sleep(1_000);
//        arena.close();
//    }
}
