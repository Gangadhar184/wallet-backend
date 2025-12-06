package com.example.wallet.services;

import com.example.wallet.dto.TransactionDto;
import com.example.wallet.models.Account;
import com.example.wallet.models.TransactionHistory;
import com.example.wallet.models.User;
import com.example.wallet.repositories.AccountRepository;
import com.example.wallet.repositories.TransactionHistoryRepository;
import com.example.wallet.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class WalletService {

    private final AccountRepository accountRepository;
    private final UserRepository userRepository;
    private final TransactionHistoryRepository transactionHistoryRepository;

    @Transactional
    public void transfer(String fromUsername, String toUsername, BigDecimal amount, String requestId) {

        if (transactionHistoryRepository.existsByRequestId(requestId)) {
            return; // idempotency
        }

        User fromUser = null;
        User toUser = null;

        try {
            if (fromUsername.equals(toUsername)) {
                throw new IllegalArgumentException("You cannot send money to yourself");
            }

            fromUser = userRepository.findByUsername(fromUsername)
                    .orElseThrow(() -> new IllegalArgumentException("Sender not found"));

            toUser = userRepository.findByUsername(toUsername)
                    .orElseThrow(() -> new IllegalArgumentException("Receiver not found"));

            Account fromAcc = accountRepository.findByUserForUpdate(fromUser)
                    .orElseThrow(() -> new IllegalArgumentException("Sender account not found"));

            Account toAcc = accountRepository.findByUserForUpdate(toUser)
                    .orElseThrow(() -> new IllegalArgumentException("Receiver account not found"));

            if (!fromAcc.isActive() || !toAcc.isActive()) {
                throw new IllegalArgumentException("One of the accounts is inactive");
            }

            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("Amount must be positive");
            }

            if (fromAcc.getBalance().compareTo(amount) < 0) {
                throw new IllegalArgumentException("Insufficient balance");
            }

            // SUCCESS FLOW
            fromAcc.setBalance(fromAcc.getBalance().subtract(amount));
            toAcc.setBalance(toAcc.getBalance().add(amount));

            accountRepository.save(fromAcc);
            accountRepository.save(toAcc);

            logSuccessTransfer(fromUser, toUser, amount, requestId);

        } catch (Exception ex) {

            if (fromUser != null && toUser != null) {
                logFailedTransfer(fromUser, toUser, amount, requestId, ex.getMessage());
            }
            throw ex;
        }
    }

    private void logSuccessTransfer(User fromUser, User toUser, BigDecimal amount, String requestId) {
        transactionHistoryRepository.save(TransactionHistory.builder()
                .fromUser(fromUser)
                .toUser(toUser)
                .amount(amount)
                .type("DEBIT")
                .status("SUCCESS")
                .requestId(requestId)
                .build());

    }

    private void logFailedTransfer(User fromUser, User toUser, BigDecimal amount, String requestId, String reason) {
        transactionHistoryRepository.save(TransactionHistory.builder()
                .fromUser(fromUser)
                .toUser(toUser)
                .amount(amount)
                .type("DEBIT")
                .status("FAILED: " + reason)
                .requestId(requestId)
                .build());
    }
    public List<TransactionDto> getTransactions(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow();

        return transactionHistoryRepository
                .findByFromUserOrToUserOrderByCreatedAtDesc(user, user)
                .stream()
                .map(tx -> new TransactionDto(
                        tx.getFromUser() != null ? tx.getFromUser().getUsername() : null,
                        tx.getToUser() != null ? tx.getToUser().getUsername() : null,
                        tx.getAmount(),
                        tx.getType(),
                        tx.getStatus(),
                        tx.getCreatedAt()
                ))
                .toList();
    }

}


