package com.transaction.training;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableTransactionManagement
public class TransactionTrainingApplication {

    public static void main(String[] args) {
        SpringApplication.run(TransactionTrainingApplication.class, args);
    }
}
