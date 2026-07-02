package com.citrus.push.task;

import com.citrus.auth.entity.User;
import com.citrus.auth.mapper.UserMapper;
import com.citrus.collector.entity.DiseaseDetection;
import com.citrus.collector.mapper.DiseaseDetectionMapper;
import com.citrus.orchard.entity.Orchard;
import com.citrus.orchard.mapper.OrchardMapper;
import com.citrus.push.entity.AlertMessage;
import com.citrus.push.mapper.AlertMessageMapper;
import com.citrus.push.service.SmsPushService;
import com.citrus.push.service.WechatPushService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DetectionEscalationTaskTest {

    @Mock private DiseaseDetectionMapper diseaseDetectionMapper;
    @Mock private OrchardMapper orchardMapper;
    @Mock private UserMapper userMapper;
    @Mock private AlertMessageMapper alertMessageMapper;
    @Mock private WechatPushService wechatPushService;
    @Mock private SmsPushService smsPushService;
    @Mock private ObjectMapper objectMapper;

    @InjectMocks
    private DetectionEscalationTask task;

    private Orchard orchard;
    private User owner;
    private DiseaseDetection det;

    @BeforeEach
    void setUp() {
        orchard = new Orchard();
        orchard.setId(100L);
        orchard.setOwnerId(10L);

        owner = new User();
        owner.setId(10L);
        owner.setPushEnabled(true);
        owner.setPhone("13800138000");

        det = new DiseaseDetection();
        det.setId(1L);
        det.setOrchardId(100L);
        det.setProcessed(false);
        det.setCreatedAt(LocalDateTime.now().minusHours(30));
        det.setDiseaseNameRaw("溃疡病");
    }

    @Test
    void checkAndEscalate_shouldSkip_whenNoUnprocessedDetections() {
        when(orchardMapper.selectById(100L)).thenReturn(orchard);
        when(userMapper.selectById(10L)).thenReturn(owner);
        when(diseaseDetectionMapper.selectList(any())).thenReturn(List.of());

        task.checkAndEscalate(100L);

        verify(alertMessageMapper, never()).insert((AlertMessage) argThat((AlertMessage msg) -> true));
        verify(wechatPushService, never()).sendAlertNotification(any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void checkAndEscalate_shouldCreateEscalationAndPushWeChat() {
        when(orchardMapper.selectById(100L)).thenReturn(orchard);
        when(userMapper.selectById(10L)).thenReturn(owner);
        when(diseaseDetectionMapper.selectList(any())).thenReturn(List.of(det));
        when(alertMessageMapper.selectCount(any())).thenReturn(0L);
        when(wechatPushService.sendAlertNotification(any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(true);
        when(smsPushService.sendAlertSms(any(), any(), any(), any())).thenReturn(true);

        task.checkAndEscalate(100L);

        verify(alertMessageMapper).insert((AlertMessage) argThat((AlertMessage msg) -> {
            assertThat(msg.getTitle()).isEqualTo("严重超时未处理");
            assertThat(msg.getMessageType()).isEqualTo("escalation");
            assertThat(msg.getRelatedType()).isEqualTo("detection");
            assertThat(msg.getRelatedId()).isEqualTo(1L);
            assertThat(msg.getUserId()).isEqualTo(10L);
            return true;
        }));

        verify(wechatPushService).sendAlertNotification(
                any(), eq(10L), eq("严重超时未处理"), any(), any(), eq(1), eq("openid_10"));
        verify(smsPushService).sendAlertSms(eq(10L), eq("13800138000"), any(), eq(1));
    }

    @Test
    void checkAndEscalate_shouldNotDuplicateEscalation() {
        when(orchardMapper.selectById(100L)).thenReturn(orchard);
        when(userMapper.selectById(10L)).thenReturn(owner);
        when(diseaseDetectionMapper.selectList(any())).thenReturn(List.of(det));
        when(alertMessageMapper.selectCount(any())).thenReturn(1L); // 已有

        task.checkAndEscalate(100L);

        verify(alertMessageMapper, never()).insert((AlertMessage) argThat((AlertMessage msg) -> true));
        verify(wechatPushService, never()).sendAlertNotification(any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void checkAndEscalate_shouldSkip_whenOrchardDeleted() {
        orchard.setDeleted(true);
        when(orchardMapper.selectById(100L)).thenReturn(orchard);

        task.checkAndEscalate(100L);

        verify(diseaseDetectionMapper, never()).selectList(any());
    }

    @Test
    void checkAndEscalate_shouldSkip_whenOwnerNotFound() {
        when(orchardMapper.selectById(100L)).thenReturn(orchard);
        when(userMapper.selectById(10L)).thenReturn(null);

        task.checkAndEscalate(100L);

        verify(diseaseDetectionMapper, never()).selectList(any());
    }

    @Test
    void checkAndEscalate_shouldSkip_whenDetectionNotYetExpired() {
        det.setCreatedAt(LocalDateTime.now().minusHours(10)); // 未超24h

        when(orchardMapper.selectById(100L)).thenReturn(orchard);
        when(userMapper.selectById(10L)).thenReturn(owner);
        when(diseaseDetectionMapper.selectList(any())).thenReturn(List.of());

        task.checkAndEscalate(100L);

        verify(alertMessageMapper, never()).insert((AlertMessage) argThat((AlertMessage msg) -> true));
    }

    @Test
    void checkAndEscalate_shouldNotPushWeChat_whenOwnerPushDisabled() {
        owner.setPushEnabled(false);

        when(orchardMapper.selectById(100L)).thenReturn(orchard);
        when(userMapper.selectById(10L)).thenReturn(owner);
        when(diseaseDetectionMapper.selectList(any())).thenReturn(List.of(det));
        when(alertMessageMapper.selectCount(any())).thenReturn(0L);

        task.checkAndEscalate(100L);

        verify(alertMessageMapper).insert((AlertMessage) argThat((AlertMessage msg) -> true));
        verify(wechatPushService, never()).sendAlertNotification(any(), any(), any(), any(), any(), any(), any());
    }
}
