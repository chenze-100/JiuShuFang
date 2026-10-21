package com.JiuShuFangSecondHand.config;

import com.JiuShuFangSecondHand.interceptor.LoginInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.format.datetime.standard.DateTimeFormatterRegistrar;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.time.format.DateTimeFormatter;

@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final LoginInterceptor loginInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(loginInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/capture/**",        // 图形验证码
                        "/user/login/**",     // 登录相关
                        "/user/check",        // 扫码状态检查
                        "/user/wx/check",     // 微信回调
                        "/code/**",           // 短信验证码
                        "/goods/list",        // 商品列表
                            // 商品详情
                        "/goods/stats/**",    // 商品统计
                        "/category/**",       // 分类
                        // 图片下载放行：img 标签无法携带 token，只放行下载类路径，
                        // 上传接口 /image/picture/upload 仍需登录
                        "/image/download/**",
                        "/image/book/upload/**",
                        "/comment/list/**"   // 评论查看

                );
    }

    @Override
    public void addFormatters(FormatterRegistry registry) {
        DateTimeFormatterRegistrar registrar = new DateTimeFormatterRegistrar();
        registrar.setDateTimeFormatter(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        registrar.registerFormatters(registry);
    }

}