package com.chuadatten.wallet.service.impl;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.chuadatten.wallet.common.Status;
import com.chuadatten.wallet.dto.BankAccountDto;
import com.chuadatten.wallet.dto.PaymentDto;
import com.chuadatten.wallet.dto.WalletDto;
import com.chuadatten.wallet.dto.WithdrawalRequestDto;
import com.chuadatten.wallet.entity.BankAccount;
import com.chuadatten.wallet.entity.Payment;
import com.chuadatten.wallet.entity.Wallet;
import com.chuadatten.wallet.entity.WithdrawalRequest;
import com.chuadatten.wallet.exceptions.CustomException;
import com.chuadatten.wallet.exceptions.ErrorCode;
import com.chuadatten.wallet.mapper.PaymentMapper;
import com.chuadatten.wallet.mapper.WalletMapper;
import com.chuadatten.wallet.mapper.WithdrawalRequestMapper;
import com.chuadatten.wallet.repository.BankAccountRepository;
import com.chuadatten.wallet.repository.PaymentRepository;
import com.chuadatten.wallet.repository.WalletRepository;
import com.chuadatten.wallet.repository.WithdrawalRequestRepository;
import com.chuadatten.wallet.request.RejectWithdrawalRequest;
import com.chuadatten.wallet.responses.ApiResponse;
import com.chuadatten.wallet.service.AdminService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final WalletRepository walletRepository;
    private final PaymentRepository paymentRepository;
    private final WithdrawalRequestRepository withdrawalRequestRepository;
    private final BankAccountRepository bankAccountRepository;
    
    private final WalletMapper walletMapper;
    private final PaymentMapper paymentMapper;
    private final WithdrawalRequestMapper withdrawalRequestMapper;
    
    // Constants for filter keys
    private static final String FILTER_USER_ID = "userId";
    private static final String FILTER_CURRENCY = "currency";
    private static final String FILTER_STATUS = "status";
    private static final String FILTER_ORDER_ID = "orderId";
    private static final String FILTER_MIN_AMOUNT = "minAmount";
    private static final String FILTER_MAX_AMOUNT = "maxAmount";
    private static final String FILTER_FROM_DATE = "fromDate";
    private static final String FILTER_TO_DATE = "toDate";
    private static final String FILTER_BANK_CODE = "bankCode";
    private static final String FIELD_CREATED_AT = "createdAt";
    private static final String FIELD_AMOUNT = "amount";
    private static final String STATISTICS_TOTAL = "total";
    
    // Suspicious transaction threshold (100M VND)
    private static final BigInteger SUSPICIOUS_AMOUNT_THRESHOLD = BigInteger.valueOf(100_000_000L);

    @Override
    public ApiResponse<Page<WalletDto>> viewAllWallets(Map<String, Object> filters, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(FIELD_CREATED_AT).descending());
        
        Specification<Wallet> spec = buildWalletSpecification(filters);
        Page<Wallet> wallets = walletRepository.findAll(spec, pageable);
        Page<WalletDto> walletDtos = wallets.map(walletMapper::toDto);
        
        return ApiResponse.<Page<WalletDto>>builder()
                .data(walletDtos)
                .build();
    }

    private Specification<Wallet> buildWalletSpecification(Map<String, Object> filters) {
        return (root, query, criteriaBuilder) -> {
            var predicates = new java.util.ArrayList<jakarta.persistence.criteria.Predicate>();
            
            if (filters != null) {
                if (filters.containsKey(FILTER_USER_ID)) {
                    predicates.add(criteriaBuilder.equal(root.get(FILTER_USER_ID), 
                        UUID.fromString(filters.get(FILTER_USER_ID).toString())));
                }
                if (filters.containsKey(FILTER_CURRENCY)) {
                    predicates.add(criteriaBuilder.equal(root.get(FILTER_CURRENCY), 
                        filters.get(FILTER_CURRENCY)));
                }
                if (filters.containsKey(FILTER_STATUS)) {
                    predicates.add(criteriaBuilder.equal(root.get(FILTER_STATUS), 
                        Status.valueOf(filters.get(FILTER_STATUS).toString())));
                }
                if (filters.containsKey("minBalance")) {
                    predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("balance"), 
                        new BigDecimal(filters.get("minBalance").toString())));
                }
                if (filters.containsKey("maxBalance")) {
                    predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("balance"), 
                        new BigDecimal(filters.get("maxBalance").toString())));
                }
            }
            
            return criteriaBuilder.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };
    }

    @Override
    @Transactional
    public ApiResponse<WalletDto> changeStatusWallet(UUID walletId, Status status) {
        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new CustomException(ErrorCode.WALLET_NOT_FOUND));
        
        wallet.setStatus(status);
        wallet.setUpdatedAt(LocalDateTime.now());
        walletRepository.save(wallet);
        
        return ApiResponse.<WalletDto>builder()
                .data(walletMapper.toDto(wallet))
                .build();
    }

    @Override
    public ApiResponse<Page<PaymentDto>> viewAllPayments(Map<String, Object> filters, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(FIELD_CREATED_AT).descending());
        
        Specification<Payment> spec = buildPaymentSpecification(filters);
        Page<Payment> payments = paymentRepository.findAll(spec, pageable);
        Page<PaymentDto> paymentDtos = payments.map(paymentMapper::toDto);
        
        return ApiResponse.<Page<PaymentDto>>builder()
                .data(paymentDtos)
                .build();
    }

    private Specification<Payment> buildPaymentSpecification(Map<String, Object> filters) {
        return (root, query, criteriaBuilder) -> {
            var predicates = new java.util.ArrayList<jakarta.persistence.criteria.Predicate>();
            
            if (filters != null) {
                if (filters.containsKey(FILTER_USER_ID)) {
                    predicates.add(criteriaBuilder.equal(root.get(FILTER_USER_ID), 
                        UUID.fromString(filters.get(FILTER_USER_ID).toString())));
                }
                if (filters.containsKey(FILTER_STATUS)) {
                    predicates.add(criteriaBuilder.equal(root.get(FILTER_STATUS), 
                        Status.valueOf(filters.get(FILTER_STATUS).toString())));
                }
                if (filters.containsKey(FILTER_ORDER_ID)) {
                    predicates.add(criteriaBuilder.equal(root.get(FILTER_ORDER_ID), 
                        UUID.fromString(filters.get(FILTER_ORDER_ID).toString())));
                }
                if (filters.containsKey(FILTER_MIN_AMOUNT)) {
                    predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get(FIELD_AMOUNT), 
                        new BigInteger(filters.get(FILTER_MIN_AMOUNT).toString())));
                }
                if (filters.containsKey(FILTER_MAX_AMOUNT)) {
                    predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get(FIELD_AMOUNT), 
                        new BigInteger(filters.get(FILTER_MAX_AMOUNT).toString())));
                }
                if (filters.containsKey(FILTER_FROM_DATE)) {
                    predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get(FIELD_CREATED_AT), 
                        (LocalDateTime) filters.get(FILTER_FROM_DATE)));
                }
                if (filters.containsKey(FILTER_TO_DATE)) {
                    predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get(FIELD_CREATED_AT), 
                        (LocalDateTime) filters.get(FILTER_TO_DATE)));
                }
            }
            
            return criteriaBuilder.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };
    }

    @Override
    public ApiResponse<Page<WithdrawalRequestDto>> viewAllWithdrawals(Map<String, Object> filters, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(FIELD_CREATED_AT).descending());
        
        Specification<WithdrawalRequest> spec = buildWithdrawalSpecification(filters);
        Page<WithdrawalRequest> withdrawals = withdrawalRequestRepository.findAll(spec, pageable);
        Page<WithdrawalRequestDto> withdrawalDtos = withdrawals.map(withdrawalRequestMapper::toDto);
        
        return ApiResponse.<Page<WithdrawalRequestDto>>builder()
                .data(withdrawalDtos)
                .build();
    }

    private Specification<WithdrawalRequest> buildWithdrawalSpecification(Map<String, Object> filters) {
        return (root, query, criteriaBuilder) -> {
            var predicates = new java.util.ArrayList<jakarta.persistence.criteria.Predicate>();
            
            if (filters != null) {
                if (filters.containsKey(FILTER_USER_ID)) {
                    predicates.add(criteriaBuilder.equal(root.get(FILTER_USER_ID), 
                        UUID.fromString(filters.get(FILTER_USER_ID).toString())));
                }
                if (filters.containsKey(FILTER_STATUS)) {
                    predicates.add(criteriaBuilder.equal(root.get(FILTER_STATUS), 
                        Status.valueOf(filters.get(FILTER_STATUS).toString())));
                }
                if (filters.containsKey(FILTER_MIN_AMOUNT)) {
                    predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get(FIELD_AMOUNT), 
                        Long.valueOf(filters.get(FILTER_MIN_AMOUNT).toString())));
                }
                if (filters.containsKey(FILTER_MAX_AMOUNT)) {
                    predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get(FIELD_AMOUNT), 
                        Long.valueOf(filters.get(FILTER_MAX_AMOUNT).toString())));
                }
                if (filters.containsKey(FILTER_FROM_DATE)) {
                    predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get(FIELD_CREATED_AT), 
                        (LocalDateTime) filters.get(FILTER_FROM_DATE)));
                }
                if (filters.containsKey(FILTER_TO_DATE)) {
                    predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get(FIELD_CREATED_AT), 
                        (LocalDateTime) filters.get(FILTER_TO_DATE)));
                }
            }
            
            return criteriaBuilder.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };
    }

    @Override
    @Transactional
    public ApiResponse<WithdrawalRequestDto> approveWithdrawal(UUID withdrawalId) {
        WithdrawalRequest withdrawal = withdrawalRequestRepository.findById(withdrawalId)
                .orElseThrow(() -> new CustomException(ErrorCode.WITHDRAWAL_REQUEST_NOT_FOUND));
        
        if (withdrawal.getStatus() != Status.PENDING) {
            throw new CustomException(ErrorCode.WITHDRAWAL_REQUEST_ALREADY_PROCESSED);
        }
        
        withdrawal.setStatus(Status.APPROVED);
        withdrawal.setUpdatedAt(LocalDateTime.now());
        
        withdrawalRequestRepository.save(withdrawal);
        
        return ApiResponse.<WithdrawalRequestDto>builder()
                .data(withdrawalRequestMapper.toDto(withdrawal))
                .build();
    }

    @Override
    @Transactional
    public ApiResponse<WithdrawalRequestDto> rejectWithdrawal(UUID withdrawalId, RejectWithdrawalRequest request) {
        WithdrawalRequest withdrawal = withdrawalRequestRepository.findById(withdrawalId)
                .orElseThrow(() -> new CustomException(ErrorCode.WITHDRAWAL_REQUEST_NOT_FOUND));
        
        if (withdrawal.getStatus() != Status.PENDING) {
            throw new CustomException(ErrorCode.WITHDRAWAL_REQUEST_ALREADY_PROCESSED);
        }
        
        withdrawal.setStatus(Status.REJECTED);
        // Store rejection reason in metadata field since rejectionReason field doesn't exist
        withdrawal.setMetadata("{\"rejectionReason\":\"" + request.getReason() + "\"}");
        withdrawal.setUpdatedAt(LocalDateTime.now());
        
        withdrawalRequestRepository.save(withdrawal);
        
        return ApiResponse.<WithdrawalRequestDto>builder()
                .data(withdrawalRequestMapper.toDto(withdrawal))
                .build();
    }

    @Override
    public ApiResponse<Page<BankAccountDto>> viewAllBankAccounts(Map<String, Object> filters, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(FIELD_CREATED_AT).descending());
        
        Specification<BankAccount> spec = buildBankAccountSpecification(filters);
        Page<BankAccount> bankAccounts = bankAccountRepository.findAll(spec, pageable);
        Page<BankAccountDto> bankAccountDtos = bankAccounts.map(this::convertToBankAccountDto);
        
        return ApiResponse.<Page<BankAccountDto>>builder()
                .data(bankAccountDtos)
                .build();
    }

    private Specification<BankAccount> buildBankAccountSpecification(Map<String, Object> filters) {
        return (root, query, criteriaBuilder) -> {
            var predicates = new java.util.ArrayList<jakarta.persistence.criteria.Predicate>();
            
            if (filters != null) {
                if (filters.containsKey(FILTER_USER_ID)) {
                    predicates.add(criteriaBuilder.equal(root.get(FILTER_USER_ID), 
                        UUID.fromString(filters.get(FILTER_USER_ID).toString())));
                }
                if (filters.containsKey(FILTER_BANK_CODE)) {
                    predicates.add(criteriaBuilder.equal(root.get(FILTER_BANK_CODE), 
                        filters.get(FILTER_BANK_CODE)));
                }
            }
            
            return criteriaBuilder.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };
    }

    @Override
    public ApiResponse<Map<String, Object>> getSystemStatistics() {
        Map<String, Object> statistics = new HashMap<>();
        
        // Wallet statistics
        long totalWallets = walletRepository.count();
        long activeWallets = walletRepository.count((root, query, criteriaBuilder) -> 
            criteriaBuilder.equal(root.get(FILTER_STATUS), Status.ACTIVE));
        
        // Payment statistics
        long totalPayments = paymentRepository.count();
        long successfulPayments = paymentRepository.count((root, query, criteriaBuilder) -> 
            criteriaBuilder.equal(root.get(FILTER_STATUS), Status.SUCCEEDED));
        long failedPayments = paymentRepository.count((root, query, criteriaBuilder) -> 
            criteriaBuilder.equal(root.get(FILTER_STATUS), Status.FAILED));
        
        // Withdrawal statistics
        long totalWithdrawals = withdrawalRequestRepository.count();
        long pendingWithdrawals = withdrawalRequestRepository.count((root, query, criteriaBuilder) -> 
            criteriaBuilder.equal(root.get(FILTER_STATUS), Status.PENDING));
        long approvedWithdrawals = withdrawalRequestRepository.count((root, query, criteriaBuilder) -> 
            criteriaBuilder.equal(root.get(FILTER_STATUS), Status.APPROVED));
        
        // Bank account statistics
        long totalBankAccounts = bankAccountRepository.count();
        
        // Add statistics to map
        statistics.put("wallets", Map.of(
            STATISTICS_TOTAL, totalWallets,
            "active", activeWallets
        ));
        
        statistics.put("payments", Map.of(
            STATISTICS_TOTAL, totalPayments,
            "successful", successfulPayments,
            "failed", failedPayments,
            "successRate", totalPayments > 0 ? (double) successfulPayments / totalPayments * 100 : 0
        ));
        
        statistics.put("withdrawals", Map.of(
            STATISTICS_TOTAL, totalWithdrawals,
            "pending", pendingWithdrawals,
            "approved", approvedWithdrawals
        ));
        
        statistics.put("bankAccounts", Map.of(
            STATISTICS_TOTAL, totalBankAccounts
        ));
        
        statistics.put("generatedAt", LocalDateTime.now());
        
        return ApiResponse.<Map<String, Object>>builder()
                .data(statistics)
                .build();
    }

    @Override
    public ApiResponse<Page<Object>> getSuspiciousTransactions(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(FIELD_CREATED_AT).descending());
        
        // Define criteria for suspicious transactions using JPA Specification
        Specification<Payment> suspiciousSpec = (root, query, criteriaBuilder) -> {
            var predicates = new java.util.ArrayList<jakarta.persistence.criteria.Predicate>();
            
            // Large amounts (over 100M VND)
            predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get(FIELD_AMOUNT), SUSPICIOUS_AMOUNT_THRESHOLD));
            
            // Failed payments
            predicates.add(criteriaBuilder.equal(root.get(FILTER_STATUS), Status.FAILED));
            
            // Long processing transactions (over 1 hour)
            predicates.add(criteriaBuilder.and(
                criteriaBuilder.greaterThanOrEqualTo(root.get(FIELD_CREATED_AT), LocalDateTime.now().minusHours(1)),
                criteriaBuilder.equal(root.get(FILTER_STATUS), Status.PROCESSING)
            ));
            
            return criteriaBuilder.or(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };
        
        Page<Payment> suspiciousPayments = paymentRepository.findAll(suspiciousSpec, pageable);
        
        Page<Object> suspiciousTransactions = suspiciousPayments.map(payment -> {
            Map<String, Object> suspiciousTransaction = new HashMap<>();
            suspiciousTransaction.put("transactionId", payment.getId());
            suspiciousTransaction.put("type", "PAYMENT");
            suspiciousTransaction.put(FILTER_USER_ID, payment.getUserId());
            suspiciousTransaction.put(FIELD_AMOUNT, payment.getAmount());
            suspiciousTransaction.put(FILTER_STATUS, payment.getStatus());
            suspiciousTransaction.put(FIELD_CREATED_AT, payment.getCreatedAt());
            suspiciousTransaction.put("reason", determineSuspiciousReason(payment));
            return suspiciousTransaction;
        });
        
        return ApiResponse.<Page<Object>>builder()
                .data(suspiciousTransactions)
                .build();
    }
    
    private String determineSuspiciousReason(Payment payment) {
        if (payment.getAmount().compareTo(SUSPICIOUS_AMOUNT_THRESHOLD) >= 0) {
            return "Large amount transaction";
        }
        if (payment.getStatus() == Status.FAILED) {
            return "Multiple failed attempts";
        }
        if (payment.getStatus() == Status.PROCESSING && 
            payment.getCreatedAt().isAfter(LocalDateTime.now().minusHours(1))) {
            return "Long processing time";
        }
        return "Unknown suspicious activity";
    }
    
    // Helper method to convert BankAccount to BankAccountDto
    private BankAccountDto convertToBankAccountDto(BankAccount bankAccount) {
        return BankAccountDto.builder()
                .id(bankAccount.getId())
                .userId(bankAccount.getUserId())
                .bankCode(bankAccount.getBankCode())
                .accountNumberMasked(bankAccount.getAccountNumberMasked())
                .accountName(bankAccount.getAccountName())
                .createdAt(bankAccount.getCreatedAt())
                .build();
    }
}