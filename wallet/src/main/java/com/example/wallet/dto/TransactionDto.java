package com.example.wallet.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class TransactionDto {
    private String fromUser;
    private String toUser;
    private BigDecimal amount;
    private String type;
    private String status;
    private LocalDateTime date;
}
