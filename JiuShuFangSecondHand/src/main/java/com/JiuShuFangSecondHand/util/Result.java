package com.JiuShuFangSecondHand.util;


import com.JiuShuFangSecondHand.enums.Ret;
import lombok.Data;

@Data
public class Result<T> {
    private T data;
    private String message;
    private int code;


    private Result(T data, String message, int code) {
        this.data = data;
        this.message = message;
        this.code = code;
    }

    public static <T> Result<T> success(Ret ret) {
        return new Result<>(null, ret.getMessage(), ret.getCode());
    }

    public static <T> Result<T> success(T data, String message, int code) {
        return new Result<>(data, message, code);
    }

    public static <T> Result<T> success(T data, String message) {
        return new Result<>(data, message, 200);
    }

    public static <T> Result<T> success(T data) {
        return new Result<>(data, Ret.SUCCESS.getMessage(), Ret.SUCCESS.getCode());
    }

    public static <T> Result<T> success() {
        return new Result<>(null ,  Ret.SUCCESS.getMessage(), Ret.SUCCESS.getCode());
    }



    public static <T> Result<T> error(Ret ret) {
        return new Result<>(null, ret.getMessage(), ret.getCode());
    }

    public static <T> Result<T> error(Ret ret , String message) {
        return new Result<>(null, message, ret.getCode());
    }


    public static <T> Result<T> error(String message) {
        return new Result<>(null, message, 500);
    }

    public Result<T> setCode(int code) {
        this.code = code;
        return this;
    }


}
