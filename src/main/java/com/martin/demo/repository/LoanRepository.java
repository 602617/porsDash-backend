package com.martin.demo.repository;

import com.martin.demo.model.Loan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface LoanRepository extends JpaRepository<Loan, Long> {
    Optional<Loan> findByIdAndActiveTrue(Long id);

    @Query("SELECT l FROM Loan l WHERE l.active = true AND (l.borrower.username = :username OR l.lender.username = :username) ORDER BY l.createdAt DESC")
    List<Loan> findActiveLoansForUser(@Param("username") String username);
}
