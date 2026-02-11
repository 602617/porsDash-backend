package com.martin.demo.model;

import com.martin.demo.auth.AppUser;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

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

    @ManyToMany
    @JoinTable(
            name = "loan_access",
            joinColumns = @JoinColumn(name = "loan_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<AppUser> allowedUsers = new HashSet<>();


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

    public Set<AppUser> getAllowedUsers() {
        return allowedUsers;
    }

    public void setAllowedUsers(Set<AppUser> allowedUsers) {
        this.allowedUsers = allowedUsers;
    }
}
