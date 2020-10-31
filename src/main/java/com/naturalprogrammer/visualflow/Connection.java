package com.naturalprogrammer.visualflow;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * A connection to a next FlowObject
 */
@Getter
@AllArgsConstructor
public class Connection {

    /**
     * Value of the connection (sequenceFlow)
     */
    private final String value;

    /**
     * Next FlowObject
     */
    private final FlowObject next;
}
