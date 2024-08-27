package io.goji.exp.webfetcher;

import java.util.Map;

final class FakeFetcher implements Fetcher {
    private final Map<String, Result> results;

    public FakeFetcher(Map<String, Result> results) {
        this.results = results;
    }

    @Override
    public Result fetch(String url) throws FetcherException {
        var result = this.results.get(url);
        if (result == null) {
            throw new FetcherException("Not Found: " + url);
        } else {
            return result;
        }
    }

    public static Fetcher example() {
        return new FakeFetcher(Map.of(
                "https://golang.org/", new Fetcher.Result(
                        "The Go Programming Language",
                        new String[]{
                                "https://golang.org/pkg/",
                                "https://golang.org/cmd/"
                        }
                ),
                "https://golang.org/pkg/", new Fetcher.Result(
                        "Packages",
                        new String[]{
                                "https://golang.org/",
                                "https://golang.org/cmd/",
                                "https://golang.org/pkg/fmt/",
                                "https://golang.org/pkg/os/",
                        }
                ),
                "https://golang.org/pkg/fmt/", new Fetcher.Result(
                        "Package fmt",
                        new String[]{
                                "https://golang.org/",
                                "https://golang.org/pkg/",
                        }
                ),
                "https://golang.org/pkg/os/", new Fetcher.Result(
                        "Package os",
                        new String[]{
                                "https://golang.org/",
                                "https://golang.org/pkg/",
                        }
                )
        ));
    }
}
