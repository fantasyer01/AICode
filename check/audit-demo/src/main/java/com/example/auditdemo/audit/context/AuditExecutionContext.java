package com.example.auditdemo.audit.context;

/**
 * Context to mark whether the current execution is an audit callback.
 * When in callback mode, the AOP aspect will allow the method to execute normally.
 */
public class AuditExecutionContext {

    private static final ThreadLocal<Boolean> CALLBACK_MODE = ThreadLocal.withInitial(() -> false);

    /**
     * Enter callback mode - method will execute normally without being intercepted
     */
    public static void enterCallbackMode() {
        CALLBACK_MODE.set(true);
    }

    /**
     * Exit callback mode
     */
    public static void exitCallbackMode() {
        CALLBACK_MODE.remove();
    }

    /**
     * Check if currently in callback mode
     */
    public static boolean isCallbackMode() {
        return Boolean.TRUE.equals(CALLBACK_MODE.get());
    }
}
