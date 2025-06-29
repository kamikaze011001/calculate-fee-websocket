package org.aibles.cal_eos_fee.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class SendTransactionResponseExceptionStack {
    
    private Context context;
    private String format;
    private Object data;
    
    @Data
    public static class Context {
        private String level;
        private String file;
        private int line;
        private String method;
        private String hostname;
        
        @JsonProperty("thread_name")
        private String threadName;
        
        private String timestamp;
    }
}