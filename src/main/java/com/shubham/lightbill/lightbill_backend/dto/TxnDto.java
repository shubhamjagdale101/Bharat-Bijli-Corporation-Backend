package com.shubham.lightbill.lightbill_backend.dto;

import com.shubham.lightbill.lightbill_backend.constants.PaymentStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TxnDto {
    @NotBlank(message = "transactionId is requires")
    private String transactionId; // Unique identifier for the transaction
    private PaymentStatus status;
    @NotBlank(message = "paymentMethod should not be blank")
    private String paymentMethod;
}
