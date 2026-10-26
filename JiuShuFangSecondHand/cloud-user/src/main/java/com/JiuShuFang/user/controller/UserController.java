package com.JiuShuFang.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import cn.hutool.json.JSONUtil;
import com.JiuShuFang.user.domain.dto.SetPasswordDto;
import com.JiuShuFang.user.domain.pojo.User;
import com.JiuShuFang.user.domain.vo.UserLoginVO;
import com.JiuShuFang.user.domain.vo.UserVO;
import com.JiuShuFang.user.properties.ServerProperties;
import com.JiuShuFang.user.service.IUserService;
import com.JiuShuFang.common.Result;
import com.JiuShuFang.common.util.UserContext;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @since 2026-04-07
 */
@Slf4j
@Tag(name = "用户", description = "用户相关接口")
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {
    private final IUserService userService;
    private final ServerProperties serverProperties;

    @GetMapping("/login/check/code")
    public Result<UserLoginVO> phoneLoginCheck(
            @RequestParam("phone") String phone,
            @RequestParam("msg") String msg,
            @RequestParam("type") Integer type //0密码登录 1短信验证码登录
    ) {
        return userService.LoginCheck(phone, msg, type);

    }


    @GetMapping("/login/login/QRCode")
    public void loginCheck(HttpServletResponse response) {
        userService.loginCheck(response);
    }


    @Operation(summary = "查询loginCheck")
    @GetMapping("/login/check/QRCode")
    public void login(
            String loginId,
            @RequestParam(value = "code", required = false) String code,
            HttpServletResponse response
    ) throws IOException {
        Result<UserLoginVO> result = userService.QRCodeLogin(code, loginId);
        if (code != null && !code.isEmpty() && result.getCode() == 200 && result.getData() != null) {
            String redirectUrl = serverProperties.getVueUrl()
                    + "/#/wechat-auth?loginId="
                    + URLEncoder.encode(loginId, StandardCharsets.UTF_8)
                    + "&code="
                    + URLEncoder.encode(code, StandardCharsets.UTF_8);
            log.debug("重定向到： {}", redirectUrl);
            response.sendRedirect(redirectUrl);
        }
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(JSONUtil.toJsonStr(result));
    }


    @GetMapping("/wx/check")
    public String wxCheck(
            @RequestParam(value = "signature", required = false) String signature,
            @RequestParam(value = "timestamp", required = false) String timestamp,
            @RequestParam(value = "nonce", required = false) String nonce,
            @RequestParam(value = "echostr", required = false) String echostr
    ) {
        log.debug("signature: {}, timestamp: {}, nonce: {}, echostr: {}", signature, timestamp, nonce, echostr);
        return echostr;
    }

    @GetMapping("/login/confirm")
    public Result<UserLoginVO> confirmLogin(@RequestParam String loginId) {
        return userService.confirmLogin(loginId);
    }


    @GetMapping("/check")
    public Result<String> check(@RequestParam String loginId) {
        return userService.check(loginId);
    }

    @PutMapping("/pwd/forget")
    public Result forgetPassword(@RequestBody @Valid SetPasswordDto dto) {
        return userService.forgetPassword(dto);
    }

    @Operation(summary = "查询forgetPassword")
    @GetMapping("/info/{id}")
    public Result<UserVO> getUserInfo(@PathVariable Long id) {
        UserVO userVO = userService.getUserInfoById(id);
        return Result.success(userVO);
    }

    @PutMapping("/update/simple")
    public Result updateUserSimple(@RequestBody @Valid User user) {
        user.setId(UserContext.getUserId());
        return userService.updateUserSimpleById(user);
    }


    @Operation(summary = "更新updateUserSimple")
    @PutMapping("/pwd/update")
    public Result updatePassword(@RequestBody @Valid SetPasswordDto dto) {
        return userService.updatePassword(dto);
    }


    @Operation(summary = "查询updatePassword")
    @GetMapping("/banance")
    public Result<Double> getUserBalance() {
        return userService.getUserBalance();
    }


    @GetMapping("/logout")
    public Result logout() {
        return Result.success("登出成功");
    }
}
