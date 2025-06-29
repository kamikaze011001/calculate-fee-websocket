package org.aibles.cal_eos_fee.websocket;

import org.springframework.http.server.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

import java.security.Principal;
import java.util.Map;
import java.util.UUID;

@Component
public class SessionPrincipalHandler extends DefaultHandshakeHandler {

    @Override
    protected Principal determineUser(ServerHttpRequest request, WebSocketHandler wsHandler, Map<String, Object> attributes) {
        String sessionId = UUID.randomUUID().toString();
        return new SessionPrincipal(sessionId);
    }

    public static class SessionPrincipal implements Principal {
        private final String sessionId;

        public SessionPrincipal(String sessionId) {
            this.sessionId = sessionId;
        }

        @Override
        public String getName() {
            return sessionId;
        }
    }
}