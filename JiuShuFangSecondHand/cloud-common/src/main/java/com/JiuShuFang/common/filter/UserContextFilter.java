package com.JiuShuFang.common.filter;

import com.JiuShuFang.common.util.UserContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * 网关已完成 JWT 校验并下发 X-User-Id 请求头，
 * 下游服务只需把该头里的 userId 放进 UserContext（ThreadLocal），
 * 供 Service 层通过 UserContext.getUserId() 取用，不再重复验签。
 *
 * 对于网关白名单放行的接口（如商品列表），网关不注入 X-User-Id，
 * 此时 UserContext 保持 null，由对应接口自行决定是否需要登录态。
 */
@Component
public class UserContextFilter extends OncePerRequestFilter {

    private static final String HEADER_USER_ID = "X-User-Id";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            String userIdHeader = request.getHeader(HEADER_USER_ID);
            if (userIdHeader != null && !userIdHeader.isBlank()) {
                try {
                    UserContext.setUser(Long.valueOf(userIdHeader.trim()));
                } catch (NumberFormatException ignored) {
                    // X-User-Id 非法时忽略，UserContext 保持 null
                }
            }
            filterChain.doFilter(request, response);
        } finally {
            UserContext.clear();
        }
    }
}
