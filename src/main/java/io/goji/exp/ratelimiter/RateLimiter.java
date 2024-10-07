package io.goji.exp.ratelimiter;

public abstract class RateLimiter {
    abstract public boolean acquire(long n);
}
