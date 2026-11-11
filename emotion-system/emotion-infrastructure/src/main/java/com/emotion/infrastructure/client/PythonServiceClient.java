package com.emotion.infrastructure.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class PythonServiceClient {

    private final RestTemplate restTemplate;

    @Value("${python.service.url:http://localhost:8001}")
    private String pythonServiceUrl;

    public Map<String, Object> detectEmotion(MultipartFile file) {
        String url = pythonServiceUrl + "/api/inference/image";
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            org.springframework.util.LinkedMultiValueMap<String, Object> body = new org.springframework.util.LinkedMultiValueMap<>();
            body.add("file", file.getResource());
            HttpEntity<org.springframework.util.LinkedMultiValueMap<String, Object>> request = new HttpEntity<>(body, headers);
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, request, Map.class);
            return response.getBody();
        } catch (Exception e) {
            log.error("调用Python检测服务失败", e);
            throw new RuntimeException("检测服务不可用");
        }
    }

    public boolean checkHealth() {
        try {
            ResponseEntity<Map> response = restTemplate.getForEntity(
                    pythonServiceUrl + "/api/inference/health", Map.class);
            return response.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            return false;
        }
    }
}
