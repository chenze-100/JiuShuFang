package com.JiuShuFang.ai.controller;

import com.JiuShuFang.common.Result;
import com.wf.captcha.SpecCaptcha;
import com.wf.captcha.base.Captcha;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.TimeUnit;
@Tag(name = "验证码", description = "验证码相关接口")

@RestController
@RequestMapping("/capture")
@RequiredArgsConstructor
public class CaptureController {

    private final StringRedisTemplate stringRedisTemplate;


    @GetMapping("/captcha")
    public void captcha(@RequestParam("key") String key, HttpServletResponse response) throws Exception {
        // 生成验证码
        SpecCaptcha captcha = new SpecCaptcha(135, 32, 4);
        captcha.setCharType(Captcha.TYPE_NUM_AND_UPPER);
        String code = captcha.text();

        // 存入 Redis，有效期 5 分钟
        stringRedisTemplate.opsForValue()
                .set("captcha:" + key, code, 50, TimeUnit.SECONDS);

        // 输出图片流
        response.setContentType("image/png");
        captcha.out(response.getOutputStream());
    }

    @GetMapping(value = "/captcha/check")
    public Result checkCaptcha(
            @RequestParam("key") String key,
            @RequestParam("code") String code
    ) {
        String redisCode = stringRedisTemplate.opsForValue().get("captcha:" + key);
        boolean ok = redisCode != null && redisCode.equalsIgnoreCase(code);
        stringRedisTemplate.delete("captcha:" + key);
        if (ok) {
            return Result.success();
        }
        return Result.error("验证码错误");

    }
}
