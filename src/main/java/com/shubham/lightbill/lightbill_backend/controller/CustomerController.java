package com.shubham.lightbill.lightbill_backend.controller;

import com.shubham.lightbill.lightbill_backend.dto.TxnDto;
import com.shubham.lightbill.lightbill_backend.model.Bill;
import com.shubham.lightbill.lightbill_backend.model.Transaction;
import com.shubham.lightbill.lightbill_backend.model.User;
import com.shubham.lightbill.lightbill_backend.response.ApiResponse;
import com.shubham.lightbill.lightbill_backend.service.BillService;
import com.shubham.lightbill.lightbill_backend.service.CustomerService;
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
import org.springframework.web.servlet.view.RedirectView;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/customer")
@CrossOrigin(origins = {"http://localhost:4200", "http://localhost:8081"}, allowCredentials = "true")
public class CustomerController {
    @Autowired
    private CustomerService customerService;
    @Autowired
    private BillService billService;
    @Autowired
    private TransactionService transactionService;

    @GetMapping("/getCustomerProfile")
    public ApiResponse<User> getEmployeeProfile(){
        String userId = SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString();
        User customerProfile = customerService.getCustomerProfile(userId);
        return ApiResponse.success(customerProfile, "", 100);
    }

    @GetMapping("/getBills")
    public ApiResponse<List<Bill>> getBills(
            @RequestParam(name = "page",defaultValue = "0") int pageNumber,
            @RequestParam(name = "size", defaultValue = "10") int pageSize
    ){
        String userId = SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString();
        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        Page<Bill> result = billService.getBillsUsingPagination(pageable);
        return ApiResponse.success(result.getContent(), ((Integer) result.getTotalPages()).toString(), HttpStatus.OK.value());
    }

    @GetMapping("/getTransactions")
    public ApiResponse<List<Transaction>> getTransactions(
            @RequestParam(name = "page",defaultValue = "0") int pageNumber,
            @RequestParam(name = "size", defaultValue = "10") int pageSize
    ){
        String userId = SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString();
        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        Page<Transaction> res = transactionService.getTransactionUsingPaginationForUserId(userId, pageable);
        return ApiResponse.success(res.getContent(), ((Integer) res.getTotalPages()).toString(), HttpStatus.OK.value());
    }

    @GetMapping("/getPaidTransactions")
    public ApiResponse<List<Transaction>> getPaidTransactions(
            @RequestParam(name = "page",defaultValue = "0") int pageNumber,
            @RequestParam(name = "size", defaultValue = "10") int pageSize
    ) throws Exception {
        String userId = SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString();
        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        Page<Transaction> res = transactionService.getTransactionByFilterWithPagination("TRANSACTION_STATUS", "PAID", pageable);
        return ApiResponse.success(res.getContent(), ((Integer) res.getTotalPages()).toString(), HttpStatus.OK.value());
    }

    @GetMapping("/payBillByCards/{billId}")
    public RedirectView payBillByCards(@PathVariable("billId") String billId) throws Exception {
        String url = customerService.payBillByCards(billId);
        return new RedirectView(url);
    }

    @PostMapping("/handleCardPaymentCallback")
    public void handleCardPaymentCallback(@RequestBody @Valid TxnDto txnDto) throws Exception {
        log.info("reached");
        customerService.updateTransaction(txnDto);
        return;
    }

    @GetMapping("/payByWallet/{billId}")
    public ApiResponse<String> payByWallet(@PathVariable("billId") String billId) throws Exception {
        String userId = SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString();
        String res = customerService.payByWallet(userId, billId);
        return ApiResponse.success(res, "", HttpStatus.OK.value());
    }

    @GetMapping("/payByCash/{billId}")
    public ApiResponse<String> payByCash(@PathVariable("billId") String billId){
        String res = customerService.payByCash(billId);
        return ApiResponse.success(res, "", HttpStatus.OK.value());
    }

    @GetMapping("/redirect")
    public ApiResponse<String> redirect(){
        log.info("redirect reached");
        return ApiResponse.success("", "http://localhost:4200", 200);
    }

    @GetMapping("/validate")
    public void validate(){}
}
