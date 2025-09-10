package com.chuadatten.user.services.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.chuadatten.event.OtpEvent;
import com.chuadatten.event.PasswordResetEvent;
import com.chuadatten.event.RegistrationEvent;
import com.chuadatten.event.StrangeDevice;
import com.chuadatten.user.anotation.CusAuditable;
import com.chuadatten.user.common.ConstString;
import com.chuadatten.user.common.JsonParserUtil;
import com.chuadatten.user.common.RoleName;
import com.chuadatten.user.common.Status;
import com.chuadatten.user.common.TokenType;
import com.chuadatten.user.entity.DeviceManager;
import com.chuadatten.user.entity.PasswordHistory;
import com.chuadatten.user.entity.Role;
import com.chuadatten.user.entity.UserAuth;
import com.chuadatten.user.entity.UserInf;
import com.chuadatten.user.entity.UserRole;
import com.chuadatten.user.entity.WhiteList;
import com.chuadatten.user.exceptions.CustomException;
import com.chuadatten.user.exceptions.ErrorCode;
import com.chuadatten.user.jwt.JwtUtils;
import com.chuadatten.user.kafka.EventProducer;
import com.chuadatten.user.kafka.KafkaTopic;
import com.chuadatten.user.otp.OtpModel;
import com.chuadatten.user.otp.OtpType;
import com.chuadatten.user.outbox.OutboxEvent;
import com.chuadatten.user.outbox.OutboxRepository;
import com.chuadatten.user.redis.services.OtpModelCacheService;
import com.chuadatten.user.redis.services.WhiteListCacheService;
import com.chuadatten.user.repository.DeviceManagerRepository;
import com.chuadatten.user.repository.PasswordHistoryRepository;
import com.chuadatten.user.repository.RoleRepository;
import com.chuadatten.user.repository.UserAuthRepository;
import com.chuadatten.user.repository.UserInfRepository;
import com.chuadatten.user.repository.UserRoleRepository;
import com.chuadatten.user.requests.AccessTokenRequest;
import com.chuadatten.user.requests.ChangePwdRequest;
import com.chuadatten.user.requests.Disable2FaRequest;
import com.chuadatten.user.requests.ForgotPwdRequest;
import com.chuadatten.user.requests.LoginRequest;
import com.chuadatten.user.requests.LogoutRequest;
import com.chuadatten.user.requests.RegisterRequest;
import com.chuadatten.user.requests.ResetPwdRequest;
import com.chuadatten.user.requests.Verify2FaRequest;
import com.chuadatten.user.responses.ApiResponse;
import com.chuadatten.user.responses.LoginResponse;
import com.chuadatten.user.securities.CustomPasswordEncoder;
import com.chuadatten.user.services.AuthService;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final UserAuthRepository userRepository;
    private final CustomPasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final JwtUtils jwtUtils;
    private final WhiteListCacheService whiteListCacheService;
    private final OtpModelCacheService otpModelCacheService;
    private final PasswordHistoryRepository passwordHistoryRepository;
    private final DeviceManagerRepository deviceManagerRepository;
    private final UserInfRepository userInfRepository;
    private final JsonParserUtil jsonParserUtil;
    private final OutboxRepository outboxRepository;
    private final EventProducer eventProducer;

    @Override
    @Transactional
    public ApiResponse<String> register(RegisterRequest registerRequest) {
        String responseData = "";
        if (this.existUsername(registerRequest.getUsername())) {
            responseData = "Username already exists";
        }
        if (this.existEmail(registerRequest.getEmail())) {
            responseData += (responseData.isEmpty() ? "" : ", ") + "Email already exists";
        }
        if (!responseData.isEmpty()) {
            return ApiResponse.<String>builder()
                    .message("Registration failed")
                    .data(responseData)
                    .build();
        }

        Role defaultRole = this.roleRepository.findByName(RoleName.ROLE_USER.toString())
                .orElseThrow(() -> new CustomException(ErrorCode.ROLE_NOT_FOUND));

        UserAuth user = new UserAuth();
        user.setUsername(registerRequest.getUsername());
        user.setEmail(registerRequest.getEmail());
        user.setPasswordHash(this.passwordEncoder.passwordEncoder().encode(registerRequest.getPassword()));

        UserRole userRole = UserRole.builder()
                .user(user)
                .role(defaultRole)
                .build();

        user.setUserRoles(Set.of(userRole));
        this.userRepository.save(user);

        UserInf userInf = UserInf.builder()
                .id(user.getId())
                .email(user.getEmail())
                .status(Status.INACTIVE)
                .isSeller(false)
                .displayName(user.getUsername())
                .build();

        this.userInfRepository.save(userInf);
        this.generateRegistrationEventOutBox(user);

        PasswordHistory newPasswordHistory = new PasswordHistory();
        newPasswordHistory.setUser(user);
        newPasswordHistory
                .setPasswordHash(this.passwordEncoder.passwordEncoder().encode(registerRequest.getPassword()));
        newPasswordHistory.setCurrentIndex(0);
        this.passwordHistoryRepository.save(newPasswordHistory);

        return ApiResponse.<String>builder()
                .message("Registration successful, please check your email to activate your account")
                .data("Registration")
                .build();
    }

    private boolean existUsername(String username) {
        return this.userRepository.existsByUsername(username);
    }

    private boolean existEmail(String email) {
        return this.userRepository.existsByEmail(email);
    }

    private void generateRegistrationEventOutBox(UserAuth user) {
        RegistrationEvent registrationEvent = RegistrationEvent.builder()
                .email(user.getEmail())
                .userId(user.getId().toString())
                .build();
        String var10000 = ConstString.DOMAIN_NAME.getValue();

        String url = var10000 + "activate?token=" + this.jwtUtils.generateActivationToken(user);
        registrationEvent.setUrlActive(url);
        OutboxEvent outboxEvent = OutboxEvent.builder()
                .aggregateId(user.getId().toString())
                .aggregateType("User")
                .eventType(KafkaTopic.REGISTER.name())
                .headers(registrationEvent.getClass().getPackageName())
                .payload(jsonParserUtil.toJson(registrationEvent))
                .build();

        outboxRepository.save(outboxEvent);
    }

    @Override
    @Transactional
    public ApiResponse<LoginResponse> login(LoginRequest loginRequest, HttpServletResponse response) {
        UserAuth user = userRepository.findByUsername(loginRequest.getUsername())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        if (user.getStatus() != Status.ACTIVE) {
            return ApiResponse.<LoginResponse>builder()
                    .message("Account is not active")
                    .build();
        }
        if (!this.passwordEncoder.passwordEncoder().matches(loginRequest.getPassword(), user.getPasswordHash())) {
            return ApiResponse.<LoginResponse>builder()
                    .message("Invalid username or password")
                    .build();
        }

        this.handlerDeviceInformation(user, loginRequest.getDeviceName(), loginRequest.getDeviceType());
        if (Boolean.TRUE.equals(user.getTwoFactorEnabled())) {
            return this.handlerTwoFAuth(user);
        }
        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);
        return this.generateLoginResponse(user, response);

    }

    private void handlerDeviceInformation(UserAuth user, String deviceName, String deviceTpe) {
        AtomicBoolean isTrust = new AtomicBoolean(false);
        List<DeviceManager> listDevice = this.deviceManagerRepository.findByUserId(user.getId());
        if (!listDevice.isEmpty()) {
            for (DeviceManager di : listDevice) {
                if (di.getDeviceName().equals(deviceName) && di.getDeviceType().equals(deviceTpe)) {
                    isTrust.set(true);
                    di.setLastLoginAt(LocalDateTime.now());
                    this.deviceManagerRepository.save(di);
                    break;
                }
            }
        }

        if (isTrust.get()) {
            return;
        }
        newDeviceInformation(deviceName, deviceTpe, user);
        this.eventProducer.strangeDevice(
                StrangeDevice.builder()
                        .email(user.getEmail())
                        .devideName(deviceName)
                        .deviceType(deviceTpe)
                        .build());
    }

    private ApiResponse<LoginResponse> generateLoginResponse(UserAuth user, HttpServletResponse response) {
        String token = this.jwtUtils.generateToken(user);
        String refreshToken = this.jwtUtils.generateRefreshToken(user);
        String jti = this.jwtUtils.extractClaim(refreshToken, "jti");
        this.whiteListCacheService.saveToCache(new WhiteList(jti, user.getId()));

        Cookie cookie = new Cookie("access_token", token);
        cookie.setHttpOnly(true);
        cookie.setSecure(false);
        cookie.setPath("/");
        cookie.setMaxAge(3600000);
        response.addCookie(cookie);

        Cookie refreshTokenCookie = new Cookie("refresh_token", refreshToken);
        refreshTokenCookie.setHttpOnly(true);
        refreshTokenCookie.setSecure(false);
        refreshTokenCookie.setPath("/");
        refreshTokenCookie.setMaxAge(604800000);
        response.addCookie(refreshTokenCookie);
        return ApiResponse.<LoginResponse>builder()
                .message("Login successful")
                .data(LoginResponse.builder()
                        .build())
                .build();
    }

    private ApiResponse<LoginResponse> handlerTwoFAuth(UserAuth user) {
        String tmpToken = this.jwtUtils.generateTmpToken(user);
        this.saveAndSendOtp(user);
        return ApiResponse.<LoginResponse>builder()
                .message("Two-factor authentication required")
                .data(LoginResponse.builder()
                        .accessToken(tmpToken)
                        .build())
                .build();
    }

    private void saveAndSendOtp(UserAuth user) {
        OtpModel otpModel = new OtpModel();
        otpModel.generateOtp();
        otpModel.setUserId(user.getId());
        otpModel.setOtpType(OtpType.TWO_FACTOR_AUTHENTICATION);
        this.otpModelCacheService.saveOtpModel(otpModel);
        OtpEvent otpEvent = OtpEvent.builder().email(user.getEmail()).otp(otpModel.getOtp()).build();
        this.eventProducer.sendOtp(otpEvent);
    }

    @Override
    public ApiResponse<LoginResponse> verifyTwoFAuth(Verify2FaRequest verify2FaRequest, HttpServletResponse response) {
        String secret = verify2FaRequest.getSecret();
        if (!this.jwtUtils.isTokenValid(secret, TokenType.TMP_TOKEN)) {
            throw new CustomException(ErrorCode.INVALID_TOKEN);
        }
        UUID userId = UUID.fromString(this.jwtUtils.extractClaim(secret, "id"));
        if (!this.otpModelCacheService.isPresentAndValidInCache(
                userId,
                verify2FaRequest.getOtp(),
                OtpType.TWO_FACTOR_AUTHENTICATION)) {
            throw new CustomException(ErrorCode.UNAUTHORIZED);
        }
        UserAuth user = this.userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        return this.generateLoginResponse(user, response);

    }

    @Override
    public ApiResponse<String> logout(LogoutRequest logoutRequest) {
        String jtiRefreshToken = logoutRequest.getRefreshToken();
        UUID userId = this.jwtUtils.extractClaim(jtiRefreshToken, "id") != null
                ? UUID.fromString(this.jwtUtils.extractClaim(jtiRefreshToken, "id"))
                : null;
        if (this.whiteListCacheService.isPresentInCache(jtiRefreshToken, userId)) {
            this.whiteListCacheService.deleteFromCache(jtiRefreshToken, userId);
        }
        return ApiResponse.<String>builder()
                .message("Logout successful")
                .build();

    }

    @Override
    public ApiResponse<String> logoutAll(UUID userId) {
        this.whiteListCacheService.deleteAllByUserId(userId);
        return ApiResponse.<String>builder()
                .message("Logout all successful")
                .build();
    }

    @Override
    public ApiResponse<LoginResponse> accessToken(AccessTokenRequest accessTokenRequest) {
        String refreshToken = accessTokenRequest.getRefreshToken();
        if (!this.jwtUtils.isTokenValid(refreshToken, TokenType.REFRESH_TOKEN)) {
            throw new CustomException(ErrorCode.INVALID_TOKEN);
        }
        UUID userId = UUID.fromString(this.jwtUtils.extractClaim(refreshToken, "id"));
        if (!this.whiteListCacheService.isPresentInCache(refreshToken, userId)) {
            throw new CustomException(ErrorCode.UNAUTHORIZED);
        }
        UserAuth user = this.userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        String token = this.jwtUtils.generateToken(user);
        return ApiResponse.<LoginResponse>builder()
                .message("Access token generated successfully")
                .data(LoginResponse.builder()
                        .accessToken(token)
                        .refreshToken(refreshToken)
                        .build())
                .build();

    }

    @Override
    @CusAuditable(action = "Change Password", description = "UserAuth changes their password")
    public ApiResponse<String> changePassword(ChangePwdRequest changePwdRequest, UUID userId) {
        UserAuth user = this.userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        this.validateChangePasswordRequest(changePwdRequest, user.getPasswordHash(), userId);
        String newPassword = changePwdRequest.getNewPassword();
        user.setPasswordHash(this.passwordEncoder.passwordEncoder().encode(newPassword));
        this.userRepository.save(user);
        return ApiResponse.<String>builder()
                .message("Password changed successfully")
                .build();
    }

    private void validateChangePasswordRequest(ChangePwdRequest changePwdRequest, String oldPassword, UUID userId) {

        String oldPasswordRq = changePwdRequest.getOldPassword();
        String newPassword = changePwdRequest.getNewPassword();
        String confirmPassword = changePwdRequest.getConfirmPassword();
        if (!this.passwordEncoder.passwordEncoder().matches(oldPasswordRq, oldPassword)) {
            throw new CustomException(ErrorCode.PASSWORD_MISMATCH);
        }
        if (!newPassword.equals(confirmPassword)) {
            throw new CustomException(ErrorCode.PASSWORDS_DO_NOT_MATCH);
        }

        List<PasswordHistory> passwordHistories = this.passwordHistoryRepository.getTop3(userId);
        if (passwordHistories.stream()
                .anyMatch(ph -> this.passwordEncoder.passwordEncoder().matches(newPassword, ph.getPasswordHash()))) {
            throw new CustomException(ErrorCode.PASSWORD_RECENTLY_USED);
        }

        int lastIndex = passwordHistories.isEmpty() ? 0 : passwordHistories.getFirst().getCurrentIndex();
        UserAuth user = this.userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        PasswordHistory newPasswordHistory = new PasswordHistory();
        newPasswordHistory.setUser(user);
        newPasswordHistory.setPasswordHash(this.passwordEncoder.passwordEncoder().encode(newPassword));
        newPasswordHistory.setCurrentIndex(lastIndex + 1);
        this.passwordHistoryRepository.save(newPasswordHistory);

    }

    @Override
    public ApiResponse<String> forgotPassword(ForgotPwdRequest forgotPwdRequest) {
        String email = forgotPwdRequest.getEmail();
        UserAuth user = this.userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        if (user.getStatus() != Status.ACTIVE) {
            return ApiResponse.<String>builder()
                    .message("Account is not active")
                    .build();
        }
        String token = this.jwtUtils.generateResetPasswordToken(user);
        String var10000 = ConstString.DOMAIN_NAME.getValue();
        String url = var10000 + "reset-password?token=" + token;
        eventProducer.forgotPassword(
                PasswordResetEvent.builder()
                        .email(email)
                        .url(url)
                        .build());
        return ApiResponse.<String>builder()
                .message("Forgot password request successful, please check your email")
                .data("success")
                .build();
    }

    @Override
    public ApiResponse<String> enableTwoFAuth(UUID userId) {
        UserAuth user = this.userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        if (Boolean.TRUE.equals(user.getTwoFactorEnabled())) {
            return ApiResponse.<String>builder()
                    .message("Two-factor authentication is already enabled")
                    .build();
        }
        user.setTwoFactorEnabled(true);
        this.userRepository.save(user);
        return ApiResponse.<String>builder()
                .message("Two-factor authentication enabled successfully")
                .build();
    }

    @Override
    @CusAuditable(action = "Disable 2FA", description = "UserAuth disables two-factor authentication")
    public ApiResponse<String> disableTwoFAuth(Disable2FaRequest disable2FAuthRequest, UUID userId) {

        boolean isValid = otpModelCacheService.isPresentAndValidInCache(
                userId,
                disable2FAuthRequest.getOtp(),
                OtpType.TWO_FACTOR_AUTHENTICATION);
        if (!isValid) {
            throw new CustomException(ErrorCode.UNAUTHORIZED);
        }
        UserAuth user = this.userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        if (!Boolean.TRUE.equals(user.getTwoFactorEnabled())) {
            return ApiResponse.<String>builder()
                    .message("Two-factor authentication is not enabled")
                    .build();
        }
        user.setTwoFactorEnabled(false);
        this.userRepository.save(user);
        return ApiResponse.<String>builder()
                .message("Two-factor authentication disabled successfully")
                .build();
    }

    @Override
    @CusAuditable(action = "Trust Device", description = "UserAuth trusts a device for future logins")
    public ApiResponse<String> trustDevice(String deviceName, String deviceType, UUID userId) {
        UserAuth user = this.userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        newDeviceInformation(deviceName, deviceType, user);
        return ApiResponse.<String>builder()
                .message("Device trusted successfully")
                .data("Device trusted successfully")
                .build();
    }

    private void newDeviceInformation(String deviceName, String deviceType, UserAuth user) {
        DeviceManager deviceInformation = new DeviceManager();
        deviceInformation.setUser(user);
        deviceInformation.setDeviceName(deviceName);
        deviceInformation.setDeviceType(deviceType);
        deviceInformation.setLastLoginAt(LocalDateTime.now());
        deviceInformation.setCreatedAt(LocalDateTime.now());
        this.deviceManagerRepository.save(deviceInformation);
    }

    @Override
    public ApiResponse<String> activateAccount(String token) {
        if (!this.jwtUtils.isTokenValid(token, TokenType.ACTIVATION_TOKEN)) {
            throw new CustomException(ErrorCode.INVALID_TOKEN);
        }
        String id = this.jwtUtils.extractClaim(token, "id");
        UserAuth user = this.userRepository.findById(UUID.fromString(id))
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        UserInf userInf = this.userInfRepository.findById(user.getId())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        if (user.getStatus() == Status.ACTIVE) {
            return ApiResponse.<String>builder()
                    .message("Account is already active")
                    .build();
        }
        userInf.setStatus(Status.ACTIVE);
        user.setStatus(Status.ACTIVE);
        this.userRepository.save(user);
        return ApiResponse.<String>builder()
                .message("Account activated successfully")
                .data("Account activated successfully")
                .build();
    }

    @Override
    public ApiResponse<String> resetPassword(ResetPwdRequest resetPwdRequest, String token) {
        if (!this.jwtUtils.isTokenValid(token, TokenType.PASSWORD_RESET_TOKEN)) {
            throw new CustomException(ErrorCode.INVALID_TOKEN);
        }
        UUID userId = UUID.fromString(this.jwtUtils.extractClaim(token, "id"));
        UserAuth user = this.userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        String newPassword = resetPwdRequest.getNewPassword();
        String confirmPassword = resetPwdRequest.getConfirmPassword();
        if (!newPassword.equals(confirmPassword)) {
            throw new CustomException(ErrorCode.PASSWORDS_DO_NOT_MATCH);
        }
        List<PasswordHistory> passwordHistories = this.passwordHistoryRepository.getTop3(userId);
        if (passwordHistories.stream()
                .anyMatch(ph -> this.passwordEncoder.passwordEncoder().matches(newPassword, ph.getPasswordHash()))) {
            throw new CustomException(ErrorCode.PASSWORD_RECENTLY_USED);
        }

        int lastIndex = passwordHistories.isEmpty() ? 0 : passwordHistories.getFirst().getCurrentIndex();
        PasswordHistory newPasswordHistory = new PasswordHistory();
        newPasswordHistory.setUser(user);
        newPasswordHistory.setPasswordHash(this.passwordEncoder.passwordEncoder().encode(newPassword));
        newPasswordHistory.setCurrentIndex(lastIndex + 1);
        this.passwordHistoryRepository.save(newPasswordHistory);
        user.setPasswordHash(this.passwordEncoder.passwordEncoder().encode(newPassword));
        this.userRepository.save(user);
        return ApiResponse.<String>builder()
                .message("Password reset successfully")
                .data("Password reset successfully")
                .build();
    }

    @Override
    public ApiResponse<String> assignRoleToUser(UUID userId, RoleName roleName) {
        return assignRoleHelper(
                userId,
                roleName);
    }

    private ApiResponse<String> assignRoleHelper(UUID userId, RoleName roleName) {

        UserAuth user = this.userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        Role role = this.roleRepository.findByName(roleName.name())
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND));
        if (user.getUserRoles().stream().anyMatch(ur -> ur.getRole().getName().equals(roleName.name()))) {
            throw new CustomException(ErrorCode.INVALID_INPUT);
        }

        switch (roleName) {
            case ROLE_SELLER:
                if (!user.getIsKyc().equals(true)) {
                    throw new CustomException(ErrorCode.NOT_KYC);
                }
                break;

            default:
                break;
        }
        UserRole userRole = UserRole.builder().user(user).role(role).build();
        user.getUserRoles().add(userRole);
        userRoleRepository.save(userRole);
        this.userRepository.save(user);
        return ApiResponse.<String>builder()
                .message("Role " + roleName + " assigned to user successfully")
                .data("Role " + roleName + " assigned to user successfully")
                .build();
    }
}
