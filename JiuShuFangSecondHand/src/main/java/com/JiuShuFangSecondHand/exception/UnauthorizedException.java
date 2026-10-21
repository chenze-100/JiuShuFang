package com.JiuShuFangSecondHand.exception;

import com.JiuShuFangSecondHand.enums.BusinessErrorCode;

public class UnauthorizedException extends BusinessException {
    public UnauthorizedException(BusinessErrorCode errorCode) {
        super(errorCode);
    }

    public UnauthorizedException(String message) {
        super(401, message);
    }

    public UnauthorizedException(BusinessErrorCode errorCode, String message) {
        super(errorCode, message);
    }
}
