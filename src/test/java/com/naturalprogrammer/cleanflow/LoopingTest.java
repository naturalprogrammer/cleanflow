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

import com.naturalprogrammer.cleanflow.services.Logger;
import com.naturalprogrammer.cleanflow.services.LoopingService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class LoopingTest {

    @Spy
    private Logger logger;

    @InjectMocks
    private LoopingService service;

    @Test
    void testLooping() {

        // when
        service.achieveGoal();

        // then
        verify(logger).info("Achieving goal");
        verify(logger, times(3)).info("Goal achieved: NO");
        verify(logger).info("Working 0");
        verify(logger).info("Working 1");
        verify(logger).info("Working 2");
        verify(logger).info("Goal achieved: YES");
        verify(logger).info("Achieved");

        verifyNoMoreInteractions(logger);
    }
}
