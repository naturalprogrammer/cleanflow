package com.naturalprogrammer.cleanflow.services;

import com.naturalprogrammer.cleanflow.CleanFlow;
import com.naturalprogrammer.cleanflow.Returns;
import com.naturalprogrammer.cleanflow.domain.OrderForm;
import com.naturalprogrammer.cleanflow.domain.OrderResource;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

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
