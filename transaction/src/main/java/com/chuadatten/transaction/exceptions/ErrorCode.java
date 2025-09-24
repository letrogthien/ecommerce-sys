package com.chuadatten.transaction.exceptions;

import org.springframework.http.HttpStatus;

import lombok.Getter;

@Getter
public enum ErrorCode {
    CREATE_DIRECTORY_FAILED("E014", "Create directory failed", HttpStatus.INTERNAL_SERVER_ERROR),
    UNAUTHORIZED("E001", "Unauthorized", HttpStatus.UNAUTHORIZED),
    INVALID_TOKEN("E004", "Invalid token", HttpStatus.UNAUTHORIZED),

    FILE_UPLOAD_FAILED("E015", "File upload failed", HttpStatus.INTERNAL_SERVER_ERROR),
    FILE_NOT_FOUND("E016", "File not found", HttpStatus.NOT_FOUND),
    CANT_READ_FILE("E017", "Can't read file", HttpStatus.INTERNAL_SERVER_ERROR),
    FILE_EMPTY("E018", "File is empty", HttpStatus.BAD_REQUEST),
    FILE_TOO_BIG("E019", "File too big", HttpStatus.BAD_REQUEST),
    INVALID_CLAIM("E002", "Invalid claim", HttpStatus.UNAUTHORIZED),
    CATEGORY_NOT_FOUND("E026", "Category not found", HttpStatus.NOT_FOUND),
    SLUG_EXIST("E027", "Slug exist", HttpStatus.BAD_REQUEST),
    PRODUCT_NOT_FOUND("E028", "Product not found", HttpStatus.NOT_FOUND),
    U_NOT_HAVE_PERMISSION("E029", "You don't have permission", HttpStatus.FORBIDDEN), 
    PRODUCT_VARIANT_NOT_FOUND("E030", "Product variant not found", HttpStatus.NOT_FOUND), 
    PRODUCT_VARIANT_NOT_ENOUGH_QUANTITY("E031", "Product variant not enough quantity", HttpStatus.BAD_REQUEST), 
    ORDER_NOT_FOUND("E032", "Order not found", HttpStatus.NOT_FOUND), 
    ORDER_CANNOT_CANCEL("E033", "Order cannot cancel", HttpStatus.BAD_REQUEST), 
    ORDER_CANNOT_UPLOAD_PROOF("E034", "Order cannot upload proof", HttpStatus.BAD_REQUEST), 
    ORDER_DISPUTE_ALREADY_OPENED("E035", "Order dispute already opened", HttpStatus.BAD_REQUEST), 
    DISPUTE_NOT_FOUND("E036", "Dispute not found", HttpStatus.NOT_FOUND), 
    REFUND_FOR_ORDER_NOT_FOUND("E037", "Refund for order not found", HttpStatus.NOT_FOUND), 
    ORDER_DISPUTE_NOT_FOUND("E038", "Order dispute not found", HttpStatus.NOT_FOUND),
    
    // Analytics related errors
    INVALID_PARAMETER("E039", "Invalid parameter", HttpStatus.BAD_REQUEST),
    INVALID_DATE_RANGE("E040", "Invalid date range", HttpStatus.BAD_REQUEST),
    MISSING_REQUIRED_PARAMETER("E041", "Missing required parameter", HttpStatus.BAD_REQUEST),
    NOT_IMPLEMENTED("E042", "Feature not implemented yet", HttpStatus.NOT_IMPLEMENTED),
    INTERNAL_SERVER_ERROR("E043", "Internal server error", HttpStatus.INTERNAL_SERVER_ERROR),

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
