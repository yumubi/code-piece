package io.goji.exp.httpclient;

import javax.swing.text.html.HTML;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.*;
import java.time.Duration;
import java.time.Month;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Pattern;
import java.util.Comparator;

/**
 * 异步编排
 */
public class Mysqltaobao {

    private static final int MAX_RETRIES = 3;
    private static final Duration TIMEOUT = Duration.ofSeconds(5);

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        var client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();

        YearMonth now = YearMonth.now();
        YearMonth curr = YearMonth.of(2014, Month.AUGUST);

        List<CompletableFuture<MonthlyReport>> futures = new ArrayList<>();
        CopyOnWriteArrayList<MonthlyReport> reports = new CopyOnWriteArrayList<>();

        while (curr.isBefore(now)) {
            YearMonth finalCurr = curr;
            String url = String.format("http://mysql.taobao.org/monthly/%4d/%02d/", curr.getYear(), curr.getMonthValue());
            System.out.println("log: " + url);
            var req = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(TIMEOUT)
                    .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.3")
                    .version(HttpClient.Version.HTTP_1_1)
                    .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                    .GET()
                    .build();

            CompletableFuture<MonthlyReport> future = sendRequestWithRetries(client, req, MAX_RETRIES)
                    .thenCompose(response -> {
                        List<String> posts = extractElementListsByClassName(response, HTML.Tag.UL, "posts", " ", "");
                        if (posts.size() < 1) {
                            return CompletableFuture.failedFuture(new Exception("no posts"));
                        }
                        return CompletableFuture.completedFuture(posts);
                    })
                    .thenCompose(posts -> {
                        var titles = new ArrayList<String>();
                        if (posts == null) {
                            return CompletableFuture.failedFuture(new Exception("the posts is null"));
                        }
                        for (String post : posts) {
                            titles.addAll(extractValueFromElement(post, HTML.Tag.A, "main"));
                        }
                        return CompletableFuture.completedFuture(new MonthlyReport(finalCurr, titles));
                    })
                    .exceptionally(e -> {
                        System.out.println("error: " + e);
                        return null;
                    });

            futures.add(future);

            curr = curr.plusMonths(1);
        }

        // Wait for all futures to complete
        CompletableFuture<Void> allOf = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
        allOf.join();

        // 可以断网测试
        System.out.println("log: " + "已经完成所有请求");

        Thread.sleep(1000 * 10);


        // Collect results
        for (CompletableFuture<MonthlyReport> future : futures) {
            MonthlyReport report = future.join();
            if (report != null) {
                reports.add(report);
            }
        }

        // Sort by date
        reports.sort(Comparator.comparing(report -> report.date));

        // Write to file
        try {
            Files.writeString(Paths.get("数据库内核月报.md"), "", StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            for (MonthlyReport report : reports) {
                StringBuilder sb = new StringBuilder("\n### ")
                        .append("[")
                        .append("数据库内核月报 ")
                        .append(report.date)
                        .append("](")
                        .append(String.format("http://mysql.taobao.org/monthly/%4d/%02d/", report.date.getYear(), report.date.getMonthValue()))
                        .append(")");

                for (String title : report.titles) {
                    sb.append("\n")
                            .append("##### ")
                            .append(title);
                }
                Files.writeString(Paths.get("数据库内核月报.md"),
                        sb.toString(),
                        StandardOpenOption.APPEND);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        client.close();
    }

    private static CompletableFuture<String> sendRequestWithRetries(HttpClient client, HttpRequest req, int retries) {
        return client.sendAsync(req, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .exceptionally(ex -> {
                    if (retries > 0) {
                        System.out.println("Retrying... (" + (MAX_RETRIES - retries + 1) + ")");
                        return sendRequestWithRetries(client, req, retries - 1).join();
                    } else {
                        throw new RuntimeException(ex);
                    }
                });
    }

    public static List<String> extractElementListsByClassName(String html, HTML.Tag tag, String className, String valBeforeClass, String valAfterClass) {
        List<String> result = new ArrayList<>();
        while (true) {
            String startRegex = "<" + tag + valBeforeClass + "class=\"" + className + "\"" + valAfterClass + ">";
            int start = html.indexOf(startRegex);
            if (start == -1) {
                break;
            }
            String endRegex = "</" + tag + ">";
            int end = html.indexOf(endRegex, start);
            String content = html.substring(start + startRegex.length(), end);
            if (className.equals("main")) {
                System.out.println("捕获到的内容: " + content + "\n");
            }
            result.add(content);
            html = html.substring(end);
        }
        return result;
    }

    public static List<String> extractValueFromElement(String html, HTML.Tag tag, String feature) {
        Pattern pattern = Pattern.compile("<" + tag + ".*" + feature + ".*>(.*)</" + tag + ">");
        var matcher = pattern.matcher(html);
        List<String> result = new ArrayList<>();
        while (matcher.find()) {
            System.out.println("捕获到的内容: " + matcher.group(1) + "\n");
            result.add(matcher.group(1));
        }
        return result;
    }

    static class MonthlyReport {
        YearMonth date;
        List<String> titles;

        MonthlyReport(YearMonth date, List<String> titles) {
            this.date = date;
            this.titles = titles;
        }
    }
}
