package com.naturalprogrammer.visualflow;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.lang.reflect.Method;
import java.util.List;

@Getter
@Builder
public class FlowObject {

    private final FlowObjectType type;
    private final Method method;
    private final List<String> parameterNames;
    private final List<String> returnValueNames;

    private List<Connection> connections;

    @Override
    public String toString() {
        return type
                + "(method=" + (method == null ? "null" : method.getName())
                + ", parameterNames=" + parameterNames
                + ", returnValueNames=" + returnValueNames
                + ", next-count=" + (connections == null ? 0 : connections.size()) + ")";
    }

    void setConnections(List<Connection> connections) {
        this.connections = connections;
    }
}
