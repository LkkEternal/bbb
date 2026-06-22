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
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MessageServiceImpl implements MessageService {

    private final AlertMessageMapper alertMessageMapper;
    private final OrchardMapper orchardMapper;

    @Override
    public PageResult<MessageListVO> getMessageList(Integer level, Integer readStatus,
                                                     Integer page, Integer size, Long userId) {
        LambdaQueryWrapper<AlertMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AlertMessage::getUserId, userId);
        if (level != null) {
            wrapper.eq(AlertMessage::getLevel, level);
        }
        if (readStatus != null) {
            wrapper.eq(AlertMessage::getReadStatus, readStatus);
        }
        wrapper.orderByDesc(AlertMessage::getCreatedAt);

        Page<AlertMessage> pageParam = new Page<>(page, size);
        Page<AlertMessage> pageResult = alertMessageMapper.selectPage(pageParam, wrapper);

        List<MessageListVO> records = pageResult.getRecords().stream()
                .map(this::convertToListVO)
                .collect(Collectors.toList());

        return PageResult.of(records, pageResult.getTotal(), page, size);
    }

    @Override
    public UnreadCountVO getUnreadCount(Long userId) {
        LambdaQueryWrapper<AlertMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AlertMessage::getUserId, userId)
               .eq(AlertMessage::getReadStatus, 0);
        long total = alertMessageMapper.selectCount(wrapper);

        Map<String, Long> byLevel = new HashMap<>();
        for (int i = 1; i <= 3; i++) {
            LambdaQueryWrapper<AlertMessage> levelWrapper = new LambdaQueryWrapper<>();
            levelWrapper.eq(AlertMessage::getUserId, userId)
                       .eq(AlertMessage::getReadStatus, 0)
                       .eq(AlertMessage::getLevel, i);
            byLevel.put("L" + i, alertMessageMapper.selectCount(levelWrapper));
        }

        UnreadCountVO vo = new UnreadCountVO();
        vo.setTotal(total);
        vo.setByLevel(byLevel);
        return vo;
    }

    @Override
    public void markAsRead(Long messageId, Long userId) {
        AlertMessage message = alertMessageMapper.selectById(messageId);
        if (message == null || !message.getUserId().equals(userId)) {
            throw new BizException(ResultCode.MESSAGE_NOT_FOUND);
        }
        if (message.getReadStatus() == 0) {
            LambdaUpdateWrapper<AlertMessage> wrapper = new LambdaUpdateWrapper<>();
            wrapper.eq(AlertMessage::getId, messageId)
                   .set(AlertMessage::getReadStatus, 1)
                   .set(AlertMessage::getReadTime, LocalDateTime.now());
            alertMessageMapper.update(null, wrapper);
        }
    }

    @Override
    public int markAllAsRead(Long userId) {
        LambdaUpdateWrapper<AlertMessage> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(AlertMessage::getUserId, userId)
               .eq(AlertMessage::getReadStatus, 0)
               .set(AlertMessage::getReadStatus, 1)
               .set(AlertMessage::getReadTime, LocalDateTime.now());
        return alertMessageMapper.update(null, wrapper);
    }

    @Override
    public MessageDetailVO getMessageDetail(Long messageId, Long userId) {
        AlertMessage message = alertMessageMapper.selectById(messageId);
        if (message == null || !message.getUserId().equals(userId)) {
            throw new BizException(ResultCode.MESSAGE_NOT_FOUND);
        }

        if (message.getReadStatus() == 0) {
            LambdaUpdateWrapper<AlertMessage> wrapper = new LambdaUpdateWrapper<>();
            wrapper.eq(AlertMessage::getId, messageId)
                   .set(AlertMessage::getReadStatus, 1)
                   .set(AlertMessage::getReadTime, LocalDateTime.now());
            alertMessageMapper.update(null, wrapper);
            message.setReadStatus(1);
            message.setReadTime(LocalDateTime.now());
        }

        return convertToDetailVO(message);
    }

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

        Orchard orchard = orchardMapper.selectById(message.getOrchardId());
        if (orchard != null) {
            vo.setOrchardName(orchard.getName());
        }

        if (message.getRuleId() != null) {
            vo.setRuleCode("R" + String.format("%03d", message.getRuleId()));
        }

        return vo;
    }

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

        Orchard orchard = orchardMapper.selectById(message.getOrchardId());
        if (orchard != null) {
            vo.setOrchardName(orchard.getName());
        }

        if (message.getRuleId() != null) {
            vo.setRuleCode("R" + String.format("%03d", message.getRuleId()));
        }

        return vo;
    }
}
