package com.citrus.push.service.impl;

import com.citrus.auth.entity.User;
import com.citrus.auth.mapper.UserMapper;
import com.citrus.calendar.entity.FarmingCalendar;
import com.citrus.calendar.mapper.FarmingCalendarMapper;
import com.citrus.collector.entity.DiseaseCatalog;
import com.citrus.collector.entity.DiseaseDetection;
import com.citrus.collector.entity.EnvironmentData;
import com.citrus.collector.event.DetectionCreatedEvent;
import com.citrus.collector.mapper.DiseaseCatalogMapper;
import com.citrus.collector.mapper.DiseaseDetectionMapper;
import com.citrus.collector.mapper.EnvironmentDataMapper;
import com.citrus.knowledge.service.RagService;
import com.citrus.knowledge.vo.RagResponse;
import com.citrus.orchard.entity.Orchard;
import com.citrus.orchard.mapper.OrchardMapper;
import com.citrus.push.entity.AlertMessage;
import com.citrus.push.mapper.AlertMessageMapper;
import com.citrus.push.service.SmsPushService;
import com.citrus.push.service.WechatPushService;
import com.citrus.push.task.DetectionEscalationTask;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DetectionCreatedEventHandlerTest {

    @Mock private DiseaseDetectionMapper diseaseDetectionMapper;
    @Mock private DiseaseCatalogMapper diseaseCatalogMapper;
    @Mock private EnvironmentDataMapper environmentDataMapper;
    @Mock private FarmingCalendarMapper farmingCalendarMapper;
    @Mock private OrchardMapper orchardMapper;
    @Mock private UserMapper userMapper;
    @Mock private AlertMessageMapper alertMessageMapper;
    @Mock private RagService ragService;
    @Mock private WechatPushService wechatPushService;
    @Mock private SmsPushService smsPushService;
    @Mock private DetectionEscalationTask detectionEscalationTask;
    @Mock private ObjectMapper objectMapper;

    @InjectMocks
    private DetectionCreatedEventHandler eventHandler;

    private DiseaseDetection detection;
    private Orchard orchard;
    private User owner;
    private RagResponse ragResponse;

    @BeforeEach
    void setUp() {
        detection = new DiseaseDetection();
        detection.setId(1L);
        detection.setOrchardId(100L);
        detection.setDiseaseCatalogId(5);
        detection.setDiseaseNameRaw("溃疡病");
        detection.setTopClassCode("S14");
        detection.setTopProbability(new BigDecimal("0.92"));
        detection.setProcessed(false);
        detection.setCreatedAt(LocalDateTime.now());

        orchard = new Orchard();
        orchard.setId(100L);
        orchard.setOwnerId(10L);
        orchard.setName("示范果园");

        owner = new User();
        owner.setId(10L);
        owner.setPushEnabled(true);
        owner.setEmailEnabled(true);
        owner.setPhone("13800138000");

        ragResponse = new RagResponse();
        ragResponse.setAnswer(
                "【紧急程度】高\n【防治措施】喷施波尔多液\n【不处理的后果】可能导致严重减产");
        ragResponse.setConfidence(0.85);
    }

    @Test
    void handleDetectionCreated_shouldPushWeChat() {
        when(diseaseDetectionMapper.selectBatchIds(List.of(1L))).thenReturn(List.of(detection));
        when(orchardMapper.selectById(100L)).thenReturn(orchard);
        when(userMapper.selectById(10L)).thenReturn(owner);
        when(environmentDataMapper.selectOne(any())).thenReturn(null);
        when(farmingCalendarMapper.selectList(any())).thenReturn(List.of());
        when(diseaseCatalogMapper.selectById(5)).thenReturn(createCatalog("溃疡病", 2));
        when(ragService.queryForDetection(any(), any(), any(), any())).thenReturn(ragResponse);
        when(wechatPushService.sendAlertNotification(any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(true);

        eventHandler.handleDetectionCreated(new DetectionCreatedEvent(List.of(1L)));

        verify(alertMessageMapper).insert((AlertMessage) argThat((AlertMessage msg) -> true));
        verify(wechatPushService).sendAlertNotification(
                any(), eq(10L), contains("溃疡病"), any(), any(), eq(2), eq("openid_10"));
        verify(detectionEscalationTask).checkAndEscalate(100L);
    }

    @Test
    void handleDetectionCreated_shouldNotPush_whenPushDisabled() {
        owner.setPushEnabled(false);

        when(diseaseDetectionMapper.selectBatchIds(List.of(1L))).thenReturn(List.of(detection));
        when(orchardMapper.selectById(100L)).thenReturn(orchard);
        when(userMapper.selectById(10L)).thenReturn(owner);
        when(environmentDataMapper.selectOne(any())).thenReturn(null);
        when(farmingCalendarMapper.selectList(any())).thenReturn(List.of());
        when(diseaseCatalogMapper.selectById(5)).thenReturn(createCatalog("溃疡病", 2));
        when(ragService.queryForDetection(any(), any(), any(), any())).thenReturn(ragResponse);

        eventHandler.handleDetectionCreated(new DetectionCreatedEvent(List.of(1L)));

        verify(alertMessageMapper).insert((AlertMessage) argThat((AlertMessage msg) -> true));
        verify(wechatPushService, never()).sendAlertNotification(any(), any(), any(), any(), any(), any(), any());
        verify(detectionEscalationTask).checkAndEscalate(100L);
    }

    @Test
    void handleDetectionCreated_shouldFallbackAdvice_whenRagReturnsNull() {
        when(diseaseDetectionMapper.selectBatchIds(List.of(1L))).thenReturn(List.of(detection));
        when(orchardMapper.selectById(100L)).thenReturn(orchard);
        when(userMapper.selectById(10L)).thenReturn(owner);
        when(environmentDataMapper.selectOne(any())).thenReturn(null);
        when(farmingCalendarMapper.selectList(any())).thenReturn(List.of());
        when(diseaseCatalogMapper.selectById(5)).thenReturn(createCatalog("溃疡病", 2));
        when(ragService.queryForDetection(any(), any(), any(), any())).thenReturn(null);
        when(wechatPushService.sendAlertNotification(any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(true);

        eventHandler.handleDetectionCreated(new DetectionCreatedEvent(List.of(1L)));

        verify(alertMessageMapper).insert((AlertMessage) argThat((AlertMessage msg) -> {
            assertThat(msg.getContent()).isEqualTo("在果园「示范果园」检测到溃疡病");
            assertThat(msg.getAdvice()).isNull();
            return true;
        }));
    }

    @Test
    void handleDetectionCreated_shouldUseCatalogName_whenAvailable() {
        detection.setDiseaseCatalogId(5);
        detection.setDiseaseNameRaw("Citrus_canker");

        when(diseaseDetectionMapper.selectBatchIds(List.of(1L))).thenReturn(List.of(detection));
        when(orchardMapper.selectById(100L)).thenReturn(orchard);
        when(userMapper.selectById(10L)).thenReturn(owner);
        when(environmentDataMapper.selectOne(any())).thenReturn(null);
        when(farmingCalendarMapper.selectList(any())).thenReturn(List.of());
        when(diseaseCatalogMapper.selectById(5)).thenReturn(createCatalog("溃疡病", 2));
        when(ragService.queryForDetection(eq("溃疡病"), any(), any(), any())).thenReturn(ragResponse);
        when(wechatPushService.sendAlertNotification(any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(true);

        eventHandler.handleDetectionCreated(new DetectionCreatedEvent(List.of(1L)));

        verify(wechatPushService).sendAlertNotification(
                any(), anyLong(), contains("溃疡病"), any(), any(), anyInt(), anyString());
    }

    @Test
    void handleDetectionCreated_shouldSkipEmptyEvent() {
        eventHandler.handleDetectionCreated(new DetectionCreatedEvent(List.of()));

        verify(diseaseDetectionMapper, never()).selectBatchIds(any());
    }

    private DiseaseCatalog createCatalog(String name, int level) {
        DiseaseCatalog c = new DiseaseCatalog();
        c.setId(5);
        c.setDiseaseName(name);
        c.setDiseaseCode("S14");
        c.setSeverityLevel(level);
        return c;
    }
}
