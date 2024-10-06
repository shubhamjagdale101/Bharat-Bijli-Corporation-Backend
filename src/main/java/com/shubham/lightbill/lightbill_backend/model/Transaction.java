package com.shubham.lightbill.lightbill_backend.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.shubham.lightbill.lightbill_backend.constants.PaymentMethod;
import com.shubham.lightbill.lightbill_backend.constants.TransactionStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.util.Date;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Transaction {
    @Id
    private String txnId;

    @ManyToOne()
    @JoinColumn(name = "billId", referencedColumnName = "billId", nullable = false)
    @JsonIgnoreProperties(value = {"txnList"})
    private Bill bill;

    @ManyToOne()
    @JoinColumn(name = "userId", referencedColumnName = "userId")
    private User user;

    @Enumerated(value = EnumType.STRING)
    @Column()
    private PaymentMethod paymentMethod;

    @Enumerated(value = EnumType.STRING)
    @Column(nullable = false)
    private TransactionStatus transactionStatus;

    @UpdateTimestamp
    private Date updatedAt;
}
