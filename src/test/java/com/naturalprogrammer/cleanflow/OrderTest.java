package com.naturalprogrammer.cleanflow;

import com.naturalprogrammer.cleanflow.domain.OrderForm;
import com.naturalprogrammer.cleanflow.services.Logger;
import com.naturalprogrammer.cleanflow.services.OrderCreationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OrderTest
{
    @Spy
    private Logger logger;

    @InjectMocks
    private OrderCreationService service;

    @Test
    void testOrder() {

        service.setCurrentlyLoggedInCustomerId(12);
        service.createOrder(new OrderForm(12));
    }
}
