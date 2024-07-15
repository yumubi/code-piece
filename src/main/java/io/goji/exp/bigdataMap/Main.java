package io.goji.exp.bigdataMap;

import java.util.*;

public class Main {

    static int NUMBER_DISTINCT_KEYS = 500_000_000;
    static int STRING_SIZE_FACTOR = 1;
    static int CHECKPOINT_INTERVAL = 1000;
    static int METRICS_INTERVAL_MILLIS = 1000;

    static boolean FAST_FILL_MAP = false;

    static boolean OPTIMIZE_VALUE_ALLOCATION = false;

    static boolean CREATE_EXTRA_GARBAGE = false;
    static int GARBAGE_PASSES_PER_LOOP = 1;
    static int GARBAGE_BIN_SIZE = 2000;

    public static void main(String[] args) {
        stress();
    }

    private interface Mapper {
        int updateStats(String key);
        long size();
    }

    private static class IntegerMapper implements Mapper {
        Map<String, Integer> values = new HashMap<>();

        @Override
        public int updateStats(String key) {
            return values.compute(key, (k, i) -> i == null ? 1 : i + 1);
        }

        @Override
        public long size() {
            return values.size();
        }
    }

    private static class MemoryOptimizedMapper implements Mapper {
        Map<String, IntHolder> values = new HashMap<>();

        @Override
        public int updateStats(String key) {
            return values.compute(key, (k, h) -> h == null ? new IntHolder(1) : h.add()).accumulator;
        }

        @Override
        public long size() {
            return values.size();
        }

        private static class IntHolder {
            public IntHolder(int accumulator) {
                this.accumulator = accumulator;
            }

            int accumulator = 0;

            public IntHolder add() {
                accumulator++;
                return this;
            }
        }
    }

    private static void stress() {
        Mapper mapper = OPTIMIZE_VALUE_ALLOCATION ? new MemoryOptimizedMapper() : new IntegerMapper();

        int count = 0;
        long start = System.currentTimeMillis();
        long lastCheckpoint = start;
        int lastCount = 0;
        int largestValue = 0;

        Set<String> garbageBin = new HashSet<>();
        long garbageBinSize = 0;

        while (true) {
            String key = createKey(count);
            int value = mapper.updateStats(key);
            if (value > largestValue) {
                largestValue = value;
            }

            // Create and eventually discard some extra garbage to simulate extra memory presure.
            if (CREATE_EXTRA_GARBAGE) {
                for (int i = 0; i < GARBAGE_PASSES_PER_LOOP; i++) {
                    garbageBin.add(String.format("This is some extra garbage: %s, %d", key, i));
                    garbageBinSize = garbageBin.size();
                    if (garbageBinSize == GARBAGE_BIN_SIZE) {
                        garbageBin = new HashSet<>();
                    }
                }
            }

            count++;
            if (count % CHECKPOINT_INTERVAL == 0) {
                long now = System.currentTimeMillis();

                if (lastCheckpoint + METRICS_INTERVAL_MILLIS <= now) {
                    int delta = count - lastCount;
                    long duration = now - lastCheckpoint;
                    double rate = (double) delta * 1000 / duration;
                    long size = mapper.size();
                    long totalDuration = now - start;
                    double totalRate = (double) count * 1000 / totalDuration;

                    System.out.printf("Size: %d;  Inserted: %d;  Rate since last: %.2f;  Rate since start: %.2f; Max value: %d; Garbage bin size: %d%n", size, delta, rate, totalRate, largestValue, garbageBinSize);
                    lastCheckpoint = now;
                    lastCount = count;
                }
            }
        }
    }

    static Random random = new Random(System.currentTimeMillis());

    // Create a large string.  MAX_VALUE possible distinct values.
    private static String createKey(int count) {
        int keyNumericValue;
        if (FAST_FILL_MAP && count < NUMBER_DISTINCT_KEYS) {
            keyNumericValue = count;
        } else {
            keyNumericValue = random.nextInt(NUMBER_DISTINCT_KEYS);
        }
        String base = Integer.toString(keyNumericValue);
        if (STRING_SIZE_FACTOR == 1) {
            return base;
        } else {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < STRING_SIZE_FACTOR; i++) {
                sb.append(base);
            }
            return sb.toString();
        }
    }
}
