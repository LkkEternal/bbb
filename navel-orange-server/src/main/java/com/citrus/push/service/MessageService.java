package com.citrus.push.service;

import com.citrus.common.result.PageResult;
import com.citrus.push.vo.MessageDetailVO;
import com.citrus.push.vo.MessageListVO;
import com.citrus.push.vo.UnreadCountVO;

public interface MessageService {

    PageResult<MessageListVO> getMessageList(Integer level, Integer readStatus, Integer page, Integer size, Long userId);

    UnreadCountVO getUnreadCount(Long userId);

    void markAsRead(Long messageId, Long userId);

    int markAllAsRead(Long userId);

    MessageDetailVO getMessageDetail(Long messageId, Long userId);
}
