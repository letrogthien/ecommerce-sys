package com.chuadatten.user.services;

import java.util.UUID;

import org.springframework.web.multipart.MultipartFile;

import com.chuadatten.user.dto.BillingAddressDto;
import com.chuadatten.user.dto.PreferenceDto;
import com.chuadatten.user.dto.UserInfDto;
import com.chuadatten.user.requests.BillingAddressRequest;
import com.chuadatten.user.requests.UpdatePreferenceRequest;
import com.chuadatten.user.requests.UpdateUserRequest;
import com.chuadatten.user.responses.ApiResponse;

public interface UserInforService {
    /**
     * Get user information by id
     * 
     * @param userId: user id
     * @return: ApiResponse<UserInfDto>
     */
    ApiResponse<UserInfDto> getUserById(UUID userId );

    /**
     * Update user information
     * @param UpdateUserRequest: user information
     * @return: ApiResponse<UserInfDto>
     */
    ApiResponse<UserInfDto> updateUser(UUID userId,UpdateUserRequest updateUserRequest);

    /**
     * Update avatar 
     * @param userId: user id
     * @param MultilpartFile: avatar
     * @return: ApiResponse<UserInfDto>
     */
    ApiResponse<UserInfDto> updateAvatar(UUID userId, MultipartFile avatar);

    /**
     * Get user preference setting 
     * @param userId
     * @return: ApiResponse<PreferenceDto>
     */
    ApiResponse<PreferenceDto> getPreference(UUID userId);


    /**
     * Update user preference setting
     * @param userId
     * @param UpdatePreferenceRequest: user preference setting
     * @return: ApiResponse<PreferenceDto>
     */
    ApiResponse<PreferenceDto> updatePreference(UUID userId, UpdatePreferenceRequest updatePreferenceRequest);

   
    /**
     * Create Billing Address 
     * @param userId
     * @param BillingAddressRequest
     * @return: ApiResponse<BillingAddressDto>
     */
    ApiResponse<BillingAddressDto> createBillingAddress(UUID userId, BillingAddressRequest billingAddressRequest);

    /**
     * Update Billing Address
     * @param userId
     * @param BillingAddressRequest
     * @return: ApiResponse<BillingAddressDto>
     */
    ApiResponse<BillingAddressDto> updateBillingAddress(UUID userId, BillingAddressRequest billingAddressRequest);

    /**
     * Get Billing Address
     * @param userId
     * @return: ApiResponse<BillingAddressDto>
     */
    ApiResponse<BillingAddressDto> getBillingAddress(UUID userId);



    /**
     * Get User by userName
     * @param userName
     * @return: ApiResponse<UserInfDto>
     */
    ApiResponse<UserInfDto> getUserByUserName(String userName);
}
