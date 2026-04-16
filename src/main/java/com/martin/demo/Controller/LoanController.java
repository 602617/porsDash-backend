package com.martin.demo.Controller;

import com.martin.demo.dto.CreateLoanDto;
import com.martin.demo.dto.CreatePaymentDto;
import com.martin.demo.dto.LoanListDto;
import com.martin.demo.dto.LoanPaymentDto;
import com.martin.demo.dto.LoanSummaryDto;
import com.martin.demo.dto.UpdatePaymentDto;
import com.martin.demo.service.LoanService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/loans")
public class LoanController {

    private final LoanService service;

    public LoanController(LoanService service) {
        this.service = service;
    }

    @GetMapping
    public List<LoanListDto> listLoans(Authentication auth) {
        return service.listLoans(auth.getName());
    }

    @PostMapping
    public LoanSummaryDto createLoan(@RequestBody CreateLoanDto dto, Authentication auth) {
        return service.createLoan(dto, auth.getName());
    }

    @DeleteMapping("/{loanId}")
    public void deleteLoan(@PathVariable Long loanId, Authentication auth) {
        service.deleteLoan(loanId, auth.getName());
    }

    @GetMapping("/{loanId}")
    public LoanSummaryDto getLoan(@PathVariable Long loanId, Authentication auth) {
        System.out.println("JWT principal = " + auth.getName());
        return service.getLoan(loanId, auth.getName());
    }

    @PostMapping("/{loanId}/payments")
    public LoanPaymentDto addPayment(@PathVariable Long loanId,
                                     @RequestBody CreatePaymentDto dto,
                                     Authentication auth) {
        return service.addPayment(loanId, dto, auth.getName());
    }

    @PutMapping("/payments/{paymentId}")
    public LoanPaymentDto updatePayment(@PathVariable Long paymentId,
                                        @RequestBody UpdatePaymentDto dto,
                                        Authentication auth) {
        return service.updatePayment(paymentId, dto, auth.getName());
    }

    @DeleteMapping("/payments/{paymentId}")
    public void deletePayment(@PathVariable Long paymentId, Authentication auth) {
        service.deletePayment(paymentId, auth.getName());
    }
}

