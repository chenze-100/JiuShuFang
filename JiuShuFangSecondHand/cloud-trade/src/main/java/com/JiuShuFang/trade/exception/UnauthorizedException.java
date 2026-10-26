package com.JiuShuFang.trade.exception;

import com.JiuShuFang.common.enums.BusinessErrorCode;
import com.JiuShuFang.common.exception.BusinessException;

public class UnauthorizedException extends BusinessException {
    public UnauthorizedException(BusinessErrorCode errorCode) {
        super(errorCode.getCode(), errorCode.getMessage());
    }

    public UnauthorizedException(String message) {
        super(401, message);
    }

    public UnauthorizedException(BusinessErrorCode errorCode, String message) {
        super(errorCode.getCode(), message);
    }
}
