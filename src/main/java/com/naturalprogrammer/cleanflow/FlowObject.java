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

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static java.lang.String.format;

/**
 * Represents a node in the diagram
 */
@Getter
@Builder
@Slf4j
public class FlowObject {

    private final String id;
    private final FlowObjectType type;
    private final Method method;
    private final List<String> parameterNames;
    private final List<String> returnValueNames;

    @Setter(AccessLevel.PACKAGE)
    private List<Connection> connections;

    @Override
    public String toString() {
        return type
                + "(id=" + id
                + ", method=" + (method == null ? "null" : method.getName())
                + ", parameterNames=" + parameterNames
                + ", returnValueNames=" + returnValueNames
                + ", next-count=" + (connections == null ? 0 : connections.size()) + ")";
    }

    public void validate() {
        ensureMapped();
        validateConnectionCount();
        if (type.equals(FlowObjectType.EXCLUSIVE_GATEWAY))
            validateDuplicateConnectionsFromExclusiveGateway();
    }

    private void ensureMapped() {
        if (type.mustBeMapped() && method == null) {
            String error = format("Parsing Failed: Method not found for flowObjectType %s having id '%s'", type, id);
            log.error(error);
            throw new UnsupportedOperationException(error);
        }
    }

    private void validateDuplicateConnectionsFromExclusiveGateway() {
        Set<String> connectionNameSet = connections.stream().map(Connection::getValue).collect(Collectors.toSet());
        if (connectionNameSet.size() < connections.size()) {
            String error = format("Parsing Failed: %d pair of duplicate connections after EXCLUSIVE_GATEWAY '%s': %s",
                    connections.size() - connectionNameSet.size(), id, connections);
            log.error(error);
            throw new IndexOutOfBoundsException(error);
        }
    }

    private void validateConnectionCount() {
        if (!type.connectionCountIsValid(connections.size())) {
            String error = format("Parsing Failed: %d paths to follow after %s '%s'", connections.size(), type, id);
            log.error(error);
            throw new IndexOutOfBoundsException(error);
        }
    }

    public void ensureConnectionsFollowed(int followCount) {
        if (!type.followCountIsValid(followCount)) {
            String error = format("Running Failed: %d paths to follow after %s '%s'", followCount, type, id);
            log.error(error);
            throw new IndexOutOfBoundsException(error);
        }
    }
}
