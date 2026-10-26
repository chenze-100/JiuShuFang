package com.JiuShuFang.common;

import lombok.Data;

import java.io.Serializable;

@Data
public class Result<T> implements Serializable {

    private Integer code;
    private String message;
    private T data;

    private Result() {}

    public static <T> Result<T> ok(T data) {
        Result<T> r = new Result<>();
        r.setCode(Ret.SUCCESS.getCode());
        r.setMessage(Ret.SUCCESS.getMsg());
        r.setData(data);
        return r;
    }

    public static <T> Result<T> ok() {
        return ok(null);
    }

    public static <T> Result<T> fail(String msg) {
        Result<T> r = new Result<>();
        r.setCode(Ret.INTERNAL_ERROR.getCode());
        r.setMessage(msg);
        return r;
    }

    public static <T> Result<T> fail(int code, String msg) {
        Result<T> r = new Result<>();
        r.setCode(code);
        r.setMessage(msg);
        return r;
    }

    // ========== 以下为兼容单体项目原有 API ==========

    public static <T> Result<T> success(T data, String message, int code) {
        Result<T> r = new Result<>();
        r.setCode(code);
        r.setMessage(message);
        r.setData(data);
        return r;
    }

    public static <T> Result<T> success(T data, String message) {
        return success(data, message, Ret.SUCCESS.getCode());
    }

    public static <T> Result<T> success(T data) {
        return success(data, Ret.SUCCESS.getMsg(), Ret.SUCCESS.getCode());
    }

    public static <T> Result<T> success() {
        return success(null);
    }

    public static <T> Result<T> error(String message) {
        return success(null, message, Ret.INTERNAL_ERROR.getCode());
    }

    public static <T> Result<T> error(int code, String message) {
        return success(null, message, code);
    }

    public Result<T> setCode(int code) {
        this.code = code;
        return this;
    }
}
