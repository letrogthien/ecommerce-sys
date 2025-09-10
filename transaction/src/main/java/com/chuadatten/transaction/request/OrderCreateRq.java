
package com.chuadatten.transaction.request;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;


import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;


@Getter
public class OrderCreateRq {

 

    @NotNull(message = "Seller ID is required")
    private UUID sellerId;

    @NotNull(message = "Total amount is required")
    @DecimalMin(value = "0.01", message = "Total amount must be greater than 0")
    private BigDecimal totalAmount;

    @NotBlank(message = "Currency is required")
    @Size(min = 3, max = 3, message = "Currency must be 3 characters")
    private String currency;


    @NotEmpty(message = "Items cannot be empty")
    @Valid
    private List<OrderItemCreateRq> items;
}
