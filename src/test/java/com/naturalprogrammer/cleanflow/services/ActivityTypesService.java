/*
 * Copyright 2020-2021 the original author or authors.
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

import static com.naturalprogrammer.cleanflow.services.Utils.mapOf;

public class ActivityTypesService {

    private Logger log;

    public void executeAllActivities() {

        log.info("Executing all activities");
        CleanFlow.execute("clean-flows/activity-types.bpmn",
                this, mapOf());

        log.info("Executed  all activities");
    }

    private void task() {
        log.info("Executed task");
    }

    private void sendTask() {
        log.info("Executed sendTask");
    }

    private void receiveTask() {
        log.info("Executed receiveTask");
    }

    private void userTask() {
        log.info("Executed userTask");
    }

    private void manualTask() {
        log.info("Executed manualTask");
    }

    private void businessRuleTask() {
        log.info("Executed businessRuleTask");
    }

    private void serviceTask() {
        log.info("Executed serviceTask");
    }

    private void scriptTask() {
        log.info("Executed scriptTask");
    }

    private void callActivity() {
        log.info("Executed callActivity");
    }

    private void subProcess() {
        log.info("Executed subProcess");
    }
}
