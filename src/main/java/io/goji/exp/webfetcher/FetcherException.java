package io.goji.exp.webfetcher;

final class FetcherException extends Exception {
    FetcherException(String message) {
        super(message);
    }
}

interface Fetcher {
    record Result(String body, String[] urls) {
    }

    Result fetch(String url) throws FetcherException;
}
