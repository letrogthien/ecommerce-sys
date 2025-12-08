package com.chuadatten.user.services;

import java.util.UUID;

import org.springframework.data.domain.Page;

import com.chuadatten.user.common.RoleName;
import com.chuadatten.user.common.Status;
import com.chuadatten.user.dto.AuditLogDto;
import com.chuadatten.user.dto.DeviceManagerDto;
import com.chuadatten.user.dto.KycDto;
import com.chuadatten.user.dto.LoginHistoryDto;
import com.chuadatten.user.dto.MessageErrorDto;
import com.chuadatten.user.dto.SellerApplicationDto;
import com.chuadatten.user.dto.SellerRatingDto;
import com.chuadatten.user.dto.UserActivitySummaryDto;
import com.chuadatten.user.dto.UserInfDto;
import com.chuadatten.user.dto.UserStatsDto;
import com.chuadatten.user.dto.request.KycRejectRequest;
import com.chuadatten.user.dto.request.KycReviewRequest;
import com.chuadatten.user.dto.request.SellerApplicationReviewRequest;
import com.chuadatten.user.responses.ApiResponse;

public interface AdminService {
    /*
     * Get all users
     * 
     */
    ApiResponse<Page<UserInfDto>> getUserList(int page, int size, Status status);

    /*
     * Get user
     * @Param userId
     * @return UserInfDto
     */
    ApiResponse<UserInfDto> getUser(UUID userId);

    /**
     * Approves a user's registration or application.
     *
     * @param userId the ID of the user to approve
     * @return ApiResponse containing a success or failure message
     */     
    ApiResponse<String> approveUser(UUID userId);

    /**
     * Rejects a user's registration or application.
     *
     * @param userId the ID of the user to reject
     * @return ApiResponse containing a success or failure message
     */
    ApiResponse<String> rejectUser(UUID userId);

    /**
     * Suspends a user's account.
     *
     * @param userId the ID of the user to suspend
     * @return ApiResponse containing a success or failure message
     */
    ApiResponse<String> suspendUser(UUID userId);

    /**
     * Deletes a user from the system.
     *
     * @param userId the ID of the user to delete
     * @return ApiResponse containing a success or failure message
     */
    ApiResponse<String> deleteUser(UUID userId);

    /**
     * Sets a specific role for a user.
     *
     * @param userId   the ID of the user
     * @param roleName the role to assign to the user
     * @return ApiResponse containing a success or failure message
     */
    ApiResponse<String> setRoleForUser(UUID userId, RoleName roleName);


    /**
     * Change status seller of user.
     * 
     * @Param userId
     * @Param status
     * @return ApiResponse containing a success or failure message
     */
    ApiResponse<UserInfDto> changeStatusSeller(UUID userId, Status status);

    // ===================== AUDIT LOGS =====================
    
    ApiResponse<Page<AuditLogDto>> getAuditLogs(int page, int size, UUID userId, String action);
    
    ApiResponse<Page<AuditLogDto>> getUserAuditLogs(UUID userId, int page, int size);

    // ===================== LOGIN HISTORY =====================
    
    ApiResponse<Page<LoginHistoryDto>> getLoginHistory(int page, int size, UUID userId, Boolean success);
    
    ApiResponse<Page<LoginHistoryDto>> getUserLoginHistory(UUID userId, int page, int size);

    // ===================== DEVICE MANAGEMENT =====================
    
    ApiResponse<Page<DeviceManagerDto>> getDevices(int page, int size, UUID userId, String deviceType);
    
    ApiResponse<String> revokeDevice(UUID deviceId);

    // ===================== KYC MANAGEMENT =====================
    
    ApiResponse<Page<KycDto>> getPendingKyc(int page, int size);
    
    ApiResponse<String> approveKyc(UUID kycId, KycReviewRequest request);
    
    ApiResponse<String> rejectKyc(UUID kycId, KycRejectRequest request);
    
    ApiResponse<Page<KycDto>> getKycDeleteRequests(int page, int size);

    // ===================== SELLER MANAGEMENT =====================
    
    ApiResponse<Page<SellerApplicationDto>> getSellerApplications(int page, int size, String status);
    
    ApiResponse<String> reviewSellerApplication(UUID appId, SellerApplicationReviewRequest request);
    
    ApiResponse<Page<SellerRatingDto>> getSellerRatings(int page, int size, UUID sellerId, Integer rating);
    
    ApiResponse<String> deleteSellerRating(UUID ratingId);

    // ===================== SYSTEM MONITORING =====================
    
    ApiResponse<Page<MessageErrorDto>> getMessageErrors(int page, int size, String errorType, Boolean resolved);
    
    ApiResponse<Page<Object>> getOutboxEvents(int page, int size);

    // ===================== USER ANALYTICS =====================
    
    ApiResponse<UserStatsDto> getUserStats();
    
    ApiResponse<UserActivitySummaryDto> getUserActivitySummary(String startDate, String endDate);
}