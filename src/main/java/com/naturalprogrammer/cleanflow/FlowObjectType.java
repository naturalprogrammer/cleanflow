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

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum FlowObjectType {

    START_EVENT(false),
    END_EVENT(false),

    TASK(true),
    SEND_TASK(true),
    RECEIVE_TASK(true),
    USER_TASK(true),
    MANUAL_TASK(true),
    BUSINESS_RULE_TASK(true),
    SERVICE_TASK(true),
    SCRIPT_TASK(true),
    CALL_ACTIVITY(true),
    SUB_PROCESS(true),

    EXCLUSIVE_GATEWAY(true),
    INCLUSIVE_GATEWAY(false);

    private final boolean mustBeMapped;

    public boolean mustBeMapped() {
        return mustBeMapped;
    }
}
