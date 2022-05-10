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

import com.naturalprogrammer.cleanflow.CleanFlow;

public class Camunda8ParallelGatewayService {

    private Logger log;

    public void execute() {
        CleanFlow.execute("clean-flows/camunda-8-parallel-gateway.bpmn", this);
    }

    public void taskA() {
        log.info("Doing task A");
    }

    public void taskB1() {
        log.info("Doing task B1");
    }

    public void taskB2() {
        log.info("Doing task B2");
    }

}
