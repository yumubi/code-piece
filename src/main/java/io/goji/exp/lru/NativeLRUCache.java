package io.goji.exp.lru;



import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public class NativeLRUCache<K, V> implements Cache<K, V> {

    private final LinkedHashMapCache<K, V> cache;

    public NativeLRUCache(int capacity) {
        this.cache = new LinkedHashMapCache<K, V>(capacity);
    }

    @Override
    public Optional<V> get(K key) {
        return Optional.ofNullable(cache.get(key));
    }

    @Override
    public boolean put(K key, V value) {
        cache.put(key, value);
        return false;
    }

    public boolean containsKey(K key) {
        return cache.containsKey(key);
    }

    @Override
    public int size() {
        return cache.size();
    }

    @Override
    public boolean isEmpty() {
        return cache.isEmpty();
    }

    @Override
    public void clear() {
        cache.clear();
    }

    private static class LinkedHashMapCache<K, V> extends LinkedHashMap<K, V> {

        private final int capacity;

        public LinkedHashMapCache(int capacity) {
            super(capacity, 0.75f, true);
            this.capacity = capacity;
        }

        @Override
        protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
            return size() > capacity;
        }
    }
}
