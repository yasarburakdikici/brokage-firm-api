package com.brokage.challenge.audit;

import com.brokage.challenge.dto.CreateOrder;
import com.brokage.challenge.entity.Order;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class AuditAspect {
    
    private final AuditService auditService;
    
    public AuditAspect(AuditService auditService) {
        this.auditService = auditService;
    }
    
    @Around("@annotation(auditable)")
    public Object audit(ProceedingJoinPoint joinPoint, Auditable auditable) throws Throwable {
        String operation = auditable.operation();
        String entityType = auditable.entityType();
        
        Object[] args = joinPoint.getArgs();
        String customerId = extractCustomerId(args);
        Long entityId = extractEntityId(args);
        String details = buildDetails(args);
        
        try {
            Object result = joinPoint.proceed();
            
            // Extract entity ID from result if available
            if (result instanceof Order) {
                entityId = ((com.brokage.challenge.entity.Order) result).getId();
            }
            
            auditService.logSuccess(operation, entityType, entityId, customerId, details);
            return result;
            
        } catch (Exception e) {
            auditService.logFailure(operation, entityType, entityId, customerId, details, e.getMessage());
            throw e;
        }
    }
    
    private String extractCustomerId(Object[] args) {
        // Try to extract customer ID from CreateOrder or Order objects
        for (Object arg : args) {
            if (arg instanceof CreateOrder createOrder) {
                return createOrder.customer();
            }
            if (arg instanceof Order order) {
                return order.getCustomerId();
            }
        }
        return "unknown";
    }
    
    private Long extractEntityId(Object[] args) {
        // Try to extract entity ID from Long parameters or Order objects
        for (Object arg : args) {
            if (arg instanceof Long) {
                return (Long) arg;
            }
            if (arg instanceof Order order) {
                return order.getId();
            }
        }
        return null;
    }
    
    private String buildDetails(Object[] args) {
        StringBuilder details = new StringBuilder();
        for (Object arg : args) {
            if (arg instanceof CreateOrder createOrder) {
                details.append("Order: ").append(createOrder.side())
                       .append(" ").append(createOrder.asset())
                       .append(" Size: ").append(createOrder.size())
                       .append(" Price: ").append(createOrder.price());
                break;
            }
            if (arg instanceof Order order) {
                details.append("Order ID: ").append(order.getId())
                       .append(" Status: ").append(order.getStatus());
                break;
            }
            if (arg instanceof Long) {
                details.append("Entity ID: ").append(arg);
            }
        }
        return details.toString();
    }
}
