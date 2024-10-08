package com.shubham.lightbill.lightbill_backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SignUpDto {
    @NotBlank(message = "Name should not be blank")
    private String name;
    @Email(message = "Invalid Email")
    private String email;
    @Pattern(regexp = "^[789]\\d{9}$", message = "Invalid Phone Number")
    private String phNo;
    @NotBlank
    private String address;
    private String meterNumber;
}
