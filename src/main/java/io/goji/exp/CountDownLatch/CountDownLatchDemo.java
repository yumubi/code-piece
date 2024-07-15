package io.goji.exp.CountDownLatch;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static java.lang.StringTemplate.STR;

public class CountDownLatchDemo {

    public static void main(String[] args) throws IOException, InterruptedException {
        var server = HttpServer.create(new InetSocketAddress(8000), 0);
        server.setExecutor(Executors.newVirtualThreadPerTaskExecutor());
        server.createContext("/hello", new GreetingsHandler());

        CountDownLatch latch = new CountDownLatch(4);

        Thread.startVirtualThread(new Task("Load Config", latch));
        Thread.startVirtualThread(new Task("Init DB Connection", latch));
        Thread.startVirtualThread(new Task("Init Cache", latch));
        Thread.startVirtualThread(new Task("Start Embedded Server", latch));

        latch.await();

        System.out.println("All initializations complete. Application is starting...");

        server.start();
    }
}

record Task(String name, CountDownLatch latch) implements Runnable {
    @Override
    public void run() {
        doHeavyLifting();
        System.out.println(STR."\{name} has finished.");
        latch.countDown();
    }

    private static void doHeavyLifting() {
        try {
            TimeUnit.SECONDS.sleep(30);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}

class GreetingsHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange t) throws IOException {
        String response = "Hello world!";
        t.sendResponseHeaders(200, response.length());
        OutputStream os = t.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }
}
