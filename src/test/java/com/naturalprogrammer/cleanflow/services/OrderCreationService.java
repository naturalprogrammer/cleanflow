/*
 * Copyright 2020-2021 the original author or authors.
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

package com.naturalprogrammer.cleanflow.services;

import com.naturalprogrammer.cleanflow.CleanFlow;
import com.naturalprogrammer.cleanflow.Returns;
import com.naturalprogrammer.cleanflow.domain.Customer;
import com.naturalprogrammer.cleanflow.domain.OrderCreationForm;
import com.naturalprogrammer.cleanflow.domain.OrderResource;
import com.naturalprogrammer.cleanflow.domain.Product;
import lombok.Setter;

import java.util.Arrays;
import java.util.List;

import static com.naturalprogrammer.cleanflow.services.Utils.mapOf;

@Setter
public class OrderCreationService {

    private Logger log;
    private Integer currentlyLoggedInCustomerId;

    public OrderResource createOrder(OrderCreationForm orderCreationForm) {

        log.info("Creating order " + orderCreationForm);
        OrderResource resource = (OrderResource) CleanFlow
                .execute("clean-flows/create-order.bpmn", this,
                        mapOf("orderCreationForm", orderCreationForm))
                .get("orderResource");

        log.info("Created order " + resource);
        return resource;
    }

    @Returns({"customer", "product"})
    private List<?> validateForm(OrderCreationForm orderCreationForm) {

        log.info("Validating " + orderCreationForm);

        if (orderCreationForm.getProductId() == null) {
            log.info("No product chosen");
            throw new IllegalArgumentException("No product chosen");
        }

        Customer customer = getCurrentlyLoggedInCustomer();
        Product product = getProductById(orderCreationForm.getProductId());

        return Arrays.asList(customer, product);
    }

    private String productShippableToCustomerAddress(Customer customer, Product product) {
        String shippable = customer.getId() < 10 && product.getId() < 10 ? "YES" : "NO";
        log.info("Shippable: " + shippable);
        return shippable;
    }

    private String customerHasAnActiveCreditCard(Customer customer) {
        String hasCreditCard = customer.getId() < 5 ? "YES" : "NO";
        log.info("Customer has active credit card: " + hasCreditCard);
        return hasCreditCard;
    }

    private void rejectProductNotShippable(Product product) {
        reject("product " + product.getId() + " is not shippable to your address");
    }

    private void rejectNoActiveCard(Product product) {
        reject("Please add an active card");
    }

    @Returns("paymentSucceeded")
    private boolean processPayment(Customer customer, Product product) {
        boolean succeeded = product.getId() < 3 && customer.getId() < 3;
        log.info("Payment succeeded: " + succeeded);
        return succeeded;
    }

    private String isPaymentSucceeded(boolean paymentSucceeded) {
        String succeeded = paymentSucceeded ? "YES" : "NO";
        log.info("Payment succeeded: " + succeeded);
        return succeeded;
    }

    private String isCustomerEligibleForCoD(Customer customer) {
        String eligible = customer.getId() < 2 ? "YES" : "NO";
        log.info("Eligible for CoD: " + eligible);
        return eligible;
    }

    @Returns("orderResource")
    private OrderResource createOrderSuccessfully(Customer customer, Product product) {
        OrderResource resource = new OrderResource(customer.getId(), product.getId());
        log.info(resource.toString());
        return resource;
    }

    private void rejectPaymentProcessingFailed() {
        reject("Payment processing failed");
    }

    private Product getProductById(Integer productId) {

        Product product = new Product(productId);
        log.info("Chosen product: " + product);

        return new Product(productId);
    }

    private Customer getCurrentlyLoggedInCustomer() {

        Customer customer = new Customer(currentlyLoggedInCustomerId);
        log.info("Logged in customer: " + customer);

        return customer;
    }

    private void reject(String message) {
        log.info("Error: " + message);
        throw new BusinessException(message);
    }
}
