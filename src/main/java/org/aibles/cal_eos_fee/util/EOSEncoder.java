package org.aibles.cal_eos_fee.util;

import org.aibles.cal_eos_fee.dto.request.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;

public class EOSEncoder {

    private EOSEncoder() {}
    
    public static String encodeTransferData(TransferData transferData) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            
            // Encode 'from' account (8 bytes)
            byte[] fromBytes = encodeAccountName(transferData.getFrom());
            baos.write(fromBytes);
            
            // Encode 'to' account (8 bytes)
            byte[] toBytes = encodeAccountName(transferData.getTo());
            baos.write(toBytes);
            
            // Encode quantity (16 bytes) - convert double to EOS asset format
            byte[] assetBytes = encodeAsset(transferData.getQuantity());
            baos.write(assetBytes);
            
            // Encode memo (variable length with length prefix)
            byte[] memoBytes = encodeMemo(transferData.getMemo());
            baos.write(memoBytes);
            
            return toHexString(baos.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException("Error encoding transfer data", e);
        }
    }
    
    public static String encodeTransaction(Transaction transaction) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            
            // Encode transaction header fields (inherited from TransactionHeader)
            // Expiration (4 bytes as uint32)
            baos.write(ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt((int) transaction.getExpiration()).array());
            
            // Ref block num (2 bytes as uint16)
            baos.write(ByteBuffer.allocate(2).order(ByteOrder.LITTLE_ENDIAN).putShort((short) transaction.getRefBlockNum()).array());
            
            // Ref block prefix (4 bytes as uint32)
            baos.write(ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt((int) transaction.getRefBlockPrefix()).array());
            
            // Max net usage words (varint)
            writeVarint(baos, transaction.getMaxNetUsageWords());
            
            // Max cpu usage ms (varint)
            writeVarint(baos, transaction.getMaxCpuUsageMs());
            
            // Delay sec (varint)
            writeVarint(baos, transaction.getDelaySec());
            
            // Context free actions (varint length + array)
            if (transaction.getContextFreeActions() != null) {
                writeVarint(baos, transaction.getContextFreeActions().size());
                for (Action action : transaction.getContextFreeActions()) {
                    byte[] actionBytes = encodeAction(action);
                    baos.write(actionBytes);
                }
            } else {
                writeVarint(baos, 0);
            }
            
            // Actions (varint length + array)
            if (transaction.getActions() != null) {
                writeVarint(baos, transaction.getActions().size());
                for (Action action : transaction.getActions()) {
                    byte[] actionBytes = encodeAction(action);
                    baos.write(actionBytes);
                }
            } else {
                writeVarint(baos, 0);
            }
            
            // Transaction extensions (varint length + array)
            if (transaction.getTransactionExtensions() != null) {
                writeVarint(baos, transaction.getTransactionExtensions().size());
                for (TransactionExtension extension : transaction.getTransactionExtensions()) {
                    byte[] extensionBytes = encodeTransactionExtension(extension);
                    baos.write(extensionBytes);
                }
            } else {
                writeVarint(baos, 0);
            }
            
            return toHexString(baos.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException("Error encoding transaction", e);
        }
    }
    
    private static String toHexString(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
    
    private static byte[] encodeAccountName(String accountName) {
        // EOS account names are encoded as 64-bit integers using base32 encoding
        long encoded = 0;
        int i = 0;
        for (; i < accountName.length() && i < 12; i++) {
            char c = accountName.charAt(i);
            long charValue;
            if (c >= 'a' && c <= 'z') {
                charValue = c - 'a' + 6;
            } else if (c >= '1' && c <= '5') {
                charValue = c - '1' + 1;
            } else if (c == '.') {
                charValue = 0;
            } else {
                throw new IllegalArgumentException("Invalid character in account name: " + c);
            }
            
            if (i < 12) {
                encoded |= (charValue & 0x1F) << (64 - 5 * (i + 1));
            }
        }
        
        return ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN).putLong(encoded).array();
    }
    
    private static byte[] encodeAsset(Double quantityDouble) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            
            // Convert double to BigDecimal for precise decimal handling
            BigDecimal quantity = BigDecimal.valueOf(quantityDouble);
            String symbolPart = "EOS"; // Hardcoded as requested
            
            // EOS uses 4 decimal places, so multiply by 10000 to get the integer representation
            long quantityInt = quantity.multiply(BigDecimal.valueOf(10000)).longValue();
            
            // Encode amount (8 bytes, little-endian)
            byte[] amountBytes = ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN).putLong(quantityInt).array();
            baos.write(amountBytes);
            
            // Encode precision (1 byte) - EOS has 4 decimal places
            baos.write(0x04);
            
            // Encode symbol (7 bytes) - "EOS" + padding
            byte[] symbolBytes = new byte[7];
            byte[] symbolName = symbolPart.getBytes(StandardCharsets.UTF_8);
            System.arraycopy(symbolName, 0, symbolBytes, 0, Math.min(symbolName.length, 7));
            baos.write(symbolBytes);
            
            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Error encoding EOS asset", e);
        }
    }
    
    private static byte[] encodeMemo(String memo) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        
        if (memo == null) {
            memo = "";
        }
        
        byte[] memoBytes = memo.getBytes(StandardCharsets.UTF_8);
        
        // Write length as varint (for simplicity, using single byte for short memos)
        if (memoBytes.length < 128) {
            baos.write(memoBytes.length);
        } else {
            // For longer memos, implement proper varint encoding
            writeVarint(baos, memoBytes.length);
        }
        
        // Write memo content
        baos.write(memoBytes);
        
        return baos.toByteArray();
    }
    
    private static void writeVarint(ByteArrayOutputStream baos, int value) {
        while (value >= 0x80) {
            baos.write((value & 0x7F) | 0x80);
            value >>>= 7;
        }
        baos.write(value & 0x7F);
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
    
    private static byte[] encodePermissionLevel(PermissionLevel permissionLevel) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        
        // Encode actor account name (8 bytes)
        byte[] actorBytes = encodeAccountName(permissionLevel.getActor());
        baos.write(actorBytes);
        
        // Encode permission name (8 bytes)
        byte[] permissionBytes = encodeAccountName(permissionLevel.getPermission());
        baos.write(permissionBytes);
        
        return baos.toByteArray();
    }
    
    private static byte[] encodeAction(Action action) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        
        // Encode account name (8 bytes)
        byte[] accountBytes = encodeAccountName(action.getAccount());
        baos.write(accountBytes);
        
        // Encode action name (8 bytes)
        byte[] nameBytes = encodeAccountName(action.getName());
        baos.write(nameBytes);
        
        // Encode authorization list (varint length + encoded permission levels)
        if (action.getAuthorization() != null) {
            writeVarint(baos, action.getAuthorization().size());
            for (PermissionLevel permLevel : action.getAuthorization()) {
                byte[] permBytes = encodePermissionLevel(permLevel);
                baos.write(permBytes);
            }
        } else {
            writeVarint(baos, 0);
        }
        
        // Encode data (convert hex string to bytes with length prefix)
        byte[] dataBytes = hexStringToBytes(action.getData());
        writeVarint(baos, dataBytes.length);
        baos.write(dataBytes);
        
        return baos.toByteArray();
    }
    
    private static byte[] encodeTransactionExtension(TransactionExtension extension) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        
        // Encode type (varint)
        writeVarint(baos, extension.getType());
        
        // Encode data (convert hex string to bytes with length prefix)
        byte[] dataBytes = hexStringToBytes(extension.getData());
        writeVarint(baos, dataBytes.length);
        baos.write(dataBytes);
        
        return baos.toByteArray();
    }
}