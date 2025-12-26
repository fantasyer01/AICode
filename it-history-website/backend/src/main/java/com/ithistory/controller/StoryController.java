package com.ithistory.controller;

import com.ithistory.dto.CalendarResponse;
import com.ithistory.dto.ErrorResponse;
import com.ithistory.dto.StoryDto;
import com.ithistory.llm.LlmException;
import com.ithistory.service.StoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Year;
import java.util.List;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
@Tag(name = "IT History API", description = "API for IT history storytelling")
public class StoryController {
    
    private static final Logger logger = LoggerFactory.getLogger(StoryController.class);
    
    @Autowired
    private StoryService storyService;
    
    /**
     * Get calendar data for a specific month
     */
    @GetMapping("/calendar/{year}/{month}")
    @Operation(summary = "Get calendar metadata", 
               description = "Retrieve dates with available stories for a specific month")
    public ResponseEntity<?> getCalendar(
            @Parameter(description = "Year") @PathVariable Integer year,
            @Parameter(description = "Month (1-12)") @PathVariable Integer month) {
        
        try {
            if (month < 1 || month > 12) {
                return ResponseEntity.badRequest()
                        .body(new ErrorResponse("INVALID_MONTH", 
                                "Month must be between 1 and 12", 
                                "/api/calendar/" + year + "/" + month));
            }
            
            List<Integer> datesWithStories = storyService.getDatesWithStories(month);
            
            CalendarResponse response = new CalendarResponse();
            response.setYear(year);
            response.setMonth(month);
            response.setDatesWithStories(datesWithStories);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error getting calendar data", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("INTERNAL_ERROR", 
                            "Failed to retrieve calendar data", 
                            "/api/calendar/" + year + "/" + month));
        }
    }
    
    /**
     * Get story for a specific date
     */
    @GetMapping("/story/{month}/{day}")
    @Operation(summary = "Get IT history story", 
               description = "Retrieve or generate story for a specific date")
    public ResponseEntity<?> getStory(
            @Parameter(description = "Month (1-12)") @PathVariable Integer month,
            @Parameter(description = "Day (1-31)") @PathVariable Integer day,
            @Parameter(description = "Force regeneration") 
            @RequestParam(required = false, defaultValue = "false") Boolean refresh) {
        
        try {
            StoryDto story = storyService.getStory(month, day, refresh);
            return ResponseEntity.ok(story);
            
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid date: {}/{}", month, day);
            return ResponseEntity.badRequest()
                    .body(new ErrorResponse("INVALID_DATE", 
                            e.getMessage(), 
                            "/api/story/" + month + "/" + day));
            
        } catch (LlmException e) {
            logger.error("LLM generation failed", e);
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(new ErrorResponse("LLM_ERROR", 
                            "Failed to generate story: " + e.getMessage(), 
                            "/api/story/" + month + "/" + day));
            
        } catch (Exception e) {
            logger.error("Unexpected error", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("INTERNAL_ERROR", 
                            "An unexpected error occurred", 
                            "/api/story/" + month + "/" + day));
        }
    }
    
    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    @Operation(summary = "Health check", description = "Check if API is running")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("IT History API is running");
    }
}
