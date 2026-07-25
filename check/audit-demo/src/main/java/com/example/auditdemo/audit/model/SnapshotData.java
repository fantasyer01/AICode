package com.example.auditdemo.audit.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Enhanced snapshot data structure for display-friendly audit information
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SnapshotData {
    
    /**
     * Business type
     */
    private String businessType;
    
    /**
     * Operation type: INSERT, UPDATE, DELETE
     */
    private String operationType;
    
    /**
     * Display title (e.g., "User Information", "Order Information")
     */
    private String title;
    
    /**
     * Main entity fields for display
     */
    @Builder.Default
    private List<DisplayField> fields = new ArrayList<>();
    
    /**
     * Sub-entity sections (for multi-table operations like Order + OrderItems)
     */
    @Builder.Default
    private List<SubSection> subSections = new ArrayList<>();
    
    /**
     * Summary information (optional, for quick overview)
     */
    private String summary;
    
    /**
     * Sub-section for related data (e.g., order items)
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SubSection {
        /**
         * Section title (e.g., "Order Items")
         */
        private String title;
        
        /**
         * Table headers (for tabular display)
         */
        private List<String> headers;
        
        /**
         * Table rows
         */
        @Builder.Default
        private List<List<String>> rows = new ArrayList<>();
        
        /**
         * Old rows (for UPDATE comparison)
         */
        private List<List<String>> oldRows;
        
        /**
         * Footer summary (e.g., total amount)
         */
        private String footer;
    }
}
