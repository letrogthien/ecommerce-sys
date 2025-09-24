package com.chuadatten.transaction.request;

import java.util.UUID;

import com.chuadatten.transaction.common.Status;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefundStatusUpdateRequest {
    
    @NotNull(message = "Status is required")
    private Status status;
    
    @NotNull(message = "Updated by is required")
    private UUID updatedBy;
    
    private String note; // Optional note
}