package com.chuadatten.user.services.impl;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.chuadatten.user.anotation.CusAuditable;
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
import com.chuadatten.user.entity.AuditLog;
import com.chuadatten.user.entity.DeviceManager;
import com.chuadatten.user.entity.LoginHistory;
import com.chuadatten.user.entity.Role;
import com.chuadatten.user.entity.SellerApplication;
import com.chuadatten.user.entity.SellerRating;
import com.chuadatten.user.entity.SendMessageError;
import com.chuadatten.user.entity.UserAuth;
import com.chuadatten.user.entity.UserInf;
import com.chuadatten.user.entity.UserRole;
import com.chuadatten.user.entity.UserVerification;
import com.chuadatten.user.exceptions.CustomException;
import com.chuadatten.user.exceptions.ErrorCode;
import com.chuadatten.user.mapper.UserInforMapper;
import com.chuadatten.user.repository.AuditLogRepository;
import com.chuadatten.user.repository.DeviceManagerRepository;
import com.chuadatten.user.repository.LoginHistoryRepository;
import com.chuadatten.user.repository.MessageErrorRepository;
import com.chuadatten.user.repository.RoleRepository;
import com.chuadatten.user.repository.SellerApplicationRepository;
import com.chuadatten.user.repository.SellerRatingRepository;
import com.chuadatten.user.repository.UserAuthRepository;
import com.chuadatten.user.repository.UserInfRepository;
import com.chuadatten.user.repository.UserRoleRepository;
import com.chuadatten.user.repository.UserVerificationRepository;
import com.chuadatten.user.responses.ApiResponse;
import com.chuadatten.user.services.AdminService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {
        private final UserAuthRepository userRepository;
        private final RoleRepository roleRepository;
        private final UserRoleRepository userRoleRepository;
        private final UserInfRepository userInfRepository;
        private final UserInforMapper userInforMapper;
        private final UserAuthRepository userAuthRepository;
        
        // New repositories for additional endpoints
        private final AuditLogRepository auditLogRepository;
        private final LoginHistoryRepository loginHistoryRepository;
        private final DeviceManagerRepository deviceManagerRepository;
        private final UserVerificationRepository userVerificationRepository;
        private final SellerApplicationRepository sellerApplicationRepository;
        private final SellerRatingRepository sellerRatingRepository;
        private final MessageErrorRepository messageErrorRepository;

        @Override
        @CusAuditable(action = "APPROVE_USER", description = "Approving a user account")
        public ApiResponse<String> approveUser(UUID userId) {
                UserAuth user = userRepository.findById(userId)
                                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
                user.setStatus(Status.ACTIVE);
                userAuthRepository.save(user);
                return ApiResponse.<String>builder()
                                .data("User approved successfully")
                                .build();
        }

        @Override
        @CusAuditable(action = "REJECT_USER", description = "Rejecting a user account")
        public ApiResponse<String> rejectUser(UUID userId) {
                UserAuth user = userRepository.findById(userId)
                                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
                if (!user.getStatus().equals(Status.ACTIVE)) {
                        return ApiResponse.<String>builder()
                                        .message("User is not active")
                                        .build();
                }
                user.setStatus(Status.BLOCKED);
                userRepository.save(user);
                return ApiResponse.<String>builder()
                                .message("User rejected successfully")
                                .build();
        }

        @Override
        @CusAuditable(action = "SUSPEND_USER", description = "Suspending a user account")
        public ApiResponse<String> suspendUser(UUID userId) {
                UserAuth user = userRepository.findById(userId)
                                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
                if (!user.getStatus().equals(Status.ACTIVE)) {
                        return ApiResponse.<String>builder()
                                        .message("User is not active")
                                        .build();
                }
                user.setStatus(Status.SUSPENDED);
                userRepository.save(user);
                return ApiResponse.<String>builder()
                                .message("User suspended successfully")
                                .build();
        }

        @Override
        @CusAuditable(action = "DELETE_USER", description = "Deleting a user account")
        public ApiResponse<String> deleteUser(UUID userId) {
                UserAuth user = userRepository.findById(userId)
                                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
                user.setStatus(Status.DELETED);
                UserInf userInf = userInfRepository.findById(userId)
                                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
                userInf.setStatus(Status.DELETED);
                userInfRepository.save(userInf);
                userRepository.save(user);
                return ApiResponse.<String>builder()
                                .message("User deleted successfully")
                                .build();
        }

        @Override
        @CusAuditable(action = "SET_ROLE", description = "Assigning a role to a user")
        public ApiResponse<String> setRoleForUser(UUID userId, RoleName roleName) {
                UserAuth user = userRepository.findById(userId)
                                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
                if (user.getUserRoles().stream().anyMatch(role -> role.getRole().getName().equals(roleName.name()))) {
                        return ApiResponse.<String>builder()
                                        .message("User already has this role")
                                        .build();
                }
                Role role = roleRepository.findByName(roleName.name())
                                .orElseThrow(() -> new CustomException(ErrorCode.ROLE_NOT_FOUND));
                UserRole userRole = UserRole.builder().role(role).user(user).build();
                userRoleRepository.save(userRole);
                userRepository.save(user);
                return ApiResponse.<String>builder()
                                .message("Role " + roleName + " assigned to user successfully")
                                .build();
        }

        @Override
        public ApiResponse<Page<UserInfDto>> getUserList(int page, int size, Status status) {
                Pageable pageable = PageRequest.of(page, size);
                if (status != null) {
                        return ApiResponse.<Page<UserInfDto>>builder()
                                        .data(userInfRepository.findAllByStatus(status, pageable)
                                                        .map(userInforMapper::toDto))
                                        .message("Get list user successfully")
                                        .build();
                } else {
                        return ApiResponse.<Page<UserInfDto>>builder()
                                        .data(userInfRepository.findAll(pageable)
                                                        .map(userInforMapper::toDto))
                                        .message("Get list user successfully")
                                        .build();
                }
        }

        @Override
        public ApiResponse<UserInfDto> getUser(UUID userId) {
                UserInf user = userInfRepository.findById(userId)
                                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
                return ApiResponse.<UserInfDto>builder()
                                .data(userInforMapper.toDto(user))
                                .message("Get user successfully")
                                .build();
        }

        @Override
        public ApiResponse<UserInfDto> changeStatusSeller(UUID userId, Status status) {
                UserInf user = userInfRepository.findById(userId)
                                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
                user.setStatus(status);
                userInfRepository.save(user);
                return ApiResponse.<UserInfDto>builder()
                                .data(userInforMapper.toDto(user))
                                .message("Change status successfully")
                                .build();
        }

        // ===================== AUDIT LOGS IMPLEMENTATION =====================

        @Override
        public ApiResponse<Page<AuditLogDto>> getAuditLogs(int page, int size, UUID userId, String action) {
                Pageable pageable = PageRequest.of(page, size);
                Page<AuditLog> auditLogs;
                
                if (userId != null && action != null) {
                        auditLogs = auditLogRepository.findByUserIdAndActionContaining(userId, action, pageable);
                } else if (userId != null) {
                        auditLogs = auditLogRepository.findByUserId(userId, pageable);
                } else if (action != null) {
                        auditLogs = auditLogRepository.findByActionContaining(action, pageable);
                } else {
                        auditLogs = auditLogRepository.findAll(pageable);
                }
                
                Page<AuditLogDto> auditLogDtos = auditLogs.map(this::mapToAuditLogDto);
                
                return ApiResponse.<Page<AuditLogDto>>builder()
                                .data(auditLogDtos)
                                .message("Audit logs retrieved successfully")
                                .build();
        }

        @Override
        public ApiResponse<Page<AuditLogDto>> getUserAuditLogs(UUID userId, int page, int size) {
                Pageable pageable = PageRequest.of(page, size);
                Page<AuditLog> auditLogs = auditLogRepository.findByUserId(userId, pageable);
                Page<AuditLogDto> auditLogDtos = auditLogs.map(this::mapToAuditLogDto);
                
                return ApiResponse.<Page<AuditLogDto>>builder()
                                .data(auditLogDtos)
                                .message("User audit logs retrieved successfully")
                                .build();
        }

        // ===================== LOGIN HISTORY IMPLEMENTATION =====================

        @Override
        public ApiResponse<Page<LoginHistoryDto>> getLoginHistory(int page, int size, UUID userId, Boolean success) {
                Pageable pageable = PageRequest.of(page, size);
                Page<LoginHistory> loginHistories;
                
                if (userId != null && success != null) {
                        loginHistories = loginHistoryRepository.findByUserIdAndSuccess(userId, success, pageable);
                } else if (userId != null) {
                        loginHistories = loginHistoryRepository.findByUserId(userId, pageable);
                } else if (success != null) {
                        loginHistories = loginHistoryRepository.findBySuccess(success, pageable);
                } else {
                        loginHistories = loginHistoryRepository.findAll(pageable);
                }
                
                Page<LoginHistoryDto> loginHistoryDtos = loginHistories.map(this::mapToLoginHistoryDto);
                
                return ApiResponse.<Page<LoginHistoryDto>>builder()
                                .data(loginHistoryDtos)
                                .message("Login history retrieved successfully")
                                .build();
        }

        @Override
        public ApiResponse<Page<LoginHistoryDto>> getUserLoginHistory(UUID userId, int page, int size) {
                Pageable pageable = PageRequest.of(page, size);
                Page<LoginHistory> loginHistories = loginHistoryRepository.findByUserId(userId, pageable);
                Page<LoginHistoryDto> loginHistoryDtos = loginHistories.map(this::mapToLoginHistoryDto);
                
                return ApiResponse.<Page<LoginHistoryDto>>builder()
                                .data(loginHistoryDtos)
                                .message("User login history retrieved successfully")
                                .build();
        }

        // ===================== DEVICE MANAGEMENT IMPLEMENTATION =====================

        @Override
        public ApiResponse<Page<DeviceManagerDto>> getDevices(int page, int size, UUID userId, String deviceType) {
                Pageable pageable = PageRequest.of(page, size);
                Page<DeviceManager> devices;
                
                if (userId != null && deviceType != null) {
                        devices = deviceManagerRepository.findByUserIdAndDeviceType(userId, deviceType, pageable);
                } else if (userId != null) {
                        devices = deviceManagerRepository.findByUserId(userId, pageable);
                } else if (deviceType != null) {
                        devices = deviceManagerRepository.findByDeviceType(deviceType, pageable);
                } else {
                        devices = deviceManagerRepository.findAll(pageable);
                }
                
                Page<DeviceManagerDto> deviceDtos = devices.map(this::mapToDeviceManagerDto);
                
                return ApiResponse.<Page<DeviceManagerDto>>builder()
                                .data(deviceDtos)
                                .message("Devices retrieved successfully")
                                .build();
        }

        @Override
        @CusAuditable(action = "REVOKE_DEVICE", description = "Revoking a device")
        public ApiResponse<String> revokeDevice(UUID deviceId) {
                DeviceManager device = deviceManagerRepository.findById(deviceId)
                                .orElseThrow(() -> new CustomException(ErrorCode.DEVICE_NOT_FOUND));
                
                deviceManagerRepository.delete(device);
                
                return ApiResponse.<String>builder()
                                .data("Device revoked successfully")
                                .build();
        }

        // ===================== KYC MANAGEMENT IMPLEMENTATION =====================

        @Override
        public ApiResponse<Page<KycDto>> getPendingKyc(int page, int size) {
                Pageable pageable = PageRequest.of(page, size);
                Page<UserVerification> pendingKyc = userVerificationRepository.findByVerificationStatus(Status.PENDING, pageable);
                Page<KycDto> kycDtos = pendingKyc.map(this::mapToKycDto);
                
                return ApiResponse.<Page<KycDto>>builder()
                                .data(kycDtos)
                                .message("Pending KYC verifications retrieved successfully")
                                .build();
        }

        @Override
        @CusAuditable(action = "APPROVE_KYC", description = "Approving KYC verification")
        public ApiResponse<String> approveKyc(UUID kycId, KycReviewRequest request) {
                UserVerification kyc = userVerificationRepository.findById(kycId)
                                .orElseThrow(() -> new CustomException(ErrorCode.KYC_NOT_FOUND));
                
                kyc.setVerificationStatus(Status.VERIFIED);
                kyc.setUpdatedAt(LocalDateTime.now());
                kyc.setVerifiedAt(LocalDateTime.now());
                // Note: UserVerification entity doesn't have notes field
                // Consider adding this field if needed for admin comments
                
                userVerificationRepository.save(kyc);
                
                return ApiResponse.<String>builder()
                                .data("KYC approved successfully")
                                .build();
        }

        @Override
        @CusAuditable(action = "REJECT_KYC", description = "Rejecting KYC verification")
        public ApiResponse<String> rejectKyc(UUID kycId, KycRejectRequest request) {
                UserVerification kyc = userVerificationRepository.findById(kycId)
                                .orElseThrow(() -> new CustomException(ErrorCode.KYC_NOT_FOUND));
                
                kyc.setVerificationStatus(Status.REJECTED);
                kyc.setUpdatedAt(LocalDateTime.now());
                // Note: UserVerification entity doesn't have rejection reason field
                // Consider adding these fields to the entity if needed
                
                userVerificationRepository.save(kyc);
                
                return ApiResponse.<String>builder()
                                .data("KYC rejected successfully")
                                .build();
        }

        @Override
        public ApiResponse<Page<KycDto>> getKycDeleteRequests(int page, int size) {
                Pageable pageable = PageRequest.of(page, size);
                // Assuming there's a status for delete requests
                Page<UserVerification> deleteRequests = userVerificationRepository.findByVerificationStatus(Status.DELETE_REQUESTED, pageable);
                Page<KycDto> kycDtos = deleteRequests.map(this::mapToKycDto);
                
                return ApiResponse.<Page<KycDto>>builder()
                                .data(kycDtos)
                                .message("KYC delete requests retrieved successfully")
                                .build();
        }

        // ===================== SELLER MANAGEMENT IMPLEMENTATION =====================

        @Override
        public ApiResponse<Page<SellerApplicationDto>> getSellerApplications(int page, int size, String status) {
                Pageable pageable = PageRequest.of(page, size);
                Page<SellerApplication> applications;
                
                if (status != null) {
                        applications = sellerApplicationRepository.findByApplicationStatus(status, pageable);
                } else {
                        applications = sellerApplicationRepository.findAll(pageable);
                }
                
                Page<SellerApplicationDto> applicationDtos = applications.map(this::mapToSellerApplicationDto);
                
                return ApiResponse.<Page<SellerApplicationDto>>builder()
                                .data(applicationDtos)
                                .message("Seller applications retrieved successfully")
                                .build();
        }

        @Override
        @CusAuditable(action = "REVIEW_SELLER_APPLICATION", description = "Reviewing seller application")
        public ApiResponse<String> reviewSellerApplication(UUID appId, SellerApplicationReviewRequest request) {
                SellerApplication application = sellerApplicationRepository.findById(appId)
                                .orElseThrow(() -> new CustomException(ErrorCode.APPLICATION_NOT_FOUND));
                
                application.setApplicationStatus(request.getAction());
                application.setUpdatedAt(LocalDateTime.now());
                if (request.getComment() != null) {
                        application.setNotes(request.getComment());
                }
                
                sellerApplicationRepository.save(application);
                
                return ApiResponse.<String>builder()
                                .data("Seller application reviewed successfully")
                                .build();
        }



        @Override
        public ApiResponse<Page<SellerRatingDto>> getSellerRatings(int page, int size, UUID sellerId, Integer rating) {
                Pageable pageable = PageRequest.of(page, size);
                Page<SellerRating> ratings;
                
                if (sellerId != null && rating != null) {
                        ratings = sellerRatingRepository.findBySellerIdAndRating(sellerId, rating, pageable);
                } else if (sellerId != null) {
                        ratings = sellerRatingRepository.findBySellerId(sellerId, pageable);
                } else if (rating != null) {
                        ratings = sellerRatingRepository.findByRatingScore(rating, pageable);
                } else {
                        ratings = sellerRatingRepository.findAll(pageable);
                }
                
                Page<SellerRatingDto> ratingDtos = ratings.map(this::mapToSellerRatingDto);
                
                return ApiResponse.<Page<SellerRatingDto>>builder()
                                .data(ratingDtos)
                                .message("Seller ratings retrieved successfully")
                                .build();
        }

        @Override
        @CusAuditable(action = "DELETE_SELLER_RATING", description = "Deleting seller rating")
        public ApiResponse<String> deleteSellerRating(UUID ratingId) {
                SellerRating rating = sellerRatingRepository.findById(ratingId)
                                .orElseThrow(() -> new CustomException(ErrorCode.RATING_NOT_FOUND));
                
                sellerRatingRepository.delete(rating);
                
                return ApiResponse.<String>builder()
                                .data("Seller rating deleted successfully")
                                .build();
        }

        // ===================== SYSTEM MONITORING IMPLEMENTATION =====================

        @Override
        public ApiResponse<Page<MessageErrorDto>> getMessageErrors(int page, int size, String errorType, Boolean resolved) {
                Pageable pageable = PageRequest.of(page, size);
                Page<SendMessageError> errors;
                
                if (errorType != null && resolved != null) {
                        errors = messageErrorRepository.findByErrorTypeAndResolved(errorType, resolved, pageable);
                } else if (errorType != null) {
                        errors = messageErrorRepository.findByStatus(errorType, pageable);
                } else if (resolved != null) {
                        errors = messageErrorRepository.findByResolved(resolved, pageable);
                } else {
                        errors = messageErrorRepository.findAll(pageable);
                }
                
                Page<MessageErrorDto> errorDtos = errors.map(this::mapToMessageErrorDto);
                
                return ApiResponse.<Page<MessageErrorDto>>builder()
                                .data(errorDtos)
                                .message("Message errors retrieved successfully")
                                .build();
        }

        @Override
        public ApiResponse<Page<Object>> getOutboxEvents(int page, int size) {
                // This would need to be implemented based on your outbox pattern implementation
                // For now, returning an empty page
                Pageable pageable = PageRequest.of(page, size);
                Page<Object> emptyPage = Page.empty(pageable);
                
                return ApiResponse.<Page<Object>>builder()
                                .data(emptyPage)
                                .message("Outbox events retrieved successfully")
                                .build();
        }

        // ===================== USER ANALYTICS IMPLEMENTATION =====================

        @Override
        public ApiResponse<UserStatsDto> getUserStats() {
                Long totalUsers = userAuthRepository.count();
                Long activeUsers = userAuthRepository.countByStatus(Status.ACTIVE);
                Long newUsersToday = userAuthRepository.countByCreatedAtAfter(LocalDateTime.now().minusDays(1));
                Long newUsersThisWeek = userAuthRepository.countByCreatedAtAfter(LocalDateTime.now().minusWeeks(1));
                Long newUsersThisMonth = userAuthRepository.countByCreatedAtAfter(LocalDateTime.now().minusMonths(1));
                Long verifiedUsers = userInfRepository.countByStatus(Status.VERIFIED);
                Long unverifiedUsers = userInfRepository.countByStatus(Status.PENDING);
                Long suspendedUsers = userAuthRepository.countByStatus(Status.SUSPENDED);
                Long rejectedUsers = userAuthRepository.countByStatus(Status.REJECTED);
                
                // Count sellers - assuming there's a way to identify sellers
                Long sellerCount = userRoleRepository.countByRoleName(RoleName.ROLE_SELLER.name());
                
                UserStatsDto stats = UserStatsDto.builder()
                                .totalUsers(totalUsers)
                                .activeUsers(activeUsers)
                                .newUsersToday(newUsersToday)
                                .newUsersThisWeek(newUsersThisWeek)
                                .newUsersThisMonth(newUsersThisMonth)
                                .verifiedUsers(verifiedUsers)
                                .unverifiedUsers(unverifiedUsers)
                                .sellerCount(sellerCount)
                                .suspendedUsers(suspendedUsers)
                                .rejectedUsers(rejectedUsers)
                                .build();
                
                return ApiResponse.<UserStatsDto>builder()
                                .data(stats)
                                .message("User statistics retrieved successfully")
                                .build();
        }

        @Override
        public ApiResponse<UserActivitySummaryDto> getUserActivitySummary(String startDate, String endDate) {
                LocalDateTime start = startDate != null ? 
                        LocalDateTime.parse(startDate, DateTimeFormatter.ISO_LOCAL_DATE_TIME) : 
                        LocalDateTime.now().minusWeeks(1);
                LocalDateTime end = endDate != null ? 
                        LocalDateTime.parse(endDate, DateTimeFormatter.ISO_LOCAL_DATE_TIME) : 
                        LocalDateTime.now();
                
                Long loginCount = loginHistoryRepository.countByLoginAtBetween(start, end);
                Long registrationCount = userAuthRepository.countByCreatedAtBetween(start, end);
                Long kycSubmissions = userVerificationRepository.countByCreatedAtBetween(start, end);
                Long sellerApplications = sellerApplicationRepository.countByCreatedAtBetween(start, end);
                
                UserActivitySummaryDto summary = UserActivitySummaryDto.builder()
                                .loginCount(loginCount)
                                .registrationCount(registrationCount)
                                .kycSubmissions(kycSubmissions)
                                .sellerApplications(sellerApplications)
                                .transactionCount(0L) // Would need transaction repository
                                .periodStart(start)
                                .periodEnd(end)
                                .build();
                
                return ApiResponse.<UserActivitySummaryDto>builder()
                                .data(summary)
                                .message("User activity summary retrieved successfully")
                                .build();
        }

        // ===================== HELPER METHODS =====================

        private AuditLogDto mapToAuditLogDto(AuditLog auditLog) {
                return AuditLogDto.builder()
                                .id(auditLog.getId())
                                .userId(auditLog.getUserId())
                                .action(auditLog.getAction())
                                .description(auditLog.getDescription())
                                .createdAt(auditLog.getCreatedAt())
                                .build();
        }

        private LoginHistoryDto mapToLoginHistoryDto(LoginHistory loginHistory) {
                return LoginHistoryDto.builder()
                                .id(loginHistory.getId())
                                .userId(loginHistory.getUser().getId())
                                .username(loginHistory.getUser().getUsername())
                                .loginAt(loginHistory.getLoginAt())
                                .ipAddress(loginHistory.getIpAddress())
                                .deviceInfo(loginHistory.getDeviceInfo())
                                .success(loginHistory.getSuccess())
                                .build();
        }

        private DeviceManagerDto mapToDeviceManagerDto(DeviceManager device) {
                return DeviceManagerDto.builder()
                                .id(device.getId())
                                .userId(device.getUser().getId())
                                .username(device.getUser().getUsername())
                                .deviceName(device.getDeviceName())
                                .deviceType(device.getDeviceType())
                                .lastLoginAt(device.getLastLoginAt())
                                .createdAt(device.getCreatedAt())
                                .isActive(true) // Assuming active if not deleted
                                .build();
        }

        private KycDto mapToKycDto(UserVerification verification) {
                return KycDto.builder()
                                .id(verification.getId())
                                .userId(verification.getUser().getId())
                                .username(verification.getUser().getDisplayName())
                                .email(verification.getUser().getEmail())
                                .verificationStatus(verification.getVerificationStatus())
                                .frontIdUrl(verification.getDocumentFrontUrl())
                                .backIdUrl(verification.getDocumentBackUrl())
                                .selfieUrl(verification.getFaceIdFrontUrl())
                                .rejectionReason("") // UserVerification doesn't have rejection reason field
                                .submittedAt(verification.getCreatedAt())
                                .reviewedAt(verification.getUpdatedAt())
                                .build();
        }

        private SellerApplicationDto mapToSellerApplicationDto(SellerApplication application) {
                return SellerApplicationDto.builder()
                                .id(application.getId())
                                .userId(application.getUser().getId())
                                .applicationStatus(application.getApplicationStatus())
                                .submissionDate(application.getCreatedAt())
                                .reviewDate(application.getUpdatedAt())
                                .rejectionReason(application.getRejectionReason())
                                .notes(application.getNotes())
                                .createdAt(application.getCreatedAt())
                                .updatedAt(application.getUpdatedAt())
                                .build();
        }



        private SellerRatingDto mapToSellerRatingDto(SellerRating rating) {
                return SellerRatingDto.builder()
                                .id(rating.getId())
                                .sellerId(rating.getSeller().getId())
                                .sellerUsername(rating.getSeller().getDisplayName())
                                .buyerId(rating.getBuyer().getId())
                                .buyerUsername(rating.getBuyer().getDisplayName())
                                .rating(rating.getRatingScore())
                                .comment(rating.getReviewText())
                                .createdAt(rating.getCreatedAt())
                                .build();
        }

        private MessageErrorDto mapToMessageErrorDto(SendMessageError error) {
                return MessageErrorDto.builder()
                                .id(error.getId())
                                .topic(error.getTopic())
                                .message(error.getMessage())
                                .errorType(error.getStatus()) // Using status as errorType
                                .errorMessage("") // Entity doesn't have errorMessage field
                                .retryCount(0) // Entity doesn't have retryCount field
                                .createdAt(error.getCreatedAt())
                                .resolved(false) // Entity doesn't have resolved field, default to false
                                .build();
        }
}
