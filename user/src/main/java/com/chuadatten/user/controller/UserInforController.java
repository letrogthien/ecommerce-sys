package com.chuadatten.user.controller;

import java.util.UUID;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.chuadatten.user.anotation.JwtClaims;
import com.chuadatten.user.dto.BillingAddressDto;
import com.chuadatten.user.dto.PreferenceDto;
import com.chuadatten.user.dto.UserInfDto;
import com.chuadatten.user.requests.BillingAddressRequest;
import com.chuadatten.user.requests.UpdatePreferenceRequest;
import com.chuadatten.user.requests.UpdateUserRequest;
import com.chuadatten.user.responses.ApiResponse;
import com.chuadatten.user.services.UserInforService;

import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestParam;


@RestController
@RequestMapping("/api/v1/user-service/users")
@RequiredArgsConstructor
public class UserInforController {

    private final UserInforService userInforService;

    /**
     * Get current user info
     */
    @GetMapping("/id/{userId}")
    public ApiResponse<UserInfDto> getUserInfo(@PathVariable UUID userId) {
        return userInforService.getUserById(userId);
    }


    @GetMapping("/name/{displayName}")
    public ApiResponse<UserInfDto> getUserInfoByUserName(@PathVariable String displayName) {
        return userInforService.getUserByUserName(displayName);
    }

    /**
     * Update user info
     */
    @PutMapping("/me")

    public ApiResponse<UserInfDto> updateUserInfo(
            @Parameter(hidden = true) @JwtClaims("id") UUID userId,
            @RequestBody UpdateUserRequest updateUserRequest
    ) {
        return userInforService.updateUser(userId, updateUserRequest);
    }

    /**
     * Update user avatar
     */
    @PostMapping(value = "me/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<UserInfDto> updateAvatar(
            @Parameter(hidden = true) @JwtClaims("id") UUID userId,
            @RequestPart("avatar") MultipartFile avatar
    ) {
        return userInforService.updateAvatar(userId, avatar);
    }

    

    /**
     * Get user preference
     */
    @GetMapping("/preferences/{userId}")
    public ApiResponse<PreferenceDto> getPreference(@PathVariable UUID userId) {
        return userInforService.getPreference(userId);
    }

    /**
     * Update user preference
     */
    @PutMapping("me/preferences")
    public ApiResponse<PreferenceDto> updatePreference(
            @Parameter(hidden = true) @JwtClaims("id") UUID userId,
            @RequestBody UpdatePreferenceRequest updatePreferenceRequest
    ) {
        return userInforService.updatePreference(userId, updatePreferenceRequest);
    }

    /**
     * Get billing address
     */
    @GetMapping("me/billing-address")
    public ApiResponse<BillingAddressDto> getBillingAddress(@Parameter(hidden = true) @JwtClaims("id") UUID userId) {
        return userInforService.getBillingAddress(userId);
    }

    /**
     * Create billing address
     */
    @PostMapping("me/billing-address")
    public ApiResponse<BillingAddressDto> createBillingAddress(
            @Parameter(hidden = true) @JwtClaims("id") UUID userId,
            @RequestBody BillingAddressRequest billingAddressRequest
    ) {
        return userInforService.createBillingAddress(userId, billingAddressRequest);
    }

    

    /**
     * Update billing address
     */
    @PutMapping("me/billing-address")
    public ApiResponse<BillingAddressDto> updateBillingAddress(
            @Parameter(hidden = true) @JwtClaims("id") UUID userId,
            @RequestBody BillingAddressRequest billingAddressRequest
    ) {
        return userInforService.updateBillingAddress(userId, billingAddressRequest);
    }


}
