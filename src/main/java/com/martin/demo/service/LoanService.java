package com.martin.demo.service;

import com.martin.demo.auth.AppUser;
import com.martin.demo.dto.CreateLoanDto;
import com.martin.demo.dto.CreatePaymentDto;
import com.martin.demo.dto.LoanListDto;
import com.martin.demo.dto.LoanPaymentDto;
import com.martin.demo.dto.LoanSummaryDto;
import com.martin.demo.dto.UpdatePaymentDto;
import com.martin.demo.model.Loan;
import com.martin.demo.model.LoanPayment;
import com.martin.demo.pushnotifications.notifications.NotificationService;
import com.martin.demo.repository.AppUserRepository;
import com.martin.demo.repository.LoanPaymentRepository;
import com.martin.demo.repository.LoanRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.access.AccessDeniedException;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Service
public class LoanService {

    private final LoanRepository loans;
    private final LoanPaymentRepository payments;
    private final AppUserRepository users;
    private final NotificationService notificationService;

    public LoanService(LoanRepository loans, LoanPaymentRepository payments,
                       AppUserRepository users, NotificationService notificationService) {
        this.loans = loans;
        this.payments = payments;
        this.users = users;
        this.notificationService = notificationService;
    }

    private void assertAccess(Loan loan, String username) {
        boolean isBorrower = loan.getBorrower() != null
                && username.equals(loan.getBorrower().getUsername());

        boolean isLender = loan.getLender() != null
                && username.equals(loan.getLender().getUsername());

        boolean isShared = loan.getAllowedUsers().stream()
                .anyMatch(u -> username.equals(u.getUsername()));

        if (!isBorrower && !isLender && !isShared) {
            throw new AccessDeniedException("Not allowed");
        }
    }

    private AppUser me(String username) {
        return users.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
    }



    private void assertParticipant(Loan loan, AppUser me) {
        boolean ok = loan.getBorrower().getId().equals(me.getId())
                || loan.getLender().getId().equals(me.getId());
        if (!ok) throw new AccessDeniedException("Not allowed");
    }

    private void notifyOtherParty(Loan loan, AppUser me, String message) {
        AppUser other = loan.getBorrower().getId().equals(me.getId())
                ? loan.getLender()
                : loan.getBorrower();
        notificationService.notifyUser(other.getId(), message, "/loans/" + loan.getId());
    }

    public LoanSummaryDto getLoan(Long loanId, String username) {

        // Load loan
        Loan loan = loans.findByIdAndActiveTrue(loanId)
                .orElseThrow(() -> new EntityNotFoundException("Loan not found"));

        // 🔐 Access check (borrower OR lender OR shared)
        assertAccess(loan, username);

        // Load payment history
        List<LoanPayment> history =
                payments.findByLoanIdOrderByPaidAtDesc(loanId);

        // Calculate totals
        BigDecimal sumPaid = history.stream()
                .map(LoanPayment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal left = loan.getPrincipalAmount().subtract(sumPaid);

        // Return DTO
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

        String loanName = loan.getTitle() != null ? loan.getTitle() : "lån";
        notifyOtherParty(loan, me,
                me.getUsername() + " la til en betaling på " + dto.amount() + " kr (" + loanName + ")");

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

        LoanPaymentDto result = LoanPaymentDto.from(payments.save(p));

        String loanName = loan.getTitle() != null ? loan.getTitle() : "lån";
        notifyOtherParty(loan, me,
                me.getUsername() + " oppdaterte en betaling (" + loanName + ")");

        return result;
    }

    public void deletePayment(Long paymentId, String username) {
        LoanPayment p = payments.findById(paymentId)
                .orElseThrow(() -> new EntityNotFoundException("Payment not found"));

        Loan loan = p.getLoan();
        AppUser me = me(username);
        assertParticipant(loan, me);

        String loanName = loan.getTitle() != null ? loan.getTitle() : "lån";
        payments.delete(p);

        notifyOtherParty(loan, me,
                me.getUsername() + " slettet en betaling (" + loanName + ")");
    }

    public LoanSummaryDto createLoan(CreateLoanDto dto, String username) {
        AppUser me = me(username);
        AppUser other = users.findByUsername(dto.otherUsername())
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + dto.otherUsername()));

        if (me.getId().equals(other.getId())) {
            throw new IllegalArgumentException("Du kan ikke opprette et lån med deg selv");
        }

        Loan loan = new Loan();
        loan.setPrincipalAmount(dto.amount());
        loan.setTitle(dto.title());

        if ("BORROWER".equalsIgnoreCase(dto.myRole())) {
            loan.setBorrower(me);
            loan.setLender(other);
        } else {
            loan.setLender(me);
            loan.setBorrower(other);
        }

        Loan saved = loans.save(loan);

        String loanName = saved.getTitle() != null ? saved.getTitle() : "lån";
        notificationService.notifyUser(other.getId(),
                me.getUsername() + " opprettet et lån med deg: " + loanName
                        + " (" + saved.getPrincipalAmount() + " kr)",
                "/loans/" + saved.getId());

        List<LoanPayment> history = payments.findByLoanIdOrderByPaidAtDesc(saved.getId());
        BigDecimal sumPaid = BigDecimal.ZERO;
        BigDecimal left = saved.getPrincipalAmount();
        return LoanSummaryDto.from(saved, sumPaid, left, history);
    }

    public void deleteLoan(Long loanId, String username) {
        Loan loan = loans.findByIdAndActiveTrue(loanId)
                .orElseThrow(() -> new EntityNotFoundException("Loan not found"));

        AppUser me = me(username);
        assertParticipant(loan, me);

        loan.setActive(false);
        loans.save(loan);

        String loanName = loan.getTitle() != null ? loan.getTitle() : "lån";
        notifyOtherParty(loan, me,
                me.getUsername() + " arkiverte lånet: " + loanName);
    }

    public List<LoanListDto> listLoans(String username) {
        return loans.findActiveLoansForUser(username).stream()
                .map(LoanListDto::from)
                .toList();
    }



}

