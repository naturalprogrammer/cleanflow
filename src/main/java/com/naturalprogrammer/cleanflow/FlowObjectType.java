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

import lombok.AccessLevel;
import lombok.AllArgsConstructor;

import java.util.function.IntPredicate;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum FlowObjectType {

    START_EVENT(false, Count.ONE, Count.ONE),
    END_EVENT(false, Count.ZERO, Count.ZERO),

    TASK(true, Count.ONE, Count.ONE),
    SEND_TASK(true, Count.ONE, Count.ONE),
    RECEIVE_TASK(true, Count.ONE, Count.ONE),
    USER_TASK(true, Count.ONE, Count.ONE),
    MANUAL_TASK(true, Count.ONE, Count.ONE),
    BUSINESS_RULE_TASK(true, Count.ONE, Count.ONE),
    SERVICE_TASK(true, Count.ONE, Count.ONE),
    SCRIPT_TASK(true, Count.ONE, Count.ONE),
    CALL_ACTIVITY(true, Count.ONE, Count.ONE),
    SUB_PROCESS(true, Count.ONE, Count.ONE),

    EXCLUSIVE_GATEWAY(true, Count.MORE_THAN_ONE, Count.ONE),
    INCLUSIVE_GATEWAY(false, Count.MORE_THAN_ONE, Count.MORE_THAN_ONE),
    PARALLEL_GATEWAY(false, Count.MORE_THAN_ONE, Count.MORE_THAN_ONE);

    private final boolean mustBeMapped;
    private final IntPredicate validConnectionCount;
    private final IntPredicate validFollowCount;

    public boolean mustBeMapped() {
        return mustBeMapped;
    }

    public boolean connectionCountIsValid(int connectionCount) {
        return validConnectionCount.test(connectionCount);
    }

    public boolean followCountIsValid(int followCount) {
        return validFollowCount.test(followCount);
    }

    private static class Count {
        private static final IntPredicate ZERO = count -> count == 0;
        private static final IntPredicate ONE = count -> count == 1;
        private static final IntPredicate MORE_THAN_ONE = count -> count > 1;
    }
}
