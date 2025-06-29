package org.aibles.cal_eos_fee.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;

@Data
public class SendTransactionResponse {
    
    @JsonProperty("transaction_id")
    private String transactionId;
    
    private Processed processed;
    
    @Data
    public static class Processed {
        private String id;
        
        @JsonProperty("block_num")
        private long blockNum;
        
        @JsonProperty("block_time")
        private String blockTime;

        @JsonProperty("producer_block_id")
        private String producerBlockId;
        
        private Receipt receipt;
        private long elapsed;
        
        @JsonProperty("except")
        private SendTransactionResponseException exception;
        
        @JsonProperty("net_usage")
        private long netUsage;
        
        private boolean scheduled;
        
        @JsonProperty("action_traces")
        private List<Object> actionTraces;
        
        @JsonProperty("account_ram_delta")
        private Object accountRamDelta;

        @JsonProperty("error_code")
        private Object errorCode;
    }
    
    @Data
    public static class Receipt {
        private String status;
        
        @JsonProperty("cpu_usage_us")
        private long cpuUsageUs;
        
        @JsonProperty("net_usage_words")
        private long netUsageWords;
    }
}