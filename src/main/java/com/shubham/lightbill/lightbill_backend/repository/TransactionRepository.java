package com.shubham.lightbill.lightbill_backend.repository;

import com.shubham.lightbill.lightbill_backend.constants.PaymentMethod;
import com.shubham.lightbill.lightbill_backend.constants.TransactionStatus;
import com.shubham.lightbill.lightbill_backend.model.Transaction;
import com.shubham.lightbill.lightbill_backend.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, String> {
    Page<Transaction> findByTransactionStatus(TransactionStatus transactionStatus, Pageable pageable);
    Page<Transaction> findByPaymentMethod(PaymentMethod paymentMethod, Pageable pageable);
    Page<Transaction> findByUser(User user, Pageable pageable);

    Transaction findByTxnId(String transactionId);
}
