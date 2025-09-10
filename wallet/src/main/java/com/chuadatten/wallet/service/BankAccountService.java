package com.chuadatten.wallet.service;

import com.chuadatten.wallet.dto.BankAccountDto;
import com.chuadatten.wallet.request.CreateBankAccountRequest;
import com.chuadatten.wallet.responses.ApiResponse;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;

public interface BankAccountService {

    /**
     * Add a new bank account for user
     * @param request CreateBankAccountRequest containing user ID, bank code, account number, and account name
     * @return ApiResponse containing added bank account information
     */
    ApiResponse<BankAccountDto> addBankAccount(CreateBankAccountRequest request, UUID userId);

    /**
     * Get all bank accounts of a user
     * @param userId ID of the user
     * @return ApiResponse containing list of bank accounts
     */
    ApiResponse<List<BankAccountDto>> getBankAccounts(UUID userId);

    /**
     * Get bank account information by ID
     * @param accountId ID of the bank account
     * @return ApiResponse containing bank account information
     */
    ApiResponse<BankAccountDto> getBankAccount(UUID accountId);

    /**
     * Delete bank account
     * @param accountId ID of the bank account
     * @param userId ID of the user (for permission check)
     * @return ApiResponse containing deletion status
     */
    ApiResponse<Boolean> deleteBankAccount(UUID accountId, UUID userId);

    /**
     * Update bank account information
     * @param accountId ID of the bank account
     * @param userId ID of the user (for permission check)
     * @param bankCode Bank code (can be null)
     * @param accountName Account holder name (can be null)
     * @return ApiResponse containing updated bank account information
     */
    ApiResponse<BankAccountDto> updateBankAccount(UUID accountId, UUID userId, String bankCode, String accountName);


    /**
     * Get all bank accounts of a user with pagination
     * @param userId ID of the user
     * @param page Page number
     * @param size Page size
     * @return ApiResponse containing paginated list of bank accounts
     */
    ApiResponse<Page<BankAccountDto>> getUserBankAccounts(UUID userId, int page, int size);

    /**
     * Check if bank account belongs to user
     * @param accountId ID of the bank account
     * @param userId ID of the user
     */
    boolean isBankAccountOwnedByUser(UUID accountId, UUID userId);
}
