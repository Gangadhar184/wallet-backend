package com.example.wallet.services;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.wallet.models.Account;
import com.example.wallet.models.User;
import com.example.wallet.repositories.AccountRepository;
import com.example.wallet.repositories.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;
    private final UserRepository userRepository;

    @Transactional
    public Account createAccountForUser(User user) {

        return accountRepository.findByUser(user)
                .orElseGet(() -> {
                    Account account = Account.builder()
                            .user(user)
                            .balance(BigDecimal.ZERO)
                            .active(true)
                            .build();
                    return accountRepository.save(account);
                });
    }


    @Transactional
    public BigDecimal getBalance(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // Auto-create account if it doesn't exist
        Account account = accountRepository.findByUser(user)
                .orElseGet(() -> createAccountForUser(user));

        return account.getBalance();
    }


}
