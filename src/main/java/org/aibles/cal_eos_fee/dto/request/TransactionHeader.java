package org.aibles.cal_eos_fee.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.time.Instant;
import java.time.format.DateTimeFormatter;

@Data
public class TransactionHeader {
    
    private long expiration;
    
    @JsonProperty("ref_block_num")
    private int refBlockNum;
    
    @JsonProperty("ref_block_prefix")
    private long refBlockPrefix;
    
    @JsonProperty("max_net_usage_words")
    private int maxNetUsageWords;
    
    @JsonProperty("max_cpu_usage_ms")
    private int maxCpuUsageMs;
    
    @JsonProperty("delay_sec")
    private int delaySec;
    
    public static TransactionHeader fromGetInfoResponse(GetInfoResponse getInfoResponse) {
        TransactionHeader header = new TransactionHeader();
        
        // Calculate expiration: headBlockTime + 120 seconds
        // EOS timestamps don't include timezone, so append 'Z' for UTC
        String timeString = getInfoResponse.getHeadBlockTime();
        if (!timeString.endsWith("Z") && !timeString.contains("+")) {
            timeString += "Z";
        }
        Instant blockTime = Instant.from(DateTimeFormatter.ISO_INSTANT.parse(timeString));
        header.expiration = blockTime.plusSeconds(120).getEpochSecond();
        
        // Set refBlockNum from last_irreversible_block_num (take lower 16 bits)
        header.refBlockNum = (int) (getInfoResponse.getLastIrreversibleBlockNum() & 0xFFFF);
        
        // Set refBlockPrefix from last_irreversible_block_id (extract bytes 8-12, like TypeScript)
        String blockId = getInfoResponse.getLastIrreversibleBlockId();
        // Extract 4 bytes starting at byte position 8 (hex positions 16-23)
        String prefixHex = blockId.substring(16, 24); // 4 bytes = 8 hex chars
        // Convert hex to bytes and parse as little-endian 32-bit unsigned integer
        byte[] prefixBytes = hexStringToBytes(prefixHex);
        ByteBuffer buffer = ByteBuffer.wrap(prefixBytes).order(ByteOrder.LITTLE_ENDIAN);
        header.refBlockPrefix = buffer.getInt() & 0xFFFFFFFFL; // Convert to unsigned long
        
        // Set other fields to 0
        header.maxNetUsageWords = 0;
        header.maxCpuUsageMs = 0;
        header.delaySec = 0;
        
        return header;
    }
    
    private static byte[] hexStringToBytes(String hex) {
        if (hex == null || hex.isEmpty()) {
            return new byte[0];
        }
        
        // Remove any whitespace or prefix
        hex = hex.replaceAll("\\s", "").replaceAll("^0x", "");
        
        if (hex.length() % 2 != 0) {
            throw new IllegalArgumentException("Hex string must have even length");
        }
        
        byte[] bytes = new byte[hex.length() / 2];
        for (int i = 0; i < hex.length(); i += 2) {
            int firstDigit = Character.digit(hex.charAt(i), 16);
            int secondDigit = Character.digit(hex.charAt(i + 1), 16);
            
            if (firstDigit == -1 || secondDigit == -1) {
                throw new IllegalArgumentException("Invalid hex character in string: " + hex);
            }
            
            bytes[i / 2] = (byte) ((firstDigit << 4) + secondDigit);
        }
        
        return bytes;
    }
}
