package com.citrus.filter;

import com.citrus.auth.entity.Role;
import com.citrus.auth.mapper.UserRoleMapper;
import com.citrus.auth.model.LoginUser;
import com.citrus.common.constant.Constants;
import com.citrus.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * JWT 认证过滤器
 * 拦截请求，验证 Token 并设置用户信息到 SecurityContext
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final StringRedisTemplate redisTemplate;
    private final UserRoleMapper userRoleMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        try {
            // 1. 获取请求头中的 Token
            String token = extractToken(request);

            // 2. 如果 Token 为空，直接放行（白名单路径会通过 Security 配置放行）
            if (token == null) {
                filterChain.doFilter(request, response);
                return;
            }

            // 3. 验证 Token 是否有效（签名和过期时间）
            if (!jwtUtil.validateToken(token)) {
                log.debug("Token 无效或已过期");
                filterChain.doFilter(request, response);
                return;
            }

            // 4. 从 Token 解析用户信息
            Long userId = jwtUtil.getUserIdFromToken(token);
            String username = jwtUtil.getUsernameFromToken(token);

            // 5. 验证 Token 是否在 Redis 中有效（是否已被退出登录）
            if (!isTokenValidInRedis(userId, token)) {
                log.debug("Token 已失效（用户已退出登录）: userId={}", userId);
                filterChain.doFilter(request, response);
                return;
            }

            // 6. 查询用户角色
            List<Role> roles = userRoleMapper.selectRolesByUserId(userId);
            List<SimpleGrantedAuthority> authorities = roles.stream()
                    .map(role -> new SimpleGrantedAuthority(role.getCode()))
                    .collect(Collectors.toList());

            // 7. 创建 LoginUser
            LoginUser loginUser = new LoginUser();
            loginUser.setUserId(userId);
            loginUser.setUsername(username);
            loginUser.setAuthorities(authorities);

            // 8. 创建 Authentication 对象
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(loginUser, null, authorities);

            // 9. 设置 SecurityContext
            SecurityContextHolder.getContext().setAuthentication(authentication);

            log.debug("用户认证成功: userId={}, username={}, roles={}", userId, username, 
                    roles.stream().map(Role::getCode).collect(Collectors.joining(",")));

        } catch (Exception e) {
            log.error("JWT 认证异常: {}", e.getMessage());
        }

        // 10. 放行请求
        filterChain.doFilter(request, response);
    }

    /**
     * 从请求头提取 Token
     */
    private String extractToken(HttpServletRequest request) {
        String authHeader = request.getHeader(Constants.HEADER_AUTHORIZATION);
        if (StringUtils.hasText(authHeader) && authHeader.startsWith(Constants.TOKEN_PREFIX)) {
            return authHeader.substring(Constants.TOKEN_PREFIX.length());
        }
        return null;
    }

    /**
     * 验证 Token 是否在 Redis 中有效
     * 检查用户是否已退出登录或重新登录
     */
    private boolean isTokenValidInRedis(Long userId, String token) {
        try {
            String redisKey = Constants.REDIS_USER_TOKEN + userId;
            String storedToken = redisTemplate.opsForValue().get(redisKey);
            
            // 如果 Redis 中没有存储的 Token，说明已退出登录
            if (storedToken == null) {
                return false;
            }
            
            // 比较 Token 是否一致
            return storedToken.equals(token);
        } catch (Exception e) {
            log.error("Redis 验证异常: {}", e.getMessage());
            // Redis 异常时，降级为只验证 JWT 签名和过期时间
            return true;
        }
    }
}
