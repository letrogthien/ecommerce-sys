package com.chuadatten.wallet.service;

import java.util.UUID;

import org.springframework.data.domain.Page;

import com.chuadatten.wallet.dto.WalletDto;
import com.chuadatten.wallet.dto.WalletTransactionDto;
import com.chuadatten.wallet.responses.ApiResponse;

public interface WalletService {

    /**
     * Get wallet information (balance, reserved, status)
     * @param userId ID of the user
     * @return ApiResponse containing wallet information
     */
    ApiResponse<WalletDto> getWallet(UUID userId);



    /**
     * Get transaction history from wallet_transactions
     * @param userId ID of the user
     * @param page Page number
     * @param size Page size
     * @return ApiResponse containing paginated transaction history
     */
    ApiResponse<Page<WalletTransactionDto>> getWalletHistory(UUID userId, int page, int size);

    /**
     * Tranfer money from wallet to another wallet
     * @param fromUserId ID of the sender
     * @param toUserId ID of the receiver
     * @param amount Amount to transfer
     * @return ApiResponse indicating success or failure
     */
    ApiResponse<Void> transferMoney(UUID fromUserId, UUID toUserId, double amount);

    
    
    

}
