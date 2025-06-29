package org.aibles.cal_eos_fee.websocket.handler;

import lombok.extern.slf4j.Slf4j;
import org.aibles.cal_eos_fee.dto.websocket.MessageType;
import org.aibles.cal_eos_fee.dto.websocket.WebSocketMessage;
import org.aibles.cal_eos_fee.dto.websocket.WebSocketResponse;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;

@Component
@Slf4j
public class PingHandler {

    public WebSocketResponse handle(WebSocketMessage message, String sessionId) {
        log.debug("Processing PING request for session {}", sessionId);

        Map<String, Object> pongData = Map.of(
                "message", "pong",
                "timestamp", Instant.now().toString(),
                "sessionId", sessionId
        );

        log.debug("Sending PONG response to session {}", sessionId);
        return WebSocketResponse.success(MessageType.PING, pongData, message.getRequestId());
    }
}