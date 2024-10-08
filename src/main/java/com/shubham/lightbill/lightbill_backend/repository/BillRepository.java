package com.shubham.lightbill.lightbill_backend.repository;

import com.shubham.lightbill.lightbill_backend.constants.PaymentStatus;
import com.shubham.lightbill.lightbill_backend.model.Bill;
import com.shubham.lightbill.lightbill_backend.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;

@Repository
public interface BillRepository extends JpaRepository<Bill, String> {
    Bill findByUserAndMonthOfTheBill(User user, String monthOfTheBill);
    Page<Bill> findByUser(User user, Pageable pageable);

    Bill findByBillId(String billId);

    Page<Bill> findByPaymentStatus(PaymentStatus paymentStatus, Pageable pageble);
}
