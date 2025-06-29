package org.aibles.cal_eos_fee.websocket;

import lombok.extern.slf4j.Slf4j;
import org.aibles.cal_eos_fee.dto.websocket.WebSocketMessage;
import org.aibles.cal_eos_fee.dto.websocket.WebSocketResponse;
import org.aibles.cal_eos_fee.service.MessageHandlerService;
import org.aibles.cal_eos_fee.service.RateLimitService;
import org.aibles.cal_eos_fee.service.SessionManagementService;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
@Slf4j
public class WebSocketController {

    private final MessageHandlerService messageHandlerService;
    private final RateLimitService rateLimitService;
    private final SessionManagementService sessionManagementService;

    public WebSocketController(MessageHandlerService messageHandlerService,
                              RateLimitService rateLimitService,
                              SessionManagementService sessionManagementService) {
        this.messageHandlerService = messageHandlerService;
        this.rateLimitService = rateLimitService;
        this.sessionManagementService = sessionManagementService;
    }

    @MessageMapping("/message")
    @SendToUser("/queue/response")
    public WebSocketResponse handleMessage(WebSocketMessage message, SimpMessageHeaderAccessor headerAccessor, Principal principal) {
        if (principal == null) {
            log.warn("Received message without session principal");
            return WebSocketResponse.error("Invalid session", message.getRequestId());
        }

        String sessionId = principal.getName();
        log.debug("Processing message type {} for session {}", message.getType(), sessionId);

        try {
            sessionManagementService.updateSessionActivity(sessionId);

            if (!rateLimitService.isAllowed(sessionId, message.getType())) {
                log.warn("Rate limit exceeded for session {} and message type {}", sessionId, message.getType());
                return WebSocketResponse.error("Rate limit exceeded", message.getRequestId());
            }

            return messageHandlerService.handleMessage(message, sessionId);

        } catch (Exception e) {
            log.error("Error processing message for session {}: {}", sessionId, e.getMessage(), e);
            return WebSocketResponse.error("Internal server error", message.getRequestId());
        }
    }
}