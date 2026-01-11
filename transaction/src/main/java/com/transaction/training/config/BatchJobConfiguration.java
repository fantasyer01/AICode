package com.transaction.training.config;

import com.transaction.training.batch.AccountItemProcessor;
import com.transaction.training.batch.AccountItemReader;
import com.transaction.training.batch.AccountItemWriter;
import com.transaction.training.dto.AccountBatchDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * Spring Batch Job Configuration
 * Demonstrates chunk-oriented processing with ItemReader -> ItemProcessor -> ItemWriter
 * 
 * Spring Batch Transaction Management:
 * ====================================
 * 
 * 1. NO @Transactional Annotation Needed:
 *    - Spring Batch manages transactions automatically at the CHUNK level
 *    - Transaction boundaries are defined by chunk size, not method annotations
 * 
 * 2. Transaction Flow (AOP-based, but chunk-driven):
 *    
 *    TransactionInterceptor (Spring AOP Proxy)
 *      ↓
 *    BEGIN TRANSACTION
 *      ↓
 *    ┌─────────────────────────────────────────┐
 *    │  Chunk Processing (size = 3)            │
 *    │                                         │
 *    │  IMPORTANT: Items are processed ONE BY ONE, not all at once!
 *    │                                         │
 *    │  1. ItemReader.read()     → Item 1     │  Read 1st item
 *    │  2. ItemProcessor.process() → Item 1    │  Process it immediately
 *    │     (Item 1 stored in memory)           │  Result cached
 *    │                                         │
 *    │  3. ItemReader.read()     → Item 2     │  Then read 2nd item
 *    │  4. ItemProcessor.process() → Item 2    │  Process it immediately
 *    │     (Item 2 stored in memory)           │  Result cached
 *    │                                         │
 *    │  5. ItemReader.read()     → Item 3     │  Then read 3rd item
 *    │  6. ItemProcessor.process() → Item 3    │  Process it immediately
 *    │     (Item 3 stored in memory)           │  Result cached
 *    │                                         │
 *    │  7. ItemWriter.write([1,2,3])          │  BATCH write all 3 items
 *    │     → INSERT INTO ... (Item 1)          │  to database in one call
 *    │     → INSERT INTO ... (Item 2)          │
 *    │     → INSERT INTO ... (Item 3)          │
 *    │                                         │
 *    └─────────────────────────────────────────┘
 *      ↓
 *    COMMIT TRANSACTION (success)
 *    or
 *    ROLLBACK TRANSACTION (exception)
 * 
 * 3. Key Components:
 *    - ChunkOrientedTasklet: Core class that manages chunk transaction
 *    - RepeatTemplate: Loops through items within transaction
 *    - PlatformTransactionManager: Same transaction manager as @Transactional
 * 
 * 4. Rollback Mechanism:
 *    - If ItemProcessor throws exception → Entire chunk rolls back
 *    - If ItemWriter throws exception → Entire chunk rolls back
 *    - Previous chunks already committed → NOT affected
 * 
 * 5. AOP Implementation:
 *    Yes, Spring Batch uses AOP, but wraps the ENTIRE CHUNK in one transaction:
 *    
 *    Proxy → TransactionInterceptor.invoke() {
 *        transactionManager.getTransaction()
 *        try {
 *            chunkOrientedTasklet.execute()  // Read → Process → Write
 *            transactionManager.commit()
 *        } catch (Exception e) {
 *            transactionManager.rollback()
 *        }
 *    }
 * 
 * 6. Comparison with @Transactional:
 *    
 *    @Transactional (method-level):
 *      - One method call = One transaction
 *      - Developer controls transaction boundary
 *    
 *    Spring Batch (chunk-level):
 *      - One chunk = One transaction
 *      - Framework controls transaction boundary
 *      - Automatic retry/skip capabilities
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class BatchJobConfiguration {
    
    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    
    private final AccountItemReader accountItemReader;
    private final AccountItemProcessor accountItemProcessor;
    private final AccountItemWriter accountItemWriter;
    
    /**
     * Define the batch job for successful processing
     * Job consists of one or more Steps
     */
    @Bean
    public Job accountImportSuccessJob() {
        return new JobBuilder("accountImportSuccessJob", jobRepository)
            .start(accountImportSuccessStep())
            .build();
    }
    
    /**
     * Define the batch job for rollback scenario
     */
    @Bean
    public Job accountImportRollbackJob() {
        return new JobBuilder("accountImportRollbackJob", jobRepository)
            .start(accountImportRollbackStep())
            .build();
    }
    
    /**
     * Define step with chunk-oriented processing (success scenario)
     * Chunk size = 3: Process 3 items at a time in one transaction
     * 
     * Transaction boundaries:
     * - Chunk 1: Items 1-3 processed and committed together
     * - Chunk 2: Items 4-5 processed and committed together
     */
    @Bean
    public Step accountImportSuccessStep() {
        return new StepBuilder("accountImportSuccessStep", jobRepository)
            .<AccountBatchDto, AccountBatchDto>chunk(3, transactionManager)  // Chunk size = 3
            .reader(accountItemReader)       // Read from CSV
            .processor(accountItemProcessor)  // Validate & Transform
            .writer(accountItemWriter)        // Write to database
            .build();
    }
    
    /**
     * Define step for rollback scenario
     * Same chunk size = 3
     * When ItemProcessor throws exception in chunk, entire chunk rolls back
     */
    @Bean
    public Step accountImportRollbackStep() {
        return new StepBuilder("accountImportRollbackStep", jobRepository)
            .<AccountBatchDto, AccountBatchDto>chunk(3, transactionManager)  // Chunk size = 3
            .reader(accountItemReader)       // Read from CSV (with invalid data)
            .processor(accountItemProcessor)  // Will throw exception on invalid data
            .writer(accountItemWriter)        // Won't be called if processor fails
            .build();
    }
    
    /**
     * Alternative: Fine-grained transaction control
     * Chunk size = 2: More frequent commits, smaller transaction scope
     */
    @Bean
    public Step accountImportFineGrainedStep() {
        return new StepBuilder("accountImportFineGrainedStep", jobRepository)
            .<AccountBatchDto, AccountBatchDto>chunk(2, transactionManager)  // Chunk size = 2
            .reader(accountItemReader)
            .processor(accountItemProcessor)
            .writer(accountItemWriter)
            .build();
    }
}
