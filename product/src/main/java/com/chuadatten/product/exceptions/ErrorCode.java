package com.chuadatten.product.exceptions;

import lombok.Getter;
import org.springframework.http.HttpStatus;

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
    INSUFFICIENT_STOCK("E032", "Insufficient stock", HttpStatus.BAD_REQUEST), 
    INVALID_REQUEST("E033", "Invalid request", HttpStatus.BAD_REQUEST),

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
