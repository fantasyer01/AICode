package com.example.auditdemo;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.example.auditdemo.**.mapper")
public class AuditDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuditDemoApplication.class, args);
    }
}
