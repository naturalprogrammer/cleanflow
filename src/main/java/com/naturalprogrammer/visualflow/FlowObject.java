package com.naturalprogrammer.visualflow;

import lombok.Builder;
import lombok.Getter;

import java.lang.reflect.Method;
import java.util.List;

@Getter
@Builder
public class FlowObject {

    private final FlowObjectType type;
    private final Method method;
    private final List<String> parameterNames;
    private final List<String> returnValueNames;

    private final List<Connection> connections;
}
