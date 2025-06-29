package org.aibles.cal_eos_fee.dto.response;

import lombok.Data;
import java.util.List;

@Data
public class SendTransactionResponseException {
    
    private int code;
    private String name;
    private String message;
    private List<SendTransactionResponseExceptionStack> stack;
}