package com.securebank.user_service.entity;


import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "otp_verifications")
public class OtpVerification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id",nullable = false)
    private User user;

    @Column(nullable = false)
    private String otpCode;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OtpPurpose purpose;

    // Stores the pending new value (new email / new phone)
    // null for password change — we don't store new password here
    private String pendingValue;

    @Column(nullable = false)
    private boolean used = false;

    @CreationTimestamp
    private LocalDateTime createdAt;

    public boolean isExpired(){
        return (LocalDateTime.now().isAfter(expiresAt));
    }

}
