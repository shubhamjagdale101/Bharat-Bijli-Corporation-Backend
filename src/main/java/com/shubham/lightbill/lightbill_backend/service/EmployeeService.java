package com.shubham.lightbill.lightbill_backend.service;

import com.shubham.lightbill.lightbill_backend.constants.Role;
import com.shubham.lightbill.lightbill_backend.dto.BillDto;
import com.shubham.lightbill.lightbill_backend.model.Bill;
import com.shubham.lightbill.lightbill_backend.model.Transaction;
import com.shubham.lightbill.lightbill_backend.model.User;
import com.shubham.lightbill.lightbill_backend.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import java.util.Map;

@Slf4j
@Service
public class EmployeeService {
    @Autowired
    private IdGeneratorService idGeneratorService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private BillService billService;
    @Autowired
    private TransactionService transactionService;


    public Bill addBill(BillDto req) throws Exception {
        return billService.addBill(req);
    }

    public Page<User> getUserWithCustomerRoleWithPagination(Pageable pageable){
        Page<User> page = userRepository.findByRole(Role.CUSTOMER, pageable);
        return page;
    }

    public User getEmployeeProfile(String userId){
        return userRepository.findByUserId(userId);
    }

    public Page<User> getCustomerWithActiveStatus(Pageable pageable){
        return userRepository.findByRoleAndIsActive(Role.CUSTOMER,true, pageable);
    }

    public Page<User> getCustomerWithInactiveStatus(Pageable pageable){
        return userRepository.findByRoleAndIsActive(Role.CUSTOMER,false, pageable);
    }

    public Page<User> getUserWithCustomerRoleByStatus(Boolean status, Pageable pageble) throws Exception {
        Page<User> page = null;
        if(status) page = getCustomerWithActiveStatus(pageble);
        else page = getCustomerWithInactiveStatus(pageble);
        return page;
    }

    public Page<Transaction> getTransactionByFilterWithPagination(String filterBy, String filterValue, Pageable pageable) throws Exception {
        return transactionService.getTransactionByFilterWithPagination(filterBy, filterValue, pageable);
    }

    public User updateEmployee(Map<String, Object> req) {
        String userId = SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString();
        User user = userRepository.findByUserId(userId);

        if(req.containsKey("email")) {
            log.info("updating email");
            user.setEmail(req.get("email").toString());
        }
        if(req.containsKey("address")) {
            log.info("updating address");
            user.setAddress(req.get("address").toString());
        }
        if(req.containsKey("phNo")) {
            log.info("updating phNo");
            user.setPhNo(req.get("phNo").toString());
        }
        user = userRepository.save(user);
        return user;
    }

    public Page<Bill> getUnpaidBills(Pageable pageble) {
        Page<Bill> bills = billService.getUnpaidBills(pageble);
        return bills;
    }
}
