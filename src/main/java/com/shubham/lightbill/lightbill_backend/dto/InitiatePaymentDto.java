package com.shubham.lightbill.lightbill_backend.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class InitiatePaymentDto {
    private String receiverAccount;
    private Integer totalAmount;
    private String callbackUrl;
}
