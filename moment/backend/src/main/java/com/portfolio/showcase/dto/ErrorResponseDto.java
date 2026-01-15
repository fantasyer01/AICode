package com.portfolio.showcase.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Data Transfer Object for error responses.
 * Provides a consistent error format across all API endpoints.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Error response data")
public class ErrorResponseDto {

    @Schema(description = "Timestamp when the error occurred", example = "2024-01-14T10:30:00")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp;

    @Schema(description = "HTTP status code", example = "404")
    private Integer status;

    @Schema(description = "Error type", example = "Not Found")
    private String error;

    @Schema(description = "Detailed error message", example = "Project not found with id: 999")
    private String message;

    @Schema(description = "Request path that caused the error", example = "/api/projects/999")
    private String path;

    /**
     * Creates an ErrorResponseDto with the current timestamp.
     *
     * @param status  HTTP status code
     * @param error   error type
     * @param message detailed error message
     * @param path    request path
     * @return ErrorResponseDto instance
     */
    public static ErrorResponseDto of(Integer status, String error, String message, String path) {
        return ErrorResponseDto.builder()
                .timestamp(LocalDateTime.now())
                .status(status)
                .error(error)
                .message(message)
                .path(path)
                .build();
    }
}
