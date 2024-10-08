package com.shubham.lightbill.lightbill_backend.controller;

import com.shubham.lightbill.lightbill_backend.annotation.ValidEnum;
import com.shubham.lightbill.lightbill_backend.annotation.WithRateLimitProtection;
import com.shubham.lightbill.lightbill_backend.configuration.JwtUtil;
import com.shubham.lightbill.lightbill_backend.constants.OtpType;
import com.shubham.lightbill.lightbill_backend.constants.Role;
import com.shubham.lightbill.lightbill_backend.dto.SignUpDto;
import com.shubham.lightbill.lightbill_backend.model.User;
import com.shubham.lightbill.lightbill_backend.repository.UserRepository;
import com.shubham.lightbill.lightbill_backend.response.ApiResponse;
import com.shubham.lightbill.lightbill_backend.service.AuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.net.URLEncoder;

@Slf4j
@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "http://localhost:4200", allowCredentials = "true")
public class AuthController {
    @Autowired
    private AuthService authservice;
    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private UserRepository userRepository;
    private static final Logger logger = LoggerFactory.getLogger(JwtUtil.class);

    @GetMapping("/generateOtp")
    @Transactional
    public ApiResponse<Object> sendOtpEmail(@RequestParam("emailId") String emailId) throws Exception {
        authservice.sendOtpToEmail(emailId);
        return ApiResponse.success(null, "sending otp", HttpStatus.PROCESSING.value());
    }

    @WithRateLimitProtection(rateLimit = 20, rateDuration = 60000)
    @GetMapping("/verifyOtp")
    public ApiResponse<String> verifyOtp(
            @RequestParam("emailId") String emailId,
            @RequestParam("otp") String otp,
            HttpServletResponse response
    ){
        Boolean res = authservice.verifyOtp(otp, emailId);
        if(!res) return ApiResponse.error("incorrect otp", 100);

        User user = userRepository.findByEmail(emailId);
        String token = jwtUtil.generateToken(user.getUserId(), String.valueOf(user.getRole()));
        Cookie cookie = authservice.generateCookie("Bearer-token", token.trim());
        response.addCookie(cookie);
        return ApiResponse.success("", URLEncoder.encode(token), HttpStatus.OK.value());
    }

    @PostMapping("/signUpUser")
    public ApiResponse<User> signUpUser(@RequestBody SignUpDto req, HttpServletResponse response) throws Exception {
        User user = authservice.signUpUser(req, Role.CUSTOMER);
        String token = jwtUtil.generateToken(user.getUserId(), String.valueOf(user.getRole()));
        Cookie cookie = authservice.generateCookie("Bearer-token", token.trim());
        response.addCookie(cookie);
        return ApiResponse.success(user, "", 200);
    }

    @DeleteMapping("/clearCookies")
    public ApiResponse<String> deleteAllCookies(HttpServletRequest request, HttpServletResponse response) {
        Cookie[] cookies = request.getCookies();

        if (cookies != null) {
            for (Cookie cookie : cookies) {
                log.info("Deleting cookie: " + cookie.getName());
                Cookie deleteCookie = new Cookie(cookie.getName(), null);
                deleteCookie.setMaxAge(0); // Set the expiration to delete the cookie
                deleteCookie.setPath(cookie.getPath() != null ? cookie.getPath() : "/"); // Keep the same path
                deleteCookie.setDomain(cookie.getDomain()); // Set the same domain if it exists
                deleteCookie.setHttpOnly(cookie.isHttpOnly()); // Retain HttpOnly setting
                deleteCookie.setSecure(cookie.getSecure()); // Retain Secure setting
                response.addCookie(deleteCookie); // Add the delete cookie to the response
            }
        } else {
            log.info("No cookies found to delete.");
        }

        return ApiResponse.success("","All cookies have been deleted.", 200);
    }
}
