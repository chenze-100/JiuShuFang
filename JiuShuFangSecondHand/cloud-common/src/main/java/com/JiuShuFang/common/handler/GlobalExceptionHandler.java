package com.JiuShuFang.common.handler;

import com.JiuShuFang.common.Result;
import com.JiuShuFang.common.enums.BusinessErrorCode;
import com.JiuShuFang.common.exception.BusinessException;
import com.baomidou.mybatisplus.core.exceptions.MybatisPlusException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.servlet.http.HttpServletRequest;

import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    @ResponseStatus(HttpStatus.OK)
    public Result<?> handleBusinessException(BusinessException e, HttpServletRequest request) {
        log.error("业务异常: {} - {}", e.getCode(), e.getMessage(), e);
        return Result.error(e.getMessage()).setCode(e.getCode());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.OK)
    public Result<?> handleIllegalArgumentException(IllegalArgumentException e, HttpServletRequest request) {
        log.error("参数异常: {}", e.getMessage(), e);
        return Result.error(e.getMessage()).setCode(BusinessErrorCode.PARAMETER_ERROR.getCode());
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.OK)
    public Result<?> handleException(Exception e, HttpServletRequest request) {
        log.error("系统异常: {}", e.getMessage(), e);
        return Result.error(BusinessErrorCode.SYSTEM_ERROR.getMessage())
                .setCode(BusinessErrorCode.SYSTEM_ERROR.getCode());
    }

    @ExceptionHandler(Throwable.class)
    @ResponseStatus(HttpStatus.OK)
    public Result<?> handleException(Throwable e, HttpServletRequest request) {
        log.error("系统异常: {}", e.getMessage(), e);
        return Result.error(BusinessErrorCode.SYSTEM_ERROR.getMessage())
                .setCode(BusinessErrorCode.SYSTEM_ERROR.getCode());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.OK)
    public Result<?> handleMethodArgumentNotValidException(MethodArgumentNotValidException e, HttpServletRequest request) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + " " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));
        log.error("参数校验异常: {}", message, e);
        return Result.error(message).setCode(BusinessErrorCode.PARAMETER_ERROR.getCode());
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    @ResponseStatus(HttpStatus.OK)
    public Result<?> handleMissingServletRequestParameterException(MissingServletRequestParameterException e, HttpServletRequest request) {
        log.error("缺少请求参数: {}", e.getParameterName(), e);
        return Result.error("缺少请求参数: " + e.getParameterName())
                .setCode(BusinessErrorCode.PARAMETER_ERROR.getCode());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.OK)
    public Result<?> handleHttpMessageNotReadableException(HttpMessageNotReadableException e, HttpServletRequest request) {
        log.error("请求参数解析异常: {}", e.getMessage(), e);
        return Result.error("请求参数解析异常").setCode(BusinessErrorCode.PARAMETER_ERROR.getCode());
    }

    @ExceptionHandler(IllegalStateException.class)
    @ResponseStatus(HttpStatus.OK)
    public Result<?> handleIllegalStateException(IllegalStateException e, HttpServletRequest request) {
        log.error("状态异常: {}", e.getMessage(), e);
        return Result.error(e.getMessage()).setCode(BusinessErrorCode.SYSTEM_ERROR.getCode());
    }

    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.OK)
    public Result<?> handleRuntimeException(RuntimeException e, HttpServletRequest request) {
        log.error("运行时异常：{}", e.getMessage(), e);
        return Result.error(e.getMessage()).setCode(BusinessErrorCode.SYSTEM_ERROR.getCode());
    }

    @ExceptionHandler(MybatisPlusException.class)
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    public Result<?> handleMybatisPlusException(MybatisPlusException e, HttpServletRequest request) {
        log.error("MyBatisPlus 异常：{}", e.getMessage(), e);
        return Result.error("操作失败，请稍后重试").setCode(503);
    }
}
