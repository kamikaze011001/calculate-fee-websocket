package org.aibles.cal_eos_fee.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;
import org.aibles.cal_eos_fee.dto.request.ComputeTransactionRequest;
import org.aibles.cal_eos_fee.dto.request.GetInfoResponse;
import org.aibles.cal_eos_fee.dto.response.SendTransactionResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class EOSApiService {
    
    private final OkHttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final String eosNodeUrl;
    
    public EOSApiService(@Value("${eos.node.url}") String eosNodeUrl) {
        this.httpClient = new OkHttpClient();
        this.objectMapper = new ObjectMapper();
        this.eosNodeUrl = eosNodeUrl;
    }
    
    public GetInfoResponse getInfo() throws IOException {
        String url = eosNodeUrl + "/v1/chain/get_info";
        
        Request request = new Request.Builder()
                .url(url)
                .build();
        
        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Failed to get chain info: " + response);
            }
            
            String responseBody = response.body().string();
            return objectMapper.readValue(responseBody, GetInfoResponse.class);
        }
    }
    
    public SendTransactionResponse computeTransaction(ComputeTransactionRequest request) throws IOException {
        String url = eosNodeUrl + "/v1/chain/compute_transaction";
        
        String requestBody = objectMapper.writeValueAsString(request);
        RequestBody body = RequestBody.create(requestBody, MediaType.get("application/json; charset=utf-8"));
        
        Request httpRequest = new Request.Builder()
                .url(url)
                .post(body)
                .build();
        
        try (Response response = httpClient.newCall(httpRequest).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Failed to compute transaction: " + response.body().string());
            }
            
            return objectMapper.readValue(response.body().string(), SendTransactionResponse.class);
        }
    }
}