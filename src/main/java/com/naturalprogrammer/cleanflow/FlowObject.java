package com.naturalprogrammer.cleanflow;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.lang.reflect.Method;
import java.util.List;

/**
 * Represents a node in the diagram
 */
@Getter
@Builder
public class FlowObject {

    private final FlowObjectType type;
    private final Method method;
    private final List<String> parameterNames;
    private final List<String> returnValueNames;

    @Setter(AccessLevel.PACKAGE)
    private List<Connection> connections;

    @Override
    public String toString() {
        return type
                + "(method=" + (method == null ? "null" : method.getName())
                + ", parameterNames=" + parameterNames
                + ", returnValueNames=" + returnValueNames
                + ", next-count=" + (connections == null ? 0 : connections.size()) + ")";
    }
}
