package com.citrus.alert.task;

import com.citrus.alert.service.AlertService;
import com.citrus.alert.vo.RiskAssessmentVO;
import com.citrus.auth.entity.User;
import com.citrus.auth.mapper.UserMapper;
import com.citrus.orchard.entity.Orchard;
import com.citrus.orchard.mapper.OrchardMapper;
import com.citrus.push.service.MessageService;
import com.citrus.push.service.SmsPushService;
import com.citrus.push.service.WechatPushService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AlertEvaluationTaskTest {

    @Mock private OrchardMapper orchardMapper;
    @Mock private AlertService alertService;
    @Mock private MessageService messageService;
    @Mock private UserMapper userMapper;
    @Mock private WechatPushService wechatPushService;
    @Mock private SmsPushService smsPushService;

    @InjectMocks
    private AlertEvaluationTask task;

    private Orchard orchard;
    private User owner;
    private RiskAssessmentVO riskL1;
    private RiskAssessmentVO riskL2;
    private RiskAssessmentVO riskL3;

    @BeforeEach
    void setUp() {
        orchard = new Orchard();
        orchard.setId(1L);
        orchard.setOwnerId(10L);
        orchard.setName("测试果园");

        owner = new User();
        owner.setId(10L);
        owner.setPushEnabled(true);
        owner.setPhone("13800138000");
        owner.setEmailEnabled(true);

        riskL1 = new RiskAssessmentVO();
        riskL1.setRiskLevel(1);
        riskL1.setRiskScore(25);
        riskL1.setSuggestions(List.of("建议巡园检查"));

        riskL2 = new RiskAssessmentVO();
        riskL2.setRiskLevel(2);
        riskL2.setRiskScore(50);
        riskL2.setSuggestions(List.of("建议喷洒波尔多液"));

        riskL3 = new RiskAssessmentVO();
        riskL3.setRiskLevel(3);
        riskL3.setRiskScore(80);
        riskL3.setSuggestions(List.of("立即喷洒药剂"));
    }

    @Test
    void evaluateAllOrchards_shouldCreateMessageButNotPush_whenL1() {
        when(orchardMapper.selectList(any())).thenReturn(List.of(orchard));
        when(alertService.assessRisk(1L)).thenReturn(riskL1);
        when(messageService.createAlertMessage(any(), any(), any(), anyInt(), any(), any(), any(), any()))
                .thenReturn(100L);

        task.evaluateAllOrchards();

        verify(messageService).createAlertMessage(eq(1L), eq(10L), isNull(), eq(1), any(), any(), any(), eq("alert"));
        verify(wechatPushService, never()).sendAlertNotification(any(), any(), any(), any(), any(), any(), any());
        verify(smsPushService, never()).sendAlertSms(any(), any(), any(), any());
    }

    @Test
    void evaluateAllOrchards_shouldCreateMessageAndPushWeChat_whenL2() {
        when(orchardMapper.selectList(any())).thenReturn(List.of(orchard));
        when(alertService.assessRisk(1L)).thenReturn(riskL2);
        when(messageService.createAlertMessage(any(), any(), any(), anyInt(), any(), any(), any(), any()))
                .thenReturn(101L);
        when(userMapper.selectById(10L)).thenReturn(owner);
        when(wechatPushService.sendAlertNotification(any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(true);

        task.evaluateAllOrchards();

        verify(messageService).createAlertMessage(eq(1L), eq(10L), isNull(), eq(2), any(), any(), any(), eq("alert"));
        verify(wechatPushService).sendAlertNotification(eq(101L), eq(10L), any(), any(), any(), eq(2), eq("openid_10"));
        verify(smsPushService, never()).sendAlertSms(any(), any(), any(), any());
    }

    @Test
    void evaluateAllOrchards_shouldCreateMessageAndPushWeChatAndSms_whenL3() {
        when(orchardMapper.selectList(any())).thenReturn(List.of(orchard));
        when(alertService.assessRisk(1L)).thenReturn(riskL3);
        when(messageService.createAlertMessage(any(), any(), any(), anyInt(), any(), any(), any(), any()))
                .thenReturn(102L);
        when(userMapper.selectById(10L)).thenReturn(owner);
        when(wechatPushService.sendAlertNotification(any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(true);

        task.evaluateAllOrchards();

        verify(messageService).createAlertMessage(eq(1L), eq(10L), isNull(), eq(3), any(), any(), any(), eq("alert"));
        verify(wechatPushService).sendAlertNotification(eq(102L), eq(10L), any(), any(), any(), eq(3), eq("openid_10"));
        verify(smsPushService).sendAlertSms(eq(10L), eq("13800138000"), any(), eq(3));
    }

    @Test
    void evaluateAllOrchards_shouldSkipOrchard_whenDeleted() {
        orchard.setDeleted(true);
        when(orchardMapper.selectList(any())).thenReturn(List.of(orchard));

        task.evaluateAllOrchards();

        verify(alertService, never()).assessRisk(any());
        verify(messageService, never()).createAlertMessage(any(), any(), any(), anyInt(), any(), any(), any(), any());
    }

    @Test
    void evaluateAllOrchards_shouldSkipPush_whenOwnerNotFound() {
        when(orchardMapper.selectList(any())).thenReturn(List.of(orchard));
        when(alertService.assessRisk(1L)).thenReturn(riskL2);
        when(messageService.createAlertMessage(any(), any(), any(), anyInt(), any(), any(), any(), any()))
                .thenReturn(103L);
        when(userMapper.selectById(10L)).thenReturn(null);

        task.evaluateAllOrchards();

        verify(wechatPushService, never()).sendAlertNotification(any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void evaluateAllOrchards_shouldNotPushWeChat_whenOwnerPushDisabled() {
        owner.setPushEnabled(false);
        when(orchardMapper.selectList(any())).thenReturn(List.of(orchard));
        when(alertService.assessRisk(1L)).thenReturn(riskL2);
        when(messageService.createAlertMessage(any(), any(), any(), anyInt(), any(), any(), any(), any()))
                .thenReturn(104L);
        when(userMapper.selectById(10L)).thenReturn(owner);

        task.evaluateAllOrchards();

        verify(wechatPushService, never()).sendAlertNotification(any(), any(), any(), any(), any(), any(), any());
    }
}
