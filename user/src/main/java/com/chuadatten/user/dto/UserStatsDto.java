package com.chuadatten.user.dto;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserStatsDto {
    private Long totalUsers;
    private Long activeUsers;
    private Long newUsersToday;
    private Long newUsersThisWeek;
    private Long newUsersThisMonth;
    private Long verifiedUsers;
    private Long unverifiedUsers;
    private Long sellerCount;
    private Long suspendedUsers;
    private Long rejectedUsers;
}