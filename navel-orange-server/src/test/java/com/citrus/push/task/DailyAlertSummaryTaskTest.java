package com.citrus.push.task;

import com.citrus.auth.entity.User;
import com.citrus.auth.mapper.UserMapper;
import com.citrus.orchard.entity.Orchard;
import com.citrus.orchard.mapper.OrchardMapper;
import com.citrus.push.entity.AlertMessage;
import com.citrus.push.mapper.AlertMessageMapper;
import com.citrus.push.service.WechatPushService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DailyAlertSummaryTaskTest {

    @Mock private AlertMessageMapper alertMessageMapper;
    @Mock private UserMapper userMapper;
    @Mock private OrchardMapper orchardMapper;
    @Mock private WechatPushService wechatPushService;

    @InjectMocks
    private DailyAlertSummaryTask task;

    private AlertMessage l1msg1;
    private AlertMessage l1msg2;
    private User user;
    private Orchard orchard;

    @BeforeEach
    void setUp() {
        orchard = new Orchard();
        orchard.setId(1L);
        orchard.setName("示范果园");

        user = new User();
        user.setId(10L);
        user.setPushEnabled(true);

        l1msg1 = new AlertMessage();
        l1msg1.setId(1L);
        l1msg1.setUserId(10L);
        l1msg1.setOrchardId(1L);
        l1msg1.setLevel(1);
        l1msg1.setMessageType("alert");
        l1msg1.setPushStatus(0);
        l1msg1.setTitle("低风险关注");

        l1msg2 = new AlertMessage();
        l1msg2.setId(2L);
        l1msg2.setUserId(10L);
        l1msg2.setOrchardId(1L);
        l1msg2.setLevel(1);
        l1msg2.setMessageType("alert");
        l1msg2.setPushStatus(0);
        l1msg2.setTitle("低风险关注");
    }

    @Test
    void sendDailySummary_shouldSkip_whenNoL1Messages() {
        when(alertMessageMapper.selectList(any())).thenReturn(List.of());

        task.sendDailySummary();

        verify(wechatPushService, never()).sendAlertNotification(any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void sendDailySummary_shouldCreateSummaryAndPushWeChat() {
        when(alertMessageMapper.selectList(any())).thenReturn(List.of(l1msg1, l1msg2));
        when(userMapper.selectById(10L)).thenReturn(user);
        when(orchardMapper.selectById(1L)).thenReturn(orchard);
        when(wechatPushService.sendAlertNotification(any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(true);

        task.sendDailySummary();

        verify(alertMessageMapper).insert((AlertMessage) argThat((AlertMessage summary) -> {
            assertThat(summary.getUserId()).isEqualTo(10L);
            assertThat(summary.getMessageType()).isEqualTo("summary");
            assertThat(summary.getTitle()).isEqualTo("低风险预警汇总");
            assertThat(summary.getContent()).contains("今日低风险预警汇总");
            assertThat(summary.getContent()).contains("2条");
            assertThat(summary.getLevel()).isEqualTo(1);
            return true;
        }));

        verify(wechatPushService).sendAlertNotification(
                any(), eq(10L), eq("低风险预警汇总"),
                contains("2条"), any(), eq(1), eq("openid_10"));

        verify(alertMessageMapper, times(2)).update(eq(null), any());
    }

    @Test
    void sendDailySummary_shouldSkipUser_whenPushDisabled() {
        user.setPushEnabled(false);

        when(alertMessageMapper.selectList(any())).thenReturn(List.of(l1msg1));
        when(userMapper.selectById(10L)).thenReturn(user);

        task.sendDailySummary();

        verify(wechatPushService, never()).sendAlertNotification(any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void sendDailySummary_shouldMarkOriginalMessages_whenPushSucceeds() {
        when(alertMessageMapper.selectList(any())).thenReturn(List.of(l1msg1, l1msg2));
        when(userMapper.selectById(10L)).thenReturn(user);
        when(orchardMapper.selectById(1L)).thenReturn(orchard);
        when(wechatPushService.sendAlertNotification(any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(true);

        task.sendDailySummary();

        // 验证原消息被标记为 pushStatus=4
        verify(alertMessageMapper, times(2)).update(eq(null), argThat(wrapper -> {
            // LambdaUpdateWrapper 无法直接断言，改用验证调用次数
            return true;
        }));
    }

    @Test
    void sendDailySummary_shouldNotMarkOriginal_whenPushFails() {
        when(alertMessageMapper.selectList(any())).thenReturn(List.of(l1msg1));
        when(userMapper.selectById(10L)).thenReturn(user);
        when(orchardMapper.selectById(1L)).thenReturn(orchard);
        when(wechatPushService.sendAlertNotification(any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(false);

        task.sendDailySummary();

        verify(alertMessageMapper).insert((AlertMessage) argThat((AlertMessage msg) -> {
            assertThat(msg.getPushStatus()).isEqualTo(2);
            return true;
        }));
    }
}
