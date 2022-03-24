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
import com.naturalprogrammer.cleanflow.services.StatefulOrderCreator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@ExtendWith(MockitoExtension.class)
class OrderCreationStatefulPatternTest
{
    @Spy
    private Logger logger;

    @InjectMocks
    private StatefulOrderCreator statefulCreator;

    @BeforeAll
    static void setUp() {
        CleanFlowCache.clear("clean-flows/create-order.bpmn");
    }

    @Test
    void testOrder_NotShippableToAddress() {

        // given
        statefulCreator.setCurrentlyLoggedInCustomerId(12);

        try {

            // when
            OrderResource resource = statefulCreator.createOrder(new OrderCreationForm(12));
            fail();

        } catch (BusinessException ignored) {

            // then
            assertOrderNotShippableToAddress(logger);
        }
    }

    static void assertOrderNotShippableToAddress(Logger logger) {

        verify(logger).info("Creating order OrderCreationForm(productId=12)");
        verify(logger).info("Validating OrderCreationForm(productId=12)");
        verify(logger).info("Logged in customer: Customer(id=12)");
        verify(logger).info("Chosen product: Product(id=12)");
        verify(logger).info("Shippable: NO");
        verify(logger).info("Error: product 12 is not shippable to your address");

        verifyNoMoreInteractions(logger);
    }

    @Test
    void testOrder_CustomerHasNoActiveCreditCard() {

        // given
        statefulCreator.setCurrentlyLoggedInCustomerId(7);

        try {

            // when
            OrderResource resource = statefulCreator.createOrder(new OrderCreationForm(8));
            fail();

        } catch (BusinessException ignored) {

            // then
            assertCustomerHasNoActiveCreditCard(logger);
        }
    }

    static void assertCustomerHasNoActiveCreditCard(Logger logger) {
        verify(logger).info("Creating order OrderCreationForm(productId=8)");
        verify(logger).info("Validating OrderCreationForm(productId=8)");
        verify(logger).info("Logged in customer: Customer(id=7)");
        verify(logger).info("Chosen product: Product(id=8)");
        verify(logger).info("Shippable: YES");
        verify(logger).info("Customer has active credit card: false");
        verify(logger).info("Error: Please add an active card");

        verifyNoMoreInteractions(logger);
    }

    @Test
    void testOrder_ProcessPaymentSucceeds() {

        // given
        statefulCreator.setCurrentlyLoggedInCustomerId(2);

        // when
        OrderResource resource = statefulCreator.createOrder(new OrderCreationForm(1));

        // then
        assertProcessPaymentSucceeds(logger, resource);
    }

    static void assertProcessPaymentSucceeds(Logger logger, OrderResource resource) {

        verify(logger).info("Creating order OrderCreationForm(productId=1)");
        verify(logger).info("Validating OrderCreationForm(productId=1)");
        verify(logger).info("Logged in customer: Customer(id=2)");
        verify(logger).info("Chosen product: Product(id=1)");
        verify(logger).info("Shippable: YES");
        verify(logger).info("Customer has active credit card: true");
        verify(logger).info("Payment succeeded: true");
        verify(logger).info("Payment succeeded: YES");
        verify(logger).info("OrderResource(customerId=2, productId=1)");
        verify(logger).info("Created order OrderResource(customerId=2, productId=1)");

        verifyNoMoreInteractions(logger);
        assertEquals(2, resource.getCustomerId());
        assertEquals(1, resource.getProductId());
    }

    @Test
    void testOrder_ProcessPaymentFailedButCustomerEligibleForCoD() {

        // given
        statefulCreator.setCurrentlyLoggedInCustomerId(1);

        // when
        OrderResource resource = statefulCreator.createOrder(new OrderCreationForm(4));

        // then
        assertProcessPaymentFailedButCustomerEligibleForCoD(logger, resource);
    }

    static void assertProcessPaymentFailedButCustomerEligibleForCoD(Logger logger, OrderResource resource) {

        verify(logger).info("Creating order OrderCreationForm(productId=4)");
        verify(logger).info("Validating OrderCreationForm(productId=4)");
        verify(logger).info("Logged in customer: Customer(id=1)");
        verify(logger).info("Chosen product: Product(id=4)");
        verify(logger).info("Shippable: YES");
        verify(logger).info("Customer has active credit card: true");
        verify(logger).info("Payment succeeded: false");
        verify(logger).info("Payment succeeded: NO");
        verify(logger).info("Eligible for CoD: YES");
        verify(logger).info("OrderResource(customerId=1, productId=4)");
        verify(logger).info("Created order OrderResource(customerId=1, productId=4)");

        verifyNoMoreInteractions(logger);
        assertEquals(1, resource.getCustomerId());
        assertEquals(4, resource.getProductId());
    }

    @Test
    void testOrder_CustomerNotEligibleForCoD() {

        // given
        statefulCreator.setCurrentlyLoggedInCustomerId(2);

        try {

            // when
            OrderResource resource = statefulCreator.createOrder(new OrderCreationForm(4));
            fail();

        } catch (BusinessException ignored) {

            // then
            assertCustomerNotEligibleForCoD(logger);
        }
    }

    static void assertCustomerNotEligibleForCoD(Logger logger) {

        verify(logger).info("Creating order OrderCreationForm(productId=4)");
        verify(logger).info("Validating OrderCreationForm(productId=4)");
        verify(logger).info("Logged in customer: Customer(id=2)");
        verify(logger).info("Chosen product: Product(id=4)");
        verify(logger).info("Shippable: YES");
        verify(logger).info("Customer has active credit card: true");
        verify(logger).info("Payment succeeded: false");
        verify(logger).info("Payment succeeded: NO");
        verify(logger).info("Eligible for CoD: NO");
        verify(logger).info("Error: Payment processing failed");

        verifyNoMoreInteractions(logger);
    }
}
