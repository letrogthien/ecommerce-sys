package com.chuadatten.wallet.controllers;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.chuadatten.wallet.responses.ApiResponse;
import com.chuadatten.wallet.service.PaymentService;
import com.chuadatten.wallet.vnpay.VnpayReturnDto;
import com.fasterxml.jackson.core.JsonProcessingException;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/wallet-service/payments")
public class PaymentController {
    private final PaymentService paymentService;

    
    @GetMapping("/vnpay_return")
    public ApiResponse<VnpayReturnDto> handleVnpayReturnDeposit(@ModelAttribute VnpayReturnDto vnpayReturnDto) throws InvalidKeyException, JsonProcessingException, NoSuchAlgorithmException  {
        
        return ApiResponse.<VnpayReturnDto>builder()
                .data(vnpayReturnDto)
                .message("ok")
                .build();

    }
    @GetMapping("/IPN")
    public ApiResponse<String> handleVnpayIpn(@ModelAttribute VnpayReturnDto vnpayReturnDto) throws InvalidKeyException, JsonProcessingException, NoSuchAlgorithmException  {
        paymentService.handleProviderCallback(vnpayReturnDto);

        return ApiResponse.<String>builder()
                .data("ok")
                .message("ok")
                .build();

    }


}
