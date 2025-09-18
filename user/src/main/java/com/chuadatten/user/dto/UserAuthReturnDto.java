package com.chuadatten.user.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import com.chuadatten.user.common.Status;

import lombok.Builder;
import lombok.Data;



@Data
@Builder
public class UserAuthReturnDto {

    private UUID id;

    private String username;

    private String email;

    private Status status;

    private Boolean twoFactorEnabled;

    private Boolean isKyc;

    private String twoFactorSecret;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private LocalDateTime lastLoginAt;
}
