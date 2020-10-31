package com.naturalprogrammer.visualflow.services;

import com.naturalprogrammer.visualflow.Returns;
import com.naturalprogrammer.visualflow.VisualFlow;
import com.naturalprogrammer.visualflow.domain.Customer;
import com.naturalprogrammer.visualflow.domain.OrderForm;
import com.naturalprogrammer.visualflow.domain.OrderResource;
import com.naturalprogrammer.visualflow.domain.Product;
import lombok.Setter;

import java.util.Arrays;
import java.util.List;

import static com.naturalprogrammer.visualflow.services.Utils.mapOf;

@Setter
public class OrderCreationService {

    private Logger log;
    private Integer currentlyLoggedInCustomerId;

    public OrderResource createOrder(OrderForm orderForm) {

        log.info("Creating order " + orderForm);
        OrderResource resource = (OrderResource) VisualFlow
                .execute("visual-flows/create-order.bpmn", this,
                        mapOf("orderForm", orderForm))
                .get("orderResource");

        log.info("Created order " + resource);
        return resource;
    }

    @Returns({"customer", "product"})
    private List<?> validateForm(OrderForm orderForm) {

        log.info("Validating " + orderForm);

        if (orderForm.getProductId() == null) {
            log.info("No product chosen");
            throw new IllegalArgumentException("No product chosen");
        }

        Customer customer = getCurrentlyLoggedInCustomer();
        Product product = getProductById(orderForm.getProductId());

        return Arrays.asList(customer, product);
    }

    private void rejectProductNotShippable(Product product) {
        reject("product " + product.getId() + " is not shippable to your address");
    }

    private void rejectNoActiveCard(Product product) {
        reject("Please add an active card");
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
        throw new RuntimeException(message);
    }
}
