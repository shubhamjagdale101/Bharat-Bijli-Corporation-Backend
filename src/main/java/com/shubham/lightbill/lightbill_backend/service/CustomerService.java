package com.shubham.lightbill.lightbill_backend.service;

import com.shubham.lightbill.lightbill_backend.configuration.JwtUtil;
import com.shubham.lightbill.lightbill_backend.constants.PaymentMethod;
import com.shubham.lightbill.lightbill_backend.constants.PaymentStatus;
import com.shubham.lightbill.lightbill_backend.constants.Role;
import com.shubham.lightbill.lightbill_backend.constants.TransactionStatus;
import com.shubham.lightbill.lightbill_backend.dto.InitiatePaymentDto;
import com.shubham.lightbill.lightbill_backend.dto.TxnDto;
import com.shubham.lightbill.lightbill_backend.model.Bill;
import com.shubham.lightbill.lightbill_backend.model.Transaction;
import com.shubham.lightbill.lightbill_backend.model.User;
import com.shubham.lightbill.lightbill_backend.model.Wallet;
import com.shubham.lightbill.lightbill_backend.repository.BillRepository;
import com.shubham.lightbill.lightbill_backend.repository.TransactionRepository;
import com.shubham.lightbill.lightbill_backend.repository.UserRepository;
import com.shubham.lightbill.lightbill_backend.repository.WalletRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.chrono.ChronoLocalDate;
import java.util.*;

@Slf4j
@Service
public class CustomerService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private BillRepository billRepository;
    @Autowired
    private NetworkCalls networkCalls;
    @Autowired
    private IdGeneratorService idGeneratorService;
    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private WalletRepository walletRepository;
    @Autowired
    private TransactionRepository transactionRepository;
    @Autowired
    private TransactionService transactionService;

    public User getCustomerProfile(String userId){
        return userRepository.findByUserId(userId);
    }

    public String payBillByCards(String billId) throws Exception {
        String userId = SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString();
        User user = userRepository.findByUserId(userId);
        if(user == null || user.getRole() != Role.CUSTOMER) throw new Exception("You cant pay bill.");

        Bill bill = billRepository.findByBillId(billId);
        if(bill == null) throw new Exception("No bill found with given bill id.");

        double discount;
        if(bill.getDueDate().after(Date.from(LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant()))){
            discount = 0.95;
        }
        else {
            discount = 0.9;
        }

        String url = "http://localhost:8081/initiatePayment";
        InitiatePaymentDto paymentDto = InitiatePaymentDto.builder()
                .totalAmount((int) (bill.getAmount() * discount))
                .callbackUrl("http://localhost:8080/customer/handleCardPaymentCallback")
                .receiverAccount("2280917365276177")
                .build();

        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", jwtUtil.generateToken("server", "Admin"));
        Map result = networkCalls.makePostCall(url, paymentDto, headers);

        Object txnId = result.get("data");
        result.forEach((key, value) -> System.out.println(key + " " + value));
        System.out.println(txnId);

        Transaction txn = Transaction.builder()
                .txnId(txnId.toString())
                .user(bill.getUser())
                .bill(bill)
                .transactionStatus(TransactionStatus.PENDING)
                .build();
        txn = transactionRepository.save(txn);
        System.out.println(txn.getTxnId() + ", " + txn.getUser().getEmail());
        return result.get("message").toString();
    }

    public void updateTransaction(TxnDto txnDto) throws Exception {
        Transaction txn = transactionRepository.findByTxnId(txnDto.getTransactionId());
        System.out.println(txnDto.getTransactionId());
        if(txn == null) throw new Exception("Txn not found.");
        Bill bill = billRepository.findByBillId(txn.getBill().getBillId());
        if(txnDto.getStatus() == PaymentStatus.PAID) bill.setPaymentStatus(PaymentStatus.PAID);
        billRepository.save(bill);


        txn.setTransactionStatus(TransactionStatus.valueOf(String.valueOf(txnDto.getStatus())));
        txn.setPaymentMethod(PaymentMethod.valueOf(txnDto.getPaymentMethod()));
        transactionRepository.save(txn);
    }

    public String payByWallet(String userId, String billId) throws Exception {
        User user = userRepository.findByUserId(userId);
        if(user == null || user.getRole() != Role.CUSTOMER) throw new Exception("You cant pay bill.");

        Wallet wallet = walletRepository.findByUser(user);

        Bill bill = billRepository.findByBillId(billId);
        if(bill == null) throw new Exception("There is no record with given billId");

        Double amount = null;
        LocalDate currDate = LocalDate.now();
        LocalDate dueDate = bill.getDueDate().toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();
        if(currDate.isAfter(dueDate)) {
            amount = bill.getAmount();
        } else {
            amount = bill.getAmount() - bill.getDiscount();
        }

        if(wallet.getBalance() >= amount){
            wallet.setBalance(wallet.getBalance() - amount);
            bill.setPaymentStatus(PaymentStatus.PAID);
            walletRepository.save(wallet);

            Transaction txn = Transaction.builder()
                    .txnId(idGeneratorService.generateId(Transaction.class.getName()))
                    .paymentMethod(PaymentMethod.WALLET)
                    .bill(bill)
                    .user(user)
                    .transactionStatus(TransactionStatus.PAID)
                    .build();
            transactionRepository.save(txn);
            return "Payment Done";
        }
        return "Insufficient Balance";
    }

    public String payByCash(String billId){
        Bill bill = billRepository.findByBillId(billId);
        bill.setPaymentStatus(PaymentStatus.PAID);
        bill = billRepository.save(bill);

        Transaction txn = Transaction.builder()
                .bill(bill)
                .txnId(idGeneratorService.generateId(Bill.class.getName()))
                .transactionStatus(TransactionStatus.PAID)
                .paymentMethod(PaymentMethod.CASH)
                .build();
        txn = transactionRepository.save(txn);
        return "Paid Successfully";
    }

    public Transaction getTransactionByFilter(String filterValue) {
        String userId = SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString();
        User user = userRepository.findByUserId(userId);

        Transaction txn = null;
        txn = transactionRepository.findByTxnIdAndUserAndTransactionStatus(filterValue, user, TransactionStatus.PAID);
        if(txn != null) return txn;

        Bill bill = billRepository.findByUserAndMonthOfTheBill(user, filterValue);
        if(bill == null) return null;

        txn = transactionRepository.findByBillAndTransactionStatus(bill, TransactionStatus.PAID);
        return txn;
    }
}
