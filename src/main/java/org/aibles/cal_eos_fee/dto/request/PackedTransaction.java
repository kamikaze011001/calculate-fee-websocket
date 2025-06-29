package org.aibles.cal_eos_fee.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class PackedTransaction {

    private List<Signature> signatures;
    private int compression;
    @JsonProperty("packed_context_free_data")
    private String packedContextFreeData;
    @JsonProperty("packed_trx")
    private String packedTrx;
}
