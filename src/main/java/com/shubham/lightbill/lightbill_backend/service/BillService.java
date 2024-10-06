package com.shubham.lightbill.lightbill_backend.service;

import com.shubham.lightbill.lightbill_backend.constants.PaymentStatus;
import com.shubham.lightbill.lightbill_backend.constants.Role;
import com.shubham.lightbill.lightbill_backend.dto.BillDto;
import com.shubham.lightbill.lightbill_backend.model.Bill;
import com.shubham.lightbill.lightbill_backend.model.User;
import com.shubham.lightbill.lightbill_backend.repository.BillRepository;
import com.shubham.lightbill.lightbill_backend.repository.UserRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BillService {
    @Autowired
    private BillRepository billRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private IdGeneratorService idGeneratorService;

    private int getDiscountAmount(int amount){
        return (int) Math.min(amount * 0.05, 20);
    }
    private int getAmountForConsumption(int unitConsumptionOfElectricity) {
        return unitConsumptionOfElectricity * 10;
    }

    public Bill addBill(@Valid BillDto req) throws Exception {
        User user = userRepository.findByUserId(req.getUserId());
        if(user.getRole() == Role.EMPLOYEE) throw new Exception("Bill should be created for Customer only");

        Bill currBill = billRepository.findByUserAndMonthOfTheBill(user, req.getMonthOfTheBill());
        if(currBill != null) return currBill;

        int amount = getAmountForConsumption(req.getUnitConsumption());
        Bill bill = Bill.builder()
                .meterNumber(user.getMeterNumber())
                .billId(idGeneratorService.generateId(Bill.class.getName()))
                .amount(amount)
                .discount(getDiscountAmount(amount))
                .monthOfTheBill(req.getMonthOfTheBill())
                .dueDate(req.getDueDate())
                .unitConsumption(req.getUnitConsumption())
                .user(user)
                .paymentStatus(PaymentStatus.UNPAID)
                .build();
        return billRepository.save(bill);
    }

    public Page<Bill> getBillsUsingPagination(Pageable pageable){
        Page<Bill> page = billRepository.findAll(pageable);
        return page;
    }

    public List<Bill> getBillsWithUserIdUsingPagination(String userId, Pageable pageable){
        User user = userRepository.findByUserId(userId);
        Page<Bill> page = billRepository.findByUser(user, pageable);
        return page.getContent();
    }

    public List<Bill> getBillsOfPastSixMonths(User user){
        Pageable pageable = PageRequest.of(0, 6, Sort.by("monthOfTheBill").descending());
        Page<Bill> page = billRepository.findByUser(user, pageable);
        return page.getContent();
    }
}
