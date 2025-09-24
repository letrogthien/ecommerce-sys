package com.chuadatten.user.controller;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.chuadatten.user.anotation.JwtClaims;
import com.chuadatten.user.common.RoleName;
import com.chuadatten.user.dto.UserAuthReturnDto;
import com.chuadatten.user.requests.ChangePwdRequest;
import com.chuadatten.user.requests.Disable2FaRequest;
import com.chuadatten.user.requests.ForgotPwdRequest;
import com.chuadatten.user.requests.LoginRequest;
import com.chuadatten.user.requests.RegisterRequest;
import com.chuadatten.user.requests.ResetPwdRequest;
import com.chuadatten.user.requests.Verify2FaRequest;
import com.chuadatten.user.responses.ApiResponse;
import com.chuadatten.user.responses.LoginResponse;
import com.chuadatten.user.services.AuthService;

import io.swagger.v3.oas.annotations.Parameter;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;




@RestController
@RequestMapping("/api/v1/user-service/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserAuthReturnDto>> getMe(@Parameter(hidden = true) @JwtClaims("id") UUID userId) {
        return ResponseEntity.ok(authService.getMe(userId));
    }

    @PostMapping("/clear-cookie")
    public ApiResponse<String> clearCookies(HttpServletResponse response) {
        authService.clearCookies(response);

        return ApiResponse.<String>builder()
                .message("Clear cookies successfully")
                .data("Clear cookies")
                .build();
    }
    

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<String>> register(@RequestBody RegisterRequest registerRequest) {
        return ResponseEntity.ok(authService.register(registerRequest));
    }

    @PostMapping("/register/role")
    public ResponseEntity<Object> registerWithRole(@RequestParam UUID userId, @RequestParam RoleName roleName) {
        return ResponseEntity.ok(authService.registerWithRole(userId, roleName));
    }

    

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@RequestBody LoginRequest loginRequest, HttpServletResponse response) {
        return ResponseEntity.ok(authService.login(loginRequest, response));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<String>> logout(@CookieValue(name = "refresh_token", required = false) String tokenString) {
        return ResponseEntity.ok(authService.logout(tokenString));
    }

    @PostMapping("/logout-all")
    public ResponseEntity<ApiResponse<String>> logoutAll( UUID userId) {
        return ResponseEntity.ok(authService.logoutAll(userId));
    }

    @PostMapping("/access-token")
    public ResponseEntity<ApiResponse<String>> accessToken(@CookieValue(name = "refresh_token", required = false) String refreshToken, HttpServletResponse response) {
        return ResponseEntity.ok(authService.accessToken(refreshToken, response));
    }

    @PostMapping("/change-password")
    public ResponseEntity<ApiResponse<String>> changePassword(@RequestBody ChangePwdRequest changePwdRequest,@Parameter(hidden = true) @JwtClaims("id") UUID userId) {
        return ResponseEntity.ok(authService.changePassword(changePwdRequest, userId));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<String>> forgotPassword(@RequestBody ForgotPwdRequest forgotPwdRequest) {
        return ResponseEntity.ok(authService.forgotPassword(forgotPwdRequest));
    }

    @PostMapping("/enable-2fa")
    public ResponseEntity<ApiResponse<String>> enableTwoFAuth(@Parameter(hidden = true) @JwtClaims("id") UUID userId) {
        return ResponseEntity.ok(authService.enableTwoFAuth(userId));
    }

    @PostMapping("/disable-2fa")
    public ResponseEntity<ApiResponse<String>> disableTwoFAuth(@RequestBody Disable2FaRequest disable2FAuthRequest, @Parameter(hidden = true) @JwtClaims("id") UUID userId) {
        return ResponseEntity.ok(authService.disableTwoFAuth(disable2FAuthRequest, userId));
    }

    @PostMapping("/verify-2fa")
    public ResponseEntity<ApiResponse<LoginResponse>> verifyTwoFAuth(@RequestBody Verify2FaRequest verify2FaRequest, HttpServletResponse response) {
        return ResponseEntity.ok(authService.verifyTwoFAuth(verify2FaRequest, response));
    }

    @PostMapping("/trust-device")
    public ResponseEntity<ApiResponse<String>> trustDevice(@RequestParam String deviceName, @RequestParam String deviceType, @Parameter(hidden = true) @JwtClaims("id") UUID userId) {
        return ResponseEntity.ok(authService.trustDevice(deviceName, deviceType, userId));
    }

    @PostMapping("/activate-account")
    public ResponseEntity<ApiResponse<String>> activateAccount(@RequestParam String token) {
        return ResponseEntity.ok(authService.activateAccount(token));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<String>> resetPassword(@RequestBody ResetPwdRequest resetPwdRequest, @RequestParam String token) {
        return ResponseEntity.ok(authService.resetPassword(resetPwdRequest, token));
    }

    @PostMapping("/assign-role")
    public ResponseEntity<ApiResponse<String>> assignRoleToUser(@Parameter(hidden = true) @JwtClaims("id") UUID userId, @RequestParam RoleName roleName) {
        return ResponseEntity.ok(authService.assignRoleToUser(userId, roleName));
    }


    @GetMapping("/disable-2fa")
    public ApiResponse<String> disableTwoFA(@Parameter(hidden = true) @JwtClaims("id") UUID userId) {
        return authService.disAble2FaRequest(userId);
    }
    

    
}
