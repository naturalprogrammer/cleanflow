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

import com.naturalprogrammer.cleanflow.domain.OrderCreationForm;
import com.naturalprogrammer.cleanflow.domain.OrderResource;
import com.naturalprogrammer.cleanflow.services.BusinessException;
import com.naturalprogrammer.cleanflow.services.Logger;
import com.naturalprogrammer.cleanflow.services.OrderCreationService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import static com.naturalprogrammer.cleanflow.OrderCreationStatefulPatternTest.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@ExtendWith(MockitoExtension.class)
class OrderCreationStatelessPatternTest
{
    @Spy
    private Logger logger;

    @InjectMocks
    private OrderCreationService service;

    @BeforeAll
    static void setUp() {
        CleanFlowCache.clear();
    }

    @Test
    void testOrder_NotShippableToAddress() {

        // given
        service.setCurrentlyLoggedInCustomerId(12);

        try {

            // when
            OrderResource resource = service.createOrder(new OrderCreationForm(12));
            fail();

        } catch (BusinessException ignored) {

            // then
            assertOrderNotShippableToAddress(logger);
        }
    }

    @Test
    void testOrder_CustomerHasNoActiveCreditCard() {

        // given
        service.setCurrentlyLoggedInCustomerId(7);

        try {

            // when
            OrderResource resource = service.createOrder(new OrderCreationForm(8));
            fail();

        } catch (BusinessException ignored) {

            // then
            assertCustomerHasNoActiveCreditCard(logger);
        }
    }

    @Test
    void testOrder_ProcessPaymentSucceeds() {

        // given
        service.setCurrentlyLoggedInCustomerId(2);

        // when
        OrderResource resource = service.createOrder(new OrderCreationForm(1));

        // then
        assertProcessPaymentSucceeds(logger, resource);
    }

    @Test
    void testOrder_ProcessPaymentFailedButCustomerEligibleForCoD() {

        // given
        service.setCurrentlyLoggedInCustomerId(1);

        // when
        OrderResource resource = service.createOrder(new OrderCreationForm(4));

        // then
        assertProcessPaymentFailedButCustomerEligibleForCoD(logger, resource);
    }

    @Test
    void testOrder_CustomerNotEligibleForCoD() {

        // given
        service.setCurrentlyLoggedInCustomerId(2);

        try {

            // when
            OrderResource resource = service.createOrder(new OrderCreationForm(4));
            fail();

        } catch (BusinessException ignored) {

            // then
            assertCustomerNotEligibleForCoD(logger);
        }
    }
}
