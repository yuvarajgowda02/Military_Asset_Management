package com.military.assetmanagement.aspect;

import com.military.assetmanagement.entity.AuditLog;
import com.military.assetmanagement.repository.AuditLogRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Cross-cutting audit logger. Every call into a @RestController method is
 * captured with actor, endpoint, action and outcome for accountability /
 * traceability purposes as required by the system spec.
 */
@Aspect
@Component
@RequiredArgsConstructor
public class AuditLogAspect {

    private static final Logger log = LoggerFactory.getLogger(AuditLogAspect.class);

    private final AuditLogRepository auditLogRepository;

    @Around("within(com.military.assetmanagement.controller..*)")
    public Object logAround(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().getName();
        String className = joinPoint.getSignature().getDeclaringTypeName();

        HttpServletRequest request = currentRequest();
        String httpMethod = request != null ? request.getMethod() : "N/A";
        String uri = request != null ? request.getRequestURI() : className + "." + methodName;
        String ip = request != null ? request.getRemoteAddr() : "N/A";

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = (auth != null) ? auth.getName() : "anonymous";
        String role = (auth != null && !auth.getAuthorities().isEmpty())
                ? auth.getAuthorities().iterator().next().getAuthority()
                : "N/A";

        int statusCode = 200;
        try {
            Object result = joinPoint.proceed();
            return result;
        } catch (Throwable ex) {
            statusCode = 500;
            throw ex;
        } finally {
            try {
                AuditLog entry = new AuditLog();
                entry.setUsername(username);
                entry.setUserRole(role);
                entry.setMethod(httpMethod);
                entry.setEndpoint(uri);
                entry.setAction(className.substring(className.lastIndexOf('.') + 1) + "#" + methodName);
                entry.setStatusCode(statusCode);
                entry.setIpAddress(ip);
                auditLogRepository.save(entry);
            } catch (Exception persistEx) {
                log.warn("Failed to persist audit log entry: {}", persistEx.getMessage());
            }
        }
    }

    private HttpServletRequest currentRequest() {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attrs != null ? attrs.getRequest() : null;
    }
}
