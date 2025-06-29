package org.aibles.cal_eos_fee.service;

import lombok.extern.slf4j.Slf4j;
import org.aibles.cal_eos_fee.dto.websocket.WebSocketMessage;
import org.aibles.cal_eos_fee.dto.websocket.WebSocketResponse;
import org.aibles.cal_eos_fee.websocket.handler.CalculateFeeHandler;
import org.aibles.cal_eos_fee.websocket.handler.PingHandler;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class MessageHandlerService {

    private final CalculateFeeHandler calculateFeeHandler;
    private final PingHandler pingHandler;

    public MessageHandlerService(CalculateFeeHandler calculateFeeHandler, PingHandler pingHandler) {
        this.calculateFeeHandler = calculateFeeHandler;
        this.pingHandler = pingHandler;
    }

    @Retryable(
            value = {Exception.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    public WebSocketResponse handleMessage(WebSocketMessage message, String sessionId) {
        log.debug("Handling message type {} for session {}", message.getType(), sessionId);

        try {
            return switch (message.getType()) {
                case CALCULATE_FEE -> calculateFeeHandler.handle(message, sessionId);
                case PING -> pingHandler.handle(message, sessionId);
                default -> {
                    log.warn("Unknown message type {} from session {}", message.getType(), sessionId);
                    yield WebSocketResponse.error("Unknown message type: " + message.getType(), message.getRequestId());
                }
            };
        } catch (Exception e) {
            log.error("Error handling message type {} for session {}: {}", message.getType(), sessionId, e.getMessage(), e);
            throw e;
        }
    }
}