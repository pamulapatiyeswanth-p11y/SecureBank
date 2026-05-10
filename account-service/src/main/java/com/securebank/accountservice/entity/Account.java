package com.securebank.accountservice.entity;

import jakarta.persistence.*;
import jakarta.ws.rs.DefaultValue;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "accounts")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false,unique = true)
    private String accountNumber;

    @Column(nullable = false)
    private Long customerId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AccountType accountType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AccountStatus accountStatus;

    @Column(nullable = false,precision = 15,scale = 2)
    private BigDecimal balance;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal minimumBalance;

    private String IFSCCode;
    private Long approvedBy;

    private LocalDateTime approvedAt;
    // Reason for rejection if rejected
    private String remarks;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    // Updated only on customer transactions
    private LocalDateTime lastActivityDate;
}
