package com.JiuShuFang.ai.exception;

import com.JiuShuFang.common.exception.BusinessException;

public class UnauthorizedException extends BusinessException {

    public UnauthorizedException(String message) {
        super(401, message);
    }
}
