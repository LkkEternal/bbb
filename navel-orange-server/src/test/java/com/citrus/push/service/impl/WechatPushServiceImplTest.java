package com.citrus.push.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Flow;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class WechatPushServiceImplTest {

    private WechatPushServiceImpl pushService;
    private HttpClient httpClient;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() throws Exception {
        pushService = new WechatPushServiceImpl(objectMapper);
        setField("appid", "test_appid");
        setField("secret", "test_secret");
        setField("alertTemplateId", "template_alert_test");
        httpClient = mock(HttpClient.class);
        pushService.setHttpClient(httpClient);
    }

    private void setField(String name, String value) throws Exception {
        Field f = WechatPushServiceImpl.class.getDeclaredField(name);
        f.setAccessible(true);
        f.set(pushService, value);
    }

    @SuppressWarnings("unchecked")
    private HttpResponse<String> mockHttpResponse(String body) {
        HttpResponse<String> resp = mock(HttpResponse.class);
        when(resp.body()).thenReturn(body);
        return resp;
    }

    @Test
    void sendAlertNotification_shouldReturnTrue_whenWechatReturnsSuccess()
            throws Exception {
        HttpResponse<String> tokenResp = mockHttpResponse("{\"access_token\":\"mock_token\"}");
        HttpResponse<String> pushResp = mockHttpResponse("{\"errcode\":0,\"errmsg\":\"ok\"}");
        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(tokenResp, pushResp);

        boolean result = pushService.sendAlertNotification(
                42L, 1L, "溃疡病预警",
                "检测到柑橘溃疡病",
                "建议喷施波尔多液", 2, "mock_openid");

        assertThat(result).isTrue();
    }

    @Test
    void sendAlertNotification_shouldReturnFalse_whenUserRefused()
            throws Exception {
        HttpResponse<String> tokenResp = mockHttpResponse("{\"access_token\":\"mock_token\"}");
        HttpResponse<String> pushResp = mockHttpResponse("{\"errcode\":43101,\"errmsg\":\"user refuse\"}");
        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(tokenResp, pushResp);

        boolean result = pushService.sendAlertNotification(
                42L, 1L, "test", "test", "test", 1, "openid");

        assertThat(result).isFalse();
    }

    @Test
    void sendAlertNotification_shouldReturnFalse_whenTokenFails()
            throws Exception {
        HttpResponse<String> tokenResp = mockHttpResponse("{\"errcode\":40001,\"errmsg\":\"invalid credential\"}");
        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(tokenResp);

        boolean result = pushService.sendAlertNotification(
                42L, 1L, "test", "test", "test", 1, "openid");

        assertThat(result).isFalse();
    }

    @Test
    void sendAlertNotification_shouldIncludeCorrectPageAndFields() throws Exception {
        HttpResponse<String> tokenResp = mockHttpResponse("{\"access_token\":\"mock_token\"}");
        HttpResponse<String> pushResp = mockHttpResponse("{\"errcode\":0,\"errmsg\":\"ok\"}");

        ArgumentCaptor<HttpRequest> requestCaptor = ArgumentCaptor.captor();
        when(httpClient.send(requestCaptor.capture(), any(HttpResponse.BodyHandler.class)))
                .thenReturn(tokenResp, pushResp);

        pushService.sendAlertNotification(
                99L, 1L, "溃疡病预警",
                "检测到柑橘溃疡病",
                "建议喷施波尔多液", 3, "openid_99");

        HttpRequest pushRequest = requestCaptor.getAllValues().get(1);
        String body = readBodyToString(pushRequest);

        assertThat(body).isNotNull();
        assertThat(body).contains("\"touser\":\"openid_99\"");
        assertThat(body).contains("\"template_id\":\"template_alert_test\"");
        assertThat(body).contains("\"page\":\"pages/alert/detail?id=99\"");
        assertThat(body).contains("\"phrase4\":{\"value\":\"L3\"}");
        assertThat(body).contains("\"date3\"");
    }

    private String readBodyToString(HttpRequest req) {
        StringBuilder sb = new StringBuilder();
        req.bodyPublisher().ifPresent(publisher -> {
            CompletableFuture<Void> future = new CompletableFuture<>();
            publisher.subscribe(new Flow.Subscriber<>() {
                public void onSubscribe(Flow.Subscription s) { s.request(Long.MAX_VALUE); }
                public void onNext(ByteBuffer buf) {
                    byte[] bytes = new byte[buf.remaining()];
                    buf.get(bytes);
                    sb.append(new String(bytes));
                }
                public void onError(Throwable t) { future.completeExceptionally(t); }
                public void onComplete() { future.complete(null); }
            });
            future.join();
        });
        return sb.toString();
    }
}
