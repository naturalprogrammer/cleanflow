package com.naturalprogrammer.visualflow.services;

import java.util.HashMap;
import java.util.Map;

public class Utils {

    /**
     * Constructs a map of the key-value pairs,
     * passed as parameters
     */
    public static <K, V> Map<K, V> mapOf(Object... keyValPair) {

        if (keyValPair.length % 2 != 0) {
            throw new IllegalArgumentException("Keys and values must be in pairs");
        }

        Map<K, V> map = new HashMap<>(keyValPair.length / 2);

        for (int i = 0; i < keyValPair.length; i += 2) {
            map.put((K) keyValPair[i], (V) keyValPair[i + 1]);
        }

        return map;
    }

}
