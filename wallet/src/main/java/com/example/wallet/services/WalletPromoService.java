package com.example.wallet.services;

import com.example.wallet.models.Account;
import com.example.wallet.models.User;
import com.example.wallet.repositories.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class WalletPromoService {

    private final AccountRepository accountRepository;

    private static final BigDecimal SIGNUP_BONUS = BigDecimal.valueOf(10000);

    @Transactional
    public void applySignupBonus(User user) {

        // Locking the account row to prevent double bonus
        Account account = accountRepository.findByUserForUpdate(user)
                .orElseThrow(() -> new IllegalArgumentException("Account not found"));

        account.setBalance(account.getBalance().add(SIGNUP_BONUS));
        accountRepository.save(account);
    }
}

