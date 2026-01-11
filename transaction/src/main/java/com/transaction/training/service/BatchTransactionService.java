package com.transaction.training.service;

import com.transaction.training.batch.AccountItemReader;
import com.transaction.training.dto.DemoResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.*;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class BatchTransactionService {
    
    private final JobLauncher jobLauncher;
    private final Job accountImportSuccessJob;
    private final Job accountImportRollbackJob;
    private final AccountItemReader accountItemReader;
    
    /**
     * Demonstrate successful Spring Batch processing
     * Uses chunk-oriented processing: ItemReader -> ItemProcessor -> ItemWriter
     */
    public DemoResponse demonstrateBatchSuccess() {
        DemoResponse response = DemoResponse.success("SPRING_BATCH_SUCCESS",
            "Spring Batch chunk-oriented processing: Read CSV -> Validate/Transform -> Write to DB in chunks. All chunks succeed.");
        
        List<String> logs = new ArrayList<>();
        List<DemoResponse.DemoStep> steps = new ArrayList<>();
        long startTime = System.currentTimeMillis();
        
        try {
            // Initialize reader with valid data
            accountItemReader.initialize(false);
            
            // Step 1: Prepare job
            steps.add(DemoResponse.DemoStep.builder()
                .stepNumber(1)
                .description("Initialize Spring Batch Job")
                .status("SUCCESS")
                .details("Job: accountImportSuccessJob, Chunk size: 3")
                .duration(20L)
                .build());
            logs.add("[" + LocalDateTime.now() + "] Initializing Spring Batch job with chunk size 3");
            
            // Step 2: Launch job
            JobParameters jobParameters = new JobParametersBuilder()
                .addLong("time", System.currentTimeMillis())
                .addString("scenario", "success")
                .toJobParameters();
            
            steps.add(DemoResponse.DemoStep.builder()
                .stepNumber(2)
                .description("Launch Batch Job")
                .status("IN_PROGRESS")
                .details("Starting chunk-oriented processing")
                .duration(30L)
                .build());
            logs.add("[" + LocalDateTime.now() + "] JobLauncher starting execution...");
            
            // Execute job
            JobExecution jobExecution = jobLauncher.run(accountImportSuccessJob, jobParameters);
            
            // Step 3: Chunk 1 processing
            steps.add(DemoResponse.DemoStep.builder()
                .stepNumber(3)
                .description("Process Chunk 1 (Items 1-3)")
                .status("SUCCESS")
                .details("ItemReader reads 3 items -> ItemProcessor validates -> ItemWriter commits in transaction")
                .duration(150L)
                .build());
            logs.add("[" + LocalDateTime.now() + "] Chunk 1: Read items 1-3 from CSV");
            logs.add("[" + LocalDateTime.now() + "] Chunk 1: ItemProcessor validating items");
            logs.add("[" + LocalDateTime.now() + "] Chunk 1: ItemWriter persisting 3 accounts");
            logs.add("[" + LocalDateTime.now() + "] Chunk 1: Transaction committed successfully");
            
            // Step 4: Chunk 2 processing
            steps.add(DemoResponse.DemoStep.builder()
                .stepNumber(4)
                .description("Process Chunk 2 (Items 4-5)")
                .status("SUCCESS")
                .details("ItemReader reads 2 items -> ItemProcessor validates -> ItemWriter commits in transaction")
                .duration(120L)
                .build());
            logs.add("[" + LocalDateTime.now() + "] Chunk 2: Read items 4-5 from CSV");
            logs.add("[" + LocalDateTime.now() + "] Chunk 2: ItemProcessor validating items");
            logs.add("[" + LocalDateTime.now() + "] Chunk 2: ItemWriter persisting 2 accounts");
            logs.add("[" + LocalDateTime.now() + "] Chunk 2: Transaction committed successfully");
            
            // Step 5: Job completion
            BatchStatus batchStatus = jobExecution.getStatus();
            steps.add(DemoResponse.DemoStep.builder()
                .stepNumber(5)
                .description("Batch Job Completed")
                .status(batchStatus == BatchStatus.COMPLETED ? "SUCCESS" : "ERROR")
                .details("Status: " + batchStatus + ", Exit: " + jobExecution.getExitStatus().getExitCode())
                .duration(40L)
                .build());
            logs.add("[" + LocalDateTime.now() + "] Batch job completed: " + batchStatus);
            logs.add("[" + LocalDateTime.now() + "] Total records processed: 5, All committed across 2 chunks");
            
            // Set response data
            Map<String, Object> results = new HashMap<>();
            results.put("totalRecords", 5);
            results.put("chunksProcessed", 2);
            results.put("chunkSize", 3);
            results.put("recordsCommitted", 5);
            results.put("jobStatus", batchStatus.name());
            results.put("duration", System.currentTimeMillis() - startTime);
            
            response.setSteps(steps);
            response.setLogs(logs);
            response.setResults(results);
            response.setCodeSnippet("""
                // Spring Batch Configuration
                @Bean
                public Step chunkStep() {
                    return stepBuilder
                        .<AccountDto, AccountDto>chunk(3, transactionManager)  // Chunk size = 3
                        .reader(itemReader)     // Read from CSV
                        .processor(itemProcessor) // Validate & Transform
                        .writer(itemWriter)     // Write to DB
                        .build();
                }
                
                // Transaction Boundaries:
                // - Chunk 1: Items 1-3 in one transaction
                // - Chunk 2: Items 4-5 in one transaction
                // Each chunk commits independently
                """);
            
        } catch (Exception e) {
            log.error("Batch job error", e);
            response.setSuccess(false);
            response.setExplanation("Batch job failed: " + e.getMessage());
        }
        
        return response;
    }
    
    /**
     * Demonstrate batch processing with rollback on error
     * Shows how Spring Batch rolls back the current chunk when ItemProcessor fails
     */
    public DemoResponse demonstrateBatchRollback() {
        DemoResponse response = DemoResponse.success("SPRING_BATCH_ROLLBACK",
            "Spring Batch chunk rollback: When ItemProcessor fails validation, current chunk rolls back. Previous chunks remain committed.");
        
        List<String> logs = new ArrayList<>();
        List<DemoResponse.DemoStep> steps = new ArrayList<>();
        long startTime = System.currentTimeMillis();
        
        try {
            // Initialize reader with invalid data (negative balance at line 3)
            accountItemReader.initialize(true);
            
            // Step 1: Prepare job
            steps.add(DemoResponse.DemoStep.builder()
                .stepNumber(1)
                .description("Initialize Spring Batch Job")
                .status("SUCCESS")
                .details("Job: accountImportRollbackJob, Chunk size: 3")
                .duration(20L)
                .build());
            logs.add("[" + LocalDateTime.now() + "] Initializing Spring Batch job with chunk size 3");
            logs.add("[" + LocalDateTime.now() + "] CSV contains invalid data at line 3 (negative balance)");
            
            // Step 2: Launch job
            JobParameters jobParameters = new JobParametersBuilder()
                .addLong("time", System.currentTimeMillis())
                .addString("scenario", "rollback")
                .toJobParameters();
            
            steps.add(DemoResponse.DemoStep.builder()
                .stepNumber(2)
                .description("Launch Batch Job")
                .status("IN_PROGRESS")
                .details("Starting chunk-oriented processing")
                .duration(30L)
                .build());
            logs.add("[" + LocalDateTime.now() + "] JobLauncher starting execution...");
            
            // Execute job (will fail)
            JobExecution jobExecution = jobLauncher.run(accountImportRollbackJob, jobParameters);
            
            // Step 3: Chunk 1 - processes items 1-3, item 3 fails
            steps.add(DemoResponse.DemoStep.builder()
                .stepNumber(3)
                .description("Process Chunk 1 (Items 1-3)")
                .status("ERROR")
                .details("ItemProcessor failed validation on item 3 (negative balance). Entire chunk rolled back.")
                .duration(100L)
                .build());
            logs.add("[" + LocalDateTime.now() + "] Chunk 1: Read items 1-3 from CSV");
            logs.add("[" + LocalDateTime.now() + "] Chunk 1: ItemProcessor processing item 1 - VALID");
            logs.add("[" + LocalDateTime.now() + "] Chunk 1: ItemProcessor processing item 2 - VALID");
            logs.add("[" + LocalDateTime.now() + "] Chunk 1: ItemProcessor processing item 3 - INVALID (negative balance)");
            logs.add("[" + LocalDateTime.now() + "] ERROR: Validation failed at line 3: Batch_Charlie, balance: -1000.00");
            logs.add("[" + LocalDateTime.now() + "] Chunk 1: ROLLBACK - All 3 items in chunk rolled back");
            
            // Step 4: Job failure
            BatchStatus batchStatus = jobExecution.getStatus();
            steps.add(DemoResponse.DemoStep.builder()
                .stepNumber(4)
                .description("Batch Job Failed")
                .status("ERROR")
                .details("Status: " + batchStatus + ", Exit: " + jobExecution.getExitStatus().getExitCode())
                .duration(20L)
                .build());
            logs.add("[" + LocalDateTime.now() + "] Batch job failed: " + batchStatus);
            logs.add("[" + LocalDateTime.now() + "] Transaction rollback ensures data consistency - no partial commits");
            
            response.setSuccess(false);
            response.setExplanation(
                "Chunk 1 failed on item 3 validation. Spring Batch rolled back the entire chunk. " +
                "Items 1-2 were NOT persisted. Items 4-5 were never processed.");
            
            // Set response data
            Map<String, Object> results = new HashMap<>();
            results.put("totalRecords", 5);
            results.put("attemptedRecords", 3);
            results.put("persistedRecords", 0);
            results.put("chunkSize", 3);
            results.put("failedChunk", 1);
            results.put("rollbackReason", "Invalid negative balance at line 3");
            results.put("jobStatus", batchStatus.name());
            results.put("duration", System.currentTimeMillis() - startTime);
            
            response.setSteps(steps);
            response.setLogs(logs);
            response.setResults(results);
            response.setCodeSnippet("""
                // ItemProcessor with validation
                @Override
                public AccountDto process(AccountDto item) throws Exception {
                    // Validation throws exception
                    if (item.getBalance().compareTo(BigDecimal.ZERO) < 0) {
                        throw new IllegalArgumentException("Negative balance not allowed");
                    }
                    return item;
                }
                
                // Spring Batch Transaction Behavior:
                // 1. ItemReader reads chunk (3 items)
                // 2. ItemProcessor validates each item
                // 3. If validation fails, exception thrown
                // 4. Spring Batch catches exception
                // 5. Current chunk transaction rolls back
                // 6. Job execution stops (or retries if configured)
                """);
            
        } catch (Exception e) {
            log.error("Batch job execution error", e);
            response.setSuccess(false);
            response.setExplanation("Batch job execution failed: " + e.getMessage());
        }
        
        return response;
    }
}
