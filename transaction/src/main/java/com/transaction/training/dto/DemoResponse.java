package com.transaction.training.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DemoResponse {
    
    private Boolean success;
    private String scenario;
    private List<DemoStep> steps;
    private Map<String, Object> results;
    private List<String> logs;
    private String explanation;
    private String codeSnippet;
    private DatabaseState databaseState;
    private LocalDateTime timestamp;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DemoStep {
        private Integer stepNumber;
        private String description;
        private String status;
        private String details;
        private Long duration;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DatabaseState {
        private Map<String, Object> before;
        private Map<String, Object> after;
    }
    
    public static DemoResponse success(String scenario, String explanation) {
        return DemoResponse.builder()
                .success(true)
                .scenario(scenario)
                .steps(new ArrayList<>())
                .results(new HashMap<>())
                .logs(new ArrayList<>())
                .explanation(explanation)
                .timestamp(LocalDateTime.now())
                .build();
    }
    
    public static DemoResponse failure(String scenario, String explanation) {
        return DemoResponse.builder()
                .success(false)
                .scenario(scenario)
                .steps(new ArrayList<>())
                .results(new HashMap<>())
                .logs(new ArrayList<>())
                .explanation(explanation)
                .timestamp(LocalDateTime.now())
                .build();
    }
}
