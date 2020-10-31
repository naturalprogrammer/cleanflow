package com.naturalprogrammer.visualflow;

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
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;

@Slf4j
public class VisualFlowCache {

    private static final VisualFlowCache INSTANCE = new VisualFlowCache();

    private final Map<String, VisualFlow> parsedFlows = new ConcurrentHashMap<>();
    private final XPathFactory xPathFactory = XPathFactory.newInstance();

    public static VisualFlow get(final String path, Class<?> caller) {
        return INSTANCE.parseIfAbsent(path, caller);
    }

    public VisualFlow parseIfAbsent(final String flow, Class<?> caller) {
        return parsedFlows.computeIfAbsent(flow, path -> parse(path, caller));
    }

    @SneakyThrows
    private VisualFlow parse(String path, Class<?> caller) {

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
            FlowObject start = buildFlowObject(xmlDocument, xPath, startNodeId, methods, new HashMap<>(40));
            logParsedFlow(path, start);
            return new VisualFlow(start);
        }
    }

    private FlowObject buildFlowObject(Document xmlDocument,
                                       XPath xPath,
                                       String flowObjectId,
                                       Map<String, Method> methods,
                                       Map<String, FlowObject> alreadyBuilt)
            throws XPathExpressionException, NoSuchMethodException {

        log.info("Building {}", flowObjectId);

        FlowObject flowObject = alreadyBuilt.get(flowObjectId);
        if (flowObject != null) {
            log.info("{} already built", flowObjectId);
            return flowObject;
        }

        FlowObject.FlowObjectBuilder builder = FlowObject.builder();
        FlowObjectType type = getFlowObjectType(xmlDocument, xPath, flowObjectId);
        builder.type(type);

        // Get the method for the flow object
        getMethod(flowObjectId, methods).ifPresent(method -> {
            method.setAccessible(true);
            builder.method(method)
                    .parameterNames(getParameterNames(method))
                    .returnValueNames(getReturnValueNames(method));
        });

        flowObject = builder.build();
        alreadyBuilt.put(flowObjectId, flowObject);

        // Traverse connections
        flowObject.setConnections(getConnections(xmlDocument, xPath, flowObjectId, methods, alreadyBuilt));

        log.info("Built {}: {}", flowObjectId, flowObject);
        return flowObject;
    }

    private List<Connection> getConnections(Document xmlDocument,
                                            XPath xPath,
                                            String flowObjectId,
                                            Map<String, Method> methods,
                                            Map<String, FlowObject> alreadyBuiltFlowObjects)
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
            log.info("Traversing from {} to {} via connection {} - {}", flowObjectId, connectedNodeId, i + 1, connectionName);

            FlowObject target = buildFlowObject(xmlDocument, xPath, connectedNodeId, methods, alreadyBuiltFlowObjects);
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

        return Optional.ofNullable(methods.get(flowObjectId));
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
