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
import com.naturalprogrammer.cleanflow.Returns;

import static com.naturalprogrammer.cleanflow.services.Utils.mapOf;

public class LoopingService {

    private Logger log;

    public void achieveGoal() {

        log.info("Achieving goal");
        CleanFlow.execute("clean-flows/looping.bpmn",
                this, mapOf("counter", 0));

        log.info("Achieved");
    }

    private String isGoalAchieved(int counter) {

        String goalAchieved = counter < 3 ? "NO" : "YES";
        log.info("Goal achieved: " + goalAchieved);

        return goalAchieved;
    }

    @Returns("counter")
    private int workHard(int counter) {

        log.info("Working " + counter);
        return counter + 1;
    }
}
