package com.chuadatten.user.controller;

import com.chuadatten.user.common.RoleName;
import com.chuadatten.user.common.Status;
import com.chuadatten.user.dto.*;
import com.chuadatten.user.dto.request.KycReviewRequest;
import com.chuadatten.user.dto.request.KycRejectRequest;
import com.chuadatten.user.dto.request.SellerApplicationReviewRequest;
import com.chuadatten.user.responses.ApiResponse;
import com.chuadatten.user.services.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/user-service/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    /**
     * Get all users with pagination and status filter.
     */
    @GetMapping("/users")
    public ApiResponse<Page<UserInfDto>> getUserList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Status status
    ) {
        return adminService.getUserList(page, size, status);
    }

    /**
     * Get a user by ID.
     */
    @GetMapping("/users/{userId}")
    public ApiResponse<UserInfDto> getUser(@PathVariable UUID userId) {
        return adminService.getUser(userId);
    }

    /**
     * Approve a user.
     */
    @PostMapping("/users/{userId}/approve")
    public ApiResponse<String> approveUser(@PathVariable UUID userId) {
        return adminService.approveUser(userId);
    }

    /**
     * Reject a user.
     */
    @PostMapping("/users/{userId}/reject")
    public ApiResponse<String> rejectUser(@PathVariable UUID userId) {
        return adminService.rejectUser(userId);
    }

    /**
     * Suspend a user.
     */
    @PostMapping("/users/{userId}/suspend")
    public ApiResponse<String> suspendUser(@PathVariable UUID userId) {
        return adminService.suspendUser(userId);
    }

    /**
     * Delete a user.
     */
    @DeleteMapping("/users/{userId}")
    public ApiResponse<String> deleteUser(@PathVariable UUID userId) {
        return adminService.deleteUser(userId);
    }

    /**
     * Set a role for a user.
     */
    @PostMapping("/users/{userId}/roles")
    public ApiResponse<String> setRoleForUser(
            @PathVariable UUID userId,
            @RequestParam RoleName roleName
    ) {
        return adminService.setRoleForUser(userId, roleName);
    }

    /**
     * Change seller status for a user.
     */
    @PostMapping("/users/{userId}/seller-status")
    public ApiResponse<UserInfDto> changeStatusSeller(
            @PathVariable UUID userId,
            @RequestParam Status status
    ) {
        return adminService.changeStatusSeller(userId, status);
    }



    
    /**
     * Get all login history with pagination and filters.
     */
    @GetMapping("/login-history")
    public ApiResponse<Page<LoginHistoryDto>> getLoginHistory(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) UUID userId,
            @RequestParam(required = false) Boolean success
    ) {
        return adminService.getLoginHistory(page, size, userId, success);
    }

    /**
     * Get login history for a specific user.
     */
    @GetMapping("/login-history/{userId}")
    public ApiResponse<Page<LoginHistoryDto>> getUserLoginHistory(
            @PathVariable UUID userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return adminService.getUserLoginHistory(userId, page, size);
    }

    // ===================== DEVICE MANAGEMENT =====================
    
    /**
     * Get all devices with pagination and filters.
     */
    @GetMapping("/devices")
    public ApiResponse<Page<DeviceManagerDto>> getDevices(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) UUID userId,
            @RequestParam(required = false) String deviceType
    ) {
        return adminService.getDevices(page, size, userId, deviceType);
    }

    /**
     * Revoke a device.
     */
    @PostMapping("/devices/{deviceId}/revoke")
    public ApiResponse<String> revokeDevice(@PathVariable UUID deviceId) {
        return adminService.revokeDevice(deviceId);
    }

    // ===================== KYC MANAGEMENT =====================
    
    /**
     * Get pending KYC verifications.
     */
    @GetMapping("/kyc/pending")
    public ApiResponse<Page<KycDto>> getPendingKyc(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return adminService.getPendingKyc(page, size);
    }

    /**
     * Approve a KYC verification.
     */
    @PostMapping("/kyc/{kycId}/approve")
    public ApiResponse<String> approveKyc(
            @PathVariable UUID kycId,
            @RequestBody(required = false) KycReviewRequest request
    ) {
        return adminService.approveKyc(kycId, request);
    }

    /**
     * Reject a KYC verification.
     */
    @PostMapping("/kyc/{kycId}/reject")
    public ApiResponse<String> rejectKyc(
            @PathVariable UUID kycId,
            @RequestBody KycRejectRequest request
    ) {
        return adminService.rejectKyc(kycId, request);
    }

    /**
     * Get KYC delete requests.
     */
    @GetMapping("/kyc/delete-requests")
    public ApiResponse<Page<KycDto>> getKycDeleteRequests(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return adminService.getKycDeleteRequests(page, size);
    }

    // ===================== SELLER MANAGEMENT =====================
    
    /**
     * Get seller applications with pagination and status filter.
     */
    @GetMapping("/seller-applications")
    public ApiResponse<Page<SellerApplicationDto>> getSellerApplications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String status
    ) {
        return adminService.getSellerApplications(page, size, status);
    }

    /**
     * Review a seller application.
     */
    @PostMapping("/seller-applications/{appId}/review")
    public ApiResponse<String> reviewSellerApplication(
            @PathVariable UUID appId,
            @RequestBody SellerApplicationReviewRequest request
    ) {
        return adminService.reviewSellerApplication(appId, request);
    }


    
    /**
     * Get user statistics.
     */
    @GetMapping("/users/stats")
    public ApiResponse<UserStatsDto> getUserStats() {
        return adminService.getUserStats();
    }

    /**
     * Get user activity summary.
     */
    @GetMapping("/users/activity-summary")
    public ApiResponse<UserActivitySummaryDto> getUserActivitySummary(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate
    ) {
        return adminService.getUserActivitySummary(startDate, endDate);
    }
}
