package org.aibles.cal_eos_fee.dto.websocket;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class WebSocketMessage {
    @JsonProperty("type")
    private MessageType type;
    
    @JsonProperty("data")
    private Object data;
    
    @JsonProperty("requestId")
    private String requestId;
}