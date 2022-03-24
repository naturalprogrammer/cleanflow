/*
 * Copyright 2020-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this artifact or file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.naturalprogrammer.cleanflow.services;

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
