package com.chuadatten.user.services.impl;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.chuadatten.user.dto.BillingAddressDto;
import com.chuadatten.user.dto.PreferenceDto;
import com.chuadatten.user.dto.UserInfDto;
import com.chuadatten.user.entity.BillingAddress;
import com.chuadatten.user.entity.Preference;
import com.chuadatten.user.entity.UserInf;
import com.chuadatten.user.exceptions.CustomException;
import com.chuadatten.user.exceptions.ErrorCode;
import com.chuadatten.user.file.FileStorageService;
import com.chuadatten.user.mapper.BillingAddressMapper;
import com.chuadatten.user.mapper.PreferenceMapper;
import com.chuadatten.user.mapper.UserInforMapper;
import com.chuadatten.user.repository.BillingAddressRepository;
import com.chuadatten.user.repository.PreferenceRepository;
import com.chuadatten.user.repository.UserInfRepository;
import com.chuadatten.user.requests.BillingAddressRequest;
import com.chuadatten.user.requests.UpdatePreferenceRequest;
import com.chuadatten.user.requests.UpdateUserRequest;
import com.chuadatten.user.responses.ApiResponse;
import com.chuadatten.user.services.UserInforService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserInforServiceImpl implements UserInforService {
    private final UserInfRepository userInfRepository;
    private final UserInforMapper userInforMapper;
    private final FileStorageService fileStorageService;
    private final PreferenceRepository preferenceRepository;
    private final PreferenceMapper preferenceMapper;
    private final BillingAddressMapper billingAddressMapper;
    private final BillingAddressRepository billingAddressRepository;

    @Override
    public ApiResponse<UserInfDto> getUserById(UUID userId) {
        return ApiResponse.<UserInfDto>builder()
                .data(userInforMapper.toDto(userInfRepository.findById(userId).orElse(null)))
                .build();
    }

    @Override
    public ApiResponse<UserInfDto> updateUser(UUID userId, UpdateUserRequest updateUserRequest) {
        UserInf user = userInfRepository.findById(userId).orElseThrow(
                () -> new CustomException(ErrorCode.USER_NOT_FOUND));
        if (updateUserRequest == null) {
            throw new CustomException(ErrorCode.INVALID_REQUEST);
        }
        userInforMapper.updateEntity(updateUserRequest, user);
        return ApiResponse.<UserInfDto>builder()
                .data(userInforMapper.toDto(userInfRepository.save(user)))
                .build();
    }

    @Override
    public ApiResponse<UserInfDto> updateAvatar(UUID userId, MultipartFile avatar) {
        String avtUrl = fileStorageService.storeFile(avatar, "user-avt", userId.toString());
        UserInf i = userInfRepository.findById(userId).orElseThrow(
                () -> new CustomException(ErrorCode.USER_NOT_FOUND));
        i.setAvatarUrl(avtUrl);
        return ApiResponse.<UserInfDto>builder()
                .data(userInforMapper.toDto(userInfRepository.save(i)))
                .build();
    }

    @Override
    public ApiResponse<PreferenceDto> getPreference(UUID userId) {
        return ApiResponse.<PreferenceDto>builder()
                .data(preferenceMapper.toDto(preferenceRepository.findByUserId(userId)
                        .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND))))
                .build();
    }

    @Override
    public ApiResponse<PreferenceDto> updatePreference(UUID userId, UpdatePreferenceRequest updatePreferenceRequest) {
        Preference preference = preferenceRepository.findByUserId(userId).orElseThrow(
                () -> new CustomException(ErrorCode.PREFERENCE_NOT_FOUND));
        preferenceMapper.updateEntity(updatePreferenceRequest, preference);
        return ApiResponse.<PreferenceDto>builder()
                .data(preferenceMapper.toDto(preferenceRepository.save(preference)))
                .build();
    }

    @Override
    public ApiResponse<BillingAddressDto> createBillingAddress(UUID userId,
            BillingAddressRequest billingAddressRequest) {
        UserInf user = userInfRepository.findById(userId).orElseThrow(
                () -> new CustomException(ErrorCode.USER_NOT_FOUND));
        BillingAddress b = billingAddressMapper.toEntity(billingAddressRequest);
        b.setUser(user);
        return ApiResponse.<BillingAddressDto>builder()
                .data(billingAddressMapper.toDto(billingAddressRepository.save(b)))
                .build();
    }

    @Override
    public ApiResponse<BillingAddressDto> updateBillingAddress(UUID userId,
            BillingAddressRequest billingAddressRequest) {
        BillingAddress billingAddress = billingAddressRepository.findByUserId(userId).orElseThrow(
                () -> new CustomException(ErrorCode.BILLING_ADDRESS_NOT_FOUND));
        billingAddressMapper.update(billingAddressRequest, billingAddress);
        return ApiResponse.<BillingAddressDto>builder()
                .data(billingAddressMapper.toDto(billingAddressRepository.save(billingAddress)))
                .build();

    }

    @Override
    public ApiResponse<BillingAddressDto> getBillingAddress(UUID userId) {
        return ApiResponse.<BillingAddressDto>builder()
                .data(billingAddressMapper.toDto(billingAddressRepository.findByUserId(userId)
                        .orElse(null)))
                .build();
    }

    @Override
    public ApiResponse<UserInfDto> getUserByUserName(String displayName) {
        return ApiResponse.<UserInfDto>builder()
        .data(userInforMapper.toDto(userInfRepository.findByDisplayName(displayName).orElse(null)))
        .build();
    }

}
