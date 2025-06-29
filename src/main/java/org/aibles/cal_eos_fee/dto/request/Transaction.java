package org.aibles.cal_eos_fee.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
public class Transaction extends TransactionHeader {

    @JsonProperty("context_free_actions")
    private List<Action> contextFreeActions;
    private List<Action> actions;
    @JsonProperty("transaction_extensions")
    private List<TransactionExtension> transactionExtensions;
}
