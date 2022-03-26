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

    START_EVENT(false, FollowCount.ONE),
    END_EVENT(false, FollowCount.ZERO),

    TASK(true, FollowCount.ONE),
    SEND_TASK(true, FollowCount.ONE),
    RECEIVE_TASK(true, FollowCount.ONE),
    USER_TASK(true, FollowCount.ONE),
    MANUAL_TASK(true, FollowCount.ONE),
    BUSINESS_RULE_TASK(true, FollowCount.ONE),
    SERVICE_TASK(true, FollowCount.ONE),
    SCRIPT_TASK(true, FollowCount.ONE),
    CALL_ACTIVITY(true, FollowCount.ONE),
    SUB_PROCESS(true, FollowCount.ONE),

    EXCLUSIVE_GATEWAY(true, FollowCount.ONE),
    INCLUSIVE_GATEWAY(false, FollowCount.MORE_THAN_ONE);

    private final boolean mustBeMapped;
    private final IntPredicate validFollowCount;

    public boolean mustBeMapped() {
        return mustBeMapped;
    }

    public boolean followCountIsValid(int followCount) {
        return validFollowCount.test(followCount);
    }

    private static class FollowCount {
        private static final IntPredicate ZERO = followCount -> followCount == 0;
        private static final IntPredicate ONE = followCount -> followCount == 1;
        private static final IntPredicate MORE_THAN_ONE = followCount -> followCount > 1;
    }
}
