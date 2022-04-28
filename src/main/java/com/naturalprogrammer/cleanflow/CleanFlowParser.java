/*
 * Copyright 2020-2022 the original author or authors.
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

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;

/**
 * Parses a CleanFlow
 */
@Slf4j
public class CleanFlowParser {

    private static final CleanFlowParser INSTANCE = new CleanFlowParser();
    private final XPathFactory xPathFactory = XPathFactory.newInstance();

    /**
     * Parses the diagram in the given path
     *
     * @param path   Path of the diagram
     * @param caller The caller service
     * @return the parsed CleanFlow
     */
    public static CleanFlow parse(String path, Class<?> caller) {
        return INSTANCE.parseFlow(path, caller);
    }

    /**
     * Parses the diagram in the given path
     *
     * @param path   Path of the diagram
     * @param caller The caller service
     * @return the parsed CleanFlow
     */
    @SneakyThrows
    private CleanFlow parseFlow(String path, Class<?> caller) {

        try (InputStream input = getClass().getClassLoader().getResourceAsStream(path)) {

            Document xmlDocument = DocumentBuilderFactory
                    .newInstance()
                    .newDocumentBuilder()
                    .parse(input);

            XPath xPath = xPathFactory.newXPath();

            String startEventIdPath = "string(//startEvent/@id)";
            String startNodeId = (String) xPath.compile(startEventIdPath)
                    .evaluate(xmlDocument, XPathConstants.STRING);

            // Collect all the methods in the service
            Map<String, Method> methods = Arrays.stream(caller.getDeclaredMethods())
                    .collect(toMap(Method::getName, identity()));

            log.info("Found methods {}", methods.keySet());

            // Build the flow object graph
            FlowObject start = parseFlowObject(xmlDocument, xPath, startNodeId, methods, new HashMap<>(40));
            logParsedFlow(path, start);
            return new CleanFlow(start);
        }
    }

    /**
     * Parses a FlowObject along with its children
     *
     * @param xmlDocument   The given diagram
     * @param xPath         An xPath
     * @param flowObjectId  The id of the FlowObject to parse
     * @param methods       Methods in the service
     * @param alreadyParsed A cache of already parsed FlowObjects
     * @return The FlowObject
     */
    private FlowObject parseFlowObject(Document xmlDocument,
                                       XPath xPath,
                                       String flowObjectId,
                                       Map<String, Method> methods,
                                       Map<String, FlowObject> alreadyParsed)
            throws XPathExpressionException, NoSuchMethodException {

        log.info("Building {}", flowObjectId);

        FlowObject flowObject = alreadyParsed.get(flowObjectId);
        if (flowObject != null) {
            log.info("{} already built", flowObjectId);
            return flowObject;
        }

        FlowObjectType type = getFlowObjectType(xmlDocument, xPath, flowObjectId);

        FlowObject.FlowObjectBuilder builder = FlowObject.builder();
        builder.id(flowObjectId);
        builder.type(type);

        // Get the method for the flow object
        getMethod(flowObjectId, methods).ifPresent(method -> {
            method.setAccessible(true);
            builder.method(method)
                    .parameterNames(getParameterNames(method))
                    .returnValueNames(getReturnValueNames(method));
        });

        flowObject = builder.build();
        alreadyParsed.put(flowObjectId, flowObject);

        // Traverse connections
        flowObject.setConnections(parseConnections(xmlDocument, xPath, flowObjectId, methods, alreadyParsed));

        log.info("Built {}: {}", flowObjectId, flowObject);
        flowObject.validate();
        return flowObject;
    }

