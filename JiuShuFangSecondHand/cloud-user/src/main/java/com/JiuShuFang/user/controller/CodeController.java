package com.JiuShuFang.user.controller;

import io.swagger.v3.oas.annotations.tags.Tag;

import com.JiuShuFang.user.domain.dto.CodeSendDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.JiuShuFang.user.service.ICodeService;
import com.JiuShuFang.common.Result;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@Tag(name = "验证码", description = "验证码相关接口")

@RestController
@RequestMapping("/code")
@Slf4j
@RequiredArgsConstructor
public class CodeController {

    private final ICodeService codeService;

    @GetMapping("/code")
    public Result<String> code(@RequestParam("phone") String phone) {
        return codeService.generateCode(phone);
    }

    @GetMapping("QRCode")
    public Result<String> QRCode(@RequestParam("phone") String phone) {
        return Result.success();
    }

    @PostMapping("/send")
    public Result sendCode(@RequestBody @Valid CodeSendDto dto) {
        return codeService.sendCode(dto);
    }
}
