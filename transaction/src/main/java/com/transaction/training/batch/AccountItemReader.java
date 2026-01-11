package com.transaction.training.batch;

import com.transaction.training.dto.AccountBatchDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemReader;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Custom ItemReader for reading account data from simulated CSV file
 * In Spring Batch, ItemReader is responsible for reading data from source
 */
@Slf4j
@Component
public class AccountItemReader implements ItemReader<AccountBatchDto> {
    
    private List<AccountBatchDto> data;
    private int currentIndex = 0;
    private boolean includeInvalidData = false;
    
    /**
     * Initialize reader with data source
     */
    public void initialize(boolean includeInvalidData) {
        this.includeInvalidData = includeInvalidData;
        this.data = simulateCsvRead();
        this.currentIndex = 0;
        log.info("ItemReader initialized with {} records", data.size());
    }
    
    /**
     * Read one item at a time
     * Returns null when no more items to read
     */
    @Override
    public AccountBatchDto read() {
        if (currentIndex < data.size()) {
            AccountBatchDto item = data.get(currentIndex);
            currentIndex++;
            log.debug("ItemReader reading: line {}, user {}", item.getLineNumber(), item.getUserName());
            return item;
        }
        log.info("ItemReader finished reading all records");
        return null; // Signal end of data
    }
    
    /**
     * Simulate reading CSV file content
     */
    private List<AccountBatchDto> simulateCsvRead() {
        List<AccountBatchDto> records = new ArrayList<>();
        
        // Simulate CSV header: userName,balance,email
        log.info("Simulating CSV file read...");
        
        records.add(AccountBatchDto.builder()
            .lineNumber(1)
            .userName("Batch_Alice")
            .balance(new BigDecimal("5000.00"))
            .email("alice@example.com")
            .rawLine("Batch_Alice,5000.00,alice@example.com")
            .build());
        
        records.add(AccountBatchDto.builder()
            .lineNumber(2)
            .userName("Batch_Bob")
            .balance(new BigDecimal("3000.00"))
            .email("bob@example.com")
            .rawLine("Batch_Bob,3000.00,bob@example.com")
            .build());
        
        if (includeInvalidData) {
            // Invalid record - negative balance
            records.add(AccountBatchDto.builder()
                .lineNumber(3)
                .userName("Batch_Charlie")
                .balance(new BigDecimal("-1000.00"))
                .email("charlie@example.com")
                .rawLine("Batch_Charlie,-1000.00,charlie@example.com")
                .build());
        } else {
            records.add(AccountBatchDto.builder()
                .lineNumber(3)
                .userName("Batch_Charlie")
                .balance(new BigDecimal("4500.00"))
                .email("charlie@example.com")
                .rawLine("Batch_Charlie,4500.00,charlie@example.com")
                .build());
        }
        
        records.add(AccountBatchDto.builder()
            .lineNumber(4)
            .userName("Batch_David")
            .balance(new BigDecimal("6000.00"))
            .email("david@example.com")
            .rawLine("Batch_David,6000.00,david@example.com")
            .build());
        
        records.add(AccountBatchDto.builder()
            .lineNumber(5)
            .userName("Batch_Eve")
            .balance(new BigDecimal("2500.00"))
            .email("eve@example.com")
            .rawLine("Batch_Eve,2500.00,eve@example.com")
            .build());
        
        log.info("CSV simulation complete: {} records loaded", records.size());
        return records;
    }
    
    /**
     * Reset reader for reuse
     */
    public void reset() {
        this.currentIndex = 0;
        log.info("ItemReader reset");
    }
}
