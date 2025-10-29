package com.github.loadup.gateway.facade.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Unified response result object
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Result {
    /**
     * Business code
     */
    private String code;
    /**
     * Status
     */
    private String status;
    /**
     * Message
     */
    private String message;
}
