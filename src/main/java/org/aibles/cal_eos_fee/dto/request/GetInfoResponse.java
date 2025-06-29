package org.aibles.cal_eos_fee.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class GetInfoResponse {
    
    @JsonProperty("server_version")
    private String serverVersion;
    
    @JsonProperty("chain_id")
    private String chainId;
    
    @JsonProperty("head_block_num")
    private long headBlockNum;
    
    @JsonProperty("last_irreversible_block_num")
    private long lastIrreversibleBlockNum;
    
    @JsonProperty("last_irreversible_block_id")
    private String lastIrreversibleBlockId;
    
    @JsonProperty("head_block_id")
    private String headBlockId;
    
    @JsonProperty("head_block_time")
    private String headBlockTime;
    
    @JsonProperty("head_block_producer")
    private String headBlockProducer;
    
    @JsonProperty("virtual_block_cpu_limit")
    private long virtualBlockCpuLimit;
    
    @JsonProperty("virtual_block_net_limit")
    private long virtualBlockNetLimit;
    
    @JsonProperty("block_cpu_limit")
    private long blockCpuLimit;
    
    @JsonProperty("block_net_limit")
    private long blockNetLimit;
    
    @JsonProperty("server_version_string")
    private String serverVersionString;
    
    @JsonProperty("fork_db_head_block_num")
    private long forkDbHeadBlockNum;
    
    @JsonProperty("fork_db_head_block_id")
    private String forkDbHeadBlockId;
    
    @JsonProperty("server_full_version_string")
    private String serverFullVersionString;
    
    @JsonProperty("total_cpu_weight")
    private String totalCpuWeight;
    
    @JsonProperty("total_net_weight")
    private String totalNetWeight;
    
    @JsonProperty("earliest_available_block_num")
    private long earliestAvailableBlockNum;
    
    @JsonProperty("last_irreversible_block_time")
    private String lastIrreversibleBlockTime;
}