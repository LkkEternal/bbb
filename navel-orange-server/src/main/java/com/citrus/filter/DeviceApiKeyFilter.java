package com.citrus.filter;

import com.citrus.common.constant.Constants;
import com.citrus.common.result.Result;
import com.citrus.common.result.ResultCode;
import com.citrus.orchard.entity.Device;
import com.citrus.orchard.mapper.DeviceMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@Slf4j
@Component
@RequiredArgsConstructor
public class DeviceApiKeyFilter extends OncePerRequestFilter {

    private final DeviceMapper deviceMapper;
    private final ObjectMapper objectMapper;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return !path.startsWith("/api/device/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String apiKey = request.getHeader(Constants.HEADER_API_KEY);

        if (!StringUtils.hasText(apiKey)) {
            writeError(response, ResultCode.API_KEY_INVALID);
            return;
        }

        // 先用明文查询（兼容直接存入明文Key的情况）
        Device device = deviceMapper.selectByApiKey(apiKey);

        // 查不到再用SHA256哈希查询（兼容注册时哈希存储的情况）
        if (device == null) {
            String hashedKey = sha256(apiKey);
            device = deviceMapper.selectByApiKey(hashedKey);
        }

        if (device == null) {
            log.debug("API Key无效: {}", apiKey.substring(0, Math.min(apiKey.length(), 8)) + "...");
            writeError(response, ResultCode.API_KEY_INVALID);
            return;
        }

        request.setAttribute("device", device);
        filterChain.doFilter(request, response);
    }

    private void writeError(HttpServletResponse response, ResultCode resultCode) throws IOException {
        response.setStatus(401);
        response.setContentType("application/json;charset=UTF-8");
        Result<Void> result = Result.error(resultCode);
        response.getWriter().write(objectMapper.writeValueAsString(result));
    }

    private String sha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256算法不可用", e);
        }
    }
}
