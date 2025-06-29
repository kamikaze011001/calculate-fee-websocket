package org.aibles.cal_eos_fee.config;

import org.aibles.cal_eos_fee.websocket.SessionPrincipalHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final SessionPrincipalHandler sessionPrincipalHandler;
    
    @Value("${activemq.broker.host}")
    private String activeMqHost;
    
    @Value("${activemq.broker.port}")
    private int activeMqPort;
    
    @Value("${activemq.broker.username}")
    private String activeMqUsername;
    
    @Value("${activemq.broker.password}")
    private String activeMqPassword;

    public WebSocketConfig(SessionPrincipalHandler sessionPrincipalHandler) {
        this.sessionPrincipalHandler = sessionPrincipalHandler;
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Enable ActiveMQ STOMP broker relay for multi-instance support
        config.enableStompBrokerRelay("/topic", "/queue")
                .setRelayHost(activeMqHost)
                .setRelayPort(activeMqPort)
                .setClientLogin(activeMqUsername)
                .setClientPasscode(activeMqPassword)
                .setSystemLogin(activeMqUsername)
                .setSystemPasscode(activeMqPassword);
        
        config.setApplicationDestinationPrefixes("/app");
        config.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setHandshakeHandler(sessionPrincipalHandler)
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }
}