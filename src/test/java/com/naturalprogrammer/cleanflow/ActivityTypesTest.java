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

package com.naturalprogrammer.cleanflow;

import com.naturalprogrammer.cleanflow.services.ActivityTypesService;
import com.naturalprogrammer.cleanflow.services.Logger;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@ExtendWith(MockitoExtension.class)
class ActivityTypesTest {

    @Spy
    private Logger logger;

    @InjectMocks
    private ActivityTypesService service;

    @Test
    void testActivityTypes() {

        // when
        service.executeAllActivities();

        // then
        verify(logger).info("Executing all activities");

        verify(logger).info("Executed task");
        verify(logger).info("Executed sendTask");
        verify(logger).info("Executed receiveTask");
        verify(logger).info("Executed userTask");
        verify(logger).info("Executed manualTask");
        verify(logger).info("Executed businessRuleTask");
        verify(logger).info("Executed serviceTask");
        verify(logger).info("Executed scriptTask");
        verify(logger).info("Executed callActivity");
        verify(logger).info("Executed subProcess");

        verify(logger).info("Executed  all activities");

        verifyNoMoreInteractions(logger);
    }
}
