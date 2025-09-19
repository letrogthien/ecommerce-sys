package com.chuadatten.wallet.vnpay;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;
import lombok.Getter;


@Getter
@Builder
public class VnpayReturnDto {
    @JsonProperty("vnp_Amount")
    private String amount;
    
    @JsonProperty("vnp_BankCode")
    private String bankCode;
    
    @JsonProperty("vnp_BankTranNo")
    private String bankTranNo;
    
    @JsonProperty("vnp_CardType")
    private String cardType;
    
    @JsonProperty("vnp_OrderInfo")
    private String orderInfo;
    
    @JsonProperty("vnp_PayDate")
    private String payDate;
    
    @JsonProperty("vnp_ResponseCode")
    private String responseCode;
    
    @JsonProperty("vnp_TmnCode")
    private String tmnCode;
    
    @JsonProperty("vnp_TransactionNo")
    private String transactionNo;
    
    @JsonProperty("vnp_TxnRef")
    private String txnRef;
    
    @JsonProperty("vnp_SecureHash")
    private String secureHash;

    @JsonProperty("vnp_TransactionStatus")
    private String transactionStatus;


    public Map<String, String> toMap() {
    Map<String, String> map = new HashMap<>();
    if (this.getAmount() != null) map.put("vnp_Amount", this.getAmount());
    if (this.getBankCode() != null) map.put("vnp_BankCode", this.getBankCode());
    if (this.getBankTranNo() != null) map.put("vnp_BankTranNo", this.getBankTranNo());
    if (this.getCardType() != null) map.put("vnp_CardType", this.getCardType());
    if (this.getOrderInfo() != null) map.put("vnp_OrderInfo", this.getOrderInfo());
    if (this.getPayDate() != null) map.put("vnp_PayDate", this.getPayDate());
    if (this.getResponseCode() != null) map.put("vnp_ResponseCode", this.getResponseCode());
    if (this.getTmnCode() != null) map.put("vnp_TmnCode", this.getTmnCode());
    if (this.getTransactionNo() != null) map.put("vnp_TransactionNo", this.getTransactionNo());
    if (this.getTxnRef() != null) map.put("vnp_TxnRef", this.getTxnRef());
    if (this.getTransactionStatus() != null) map.put("vnp_TransactionStatus", this.getTransactionStatus());
    return map;
}
}