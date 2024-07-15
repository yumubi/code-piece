package io.goji.jav.multiThread;

import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

public final class CacheEntry<K, V> {
    private static final Object CLAIM_TOKEN = new Object();

    private static final AtomicIntegerFieldUpdater<CacheEntry> HITS_UPDATER
            = AtomicIntegerFieldUpdater.newUpdater(CacheEntry.class, "hits");

    private static final AtomicReferenceFieldUpdater<CacheEntry, Object> TOKEN_UPDATOR
            = AtomicReferenceFieldUpdater.newUpdater(CacheEntry.class, Object.class, "accessToken");

    private final K key;
    private final long expires;
    private volatile V value;
    private volatile int hits = 1;
    private volatile Object accessToken;

    CacheEntry(K key, V value, final long expires) {
        this.key = key;
        this.value = value;
        this.expires = expires;
    }

    public V getValue() {
        return value;
    }

    public void setValue(final V value) {
        this.value = value;
    }

    public int hit() {
        while (true) {
            int i = hits;
            if (HITS_UPDATER.weakCompareAndSet(this, i, ++i)) {
                return i;
            }
        }
    }

    public K key() {
        return key;
    }

    Object claimToken() {
        while (true) {
            Object current = this.accessToken;
            if (current == CLAIM_TOKEN) {
                return Boolean.FALSE;
            }

            if (TOKEN_UPDATOR.compareAndSet(this, current, CLAIM_TOKEN)) {
                return current;
            }
        }
    }

    boolean setToken(Object token) {
        return TOKEN_UPDATOR.compareAndSet(this, CLAIM_TOKEN, token);
    }

    Object clearToken() {
        Object old = TOKEN_UPDATOR.getAndSet(this, null);
        return old == CLAIM_TOKEN ? null : old;
    }

    public long getExpires() {
        return expires;
    }
}
