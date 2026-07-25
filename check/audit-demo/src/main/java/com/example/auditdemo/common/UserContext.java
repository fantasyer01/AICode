package com.example.auditdemo.common;

/**
 * Simple user context for demo purposes.
 * In a real application, this would integrate with your authentication system.
 */
public class UserContext {

    private static final ThreadLocal<Long> USER_ID = new ThreadLocal<>();
    private static final ThreadLocal<String> USER_NAME = new ThreadLocal<>();

    /**
     * Set current user (called by interceptor or filter)
     */
    public static void setCurrentUser(Long userId, String userName) {
        USER_ID.set(userId);
        USER_NAME.set(userName);
    }

    /**
     * Get current user ID
     */
    public static Long getCurrentUserId() {
        Long userId = USER_ID.get();
        return userId != null ? userId : 1L; // Default to 1 for demo
    }

    /**
     * Get current user name
     */
    public static String getCurrentUserName() {
        String userName = USER_NAME.get();
        return userName != null ? userName : "Demo User"; // Default for demo
    }

    /**
     * Clear context
     */
    public static void clear() {
        USER_ID.remove();
        USER_NAME.remove();
    }
}
