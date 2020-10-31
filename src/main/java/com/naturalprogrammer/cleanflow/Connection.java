package com.naturalprogrammer.cleanflow;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

/**
 * A connection to a next FlowObject
 */
@Getter @ToString
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
