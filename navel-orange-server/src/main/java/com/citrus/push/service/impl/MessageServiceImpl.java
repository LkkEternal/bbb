package com.citrus.push.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.citrus.common.exception.BizException;
import com.citrus.common.result.PageResult;
import com.citrus.common.result.ResultCode;
import com.citrus.orchard.entity.Orchard;
import com.citrus.orchard.mapper.OrchardMapper;
import com.citrus.push.entity.AlertMessage;
import com.citrus.push.mapper.AlertMessageMapper;
import com.citrus.push.service.MessageService;
import com.citrus.push.vo.MessageDetailVO;
import com.citrus.push.vo.MessageListVO;
import com.citrus.push.vo.UnreadCountVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 消息推送服务实现类
 *
 * 实现消息的查询、已读标记等功能
 * 使用MyBatis-Plus进行数据库操作
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MessageServiceImpl implements MessageService {

    private final AlertMessageMapper alertMessageMapper;
    private final OrchardMapper orchardMapper;

    /**
     * 获取消息列表（分页）
     *
     * 实现逻辑：
     * 1. 构建查询条件（用户ID + 可选的等级和已读状态筛选）
     * 2. 按创建时间降序排序（最新消息在前）
     * 3. 执行分页查询
     * 4. 将实体转换为VO（包含果园名称）
     *
     * userId 为 null 时表示管理员查看所有消息，不按用户过滤
     */
    @Override
    public PageResult<MessageListVO> getMessageList(Integer level, Integer readStatus,
                                                     Integer page, Integer size, Long userId) {
        // 构建查询条件
        LambdaQueryWrapper<AlertMessage> wrapper = new LambdaQueryWrapper<>();

        // userId 非空时只查询当前用户的消息，为 null 时管理员查看所有
        if (userId != null) {
            wrapper.eq(AlertMessage::getUserId, userId);
        }

        // 可选筛选条件
        if (level != null) {
            wrapper.eq(AlertMessage::getLevel, level);  // 按预警等级筛选
        }
        if (readStatus != null) {
            wrapper.eq(AlertMessage::getReadStatus, readStatus);  // 按已读状态筛选
        }

        // 按创建时间降序排序
        wrapper.orderByDesc(AlertMessage::getCreatedAt);

        // 执行分页查询
        Page<AlertMessage> pageParam = new Page<>(page, size);
        Page<AlertMessage> pageResult = alertMessageMapper.selectPage(pageParam, wrapper);

        // 将实体列表转换为VO列表
        List<MessageListVO> records = pageResult.getRecords().stream()
                .map(this::convertToListVO)
                .collect(Collectors.toList());

        return PageResult.of(records, pageResult.getTotal(), page, size);
    }

    /**
     * 获取未读消息数量
     *
     * 实现逻辑：
     * 1. 查询当前用户所有未读消息总数
     * 2. 分别统计L1/L2/L3三个等级的未读数量
     * 3. 返回包含总数和分级数量的VO
     *
     * userId 为 null 时表示管理员统计所有消息
     */
    @Override
    public UnreadCountVO getUnreadCount(Long userId) {
        // 查询未读消息总数
        LambdaQueryWrapper<AlertMessage> wrapper = new LambdaQueryWrapper<>();
        if (userId != null) {
            wrapper.eq(AlertMessage::getUserId, userId);
        }
        wrapper.eq(AlertMessage::getReadStatus, 0);  // read_status=0 表示未读
        long total = alertMessageMapper.selectCount(wrapper);

        // 按预警等级分别统计未读数量
        Map<String, Long> byLevel = new HashMap<>();
        for (int i = 1; i <= 3; i++) {
            LambdaQueryWrapper<AlertMessage> levelWrapper = new LambdaQueryWrapper<>();
            if (userId != null) {
                levelWrapper.eq(AlertMessage::getUserId, userId);
            }
            levelWrapper.eq(AlertMessage::getReadStatus, 0)
                       .eq(AlertMessage::getLevel, i);
            byLevel.put("L" + i, alertMessageMapper.selectCount(levelWrapper));
        }

        // 组装返回结果
        UnreadCountVO vo = new UnreadCountVO();
        vo.setTotal(total);
        vo.setByLevel(byLevel);
        return vo;
    }

    /**
     * 标记单条消息为已读
     *
     * 实现逻辑：
     * 1. 查询消息是否存在且属于当前用户
     * 2. 如果消息未读，更新已读状态和阅读时间
     * 3. 如果消息已读，不做任何操作
     */
    @Override
    public void markAsRead(Long messageId, Long userId) {
        // 查询消息并验证权限
        AlertMessage message = alertMessageMapper.selectById(messageId);
        if (message == null || !message.getUserId().equals(userId)) {
            throw new BizException(ResultCode.MESSAGE_NOT_FOUND);
        }

        // 只有未读消息才需要更新
        if (message.getReadStatus() == 0) {
            LambdaUpdateWrapper<AlertMessage> wrapper = new LambdaUpdateWrapper<>();
            wrapper.eq(AlertMessage::getId, messageId)
                   .set(AlertMessage::getReadStatus, 1)  // 标记为已读
                   .set(AlertMessage::getReadTime, LocalDateTime.now());  // 记录阅读时间
            alertMessageMapper.update(null, wrapper);
        }
    }

    /**
     * 标记单条消息为未读
     *
     * 实现逻辑：
     * 1. 查询消息是否存在且属于当前用户
     * 2. 如果消息已读，更新为未读状态并清空阅读时间
     */
    @Override
    public void markAsUnread(Long messageId, Long userId) {
        AlertMessage message = alertMessageMapper.selectById(messageId);
        if (message == null || !message.getUserId().equals(userId)) {
            throw new BizException(ResultCode.MESSAGE_NOT_FOUND);
        }

        // 只有已读消息才需要更新
        if (message.getReadStatus() == 1) {
            LambdaUpdateWrapper<AlertMessage> wrapper = new LambdaUpdateWrapper<>();
            wrapper.eq(AlertMessage::getId, messageId)
                   .set(AlertMessage::getReadStatus, 0)   // 标记为未读
                   .set(AlertMessage::getReadTime, null);  // 清空阅读时间
            alertMessageMapper.update(null, wrapper);
        }
    }

    /**
     * 标记所有未读消息为已读
     *
     * 实现逻辑：
     * 批量更新当前用户所有未读消息的已读状态和阅读时间
     *
     * @return 更新的消息数量
     */
    @Override
    public int markAllAsRead(Long userId) {
        LambdaUpdateWrapper<AlertMessage> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(AlertMessage::getUserId, userId)
               .eq(AlertMessage::getReadStatus, 0)  // 只更新未读消息
               .set(AlertMessage::getReadStatus, 1)
               .set(AlertMessage::getReadTime, LocalDateTime.now());
        return alertMessageMapper.update(null, wrapper);
    }

    /**
     * 获取消息详情
     *
     * 实现逻辑：
     * 1. 查询消息并验证权限
     * 2. 如果 autoRead 为 true 且消息未读，自动标记为已读
     * 3. 转换为详情VO返回（包含完整内容和防治建议）
     *
     * userId 为 null 时表示管理员，跳过归属校验
     */
    @Override
    public MessageDetailVO getMessageDetail(Long messageId, Long userId, Boolean autoRead) {
        // 查询消息
        AlertMessage message = alertMessageMapper.selectById(messageId);
        if (message == null) {
            throw new BizException(ResultCode.MESSAGE_NOT_FOUND);
        }

        // userId 非空时校验归属（普通用户只能看自己的消息）
        if (userId != null && !message.getUserId().equals(userId)) {
            throw new BizException(ResultCode.MESSAGE_NOT_FOUND);
        }

        // 根据 autoRead 参数决定是否自动标记为已读
        if (Boolean.TRUE.equals(autoRead) && message.getReadStatus() == 0) {
            LambdaUpdateWrapper<AlertMessage> wrapper = new LambdaUpdateWrapper<>();
            wrapper.eq(AlertMessage::getId, messageId)
                   .set(AlertMessage::getReadStatus, 1)
                   .set(AlertMessage::getReadTime, LocalDateTime.now());
            alertMessageMapper.update(null, wrapper);

            // 更新内存中的对象状态
            message.setReadStatus(1);
            message.setReadTime(LocalDateTime.now());
        }

        return convertToDetailVO(message);
    }

    /**
     * 将消息实体转换为列表VO
     *
     * @param message 消息实体
     * @return 消息列表VO
     */
    private MessageListVO convertToListVO(AlertMessage message) {
        MessageListVO vo = new MessageListVO();
        vo.setId(message.getId());
        vo.setMessageType(message.getMessageType());
        vo.setTitle(message.getTitle());
        vo.setSummary(message.getSummary());
        vo.setLevel(message.getLevel());
        vo.setOrchardId(message.getOrchardId());
        vo.setReadStatus(message.getReadStatus());
        vo.setCreatedAt(message.getCreatedAt());

        // 查询果园名称
        Orchard orchard = orchardMapper.selectById(message.getOrchardId());
        if (orchard != null) {
            vo.setOrchardName(orchard.getName());
        }

        // 生成规则编码（如 R001, R002）
        if (message.getRuleId() != null) {
            vo.setRuleCode("R" + String.format("%03d", message.getRuleId()));
        }

        return vo;
    }

    /**
     * 将消息实体转换为详情VO
     *
     * @param message 消息实体
     * @return 消息详情VO
     */
    private MessageDetailVO convertToDetailVO(AlertMessage message) {
        MessageDetailVO vo = new MessageDetailVO();
        vo.setId(message.getId());
        vo.setMessageType(message.getMessageType());
        vo.setTitle(message.getTitle());
        vo.setSummary(message.getSummary());
        vo.setContent(message.getContent());
        vo.setAdvice(message.getAdvice());
        vo.setLevel(message.getLevel());
        vo.setOrchardId(message.getOrchardId());
        vo.setRuleId(message.getRuleId());
        vo.setRelatedType(message.getRelatedType());
        vo.setRelatedId(message.getRelatedId());
        vo.setExtraData(message.getExtraData());
        vo.setReadStatus(message.getReadStatus());
        vo.setReadTime(message.getReadTime());
        vo.setPushChannel(message.getPushChannel());
        vo.setPushStatus(message.getPushStatus());
        vo.setPushTime(message.getPushTime());
        vo.setCreatedAt(message.getCreatedAt());

        // 查询果园名称
        Orchard orchard = orchardMapper.selectById(message.getOrchardId());
        if (orchard != null) {
            vo.setOrchardName(orchard.getName());
        }

        // 生成规则编码
        if (message.getRuleId() != null) {
            vo.setRuleCode("R" + String.format("%03d", message.getRuleId()));
        }

        return vo;
    }

    @Override
    public Long createAlertMessage(Long orchardId, Long userId, Integer ruleId, Integer level,
                                   String title, String content, String advice, String messageType) {
        AlertMessage msg = new AlertMessage();
        msg.setOrchardId(orchardId);
        msg.setUserId(userId);
        msg.setRuleId(ruleId);
        msg.setLevel(level);
        msg.setTitle(title);
        msg.setSummary(content != null && content.length() > 200 ? content.substring(0, 200) : content);
        msg.setContent(content);
        msg.setAdvice(advice);
        msg.setMessageType(messageType);
        msg.setPushChannel("app");
        msg.setPushStatus(0);
        msg.setReadStatus(0);

        alertMessageMapper.insert(msg);
        log.info("预警消息已创建: id={}, orchardId={}, level=L{}, type={}", msg.getId(), orchardId, level, messageType);
        return msg.getId();
    }
}
