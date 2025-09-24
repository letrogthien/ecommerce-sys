package com.chuadatten.user.services.impl;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;
import java.util.UUID;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import org.springframework.stereotype.Service;

import com.chuadatten.user.dto.UserVerificationDto;
import com.chuadatten.user.entity.UserInf;
import com.chuadatten.user.entity.UserVerification;
import com.chuadatten.user.exceptions.CustomException;
import com.chuadatten.user.exceptions.ErrorCode;
import com.chuadatten.user.file.FileBase64;
import com.chuadatten.user.mapper.UserKycMapper;
import com.chuadatten.user.repository.UserInfRepository;
import com.chuadatten.user.repository.UserVerificationRepository;
import com.chuadatten.user.requests.UserVerificationRequest;
import com.chuadatten.user.responses.ApiResponse;
import com.chuadatten.user.services.KycService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class KycServiceImpl implements KycService {
    private final UserVerificationRepository userVerificationRepository;
    private final UserKycMapper userKycMapper;
    private final UserInfRepository userInfRepository;
    private final FileBase64 fileBase64;

    @Override
    public ApiResponse<UserVerificationDto> getVerificationRequest(UUID id) {
        return ApiResponse.<UserVerificationDto>builder()
                .data(userKycMapper.toDto(userVerificationRepository.findByUserId(id).orElseThrow(
                        () -> new CustomException(ErrorCode.VERIFICATION_NOT_EXIST))))
                .build();
    }

    @Override
    public ApiResponse<UserVerificationDto> submitVerificationRequest(
            UserVerificationRequest userVerificationRequest,
            UUID userId) throws IOException, InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {
        String docBack = fileBase64.fileToBase64(userVerificationRequest.getDocumentBackUrl());
        String docFront = fileBase64.fileToBase64(userVerificationRequest.getDocumentFrontUrl());
        String docSelfie = fileBase64.fileToBase64(userVerificationRequest.getFaceIdFrontUrl());

        String docBackEncrypted = fileBase64.encodeBase64(docBack);
        String docFrontEncrypted = fileBase64.encodeBase64(docFront);
        String docSelfieEncrypted = fileBase64.encodeBase64(docSelfie);

        UserInf u = userInfRepository.findById(userId).orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        Optional<UserVerification> lastestVersionOpt = userVerificationRepository
                .findTopByUserIdOrderByVersionDesc(userId);

        UserVerification newVerification = UserVerification.builder()
        .documentBackUrl(docBackEncrypted)
        .documentFrontUrl(docFrontEncrypted)
        .faceIdFrontUrl(docSelfieEncrypted)
        .user(u)
        .build();

        lastestVersionOpt.ifPresentOrElse(
                ver -> newVerification.setVersion(ver.getVersion() + 1),
                () -> newVerification.setVersion(1));

        userVerificationRepository.save(newVerification);

        return ApiResponse.<UserVerificationDto>builder()
                .data(userKycMapper.toDto(newVerification))
                .build();
    }

}
