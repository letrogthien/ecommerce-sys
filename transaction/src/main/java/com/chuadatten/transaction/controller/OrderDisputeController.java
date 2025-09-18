package com.chuadatten.transaction.controller;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.chuadatten.transaction.anotation.JwtClaims;
import com.chuadatten.transaction.common.DisputeIssueType;
import com.chuadatten.transaction.common.Status;
import com.chuadatten.transaction.dto.OrderDisputeDto;
import com.chuadatten.transaction.request.OrderDisputeCreateRq;
import com.chuadatten.transaction.request.OrderDisputeUpdateRq;
import com.chuadatten.transaction.responses.ApiResponse;
import com.chuadatten.transaction.service.OrderDisputeService;

import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("api/v1/transaction-service/disputes")
@RequiredArgsConstructor
public class OrderDisputeController {
    private final OrderDisputeService orderDisputeService;

    /**
     * Open dispute
     * Buyer or Seller opens a dispute with reason, issue type, and detailed description.
     */
    @PostMapping
    public ApiResponse<OrderDisputeDto> openDispute(
            @Parameter(hidden = true) @JwtClaims("id") UUID userId,
            @RequestBody OrderDisputeCreateRq disputeCreateRq) {
        return orderDisputeService.openDispute(disputeCreateRq, userId);
    }

    /**
     * View dispute
     * Buyer or Seller views the dispute status and related proofs.
     */
    @GetMapping("/order/{orderId}")
    public ApiResponse<OrderDisputeDto> getDispute(
            @Parameter(hidden = true) @JwtClaims("id") UUID userId,
            @PathVariable UUID orderId) {
        return orderDisputeService.getDispute(orderId, userId);
    }

    /**
     * Update dispute (owner only)
     * The user who opened the dispute updates the description or proof.
     */
    @PutMapping("/{disputeId}")
    public ApiResponse<OrderDisputeDto> updateDispute(
            @Parameter(hidden = true) @JwtClaims("id") UUID userId,
            @PathVariable UUID disputeId,
            @RequestBody OrderDisputeUpdateRq disputeUpdateRq) {
        return orderDisputeService.updateDispute(disputeId, disputeUpdateRq, userId);
    }

    /**
     * Get all disputes
     * Admin views all disputes, filtered by status and issue type.
     */
    @GetMapping
    public ApiResponse<Page<OrderDisputeDto>> getAllDisputes(
            @RequestParam(required = false) Status status,
            @RequestParam(required = false) DisputeIssueType issueType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int limit) {
        return orderDisputeService.getAllDisputes(status, issueType, page, limit);
    }
}