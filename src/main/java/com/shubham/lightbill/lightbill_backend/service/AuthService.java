package com.shubham.lightbill.lightbill_backend.service;

import com.shubham.lightbill.lightbill_backend.configuration.JwtUtil;
import com.shubham.lightbill.lightbill_backend.constants.OtpType;
import com.shubham.lightbill.lightbill_backend.constants.Role;
import com.shubham.lightbill.lightbill_backend.constants.WalletStatus;
import com.shubham.lightbill.lightbill_backend.dto.SignUpDto;
import com.shubham.lightbill.lightbill_backend.model.Otp;
import com.shubham.lightbill.lightbill_backend.model.OtpCompositeKey;
import com.shubham.lightbill.lightbill_backend.model.User;
import com.shubham.lightbill.lightbill_backend.model.Wallet;
import com.shubham.lightbill.lightbill_backend.repository.OtpRepository;
import com.shubham.lightbill.lightbill_backend.repository.UserRepository;
import com.shubham.lightbill.lightbill_backend.repository.WalletRepository;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.Cookie;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URLEncoder;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
public class AuthService {
    @Autowired
    private EmailService emailService;
    @Autowired
    private ThreadPoolService threadPool;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private WalletRepository walletRepository;
    @Autowired
    private IdGeneratorService idGeneratorService;
    @Autowired
    private OtpRepository otpRepository;
    @Autowired
    private JwtUtil jwtUtil;
    @Value("${otp.Expiration.time}")
    private long otpExpirationTime;

    public void sendEmail(String email, String username, String otp) throws MessagingException, IOException {
        Runnable task = () -> {
            try{
                emailService.sendOtpMail(email, username, otp);
            } catch (Exception ignored) {}
        };
        threadPool.submitTask(task);
    }

    public Cookie generateCookie(String name, String value){
        Cookie cookie = new Cookie(name, URLEncoder.encode(value));
        cookie.setHttpOnly(true); // Prevent JavaScript access to the cookie
        cookie.setSecure(false);   // Set to false for HTTP
        cookie.setPath("/");       // Cookie accessible to the whole application
        cookie.setMaxAge(60 * 60); // Set cookie expiry (1 hour)
        cookie.setDomain("localhost");
        return cookie;
    }

    public User generateUser(SignUpDto req, Role role){
        User user = User.builder()
                .userId(idGeneratorService.generateId(User.class.getName()))
                .name(req.getName())
                .email(req.getEmail())
                .phNo(req.getPhNo())
                .address(req.getAddress())
                .role(role)
                .isActive(true)
                .isBlocked(false)
                .build();
        return userRepository.save(user);
    }

    public Wallet generateWallet(User user){
        Wallet wallet = Wallet.builder()
                .walletId(idGeneratorService.generateId(Wallet.class.getName()))
                .user(user)
                .status(WalletStatus.ACTIVE)
                .balance(0)
                .build();
        return walletRepository.save(wallet);
    }

    public User signUpUser(@Valid SignUpDto req, Role role) throws Exception {
        User currUser = userRepository.findByEmail(req.getEmail());
        if(currUser != null) throw new Exception(("User already Exists"));

        User user = generateUser(req, role);

        if(user.getRole() == Role.CUSTOMER){
            Wallet wallet = generateWallet(user);
            user.setWallet(wallet);
            user.setMeterNumber(idGeneratorService.generateId("METER"));
            userRepository.save(user);
        }

        return user;
    }

    public Otp generateOtp(User user, String generatedOtp){
        Otp otp = Otp.builder()
                .key(
                        OtpCompositeKey.builder()
                                .type(OtpType.VERIFY_USER)
                                .user(user)
                        .build()
                )
                .otp(generatedOtp)
                .build();
        return otpRepository.save(otp);
    }

    public void sendOtpToEmail(String userId) throws Exception {
        SecureRandom random = new SecureRandom();
        String generatedOtp = String.format("%06d", random.nextInt(999999));

        User user = userRepository.findByUserId(userId);
        if(user == null) throw new Exception("User Not Found");

        Otp otp = generateOtp(user, generatedOtp);
        sendEmail(user.getEmail(), user.getName(), generatedOtp);
    }

    public Boolean verifyOtp(String userOtp, String userId, OtpType type){
        User user = userRepository.findByUserId(userId);
        OtpCompositeKey key = OtpCompositeKey.builder()
                .user(user)
                .type(type)
                .build();

        Otp dbOtp = otpRepository.findByKey(key);
        long duration = Duration.between(Instant.now(), dbOtp.getUpdatedTime().toInstant()).toMillis();

        return duration <= otpExpirationTime && Objects.equals(userOtp, dbOtp.getOtp());
    }
}
