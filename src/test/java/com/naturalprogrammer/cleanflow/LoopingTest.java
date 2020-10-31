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
