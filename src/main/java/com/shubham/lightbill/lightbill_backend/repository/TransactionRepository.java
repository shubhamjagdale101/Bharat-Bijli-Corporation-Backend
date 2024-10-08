package com.shubham.lightbill.lightbill_backend.repository;

import com.shubham.lightbill.lightbill_backend.constants.PaymentMethod;
import com.shubham.lightbill.lightbill_backend.constants.TransactionStatus;
import com.shubham.lightbill.lightbill_backend.model.Bill;
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

    Page<Transaction> findByTransactionStatusAndUser(TransactionStatus status, User user, Pageable sortedPageable);

    Page<Transaction> findByPaymentMethodAndUser(PaymentMethod method, User user, Pageable sortedPageable);

    Transaction findByBill(Bill bill);

    Transaction findByTxnIdAndUser(String filterValue, User user);

    Transaction findByTxnIdAndUserAndTransactionStatus(String filterValue, User user, TransactionStatus transactionStatus);

    Transaction findByBillAndTransactionStatus(Bill bill, TransactionStatus transactionStatus);
}
