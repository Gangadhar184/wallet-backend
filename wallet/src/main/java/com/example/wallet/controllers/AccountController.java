package com.example.wallet.controllers;

import com.example.wallet.dto.BalanceResponse;
import com.example.wallet.dto.TransactionDto;
import com.example.wallet.dto.TransferRequest;
import com.example.wallet.services.AccountService;
import com.example.wallet.services.WalletService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/account")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;
    private final WalletService walletService;

    @GetMapping("/balance")
    public ResponseEntity<BalanceResponse> getBalance(Authentication authentication) {
        String username = authentication.getName();
        BigDecimal balance = accountService.getBalance(username);

        return ResponseEntity.ok(new BalanceResponse(balance));
    }
    @PostMapping("/transfer")
    public ResponseEntity<String> transferMoney (@Valid  @RequestBody TransferRequest request, Authentication authentication) {
        String fromUsername = authentication.getName();
        walletService.transfer(fromUsername, request.getToUsername(), request.getAmount(), request.getRequestId());
        return ResponseEntity.ok("Transfer successful");
    }
    @GetMapping("/transactions")
    public ResponseEntity<List<TransactionDto>> transactions(Authentication auth) {
        return ResponseEntity.ok(walletService.getTransactions(auth.getName()));
    }
}

