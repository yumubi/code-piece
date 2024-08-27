package io.goji.exp.webfetcher;

import java.util.Arrays;

public final class WebCrawler {
    private WebCrawler() {
    }

    static void crawl(
            String url,
            int depth,
            Fetcher fetcher
    ) {
        if (depth <= 0) {
            return;
        }

        Fetcher.Result result;
        try {
            result = fetcher.fetch(url);
        } catch (FetcherException e) {
            System.out.println(e.getMessage());
            return;
        }

        var body = result.body();
        var urls = result.urls();

        System.out.printf(
                "Found: %s %s\n",
                body,
                Arrays.toString(urls)
        );

        for (var u : urls) {
            crawl(u, depth - 1, fetcher);
        }
    }

    public static void main(String[] args) {
        var fetcher = FakeFetcher.example();

        crawl("https://golang.org/", 4, fetcher);
    }
}
