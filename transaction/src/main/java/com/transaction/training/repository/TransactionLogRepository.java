package com.transaction.training.repository;

import com.transaction.training.entity.TransactionLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TransactionLogRepository extends JpaRepository<TransactionLog, Long> {
    
    List<TransactionLog> findByTransactionType(String transactionType);
    
    List<TransactionLog> findByExecutedAtAfter(LocalDateTime dateTime);
    
    List<TransactionLog> findTop50ByOrderByExecutedAtDesc();
}
