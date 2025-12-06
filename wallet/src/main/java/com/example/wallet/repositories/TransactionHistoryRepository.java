package com.example.wallet.repositories;

import com.example.wallet.models.TransactionHistory;
import com.example.wallet.models.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TransactionHistoryRepository extends JpaRepository<TransactionHistory, Long>    {
    List<TransactionHistory> findByFromUserOrToUserOrderByCreatedAtDesc(User fromUser, User toUser);
    boolean existsByRequestId(String requestId);
}

