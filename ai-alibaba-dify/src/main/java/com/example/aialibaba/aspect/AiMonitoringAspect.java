package com.example.aialibaba.aspect;

import com.example.aialibaba.model.dto.ChatRequestDTO;
import com.example.aialibaba.model.dto.ChatResponseDTO;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Aspect for monitoring AI service calls
 */
@Aspect
@Component
public class AiMonitoringAspect {

    private static final Logger monitorLogger = LoggerFactory.getLogger("com.example.aialibaba.monitor");

    @Around("execution(* com.example.aialibaba.service.ChatService.sendMessage(..)) || " +
            "execution(* com.example.aialibaba.service.AiModelService.sendMessage(..))")
    public Object monitorAiCall(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        Object[] args = joinPoint.getArgs();
        ChatRequestDTO request = (args != null && args.length > 0 && args[0] instanceof ChatRequestDTO) 
                ? (ChatRequestDTO) args[0] : null;

        String methodName = joinPoint.getSignature().getName();
        String serviceName = joinPoint.getTarget().getClass().getSimpleName();

        try {
            Object result = joinPoint.proceed();
            long duration = System.currentTimeMillis() - startTime;

            if (result instanceof ChatResponseDTO) {
                ChatResponseDTO response = (ChatResponseDTO) result;
                logCallDetails(serviceName, methodName, request, response, duration, null);
            }

            return result;
        } catch (Throwable e) {
            long duration = System.currentTimeMillis() - startTime;
            logCallDetails(serviceName, methodName, request, null, duration, e);
            throw e;
        }
    }

    private void logCallDetails(String service, String method, ChatRequestDTO request, 
                                ChatResponseDTO response, long duration, Throwable error) {
        StringBuilder log = new StringBuilder();
        log.append("Service: ").append(service)
           .append(" | Method: ").append(method)
           .append(" | Duration: ").append(duration).append("ms");

        if (request != null) {
            log.append(" | User: ").append(request.getUserId())
               .append(" | AppCode: ").append(request.getAppCode())
               .append(" | ServiceType: ").append(request.getServiceType());
        }

        if (response != null) {
            log.append(" | Status: ").append(response.getStatus());
            if (response.getUsage() != null) {
                log.append(" | Tokens: ").append(response.getUsage().getTotalTokens());
            }
        }

        if (error != null) {
            log.append(" | Error: ").append(error.getMessage());
        }

        monitorLogger.info(log.toString());
    }
}
