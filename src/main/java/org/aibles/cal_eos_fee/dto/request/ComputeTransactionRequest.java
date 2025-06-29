package org.aibles.cal_eos_fee.dto.request;

import lombok.Data;

@Data
public class ComputeTransactionRequest {

    private PackedTransaction transaction;
}
