package com.chuadatten.user.services;


import com.chuadatten.user.common.RoleName;
import com.chuadatten.user.dto.UserAuthReturnDto;
import com.chuadatten.user.requests.*;
import com.chuadatten.user.responses.ApiResponse;
import com.chuadatten.user.responses.LoginResponse;
import jakarta.servlet.http.HttpServletResponse;

import java.util.UUID;


public interface AuthService {

    /**
     * Registers a new user.
     *
     * @param registerRequest the registration request containing user details
     * @return ApiResponse containing a success or failure message
     */
    ApiResponse<String> register(RegisterRequest registerRequest);

    /**
     * Logs in a user and generates tokens.
     *
     * @param loginRequest the login credentials
     * @param response     the HTTP response to which cookies or headers can be added
     * @return ApiResponse containing login response data (tokens, etc.)
     */
    ApiResponse<LoginResponse> login(LoginRequest loginRequest, HttpServletResponse response);

    /**
     * Logs out the user from the current session.
     *
     * @param logoutRequest the logout request details
     * @return ApiResponse containing a success or failure message
     */
    ApiResponse<String> logout(String token);

    /**
     * Logs out the user from all sessions.
     *
     * @param userId the ID of the user to log out
     * @return ApiResponse containing a success or failure message
     */
    ApiResponse<String> logoutAll(UUID userId);

    /**
     * Generates a new access token using a refresh token.
     *
     * @param accessTokenRequest the request containing the refresh token
     * @return ApiResponse containing the new access token
     */
    ApiResponse<String> accessToken(String accessTokenRequest, HttpServletResponse response);

    /**
     * Changes the user's password.
     *
     * @param changePwdRequest the request containing old and new passwords
     * @param userId           the ID of the user changing the password
     * @return ApiResponse containing a success or failure message
     */
    ApiResponse<String> changePassword(ChangePwdRequest changePwdRequest, UUID userId);

    /**
     * Initiates the forgot password process by sending a reset link or code.
     *
     * @param forgotPwdRequest the request containing user identification info (e.g. email)
     * @return ApiResponse containing a success or failure message
     */
    ApiResponse<String> forgotPassword(ForgotPwdRequest forgotPwdRequest);

    /**
     * Enables two-factor authentication (2FA) for the user.
     *
     * @param userId the ID of the user enabling 2FA
     * @return ApiResponse containing a success or failure message
     */
    ApiResponse<String> enableTwoFAuth(UUID userId);


    ApiResponse<String> disAble2FaRequest(UUID userId);
    

    /**
     * Disables two-factor authentication (2FA) for the user.
     *
     * @param disable2FAuthRequest the request to disable 2FA
     * @param userId               the ID of the user disabling 2FA
     * @return ApiResponse containing a success or failure message
     */
    ApiResponse<String> disableTwoFAuth(Disable2FaRequest disable2FAuthRequest, UUID userId);

    /**
     * Verifies the 2FA code provided by the user during login.
     *
     * @param verify2FaRequest the request containing the 2FA code
     * @param response          the HTTP response to update session/cookies
     * @return ApiResponse containing login response data
     */
    ApiResponse<LoginResponse> verifyTwoFAuth(Verify2FaRequest verify2FaRequest, HttpServletResponse response);

    /**
     * Adds a trusted device for the user to skip 2FA on future logins.
     *
     * @param deviceName the name of the device
     * @param deviceType the type of the device (e.g., "mobile", "desktop")
     * @param userId     the ID of the user trusting the device
     * @return ApiResponse containing a success or failure message
     */
    ApiResponse<String> trustDevice(String deviceName, String deviceType, UUID userId);

    /**
     * Activates a user account using the provided activation token
     *
     * @param token the activation token
     * @return ApiResponse containing a success or failure message
     */
    ApiResponse<String> activateAccount(String token);

    /**
     * Resets a user's password using the provided token and new password.
     *
     * @param resetPwdRequest the request containing the new password
     * @param token           the token validating the reset request
     * @return ApiResponse containing a success or failure message
     */
    ApiResponse<String> resetPassword(ResetPwdRequest resetPwdRequest, String token);

    /**
     * Assigns a new role to a user.
     *
     * @param userId   the ID of the user
     * @param roleName the role to assign
     * @return ApiResponse containing a success or failure message
     */
    ApiResponse<String> assignRoleToUser(UUID userId, RoleName roleName);

    ApiResponse<UserAuthReturnDto> getMe(UUID userId);

    void clearCookies(HttpServletResponse response);
}