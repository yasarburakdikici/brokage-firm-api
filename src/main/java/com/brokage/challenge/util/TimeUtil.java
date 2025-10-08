package com.brokage.challenge.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;

@Component
public class TimeUtil {
    
    private static final Logger log = LoggerFactory.getLogger(TimeUtil.class);
    
    public static class ExecutionTimer {
        private final Instant startTime;
        private final String operation;
        private final Logger logger;
        
        public ExecutionTimer(String operation, Logger logger) {
            this.startTime = Instant.now();
            this.operation = operation;
            this.logger = logger;
            logger.info("Starting operation: {}", operation);
        }
        
        public void finish() {
            Instant endTime = Instant.now();
            Duration duration = Duration.between(startTime, endTime);
            logger.info("Operation '{}' completed in {}ms", operation, duration.toMillis());
        }
        
        public void finishWithError(String errorMessage) {
            Instant endTime = Instant.now();
            Duration duration = Duration.between(startTime, endTime);
            logger.error("Operation '{}' failed after {}ms - Error: {}", 
                        operation, duration.toMillis(), errorMessage);
        }
        
        public long getElapsedMillis() {
            return Duration.between(startTime, Instant.now()).toMillis();
        }
    }
    
    public static ExecutionTimer startTimer(String operation, Logger logger) {
        return new ExecutionTimer(operation, logger);
    }
    
    public static ExecutionTimer startTimer(String operation, Class<?> clazz) {
        return new ExecutionTimer(operation, LoggerFactory.getLogger(clazz));
    }
    
    public static void logExecutionTime(String operation, long startTime, Logger logger) {
        long duration = System.currentTimeMillis() - startTime;
        logger.info("Operation '{}' completed in {}ms", operation, duration);
    }
    
    public static void logExecutionTimeWithError(String operation, long startTime, String error, Logger logger) {
        long duration = System.currentTimeMillis() - startTime;
        logger.error("Operation '{}' failed after {}ms - Error: {}", operation, duration, error);
    }
}
