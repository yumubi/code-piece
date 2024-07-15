package io.goji.exp.virtualThread;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class networkingIO {
    public static void main(String[] args) {
        var networkingIO = new networkingIO();
        try {
            var urls = new URL[]{
                    new URL("https://www.bilibili.com"),
                    new URL("https://www.baidu.com"),
                    new URL("https://www.bing.com")
            };
            var urlData = networkingIO.retrieveURLs(urls);
            urlData.forEach(data -> System.out.println(data.url() + " " + data.response().length));
        } catch (Exception e) {
            e.printStackTrace();
        }

    }



    List<io.goji.jav.virtualThread.URLData> retrieveURLs(URL... urls) throws Exception {
//        try (var executor = Executors.newVirtualThreadExecutor()) {
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            var tasks = Arrays.stream(urls)
                    .map(url -> (Callable<io.goji.jav.virtualThread.URLData>)() -> getURL(url))
                    .toList();
               return executor.invokeAll(tasks)
                    .stream()
                    .map(future -> {
                        try {
                            return future.get();
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    })
                    .toList();
        }
    }



    static io.goji.jav.virtualThread.URLData getURL(URL url) throws IOException {
        try (InputStream in = url.openStream()) {
            return new io.goji.jav.virtualThread.URLData(url, in.readAllBytes());
        }
    }

}
