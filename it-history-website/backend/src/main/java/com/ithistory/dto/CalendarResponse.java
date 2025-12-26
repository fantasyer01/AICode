package com.ithistory.dto;

import lombok.Data;

import java.util.List;

@Data
public class CalendarResponse {
    private Integer year;
    private Integer month;
    private List<Integer> datesWithStories;
}
