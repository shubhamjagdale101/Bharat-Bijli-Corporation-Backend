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
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import java.util.Date;
import java.util.List;

@Service
public class BillService {
    @Autowired
    private BillRepository billRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private IdGeneratorService idGeneratorService;

    private Double getAmountForConsumption(Double unitConsumptionOfElectricity) {
        return unitConsumptionOfElectricity * 10;
    }

    private boolean checkForValidDueDate(String month, Date dueDate) {
        YearMonth givenMonth = YearMonth.parse(month, DateTimeFormatter.ofPattern("yyyy-MM"));
        LocalDate dueLocalDate = dueDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

        YearMonth nextMonth = givenMonth.plusMonths(1);
        YearMonth dueMonth = YearMonth.from(dueLocalDate);

        return dueMonth.equals(nextMonth);
    }

    public Bill addBill(@Valid BillDto req) throws Exception {
        User user = userRepository.findByUserId(req.getUserId());
        if(user.getRole() == Role.EMPLOYEE) throw new Exception("Bill should be created for Customer only");

        Bill currBill = billRepository.findByUserAndMonthOfTheBill(user, req.getMonthOfTheBill());
        if(currBill != null) throw new Exception("Bill already Exist with given user and month");

        if(!checkForValidDueDate(req.getMonthOfTheBill(), req.getDueDate())) throw new Exception("Due date should in the month of the bill");
        if(req.getUnitConsumption() <= 0) throw new Exception("Unit Consumption Should be positive number");

        Double amount = getAmountForConsumption(req.getUnitConsumption());
        Bill bill = Bill.builder()
                .meterNumber(user.getMeterNumber())
                .billId(idGeneratorService.generateId(Bill.class.getName()))
                .amount(amount)
                .discount(amount * 0.05)
                .monthOfTheBill(req.getMonthOfTheBill())
                .dueDate(req.getDueDate())
                .unitConsumption(req.getUnitConsumption())
                .user(user)
                .paymentStatus(PaymentStatus.UNPAID)
                .build();
        return billRepository.save(bill);
    }

    public Page<Bill> getBillsUsingPagination(Pageable pageable){
        Pageable sortedPageable = PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                pageable.getSort().and(Sort.by(Sort.Direction.DESC, "monthOfTheBill"))  // Combine existing sorting with new sorting
        );
        Page<Bill> page = billRepository.findAll(sortedPageable);
        return page;
    }

    public Page<Bill> getBillUsingPaginationForSpecificUser(String userId, Pageable pageable){
        Pageable sortedPageable = PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                pageable.getSort().and(Sort.by(Sort.Direction.DESC, "monthOfTheBill"))  // Combine existing sorting with new sorting
        );
        User user = userRepository.findByUserId(userId);
        Page<Bill> page = billRepository.findByUser(user, sortedPageable);
        return page;
    }

    public List<Bill> getBillsWithUserIdUsingPagination(String userId, Pageable pageable){
        Pageable sortedPageable = PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                pageable.getSort().and(Sort.by(Sort.Direction.DESC, "monthOfTheBill"))  // Combine existing sorting with new sorting
        );
        User user = userRepository.findByUserId(userId);
        Page<Bill> page = billRepository.findByUser(user, sortedPageable);
        return page.getContent();
    }

    public List<Bill> getBillsOfPastSixMonths(User user){
        Pageable pageable = PageRequest.of(0, 6, Sort.by("monthOfTheBill").descending());
        Page<Bill> page = billRepository.findByUser(user, pageable);
        return page.getContent();
    }

    public Bill getBillById(String billId) throws Exception {
        Bill bill = billRepository.findByBillId(billId);
        if(bill == null) throw new Exception("Bill with Bill number not exists");
        return bill;
    }

    public Page<Bill> getUnpaidBills(Pageable pageble) {
        return billRepository.findByPaymentStatus(PaymentStatus.UNPAID, pageble);
    }
}
