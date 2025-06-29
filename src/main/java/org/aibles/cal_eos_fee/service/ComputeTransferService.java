package org.aibles.cal_eos_fee.service;

import lombok.RequiredArgsConstructor;
import org.aibles.cal_eos_fee.dto.request.*;
import org.aibles.cal_eos_fee.dto.response.SendTransactionResponse;
import org.aibles.cal_eos_fee.util.EOSEncoder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ComputeTransferService {

    @Value("${eos.account}")
    private String eosAccount;

    @Value("${eos.action.name}")
    private String transferAction;

    @Value("${eos.permission.name}")
    private String permissionName;

    private final EOSApiService eosApiService;

    public SendTransactionResponse calculateTransferFee(TransferData transferData) throws IOException {

        GetInfoResponse getInfoResponse = eosApiService.getInfo();

        Action action = new Action();
        action.setAccount(eosAccount);
        action.setName(transferAction);

        PermissionLevel permissionLevel = new PermissionLevel();
        permissionLevel.setActor(transferData.getFrom());
        permissionLevel.setPermission(permissionName);

        List<PermissionLevel> permissions = new ArrayList<>();
        permissions.add(permissionLevel);

        action.setAuthorization(permissions);

        String encodeTransferData = EOSEncoder.encodeTransferData(transferData);

        action.setData(encodeTransferData);

        TransactionHeader transactionHeader = TransactionHeader.fromGetInfoResponse(getInfoResponse);


        Transaction transaction = new Transaction();
        transaction.setExpiration(transactionHeader.getExpiration());
        transaction.setRefBlockNum(transactionHeader.getRefBlockNum());
        transaction.setRefBlockPrefix(transactionHeader.getRefBlockPrefix());
        transaction.setMaxCpuUsageMs(transactionHeader.getMaxCpuUsageMs());
        transaction.setMaxNetUsageWords(transactionHeader.getMaxNetUsageWords());
        transaction.setDelaySec(transactionHeader.getDelaySec());

        List<Action> actions = new ArrayList<>();
        actions.add(action);
        transaction.setActions(actions);

        String encodeTransaction = EOSEncoder.encodeTransaction(transaction);

        PackedTransaction packedTransaction = new PackedTransaction();

        packedTransaction.setCompression(0);
        packedTransaction.setPackedTrx(encodeTransaction);
        packedTransaction.setSignatures(new ArrayList<>());
        packedTransaction.setPackedContextFreeData("");

        ComputeTransactionRequest  computeTransactionRequest = new ComputeTransactionRequest();
        computeTransactionRequest.setTransaction(packedTransaction);

        return eosApiService.computeTransaction(computeTransactionRequest);
    }
}
