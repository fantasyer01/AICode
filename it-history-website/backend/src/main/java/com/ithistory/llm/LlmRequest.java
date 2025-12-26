package com.ithistory.llm;

import lombok.Data;

import java.util.List;

@Data
public class LlmRequest {
    private String date;
    private Integer month;
    private Integer day;
}
