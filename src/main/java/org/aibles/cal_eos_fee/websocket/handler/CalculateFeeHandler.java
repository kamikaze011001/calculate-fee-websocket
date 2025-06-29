package org.aibles.cal_eos_fee.websocket.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.aibles.cal_eos_fee.dto.request.TransferData;
import org.aibles.cal_eos_fee.dto.response.SendTransactionResponse;
import org.aibles.cal_eos_fee.dto.response.SendTransactionResponseException;
import org.aibles.cal_eos_fee.dto.response.SendTransactionResponseExceptionStack;
import org.aibles.cal_eos_fee.dto.websocket.MessageType;
import org.aibles.cal_eos_fee.dto.websocket.WebSocketMessage;
import org.aibles.cal_eos_fee.dto.websocket.WebSocketResponse;
import org.aibles.cal_eos_fee.service.ComputeTransferService;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@Slf4j
public class CalculateFeeHandler {

    private final ComputeTransferService computeTransferService;
    private final ObjectMapper objectMapper;

    public CalculateFeeHandler(ComputeTransferService computeTransferService, ObjectMapper objectMapper) {
        this.computeTransferService = computeTransferService;
        this.objectMapper = objectMapper;
    }

    public WebSocketResponse handle(WebSocketMessage message, String sessionId) {
        log.debug("Processing CALCULATE_FEE request for session {}", sessionId);

        try {
            if (message.getData() == null) {
                log.warn("Missing transfer data in CALCULATE_FEE request from session {}", sessionId);
                return WebSocketResponse.error("Missing transfer data", message.getRequestId());
            }

            TransferData transferData = objectMapper.convertValue(message.getData(), TransferData.class);
            
            if (transferData.getFrom() == null || transferData.getTo() == null || transferData.getQuantity() == null) {
                log.warn("Invalid transfer data in CALCULATE_FEE request from session {}: from={}, to={}, quantity={}", 
                        sessionId, transferData.getFrom(), transferData.getTo(), transferData.getQuantity());
                return WebSocketResponse.error("Invalid transfer data: from, to, and quantity are required", message.getRequestId());
            }

            log.debug("Calculating fee for transfer: {} -> {} amount {} for session {}", 
                    transferData.getFrom(), transferData.getTo(), transferData.getQuantity(), sessionId);

            SendTransactionResponse feeResult = computeTransferService.calculateTransferFee(transferData);
            
            // Check if the transaction was successful or failed
            if (feeResult.getProcessed() != null) {
                SendTransactionResponse.Processed processed = feeResult.getProcessed();
                
                // Check if transaction failed (has exception and no receipt)
                if (processed.getException() != null && processed.getReceipt() == null) {
                    String errorMessage = buildErrorMessage(processed.getException());
                    log.warn("EOS transaction failed for session {}: {}", sessionId, errorMessage);
                    return WebSocketResponse.error(errorMessage, message.getRequestId());
                }
                
                // Transaction was successful - has receipt
                if (processed.getReceipt() != null) {
                    log.debug("Fee calculation completed successfully for session {}", sessionId);
                    return WebSocketResponse.success(MessageType.CALCULATE_FEE, feeResult, message.getRequestId());
                }
            }
            
            // Fallback case - unexpected response structure
            log.warn("Unexpected EOS response structure for session {}", sessionId);
            return WebSocketResponse.success(MessageType.CALCULATE_FEE, feeResult, message.getRequestId());

        } catch (IllegalArgumentException e) {
            log.warn("Invalid request data for CALCULATE_FEE from session {}: {}", sessionId, e.getMessage());
            return WebSocketResponse.error("Invalid request data: " + e.getMessage(), message.getRequestId());
        } catch (Exception e) {
            log.error("Error calculating fee for session {}: {}", sessionId, e.getMessage(), e);
            return WebSocketResponse.error("Failed to calculate fee: " + e.getMessage(), message.getRequestId());
        }
    }

    /**
     * Build a human-readable error message from EOS exception
     */
    private String buildErrorMessage(SendTransactionResponseException exception) {
        if (exception == null) {
            return "Unknown EOS error";
        }

        StringBuilder errorMsg = new StringBuilder();
        
        // Add main error message
        if (exception.getMessage() != null) {
            errorMsg.append(exception.getMessage());
        } else if (exception.getName() != null) {
            errorMsg.append(exception.getName());
        } else {
            errorMsg.append("EOS Transaction Error");
        }

        // Try to extract the specific error reason from the stack
        if (exception.getStack() != null && !exception.getStack().isEmpty()) {
            for (SendTransactionResponseExceptionStack stackItem : exception.getStack()) {
                if (stackItem.getData() instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> dataMap = (Map<String, Object>) stackItem.getData();
                    if (dataMap.containsKey("s")) {
                        String specificError = dataMap.get("s").toString();
                        errorMsg.append(": ").append(specificError);
                        break;
                    }
                }
            }
        }

        // Add error code for reference
        if (exception.getCode() != 0) {
            errorMsg.append(" (Code: ").append(exception.getCode()).append(")");
        }

        return errorMsg.toString();
    }
}