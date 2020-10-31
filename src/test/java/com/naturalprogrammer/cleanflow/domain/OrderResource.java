package com.naturalprogrammer.cleanflow.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter @Setter @ToString
@AllArgsConstructor
public class OrderResource {

    private Integer customerId;
    private Integer productId;
}
