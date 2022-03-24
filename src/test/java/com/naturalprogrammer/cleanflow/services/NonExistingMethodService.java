package com.naturalprogrammer.cleanflow.services;

import com.naturalprogrammer.cleanflow.CleanFlow;

public class NonExistingMethodService {

    public void execute() {
        CleanFlow.execute("clean-flows/non-existing-method.bpmn", this);
    }
}
