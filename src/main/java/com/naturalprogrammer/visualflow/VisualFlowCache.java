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
        return parsedFlows.computeIfAbsent(flow, path -> parseFlow(path, caller));
    }

    @SneakyThrows
    private VisualFlow parseFlow(String path, Class<?> caller) {

        try (InputStream input = getClass().getClassLoader().getResourceAsStream(path)) {

            Document xmlDocument = DocumentBuilderFactory
                    .newInstance()
                    .newDocumentBuilder()
                    .parse(input);

            XPath xPath = xPathFactory.newXPath();

            String startEventIdPath = "string(//startEvent/@id)";
            String startNodeId = (String) xPath.compile(startEventIdPath)
                    .evaluate(xmlDocument, XPathConstants.STRING);

            Map<String, Method> methods = Arrays.stream(caller.getDeclaredMethods())
                    .collect(toMap(Method::getName, identity()));

            FlowObject start = buildFlowObject(xmlDocument, xPath, startNodeId, methods);
            return new VisualFlow(start);
        }
    }

    private FlowObject buildFlowObject(Document xmlDocument,
                                       XPath xPath,
                                       String flowObjectId,
                                       Map<String, Method> methods)
            throws XPathExpressionException, NoSuchMethodException {

        FlowObject.FlowObjectBuilder builder = FlowObject.builder();

        FlowObjectType type = getFlowObjectType(xmlDocument, xPath, flowObjectId);
        builder.type(type);

        getMethod(flowObjectId, methods).ifPresent(method -> {
            method.setAccessible(true);
            builder.method(method)
                    .parameterNames(getParameterNames(method))
                    .returnValueNames(getReturnValueNames(method));
        });

        builder.connections(getConnections(xmlDocument, xPath, flowObjectId, methods));
        return builder.build();
    }

    private List<Connection> getConnections(Document xmlDocument,
                                            XPath xPath,
                                            String flowObjectId,
                                            Map<String, Method> methods)
            throws XPathExpressionException, NoSuchMethodException {

        String connectionValuePath = "//sequenceFlow[@sourceRef='" + flowObjectId + "']/@name";
        NodeList connectionValueNodes = (NodeList) xPath.compile(connectionValuePath).evaluate(xmlDocument, XPathConstants.NODESET);

        String connectedNodesPath = "//sequenceFlow[@sourceRef='" + flowObjectId + "']/@targetRef";
        NodeList connectedNodes = (NodeList) xPath.compile(connectedNodesPath).evaluate(xmlDocument, XPathConstants.NODESET);

        List<Connection> connections = new ArrayList<>(connectedNodes.getLength());
        for (int i = 0; i < connectedNodes.getLength(); i++) {

            String connectedId = connectedNodes.item(i).getNodeValue();
            log.debug(connectedId);

            FlowObject target = buildFlowObject(xmlDocument, xPath, connectedId, methods);
            String value = connectionValueNodes.item(i) == null ? null : connectionValueNodes.item(i).getNodeValue();
            log.debug("Value {}", value);
            connections.add(new Connection(value, target));
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
}
