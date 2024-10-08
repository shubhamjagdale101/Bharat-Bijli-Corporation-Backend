package com.shubham.lightbill.lightbill_backend.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.shubham.lightbill.lightbill_backend.constants.PaymentStatus;
import jakarta.persistence.*;
import lombok.*;

import java.util.Date;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Bill {
    @Id
    @Column(nullable = false, unique = true)
    private String billId;

    @ManyToOne()
    @JoinColumn(name = "userId", referencedColumnName = "userId")
    @JsonIgnoreProperties(value = {"wallet", "bills"})
    private User user;

    @Column(nullable = false)
    private String monthOfTheBill;

    @Column(nullable = false)
    private Double unitConsumption;

    @Column(nullable = false)
    private Date dueDate;

    @Column(nullable = false)
    private Double discount;

    @Column(nullable = false)
    private Double amount;

    @Column(nullable = false)
    private String meterNumber;

    @Enumerated(value = EnumType.STRING)
    private PaymentStatus paymentStatus;
}
