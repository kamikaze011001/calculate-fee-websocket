package org.aibles.cal_eos_fee.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class SessionManagementService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ConcurrentHashMap<String, Instant> localSessions = new ConcurrentHashMap<>();
    private final String REDIS_SESSION_KEY = "session:";
    
    @Value("${websocket.session.timeout-minutes}")
    private int sessionTimeoutMinutes;

    public SessionManagementService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void updateSessionActivity(String sessionId) {
        try {
            String key = REDIS_SESSION_KEY + sessionId;
            redisTemplate.opsForValue().set(key, Instant.now().toString());
            redisTemplate.expire(key, Duration.ofMinutes(sessionTimeoutMinutes));
            log.debug("Session {} activity updated in Redis", sessionId);
        } catch (Exception e) {
            log.warn("Failed to update session {} in Redis, using local fallback", sessionId, e);
            localSessions.put(sessionId, Instant.now());
        }
    }
}