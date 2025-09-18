package com.chuadatten.wallet.exceptions;



import org.springframework.http.HttpStatus;

import lombok.Getter;


@Getter
public enum ErrorCode {
    UNAUTHORIZED("E001", "Unauthorized", HttpStatus.UNAUTHORIZED), 
    INVALID_CLAIM("E002", "Invalid claim", HttpStatus.UNAUTHORIZED), 
    USER_NOT_FOUND("E003", "User not found", HttpStatus.NOT_FOUND), 
    INVALID_TOKEN("E004", "Invalid token", HttpStatus.UNAUTHORIZED), 
    TOKEN_GENERATION_FAILED("E005", "Token generation failed", HttpStatus.UNAUTHORIZED), 
    ROLE_NOT_FOUND("E006", "Role not found", HttpStatus.NOT_FOUND), 
    PASSWORD_MISMATCH("E007", "Password mismatch", HttpStatus.BAD_REQUEST), 
    PASSWORDS_DO_NOT_MATCH("E008", "Passwords do not match", HttpStatus.BAD_REQUEST), 
    PASSWORD_RECENTLY_USED("E009", "Password recently used", HttpStatus.BAD_REQUEST),
    ACCESS_DENIED("E010", "Access denied", HttpStatus.FORBIDDEN),
    INVALID_INPUT("E011", "Invalid input", HttpStatus.BAD_REQUEST),
    NOT_FOUND("E012", "Not found", HttpStatus.NOT_FOUND), 
    NOT_KYC("E013", "Not KYC", HttpStatus.BAD_REQUEST), 
    CREATE_DIRECTORY_FAILED("E014", "Create directory failed", HttpStatus.INTERNAL_SERVER_ERROR), 
    FILE_UPLOAD_FAILED("E015", "File upload failed", HttpStatus.INTERNAL_SERVER_ERROR), 
    FILE_NOT_FOUND("E016", "File not found", HttpStatus.NOT_FOUND), 
    CANT_READ_FILE("E017", "Can't read file", HttpStatus.INTERNAL_SERVER_ERROR), 
    FILE_EMPTY("E018", "File is empty", HttpStatus.BAD_REQUEST), 
    FILE_TOO_BIG("E019", "File too big", HttpStatus.BAD_REQUEST), 
    VERIFICATION_NOT_EXIST("E020", "Verification not exist", HttpStatus.BAD_REQUEST), 
    TRANSACTION_NOT_FOUND("E021", "Transaction not found", HttpStatus.NOT_FOUND), 
    INVALID_REQUEST("E022", "Invalid request", HttpStatus.BAD_REQUEST), 
     PREFERENCE_NOT_FOUND("E023", "Preference not found", HttpStatus.NOT_FOUND), 
     BILLING_ADDRESS_NOT_FOUND("E024", "Billing address not found", HttpStatus.NOT_FOUND), 
     PAYMENT_NOT_FOUND("E025", "Payment not found", HttpStatus.NOT_FOUND), 
     PAYMENT_PROCESSING("E026", "Payment processing", HttpStatus.BAD_REQUEST), 
     PAYMENT_SUCCESS("E027", "Payment success", HttpStatus.OK), 
     PAYMENT_FAILED("E028", "Payment failed", HttpStatus.BAD_REQUEST), 
     PAYMENT_CANCELED("E029", "Payment canceled", HttpStatus.BAD_REQUEST), 
     PAYMENT_REFUNDED("E030", "Payment refunded", HttpStatus.BAD_REQUEST), 
     PAYMENT_CREATED("E031", "Payment created", HttpStatus.CREATED), 
     PAYMENT_ERROR("E032", "Payment error", HttpStatus.BAD_REQUEST), 
     PAYMENT_NOT_SUCCEEDED("E032", "Payment not succeeded", HttpStatus.BAD_REQUEST), 
     WALLET_NOT_FOUND("E033", "Wallet not found", HttpStatus.NOT_FOUND), 
     INSUFFICIENT_BALANCE("E034", "Insufficient balance", HttpStatus.BAD_REQUEST),
     WITHDRAWAL_REQUEST_NOT_FOUND("E035", "Withdrawal request not found", HttpStatus.NOT_FOUND),
     WITHDRAWAL_REQUEST_ALREADY_PROCESSED("E036", "Withdrawal request already processed", HttpStatus.BAD_REQUEST),
    ;

    private final String code;
    private final String message;
    private final HttpStatus status;

    ErrorCode(String code, String message, HttpStatus status) {
        this.code = code;
        this.message = message;
        this.status = status;
    }

}
