package com.martin.demo.model;

import com.martin.demo.auth.AppUser;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
public class Loan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private AppUser borrower;

    @ManyToOne(optional = false)
    private AppUser lender;

    @Column(nullable = false)
    private BigDecimal principalAmount;

    private String title; // optional, e.g. "Private loan"

    private Instant createdAt = Instant.now();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public AppUser getBorrower() {
        return borrower;
    }

    public void setBorrower(AppUser borrower) {
        this.borrower = borrower;
    }

    public AppUser getLender() {
        return lender;
    }

    public void setLender(AppUser lender) {
        this.lender = lender;
    }

    public BigDecimal getPrincipalAmount() {
        return principalAmount;
    }

    public void setPrincipalAmount(BigDecimal principalAmount) {
        this.principalAmount = principalAmount;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
