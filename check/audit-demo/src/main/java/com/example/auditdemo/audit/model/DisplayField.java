package com.example.auditdemo.audit.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Display field for audit snapshot
 * Represents a single field with label and value for display
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DisplayField {
    
    /**
     * Field label (Chinese name for business users)
     */
    private String label;
    
    /**
     * Field value (formatted for display)
     */
    private String value;
    
    /**
     * Original value (before change, for UPDATE comparison)
     */
    private String oldValue;
    
    /**
     * Whether this field has changed (for UPDATE operations)
     */
    private Boolean changed;
    
    /**
     * Badge style: success, danger, warning, info, secondary
     */
    private String badge;
    
    /**
     * Field type hint: text, number, date, badge, etc.
     */
    private String type;
    
    // Constructors for simple cases
    public DisplayField(String label, String value) {
        this.label = label;
        this.value = value;
        this.changed = false;
    }
    
    public DisplayField(String label, String value, String badge) {
        this.label = label;
        this.value = value;
        this.badge = badge;
        this.changed = false;
    }
}
