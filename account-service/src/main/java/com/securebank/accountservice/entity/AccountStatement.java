package com.securebank.accountservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "account_statements")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class AccountStatement {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id",nullable = false)
    private Account account;

    @Column(nullable = false)
    private String transactionReference;

    @Column(nullable = false)
    private String transactionType; //Debit or Credit

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    // Balance after this transaction was applied
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal balanceAfter;

    private String description;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;
}
