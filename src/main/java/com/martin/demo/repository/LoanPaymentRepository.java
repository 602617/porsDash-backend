package com.martin.demo.repository;

import com.martin.demo.model.LoanPayment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LoanPaymentRepository extends JpaRepository<LoanPayment, Long> {
    List<LoanPayment> findByLoanIdOrderByPaidAtDesc(Long loanId);
}
