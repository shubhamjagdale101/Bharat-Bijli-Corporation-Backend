package com.shubham.lightbill.lightbill_backend.controller;

import com.shubham.lightbill.lightbill_backend.annotation.WithRateLimitProtection;
import com.shubham.lightbill.lightbill_backend.response.ApiResponse;
import com.shubham.lightbill.lightbill_backend.service.PdfService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.*;


@RestController
@RequestMapping("/invoice")
@CrossOrigin(origins = "http://localhost:4200", allowCredentials = "true")
public class InvoiceController {
    @Autowired
    private PdfService pdfService;

    @GetMapping("/hello")
    @WithRateLimitProtection(rateLimit = 20, rateDuration = 60000)
    public ApiResponse<Integer> sayHello(HttpServletRequest request){
        return ApiResponse.success(20, "success", HttpStatus.OK.value());
    }

    @GetMapping("/upload")
    public ApiResponse<String> uploadFileToCloudinary() throws IOException {
        String res =  pdfService.generateAndUploadInvoice();
        return ApiResponse.success(null, res, HttpStatus.ACCEPTED.value());
    }

    @PostMapping("/invoiceEmail")
    public ApiResponse<String> sendInvoiceEmail() throws Exception {
        String userId = SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString();
        pdfService.sendInvoiceOnMail(userId);
        return ApiResponse.success(null, "Invoice mail will be send", HttpStatus.ACCEPTED.value());
    }
}
