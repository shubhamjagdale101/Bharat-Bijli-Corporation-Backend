package com.shubham.lightbill.lightbill_backend.controller;

import com.shubham.lightbill.lightbill_backend.dto.BillDto;
import com.shubham.lightbill.lightbill_backend.dto.FilterDto;
import com.shubham.lightbill.lightbill_backend.dto.SignUpDto;
import com.shubham.lightbill.lightbill_backend.model.Bill;
import com.shubham.lightbill.lightbill_backend.model.Transaction;
import com.shubham.lightbill.lightbill_backend.model.User;
import com.shubham.lightbill.lightbill_backend.repository.TransactionRepository;
import com.shubham.lightbill.lightbill_backend.response.ApiResponse;
import com.shubham.lightbill.lightbill_backend.service.BillService;
import com.shubham.lightbill.lightbill_backend.service.EmployeeService;
import com.shubham.lightbill.lightbill_backend.service.TransactionService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/employee")
@CrossOrigin(origins = "http://localhost:4200", allowCredentials = "true")
public class EmployeeController {
    @Autowired
    private EmployeeService employeeService;
    @Autowired
    private TransactionService transactionService;
    @Autowired
    private BillService billService;

    @PostMapping("/addBill")
    public ApiResponse<Bill> addBill(@RequestBody BillDto req) throws Exception {
        Bill bill = employeeService.addBill(req);
        return ApiResponse.success(bill, "Bill get Added Successfully", 200);
    }

    @GetMapping("/getTransactions")
    public ApiResponse<List<Transaction>> getTransactions(
            @RequestParam(name = "page",defaultValue = "0") int pageNumber,
            @RequestParam(name = "size", defaultValue = "10") int pageSize
    ){
        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        List<Transaction> res = transactionService.getTransactionUsingPagination(pageable);
        return ApiResponse.success(res, "successfully fetched transactions.", HttpStatus.OK.value());
    }

    @GetMapping("/getTransactionsByFilter")
    public ApiResponse<Object> getTransactionsByFilter(
            @RequestParam(name = "filterBy") String filterBy,
            @RequestParam(name = "filterValue") String filterValue,
            @RequestParam(name = "page",defaultValue = "0") int pageNumber,
            @RequestParam(name = "size", defaultValue = "10") int pageSize
    ) throws Exception {
        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        Page<Transaction> result = employeeService.getTransactionByFilterWithPagination(filterBy, filterValue, pageable);
        return ApiResponse.success(result.getContent(), ((Integer) result.getTotalPages()).toString(), HttpStatus.OK.value());
    }

    @GetMapping("/getBills")
    public ApiResponse<List<Bill>> getBills(
            @RequestParam(name = "page",defaultValue = "0") int pageNumber,
            @RequestParam(name = "size", defaultValue = "10") int pageSize
    ){
        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        Page<Bill> result = billService.getBillsUsingPagination(pageable);
        return ApiResponse.success(result.getContent(), ((Integer) result.getTotalPages()).toString(), HttpStatus.OK.value());
    }

    @GetMapping("getBillById/{billId}")
    public ApiResponse<Bill> getBillById(@PathVariable("billId") String billId) throws Exception {
        Bill bill = billService.getBillById(billId);
        return ApiResponse.success(bill, "", 200);
    }

    @GetMapping("/getCustomers")
    public ApiResponse<List<User>> getCustomers(
            @RequestParam(name = "page",defaultValue = "0") int pageNumber,
            @RequestParam(name = "size", defaultValue = "10") int pageSize
    ){
        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        Page<User> result = employeeService.getUserWithCustomerRoleWithPagination(pageable);
        return ApiResponse.success(result.getContent(), ((Integer) result.getTotalPages()).toString(), HttpStatus.OK.value());
    }

    @GetMapping("/getCustomersByFilter")
    public ApiResponse<List<User>> getCustomersByFilter(
            @RequestParam(name = "isActiveStatus", defaultValue = "true") Boolean isActiveStatus,
            @RequestParam(name = "page",defaultValue = "0") int pageNumber,
            @RequestParam(name = "size", defaultValue = "10") int pageSize
    ) throws Exception {
        List<Object> res = new ArrayList<>();
        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        Page<User> result = employeeService.getUserWithCustomerRoleByStatus(isActiveStatus, pageable);
        return ApiResponse.success(result.getContent(), ((Integer) result.getTotalPages()).toString(), HttpStatus.OK.value());
    }

    @GetMapping("/getEmployeeProfile")
    public ApiResponse<User> getEmployeeProfile(){
        String userId = SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString();
        User employeeProfile = employeeService.getEmployeeProfile(userId);
        return ApiResponse.success(employeeProfile, "", 100);
    }

    @GetMapping("/update")
    public ApiResponse<User> updateProfile(@RequestBody Map<String, Object> req){
        User emp = employeeService.updateEmployee(req);
        return ApiResponse.success(emp, "", HttpStatus.ACCEPTED.value());
    }
}
