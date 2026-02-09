package com.martin.demo.service;

import com.martin.demo.auth.AppUser;
import com.martin.demo.dto.CreatePaymentDto;
import com.martin.demo.dto.LoanPaymentDto;
import com.martin.demo.dto.LoanSummaryDto;
import com.martin.demo.dto.UpdatePaymentDto;
import com.martin.demo.model.Loan;
import com.martin.demo.model.LoanPayment;
import com.martin.demo.repository.AppUserRepository;
import com.martin.demo.repository.LoanPaymentRepository;
import com.martin.demo.repository.LoanRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Service
public class LoanService {

    private final LoanRepository loans;
    private final LoanPaymentRepository payments;
    private final AppUserRepository users;

    public LoanService(LoanRepository loans, LoanPaymentRepository payments, AppUserRepository users) {
        this.loans = loans;
        this.payments = payments;
        this.users = users;
    }

    private AppUser me(String username) {
        return users.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
    }

    private Loan loanOrThrow(Long loanId) {
        return loans.findById(loanId)
                .orElseThrow(() -> new EntityNotFoundException("Loan not found"));
    }

    private void assertParticipant(Loan loan, AppUser me) {
        boolean ok = loan.getBorrower().getId().equals(me.getId())
                || loan.getLender().getId().equals(me.getId());
        if (!ok) throw new AccessDeniedException("Not allowed");
    }

    public LoanSummaryDto getLoan(Long loanId, String username) {
        Loan loan = loans.findById(loanId).orElseThrow(() -> new EntityNotFoundException("Loan not found"));
        AppUser me = me(username);
        assertParticipant(loan, me);

        ;
        assertParticipant(loan, me);

        List<LoanPayment> history = payments.findByLoanIdOrderByPaidAtDesc(loanId);

        BigDecimal sumPaid = history.stream()
                .map(LoanPayment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal left = loan.getPrincipalAmount().subtract(sumPaid);

        return LoanSummaryDto.from(loan, sumPaid, left, history);
    }

    public LoanPaymentDto addPayment(Long loanId, CreatePaymentDto dto, String username) {
        Loan loan = loans.findById(loanId)
                .orElseThrow(() -> new EntityNotFoundException("Loan not found"));

        // 2. Get current user
        AppUser me = me(username);

        assertParticipant(loan, me);

        LoanPayment p = new LoanPayment();
        p.setLoan(loan);
        p.setAmount(dto.amount());
        p.setNote(dto.note());
        p.setPaidAt(dto.paidAt() != null ? dto.paidAt() : Instant.now());
        p.setCreatedBy(me);

        LoanPayment saved = payments.save(p);
        return LoanPaymentDto.from(saved);
    }

    public LoanPaymentDto updatePayment(Long paymentId, UpdatePaymentDto dto, String username) {
        LoanPayment p = payments.findById(paymentId)
                .orElseThrow(() -> new EntityNotFoundException("Payment not found"));

        Loan loan = p.getLoan();
        AppUser me = me(username);
        assertParticipant(loan, me);




        if (dto.amount() != null) p.setAmount(dto.amount());
        if (dto.note() != null) p.setNote(dto.note());
        if (dto.paidAt() != null) p.setPaidAt(dto.paidAt());

        return LoanPaymentDto.from(payments.save(p));
    }

    public void deletePayment(Long paymentId, String username) {
        LoanPayment p = payments.findById(paymentId)
                .orElseThrow(() -> new EntityNotFoundException("Payment not found"));

        Loan loan = p.getLoan();
        AppUser me = me(username);
        assertParticipant(loan, me);



        payments.delete(p);
    }

    private AppUser me(Authentication auth) {
        return users.findByUsername(auth.getName())
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
    }



}

