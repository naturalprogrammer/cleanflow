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

package com.naturalprogrammer.cleanflow;

import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Holds all the parsed CleanFlows
 */
@Slf4j
public class CleanFlowCache {

    private static final CleanFlowCache CACHE = new CleanFlowCache();
    private final Map<String, CleanFlow> parsedFlows = new ConcurrentHashMap<>();

    /**
     * Clears all parsed CleanFlows from the static singleton CACHE
     */
    public static void clear() {
        CACHE.parsedFlows.clear();
    }

    /**
     * Removes the given CleanFlow from the static singleton CACHE
     *
     * @param   path    Classpath of the diagram
     */
    public static void clear(final String path) {
        CACHE.parsedFlows.remove(path);
    }

    /**
     * Gets a parsed CleanFlow from the static singleton CACHE. If not parsed yet, first parse it.
     *
     * @param   path    Classpath of the diagram
     * @param   caller  The caller service class
     * @return  The parsed CleanFlow
     */
    public static CleanFlow get(final String path, Class<?> caller) {
        return CACHE.parseIfAbsent(path, caller);
    }

    /**
     * Gets a parsed CleanFlow from the static singleton CACHE. If not parsed yet, first parse it.
     *
     * @param   path    Classpath of the diagram
     * @param   caller  The caller service class
     * @return  The parsed CleanFlow
     */
    public CleanFlow parseIfAbsent(final String path, Class<?> caller) {
        return parsedFlows.computeIfAbsent(path, key -> CleanFlowParser.parse(key, caller));
    }
}
