package org.aibles.cal_eos_fee.service;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import lombok.extern.slf4j.Slf4j;
import org.aibles.cal_eos_fee.dto.websocket.MessageType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@Slf4j
public class RateLimitService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final CircuitBreaker circuitBreaker;
    private final LocalFallbackService localFallbackService;
    
    @Value("${websocket.rate-limit.calculate-fee}")
    private int calculateFeeLimit;
    
    @Value("${websocket.rate-limit.window-minutes}")
    private int windowMinutes;

    public RateLimitService(RedisTemplate<String, Object> redisTemplate, 
                           CircuitBreaker circuitBreaker,
                           LocalFallbackService localFallbackService) {
        this.redisTemplate = redisTemplate;
        this.circuitBreaker = circuitBreaker;
        this.localFallbackService = localFallbackService;
    }

    public boolean isAllowed(String sessionId, MessageType messageType) {
        if (messageType != MessageType.CALCULATE_FEE) {
            return true;
        }

        try {
            return circuitBreaker.executeSupplier(() -> {
                String key = "rate_limit:" + sessionId + ":" + messageType.name();
                
                // Get current count - Redis increment operations return Long, get operations can return various types
                Object countObj = redisTemplate.opsForValue().get(key);
                int currentCount = 0;
                
                if (countObj != null) {
                    if (countObj instanceof Number) {
                        currentCount = ((Number) countObj).intValue();
                    } else if (countObj instanceof String) {
                        try {
                            currentCount = Integer.parseInt((String) countObj);
                        } catch (NumberFormatException e) {
                            log.warn("Invalid count format in Redis for key {}: {}", key, countObj);
                            currentCount = 0;
                        }
                    }
                }
                
                if (currentCount >= calculateFeeLimit) {
                    log.warn("Rate limit exceeded for session {} and message type {}", sessionId, messageType);
                    return false;
                }
                
                // Increment and set expiry
                Long newCount = redisTemplate.opsForValue().increment(key);
                redisTemplate.expire(key, Duration.ofMinutes(windowMinutes));
                
                log.debug("Rate limit check passed for session {}, count: {}/{}", sessionId, newCount, calculateFeeLimit);
                return true;
            });
        } catch (CallNotPermittedException e) {
            log.warn("Redis circuit breaker is open, falling back to local rate limiting for session {}", sessionId);
            return localFallbackService.isAllowed(sessionId, messageType, calculateFeeLimit);
        } catch (Exception e) {
            log.error("Error checking rate limit for session {}, falling back to local service", sessionId, e);
            return localFallbackService.isAllowed(sessionId, messageType, calculateFeeLimit);
        }
    }

    @Service
    public static class LocalFallbackService {
        private final ConcurrentHashMap<String, AtomicInteger> localCounters = new ConcurrentHashMap<>();
        private final ConcurrentHashMap<String, Long> windowStartTimes = new ConcurrentHashMap<>();

        public boolean isAllowed(String sessionId, MessageType messageType, int limit) {
            if (messageType != MessageType.CALCULATE_FEE) {
                return true;
            }

            String key = sessionId + ":" + messageType.name();
            long now = System.currentTimeMillis();
            long windowDuration = 60000; // 1 minute in milliseconds

            windowStartTimes.compute(key, (k, startTime) -> {
                if (startTime == null || (now - startTime) >= windowDuration) {
                    localCounters.put(key, new AtomicInteger(0));
                    return now;
                }
                return startTime;
            });

            AtomicInteger counter = localCounters.get(key);
            if (counter == null) {
                counter = new AtomicInteger(0);
                localCounters.put(key, counter);
            }

            return counter.incrementAndGet() <= limit;
        }
    }
}