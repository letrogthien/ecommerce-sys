package com.chuadatten.user.services;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import com.chuadatten.user.dto.UserVerificationDto;
import com.chuadatten.user.requests.UserVerificationRequest;
import com.chuadatten.user.responses.ApiResponse;

public interface KycService {
    /**
     * Submit verification request
     * 
     * @param UserVerificationRequest
     * @param UserId
     * @return ApiResponse<UserVerificationDto>
     */
    ApiResponse<UserVerificationDto> submitVerificationRequest(UserVerificationRequest userVerificationRequest, UUID userId) throws IOException, InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException;


    /**
     * Get verification request
     * 
     * @param UserId
     * @return ApiResponse<UserVerificationDto>
     */
    ApiResponse<UserVerificationDto> getVerificationRequest(UUID userId);
}
