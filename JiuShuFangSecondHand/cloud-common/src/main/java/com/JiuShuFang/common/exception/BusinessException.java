package com.JiuShuFang.common.exception;

import com.JiuShuFang.common.Ret;
import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {

    private final int code;

    public BusinessException(String msg) {
        super(msg);
        this.code = Ret.INTERNAL_ERROR.getCode();
    }

    public BusinessException(int code, String msg) {
        super(msg);
        this.code = code;
    }
}
