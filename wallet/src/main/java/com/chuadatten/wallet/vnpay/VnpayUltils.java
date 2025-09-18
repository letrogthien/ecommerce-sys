package com.chuadatten.wallet.vnpay;

import java.math.BigInteger;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.stereotype.Service;

import com.chuadatten.wallet.common.PaymentType;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class VnpayUltils {
    private final PayConfig payConfig;

    public String genTxnRef(PaymentType type, String paymentId) {
        return type.name() + "-" + paymentId + "-" + System.currentTimeMillis();
    }

    public String payUrl(String ip, BigInteger total, String detail, String ref)
            throws InvalidKeyException, NoSuchAlgorithmException {
        var params = buildParams(ip, total, detail, ref);
        return buildUrl(params);

    }

    private String buildUrl(Map<String, String> params) throws InvalidKeyException, NoSuchAlgorithmException {
        // Validate required config values
        if (payConfig.getVnpHashSecret() == null) {
            throw new IllegalStateException("VnpHashSecret is null in PayConfig");
        }
        if (payConfig.getVnpPayUrl() == null) {
            throw new IllegalStateException("VnpPayUrl is null in PayConfig");
        }

        List<String> fieldNames = new ArrayList<>(params.keySet());
        Collections.sort(fieldNames);

        Iterator<?> itr = fieldNames.iterator();
        StringBuilder sb = new StringBuilder();
        while (itr.hasNext()) {
            String fieldName = (String) itr.next();
            String fieldValue = params.get(fieldName);
            if ((fieldValue != null) && (!fieldValue.isEmpty())) {
                sb.append(fieldName);
                sb.append("=");
                sb.append(URLEncoder.encode(fieldValue, StandardCharsets.UTF_8));
            }
            if (itr.hasNext()) {
                sb.append("&");
            }
        }

        String secureHash = hmacSHA512(payConfig.getVnpHashSecret(), sb.toString());
        return payConfig.getVnpPayUrl() + "?" + sb.toString() + "&vnp_SecureHash=" + secureHash;
    }

    private boolean validateIPN(Map<String, String> params) throws NoSuchAlgorithmException, InvalidKeyException {
        String vnpSecureHash = params.get("vnp_SecureHash");

        params.remove("vnp_SecureHash");
        params.remove("vnp_SecureHashType");

        List<String> fieldNames = new ArrayList<>(params.keySet());
        Collections.sort(fieldNames);

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < fieldNames.size(); i++) {
            String key = fieldNames.get(i);
            String value = params.get(key);
            if (value != null && !value.isEmpty()) {
                sb.append(key).append("=").append(value);
                if (i < fieldNames.size() - 1) {
                    sb.append("&");
                }
            }
        }

        String checkHash = hmacSHA512(payConfig.getVnpHashSecret(), sb.toString());

        return checkHash.equalsIgnoreCase(vnpSecureHash);
    }

    public void checkCallBack(VnpayReturnDto vnpayReturnDto) throws InvalidKeyException, NoSuchAlgorithmException {
        String responseCode = vnpayReturnDto.getResponseCode();
        if (!"00".equals(responseCode)) {
            throw new PaymentException(PaymentErrorCode.valueOf(responseCode));
        }
        if (!validateIPN(vnpayReturnDto.toMap())) {
            throw new PaymentException(PaymentErrorCode.UNKNOWN);
        }
    }

    private String hmacSHA512(final String key, final String data)
            throws InvalidKeyException, NoSuchAlgorithmException {

        if (key == null || data == null) {
            throw new NullPointerException("Key or data is null. Key: " + key + ", Data: " + data);
        }

        String hashType = payConfig.getVnpHashType();
        if (hashType == null) {
            throw new NullPointerException("VnpHashType is null in PayConfig");
        }

        final Mac hmac512 = Mac.getInstance(hashType);
        byte[] hmacKeyBytes = key.getBytes();
        final SecretKeySpec secretKey = new SecretKeySpec(hmacKeyBytes, hashType);
        hmac512.init(secretKey);
        byte[] dataBytes = data.getBytes(StandardCharsets.UTF_8);
        byte[] result = hmac512.doFinal(dataBytes);
        StringBuilder sb = new StringBuilder(2 * result.length);
        for (byte b : result) {
            sb.append(String.format("%02x", b & 0xff));
        }
        return sb.toString();

    }

    private Map<String, String> buildParams(String ip, BigInteger total, String detail, String ref) {
        String vnpVersion = payConfig.getVnpVersion();
        String vnpCommand = payConfig.getVnpCommand();
        String vnpOrderInfo = detail.trim();
        String orderType = "other";
        String vnpTxnRef = ref+System.currentTimeMillis();
        String vnpIpAddr = ip;
        String vnpTmnCode = payConfig.getVnpTmnCode();
        int amount = total.multiply(BigInteger.valueOf(100)).intValue();
        Calendar expire = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
        Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        expire.add(Calendar.MINUTE, 15); // VD: cho phép thanh toán trong 15 phút
        String vnpExpireDate = formatter.format(expire.getTime());
        
        Map<String, String> vnpParams = new HashMap<>();
        vnpParams.put("vnp_Version", vnpVersion);
        vnpParams.put("vnp_Command", vnpCommand);
        vnpParams.put("vnp_TmnCode", vnpTmnCode);
        vnpParams.put("vnp_Amount", String.valueOf(amount));
        vnpParams.put("vnp_CurrCode", "VND");
        vnpParams.put("vnp_ExpireDate", vnpExpireDate);
        // String bankcode = payConfig.getVnpBankCode();
        // if (bankcode != null && !bankcode.isEmpty()) {
        // vnpParams.put("vnp_BankCode", bankcode);
        // }
        vnpParams.put("vnp_TxnRef", vnpTxnRef);
        vnpParams.put("vnp_OrderInfo", vnpOrderInfo);
        vnpParams.put("vnp_OrderType", orderType);

        vnpParams.put("vnp_Locale", "vn");
        vnpParams.put("vnp_ReturnUrl", payConfig.getVnpReturnUrl());
        vnpParams.put("vnp_IpAddr", vnpIpAddr);
        
        String vnpCreateDate = formatter.format(cld.getTime());
        vnpParams.put("vnp_CreateDate", vnpCreateDate);
        return vnpParams;

    }

}
