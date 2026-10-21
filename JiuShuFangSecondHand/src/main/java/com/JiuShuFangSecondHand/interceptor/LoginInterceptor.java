package com.JiuShuFangSecondHand.interceptor;

import cn.hutool.json.JSONUtil;
import com.JiuShuFangSecondHand.properties.LoginProperties;
import com.JiuShuFangSecondHand.util.JwtUtil;
import com.JiuShuFangSecondHand.util.Result;
import com.JiuShuFangSecondHand.util.UserContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class LoginInterceptor implements HandlerInterceptor {

    private final LoginProperties loginProperties;

    private final AntPathMatcher antPathMatcher = new AntPathMatcher();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        System.out.println("开始验证登录拦截");
        System.out.println(request.getRequestURI());
        String requestURI = request.getRequestURI();

        String token = request.getHeader("token");
        if (token == null || token.isEmpty()) {
            token = request.getParameter("token");
        }
        if (token == null || !JwtUtil.validateToken(token)) {
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write(JSONUtil.toJsonStr(Result.error("请先登录")));
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            System.out.println("登录拦截失败");
            return false;
        }
        Map<String, Object> claims = JwtUtil.getClaims(token);
        Long id = (Long) claims.get("id");
        if (id == null) {
            return false;
        }
        UserContext.setUser(id);
        System.out.println("登录拦截通过");
        System.out.println("id:"+UserContext.getUserId());
        return true;
    }



    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        com.JiuShuFangSecondHand.util.UserContext.clear();
    }
    private boolean shouldIntercept(String requestURI, List<String> includePaths, List<String> excludePaths) {
        boolean shouldInclude = false;
        for (String includePath : includePaths) {
            if (antPathMatcher.match(includePath, requestURI)) {
                shouldInclude = true;
                break;
            }
        }
        boolean shouldExclude = false;
        for (String excludePath : excludePaths) {
            if (antPathMatcher.match(excludePath, requestURI)) {
                shouldExclude = true;
                break;
            }
        }
        return shouldInclude && !shouldExclude;
    }
}
