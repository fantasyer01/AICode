package com.transaction.training.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO for batch account data processing
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountBatchDto {
    
    private String userName;
    private BigDecimal balance;
    private String email;
    
    // For tracking in demo
    private Integer lineNumber;
    private String rawLine;
}
