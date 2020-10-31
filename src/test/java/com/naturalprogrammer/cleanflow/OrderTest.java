package com.naturalprogrammer.cleanflow;

import com.naturalprogrammer.cleanflow.domain.OrderForm;
import com.naturalprogrammer.cleanflow.domain.OrderResource;
import com.naturalprogrammer.cleanflow.services.BusinessException;
import com.naturalprogrammer.cleanflow.services.Logger;
import com.naturalprogrammer.cleanflow.services.OrderCreationService;
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
class OrderTest
{
    @Spy
    private Logger logger;

    @InjectMocks
    private OrderCreationService service;

    @Test
    void testOrder_NotShippableToAddress() {

        // given
        service.setCurrentlyLoggedInCustomerId(12);

        try {

            // when
            OrderResource resource = service.createOrder(new OrderForm(12));
            fail();

        } catch (BusinessException ignored) {

            // then
            verify(logger).info("Creating order OrderForm(productId=12)");
            verify(logger).info("Validating OrderForm(productId=12)");
            verify(logger).info("Logged in customer: Customer(id=12)");
            verify(logger).info("Chosen product: Product(id=12)");
            verify(logger).info("Shippable: NO");
            verify(logger).info("Error: product 12 is not shippable to your address");

            verifyNoMoreInteractions(logger);
        }
    }

    @Test
    void testOrder_CustomerHasNoActiveCreditCard() {

        // given
        service.setCurrentlyLoggedInCustomerId(7);

        try {

            // when
            OrderResource resource = service.createOrder(new OrderForm(8));
            fail();

        } catch (BusinessException ignored) {

            // then
            verify(logger).info("Creating order OrderForm(productId=8)");
            verify(logger).info("Validating OrderForm(productId=8)");
            verify(logger).info("Logged in customer: Customer(id=7)");
            verify(logger).info("Chosen product: Product(id=8)");
            verify(logger).info("Shippable: YES");
            verify(logger).info("Customer has active credit card: NO");
            verify(logger).info("Error: Please add an active card");

            verifyNoMoreInteractions(logger);
        }
    }

    @Test
    void testOrder_ProcessPaymentSucceeds() {

        // given
        service.setCurrentlyLoggedInCustomerId(2);

        // when
        OrderResource resource = service.createOrder(new OrderForm(1));

        // then
        verify(logger).info("Creating order OrderForm(productId=1)");
        verify(logger).info("Validating OrderForm(productId=1)");
        verify(logger).info("Logged in customer: Customer(id=2)");
        verify(logger).info("Chosen product: Product(id=1)");
        verify(logger).info("Shippable: YES");
        verify(logger).info("Customer has active credit card: YES");
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
        service.setCurrentlyLoggedInCustomerId(1);

        // when
        OrderResource resource = service.createOrder(new OrderForm(4));

        // then
        verify(logger).info("Creating order OrderForm(productId=4)");
        verify(logger).info("Validating OrderForm(productId=4)");
        verify(logger).info("Logged in customer: Customer(id=1)");
        verify(logger).info("Chosen product: Product(id=4)");
        verify(logger).info("Shippable: YES");
        verify(logger).info("Customer has active credit card: YES");
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
        service.setCurrentlyLoggedInCustomerId(2);

        try {

            // when
            OrderResource resource = service.createOrder(new OrderForm(4));
            fail();

        } catch (BusinessException ignored) {

            // then
            verify(logger).info("Creating order OrderForm(productId=4)");
            verify(logger).info("Validating OrderForm(productId=4)");
            verify(logger).info("Logged in customer: Customer(id=2)");
            verify(logger).info("Chosen product: Product(id=4)");
            verify(logger).info("Shippable: YES");
            verify(logger).info("Customer has active credit card: YES");
            verify(logger).info("Payment succeeded: false");
            verify(logger).info("Payment succeeded: NO");
            verify(logger).info("Eligible for CoD: NO");
            verify(logger).info("Error: Payment processing failed");

            verifyNoMoreInteractions(logger);
        }
    }
}
