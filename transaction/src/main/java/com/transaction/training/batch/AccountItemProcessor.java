package com.transaction.training.batch;

import com.transaction.training.dto.AccountBatchDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Custom ItemProcessor for validating and transforming account data
 * In Spring Batch, ItemProcessor handles business logic and data transformation
 */
@Slf4j
@Component
public class AccountItemProcessor implements ItemProcessor<AccountBatchDto, AccountBatchDto> {
    
    /**
     * Process each item read by ItemReader
     * Can transform, validate, or filter items
     * Return null to skip the item (filter out)
     */
    @Override
    public AccountBatchDto process(AccountBatchDto item) throws Exception {
        log.info("ItemProcessor processing: line {}, user {}, balance {}", 
            item.getLineNumber(), item.getUserName(), item.getBalance());
        
        // Business validation
        validateBalance(item);
        validateUserName(item);
        
        // Data transformation (e.g., normalize username)
        item.setUserName(item.getUserName().trim());
        
        log.info("ItemProcessor validation passed for: {}", item.getUserName());
        return item;
    }
    
    /**
     * Validate balance is positive
     */
    private void validateBalance(AccountBatchDto item) {
        if (item.getBalance().compareTo(BigDecimal.ZERO) < 0) {
            String error = String.format(
                "Invalid balance at line %d: %s cannot be negative (value: %s)",
                item.getLineNumber(), item.getUserName(), item.getBalance()
            );
            log.error("Validation failed: {}", error);
            throw new IllegalArgumentException(error);
        }
        
        if (item.getBalance().compareTo(new BigDecimal("100000")) > 0) {
            String error = String.format(
                "Invalid balance at line %d: %s exceeds maximum allowed (value: %s)",
                item.getLineNumber(), item.getUserName(), item.getBalance()
            );
            log.error("Validation failed: {}", error);
            throw new IllegalArgumentException(error);
        }
    }
    
    /**
     * Validate username is not empty
     */
    private void validateUserName(AccountBatchDto item) {
        if (item.getUserName() == null || item.getUserName().trim().isEmpty()) {
            String error = String.format(
                "Invalid username at line %d: username cannot be empty",
                item.getLineNumber()
            );
            log.error("Validation failed: {}", error);
            throw new IllegalArgumentException(error);
        }
    }
}
