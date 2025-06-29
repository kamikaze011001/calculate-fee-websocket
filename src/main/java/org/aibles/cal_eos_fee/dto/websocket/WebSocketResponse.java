package org.aibles.cal_eos_fee.dto.websocket;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WebSocketResponse {
    @JsonProperty("type")
    private MessageType type;
    
    @JsonProperty("data")
    private Object data;
    
    @JsonProperty("requestId")
    private String requestId;
    
    @JsonProperty("success")
    private boolean success;
    
    @JsonProperty("error")
    private String error;
    
    public static WebSocketResponse success(MessageType type, Object data, String requestId) {
        return new WebSocketResponse(type, data, requestId, true, null);
    }
    
    public static WebSocketResponse error(String error, String requestId) {
        return new WebSocketResponse(MessageType.ERROR, null, requestId, false, error);
    }
}