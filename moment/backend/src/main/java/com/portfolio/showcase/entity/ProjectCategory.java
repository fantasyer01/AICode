package com.portfolio.showcase.entity;

/**
 * Enum representing the categories for projects.
 * Maps to the frontend Category type: 'web' | 'mobile' | 'ai' | 'data'
 */
public enum ProjectCategory {
    WEB("web"),
    MOBILE("mobile"),
    AI("ai"),
    DATA("data");

    private final String value;

    ProjectCategory(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    /**
     * Converts a string value to ProjectCategory enum.
     * Case-insensitive matching.
     *
     * @param value the string value to convert
     * @return the matching ProjectCategory
     * @throws IllegalArgumentException if no matching category found
     */
    public static ProjectCategory fromValue(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Category value cannot be null or empty");
        }
        
        String normalizedValue = value.trim().toLowerCase();
        
        for (ProjectCategory category : ProjectCategory.values()) {
            if (category.value.equals(normalizedValue)) {
                return category;
            }
        }
        
        throw new IllegalArgumentException("Invalid category: " + value + 
            ". Valid values are: web, mobile, ai, data");
    }

    /**
     * Checks if a string is a valid category value.
     *
     * @param value the string value to check
     * @return true if valid, false otherwise
     */
    public static boolean isValid(String value) {
        if (value == null || value.isBlank()) {
            return false;
        }
        
        String normalizedValue = value.trim().toLowerCase();
        
        for (ProjectCategory category : ProjectCategory.values()) {
            if (category.value.equals(normalizedValue)) {
                return true;
            }
        }
        
        return false;
    }
}