    /**
     * Parses connections of the given FlowObject
     */
    private List<Connection> parseConnections(Document xmlDocument,
                                              XPath xPath,
                                              String flowObjectId,
                                              Map<String, Method> methods,
                                              Map<String, FlowObject> alreadyParsedFlowObjects)
            throws XPathExpressionException, NoSuchMethodException {

        // Get the list of connection names
        String connectionNamePath = "//sequenceFlow[@sourceRef='" + flowObjectId + "']/@name";
        NodeList connectionNames = (NodeList) xPath.compile(connectionNamePath).evaluate(xmlDocument, XPathConstants.NODESET);

        // Get the list of connected nodes
        String connectedNodesPath = "//sequenceFlow[@sourceRef='" + flowObjectId + "']/@targetRef";
        NodeList connectedNodes = (NodeList) xPath.compile(connectedNodesPath).evaluate(xmlDocument, XPathConstants.NODESET);

        // Build connections by matching connection names and connected nodes
        List<Connection> connections = new ArrayList<>(connectedNodes.getLength());
        for (int i = 0; i < connectedNodes.getLength(); i++) {

            String connectedNodeId = connectedNodes.item(i).getNodeValue();
            String connectionName = connectionNames.item(i) == null ? null : connectionNames.item(i).getNodeValue();
            if (connectionName != null)
                connectionName = connectionName.trim();

            log.info("Traversing from {} to {} via connection {} - {}", flowObjectId, connectedNodeId, i + 1, connectionName);

            FlowObject target = parseFlowObject(xmlDocument, xPath, connectedNodeId, methods, alreadyParsedFlowObjects);
            Connection connection = new Connection(connectionName, target);
            connections.add(connection);
            log.info("Added connection {}", connection);
        }
        return connections;
    }

    private List<String> getReturnValueNames(Method method) {

        Returns returns = method.getAnnotation(Returns.class);
        return returns == null ?
                new ArrayList<>() :
                Arrays.asList(returns.value());
    }

    private List<String> getParameterNames(Method method) {

        return Arrays.stream(method.getParameters())
                .map(Parameter::getName)
                .collect(Collectors.toList());
    }

    private Optional<Method> getMethod(String flowObjectId,
                                       Map<String, Method> methods) {

        String methodName = getMethodNameFrom(flowObjectId);

        if (methodName.isEmpty())
            return Optional.empty();

        return Optional.ofNullable(methods.get(methodName));
    }

    /**
     * flowObjectId could be containing dashes,
     * e.g. isOrderProcessed-2 (when trying to map two flowObjects to the same method)
     * <p>
     * So, let's remove everything beginning with dash till the end
     *
     * @param flowObjectId ID of the flowObject
     * @return methodName
     */
    private String getMethodNameFrom(String flowObjectId) {

        int dashIndex = flowObjectId.indexOf("-");
        if (dashIndex == -1) // dash doesn't exist
            return flowObjectId;

        return flowObjectId.substring(0, dashIndex);
    }

    private FlowObjectType getFlowObjectType(Document xmlDocument, XPath xPath, String flowObjectId) throws XPathExpressionException {

        String expression = "local-name(//*[@id='" + flowObjectId + "'])";
        String type = (String) xPath.compile(expression).evaluate(xmlDocument, XPathConstants.STRING);

        return FlowObjectType.valueOf(camelToSnake(type));
    }

    private String camelToSnake(String camel) {
        return camel
                .replaceAll("([A-Z]+)([A-Z][a-z])", "$1_$2")
                .replaceAll("([a-z])([A-Z])", "$1_$2")
                .toUpperCase();
    }

    private void logParsedFlow(String path, FlowObject flowObject) {
        log.info("---------------------------------" + path + " begin --------------------------------");
        logParsedFlowObject(0, flowObject, new HashSet<>());
        log.info("---------------------------------" + path + " end ----------------------------------");
    }

    private void logParsedFlowObject(int indent, FlowObject flowObject, Set<FlowObject> logged) {

        String indentSpaces = spaces(indent);
        log.info(indentSpaces + flowObject.toString());

        if (logged.contains(flowObject))
            return;

        logged.add(flowObject);

        flowObject.getConnections().forEach(connection -> {
            if (connection.getValue() != null)
                log.info(indentSpaces + connection.getValue());
            logParsedFlowObject(indent + 4, connection.getNext(), logged);
        });
    }

    private String spaces(int indent) {
        return String.join("", Collections.nCopies(indent, " "));
    }
}
