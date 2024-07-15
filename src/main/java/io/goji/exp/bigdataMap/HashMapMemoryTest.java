package io.goji.exp.bigdataMap;

import java.util.HashMap;

public class HashMapMemoryTest {

    public static void main(String[] args) {
        HashMap<String, String> map = new HashMap<>();
        int count = 0;

        try {
            for (int i = 0; i < 1000000; i++) {
                map.put("key" + i, "value" + i);
                count++;

                // Optional: Print progress every 100,000 entries
                if (i % 100000 == 0) {
                    System.out.println("Inserted " + i + " entries");
                }
            }
        } catch (OutOfMemoryError e) {
            System.out.println("Out of memory after inserting " + count + " entries");
        }

        System.out.println("Final count: " + count);
    }
}
