package com.transaction.training.batch;

import com.transaction.training.dto.AccountBatchDto;
import com.transaction.training.entity.Account;
import com.transaction.training.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

/**
 * Custom ItemWriter for persisting account data to database
 * In Spring Batch, ItemWriter is responsible for writing data to destination
 * Spring Batch manages transaction boundaries around ItemWriter automatically
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AccountItemWriter implements ItemWriter<AccountBatchDto> {
    
    private final AccountRepository accountRepository;
    
    /**
     * Write a chunk of items to database
     * This method is called within a transaction managed by Spring Batch
     * If exception occurs, entire chunk will be rolled back
     */
    @Override
    public void write(Chunk<? extends AccountBatchDto> chunk) throws Exception {
        log.info("ItemWriter writing chunk of {} items to database", chunk.size());
        
        int writeCount = 0;
        for (AccountBatchDto dto : chunk.getItems()) {
            Account account = new Account();
            account.setUserName(dto.getUserName());
            account.setBalance(dto.getBalance());
            
            accountRepository.save(account);
            writeCount++;
            
            log.info("ItemWriter saved: {} (line {}) - balance: {}", 
                dto.getUserName(), dto.getLineNumber(), dto.getBalance());
        }
        
        log.info("ItemWriter completed writing {} items in current chunk", writeCount);
        // Transaction will commit automatically after this method returns successfully
        // If exception thrown, transaction will rollback
    }
}
