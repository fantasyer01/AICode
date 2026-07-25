package com.aiblog.exception;

import org.apache.catalina.connector.ClientAbortException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.async.AsyncRequestNotUsableException;

/**
 * View-rendering exception handler for {@code @Controller} (Thymeleaf) pages.
 *
 * Scope: catch-all advice. {@link GlobalExceptionHandler} runs first
 * (HIGHEST_PRECEDENCE) for {@code @RestController} beans and returns JSON,
 * so this advice only effectively handles plain {@code @Controller} beans.
 */
@ControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
public class WebExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(WebExceptionHandler.class);

    @ExceptionHandler(ResourceNotFoundException.class)
    public String handleNotFound(ResourceNotFoundException ex, Model model) {
        log.warn("Page not found: {}", ex.getMessage());
        model.addAttribute("message", ex.getMessage());
        return "error/404";
    }

    /**
     * Browser/client closed the connection. No point rendering a view.
     */
    @ExceptionHandler({ClientAbortException.class, AsyncRequestNotUsableException.class})
    public void handleClientAbort(Exception ex) {
        log.debug("Client aborted page request: {}", ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public String handleGeneral(Exception ex, Model model) {
        if (isClientAbort(ex)) {
            log.debug("Client aborted page request: {}", ex.getMessage());
            return null;
        }
        log.error("Unexpected page error: {}", ex.getMessage(), ex);
        model.addAttribute("message", "An unexpected error occurred");
        return "error/500";
    }

    private boolean isClientAbort(Throwable ex) {
        Throwable cur = ex;
        while (cur != null) {
            if (cur instanceof ClientAbortException) {
                return true;
            }
            cur = cur.getCause();
        }
        return false;
    }
}
