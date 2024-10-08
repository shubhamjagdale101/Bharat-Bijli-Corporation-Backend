package com.shubham.lightbill.lightbill_backend.service;

import com.shubham.lightbill.lightbill_backend.constants.FilterTypes;
import com.shubham.lightbill.lightbill_backend.constants.PaymentMethod;
import com.shubham.lightbill.lightbill_backend.constants.TransactionStatus;
import com.shubham.lightbill.lightbill_backend.model.Transaction;
import com.shubham.lightbill.lightbill_backend.model.User;
import com.shubham.lightbill.lightbill_backend.repository.TransactionRepository;
import com.shubham.lightbill.lightbill_backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TransactionService {
    @Autowired
    private TransactionRepository transactionRepository;
    @Autowired
    private UserRepository userRepository;

    public List<Transaction> getTransactionUsingPagination(Pageable pageable){
        Pageable sortedPageable = PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                pageable.getSort().and(Sort.by(Sort.Direction.DESC, "updatedAt"))  // Combine existing sorting with new sorting
        );
        Page<Transaction> page = transactionRepository.findAll(sortedPageable);
        return page.getContent();
    }

    public Page<Transaction> getTransactionUsingPaginationForUserId(String userid, Pageable pageable){
        User user = userRepository.findByUserId(userid);
        Pageable sortedPageable = PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                pageable.getSort().and(Sort.by(Sort.Direction.DESC, "updatedAt"))  // Combine existing sorting with new sorting
        );
        Page<Transaction> page = transactionRepository.findByUser(user, sortedPageable);
        return page;
    }

    private Page<Transaction> getTransactionByFilterTransactionStatus(String filterValue, Pageable pageable) throws Exception {
        String userId = SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString();
        User user = userRepository.findByUserId(userId);
        Pageable sortedPageable = PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                pageable.getSort().and(Sort.by(Sort.Direction.DESC, "updatedAt"))  // Combine existing sorting with new sorting
        );

        try{
            TransactionStatus status = TransactionStatus.valueOf(filterValue);
            return transactionRepository.findByTransactionStatusAndUser(status, user, sortedPageable);
        } catch(IllegalStateException ex){
            throw new Exception("there is no transaction status like" + filterValue);
        }
    }

    private Page<Transaction> getTransactionByFilterPaymentMethod(String filterValue, Pageable pageable) throws Exception {
        String userId = SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString();
        User user = userRepository.findByUserId(userId);
        Pageable sortedPageable = PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                pageable.getSort().and(Sort.by(Sort.Direction.DESC, "updatedAt"))  // Combine existing sorting with new sorting
        );

        try{
            PaymentMethod method = PaymentMethod.valueOf(filterValue);
            return transactionRepository.findByPaymentMethodAndUser(method, user, sortedPageable);
        } catch(IllegalStateException ex){
            throw new Exception("there is no payment method like" + filterValue);
        }
    }

    public Page<Transaction> getTransactionByFilterWithPagination(String filterBy, String filterValue, Pageable pageable) throws Exception {
        if(filterBy.equals(FilterTypes.FILTER_TYPE_FOR_TRANSACTION_BY_TRANSACTION_STATUS)){
            return getTransactionByFilterTransactionStatus(filterValue, pageable);
        } else if(filterBy.equals(FilterTypes.FILTER_TYPE_FOR_TRANSACTION_BY_PAYMENT_METHOD)){
            return getTransactionByFilterPaymentMethod(filterValue, pageable);
        } else {
            throw new Exception("filter " + filterBy + " not supported");
        }
    }
}
