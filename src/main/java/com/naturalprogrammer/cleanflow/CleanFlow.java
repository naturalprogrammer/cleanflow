package com.naturalprogrammer.cleanflow;

import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * The compiled representation of a flow diagram
 */
@Slf4j
@AllArgsConstructor
public class CleanFlow {

    /**
     * Start node
     */
    private final FlowObject start;

    /**
     * Gets the given flow diagram from cache, and executes it.
     * (If not found in cache, then first loads it)
     *
     * @param path      Path of the diagram. Usuallay a classpath
     * @param service   Caller service instance
     * @param variables variables to be used in this execution of the flow
     * @return Resultant variables, from which caller can pick the relevant ones
     */
    public static Map<String, Object> execute(String path, Object service, Map<String, Object> variables) {

        return CleanFlowCache
                .get(path, service.getClass())
                .execute(service, variables);
    }

    /**
     * Executes this flow with the given service and variables
     *
     * @param service   the calling service instance
     * @param variables variables that are used in the execution
     * @return Resultant variables, from which caller can pick the relevant ones
     */
    private Map<String, Object> execute(Object service, Map<String, Object> variables) {

        executeFlowObjectAndChildren(start, service, variables);
        return variables;
    }

    /**
     * Executes the given flowObject, along with the children tree
     */
    @SneakyThrows
    private void executeFlowObjectAndChildren(FlowObject flowObject, Object service, Map<String, Object> variables) {

        log.debug("Executing {} ... ", flowObject);
        Object returnValue = executeFlowObject(flowObject, service, variables);

        flowObject.getConnections().forEach(child -> {
            if (toFollowConnection(flowObject.getType(), returnValue, child))
                executeFlowObjectAndChildren(child.getNext(), service, variables);
        });
    }

    /**
     * Executes the given flowObject
     */
    private Object executeFlowObject(FlowObject flowObject, Object service, Map<String, Object> variables) {

        if (flowObject.getMethod() != null) {

            List<Object> parameters = getParameters(flowObject, variables);
            Object returnValue = invoke(service, flowObject.getMethod(), parameters);
            putReturnValues(flowObject, variables, returnValue);
            return returnValue;
        }

        return null;
    }

    @SneakyThrows
    private Object invoke(Object service, Method method, List<Object> parameters) {

        try {
            log.debug("Invoking {} with {} parameters", method.getName(), parameters.size());
            return method.invoke(service, parameters.toArray());

        } catch (InvocationTargetException ex) {
            throw ex.getCause();
        }
    }

    private boolean toFollowConnection(FlowObjectType type, Object returnValue, Connection child) {

        if (!FlowObjectType.EXCLUSIVE_GATEWAY.equals(type))
            return true;

        return returnValue.toString().equals(child.getValue());
    }

    private List<Object> getParameters(FlowObject flowObject, Map<String, Object> variables) {

        List<String> parameterNames = flowObject.getParameterNames();
        List<Object> parameters = new ArrayList<>(parameterNames.size());
        flowObject.getParameterNames().forEach(parameterName -> {

            Object parameter = variables.get(parameterName);
            if (parameter == null)
                throw new FlowException(String.format("Couldn't find parameter %s for method %s",
                        parameterName, flowObject.getMethod().getName()));

            parameters.add(variables.get(parameterName));
        });
        return parameters;
    }

    private void putReturnValues(FlowObject flowObject, Map<String, Object> variables, Object returnValue) {

        List<String> returnValues = flowObject.getReturnValueNames();

        if (returnValues.isEmpty())
            return;

        if (returnValues.size() == 1) {
            variables.put(returnValues.get(0), returnValue);
            return;
        }

        List<Object> returnList = (List<Object>) returnValue;
        if (returnValues.size() != returnList.size())
            throw new FlowException(String.format(
                    "Error returning from %s: expected return values are %s, but given %d parameters",
                    flowObject.getMethod().getName(), returnValues, returnList.size()));

        Iterator<Object> returnValueIterator = returnList.iterator();
        returnValues.forEach(retValName -> variables.put(retValName, returnValueIterator.next()));
    }
}
