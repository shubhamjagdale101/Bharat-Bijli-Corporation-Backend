package com.shubham.lightbill.lightbill_backend.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public class PaymentDto {
    @NotBlank
    private String paymentMethod;
    @Min(0)
    private Integer amount;
    private String walletId;
    private String debitCardNumber;
    private String creditCardNumber;
    private String cvv;
    private String expirationDate;
}
