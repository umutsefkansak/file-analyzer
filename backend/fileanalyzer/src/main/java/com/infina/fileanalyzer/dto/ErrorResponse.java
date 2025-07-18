package com.infina.fileanalyzer.dto;

import java.time.LocalDateTime;

/**
 * Data Transfer Object for standardized error responses across the application.
 * This class encapsulates error details returned to clients when exceptions occur.
 */
public class ErrorResponse {
    private int status;
    private String message;
    private LocalDateTime timestamp;

    /**
     * Constructs a new ErrorResponseDto with the specified parameters.
     *
     * @param status    HTTP status code of the error response
     * @param message   Human-readable error message
     * @param timestamp Time when the error occurred
     */
    public ErrorResponse(int status, String message, LocalDateTime timestamp) {
        this.status = status;
        this.message = message;
        this.timestamp = timestamp;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
