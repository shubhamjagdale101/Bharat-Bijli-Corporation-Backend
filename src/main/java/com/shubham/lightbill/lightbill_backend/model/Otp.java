package com.shubham.lightbill.lightbill_backend.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.util.Date;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Otp {
    @Id
    private String email;

    @Column(nullable = false)
    private String otp;

    @UpdateTimestamp
    private Date updatedTime;
}
