package org.aibles.cal_eos_fee.dto.request;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.Pattern;

@Data
public class TransferData {
    
    @NotBlank(message = "From account is required")
    @Pattern(regexp = "^[a-z1-5.]{1,12}$", message = "From account must be a valid EOS account name (1-12 characters, lowercase letters, digits 1-5, and dots)")
    private String from;
    
    @NotBlank(message = "To account is required")
    @Pattern(regexp = "^[a-z1-5.]{1,12}$", message = "To account must be a valid EOS account name (1-12 characters, lowercase letters, digits 1-5, and dots)")
    private String to;
    
    @NotNull(message = "Quantity is required")
    @Positive(message = "Quantity must be positive")
    @DecimalMax(value = "999999999.9999", message = "Quantity is too large")
    private Double quantity;
    
    private String memo;
}
