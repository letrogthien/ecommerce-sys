package com.chuadatten.wallet.service.impl;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import com.chuadatten.wallet.dto.WalletDto;
import com.chuadatten.wallet.dto.WalletTransactionDto;
import com.chuadatten.wallet.repository.WalletRepository;
import com.chuadatten.wallet.responses.ApiResponse;
import com.chuadatten.wallet.service.WalletService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class WalletServiceImpl implements WalletService {
    private final WalletRepository walletRepository;
    
    
    @Override
    public ApiResponse<WalletDto> getWallet(UUID userId) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getWallet'");
    }

    @Override
    public ApiResponse<Page<WalletTransactionDto>> getWalletHistory(UUID userId, int page, int size) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getWalletHistory'");
    }

    @Override
    public ApiResponse<Void> transferMoney(UUID fromUserId, UUID toUserId, double amount) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'transferMoney'");
    }

}