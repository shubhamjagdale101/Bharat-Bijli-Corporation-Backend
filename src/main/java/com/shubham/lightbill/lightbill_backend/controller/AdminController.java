package com.shubham.lightbill.lightbill_backend.controller;

import com.shubham.lightbill.lightbill_backend.constants.Role;
import com.shubham.lightbill.lightbill_backend.dto.SignUpDto;
import com.shubham.lightbill.lightbill_backend.model.User;
import com.shubham.lightbill.lightbill_backend.response.ApiResponse;
import com.shubham.lightbill.lightbill_backend.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/auth")
@CrossOrigin(origins = "http://localhost:4200", allowCredentials = "true")
public class AdminController {
    @Autowired
    private AuthService authservice;
    @PostMapping("/signUpUser")
    public ApiResponse<User> signUpUser(@Valid @RequestBody SignUpDto req) throws Exception {
        User user = authservice.signUpUser(req, Role.EMPLOYEE);
        return ApiResponse.success(user, "", 200);
    }
}
