//package io.goji.exp.CountDownLatch;
//
//import io.goji.jav.CountDownLatch.ConfigService;
//import io.goji.jav.CountDownLatch.MainService;
//import io.goji.jav.CountDownLatch.WaitingWorker;
//import io.goji.jav.CountDownLatch.Worker;
//import org.junit.jupiter.api.Test;
//
//import java.util.ArrayList;
//import java.util.Collections;
//import java.util.List;
//import java.util.concurrent.CountDownLatch;
//import java.util.stream.Stream;
//
//import static java.util.stream.Collectors.toList;
//
//public class CountDownLatchTest {
//    @Test
//    public void whenParallelProcessing_thenMainThreadWillBlockUntilCompletion()
//            throws InterruptedException {
//
//        List<String> outputScraper = Collections.synchronizedList(new ArrayList<>());
//        CountDownLatch countDownLatch = new CountDownLatch(5);
//        List<Thread> workers = Stream
//                .generate(() -> new Thread(new Worker(outputScraper, countDownLatch)))
//                .limit(5)
//                .collect(toList());
//
//        workers.forEach(Thread::start);
//        countDownLatch.await();
//        outputScraper.add("Latch released");
//
////        assertThat(outputScraper)
////                .containsExactly(
////                        "Counted down",
////                        "Counted down",
////                        "Counted down",
////                        "Counted down",
////                        "Counted down",
////                        "Latch released"
////                );
//
//        System.out.println(outputScraper);
//
//    }
//
//
//    @Test
//    public void whenDoingLotsOfThreadsInParallel_thenStartThemAtTheSameTime()
//            throws InterruptedException {
//
//        List<String> outputScraper = Collections.synchronizedList(new ArrayList<>());
//        CountDownLatch readyThreadCounter = new CountDownLatch(5);
//        CountDownLatch callingThreadBlocker = new CountDownLatch(1);
//        CountDownLatch completedThreadCounter = new CountDownLatch(5);
//        List<Thread> workers = Stream
//                .generate(() -> new Thread(new WaitingWorker(
//                        outputScraper, readyThreadCounter, callingThreadBlocker, completedThreadCounter)))
//                .limit(5)
//                .collect(toList());
//
//        workers.forEach(Thread::start);
//        readyThreadCounter.await();
//        outputScraper.add("Workers ready");
//        callingThreadBlocker.countDown();
//        completedThreadCounter.await();
//        outputScraper.add("Workers complete");
//
////        assertThat(outputScraper)
////                .containsExactly(
////                        "Workers ready",
////                        "Counted down",
////                        "Counted down",
////                        "Counted down",
////                        "Counted down",
////                        "Counted down",
////                        "Workers complete"
////                );
//
//        System.out.println(outputScraper);
//    }
//
//
//    @Test
//    public void StartServiceOrderly() throws InterruptedException {
//        var latch = new CountDownLatch(2);
//        var mainService = new MainService(latch);
//        var configService1 = new ConfigService(1, latch);
//        var configService2 = new ConfigService(2, latch);
//
//        mainService.start();
//        configService1.start();
//        configService2.start();
//
//        mainService.join();
//    }
//
//}
