package com.citrus.push.service;

import com.citrus.common.result.PageResult;
import com.citrus.push.dto.ProcessAlertDTO;
import com.citrus.push.vo.MessageDetailVO;
import com.citrus.push.vo.MessageListVO;
import com.citrus.push.vo.UnreadCountVO;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * 消息推送服务接口
 *
 * 提供消息的查询、已读标记等功能
 * 支持预警消息、农事提醒、系统通知三种类型
 */
public interface MessageService {

    /**
     * 获取消息列表（分页）
     *
     * @param level      预警等级筛选（1=L1 / 2=L2 / 3=L3），可为null表示不筛选
     * @param readStatus 已读状态筛选（0=未读 / 1=已读），可为null表示不筛选
     * @param processed  处理状态筛选（true=已处理 / false=未处理），可为null表示不筛选
     * @param page       页码，从1开始
     * @param size       每页数量
     * @param userId     当前登录用户ID
     * @return 分页消息列表
     */
    PageResult<MessageListVO> getMessageList(Integer level, Integer readStatus, Boolean processed, Integer page, Integer size, Long userId);

    /**
     * 获取未读消息数量
     *
     * @param userId 当前登录用户ID，管理员传 null 表示统计所有消息
     * @return 未读数量，包含总数和按等级分类的数量
     */
    UnreadCountVO getUnreadCount(Long userId);

    /**
     * 获取消息计数（全部/已处理/未处理）
     *
     * @param userId 当前登录用户ID，管理员传 null 表示统计所有消息
     * @return 包含 total / processed / unprocessed 的计数映射
     */
    Map<String, Long> getMessageCounts(Long userId);

    /**
     * 标记单条消息为已读
     *
     * @param messageId 消息ID
     * @param userId    当前登录用户ID（用于权限校验）
     * @throws com.citrus.common.exception.BizException 消息不存在或无权访问时抛出异常
     */
    void markAsRead(Long messageId, Long userId);

    /**
     * 标记单条消息为未读
     *
     * @param messageId 消息ID
     * @param userId    当前登录用户ID（用于权限校验）
     * @throws com.citrus.common.exception.BizException 消息不存在或无权访问时抛出异常
     */
    void markAsUnread(Long messageId, Long userId);

    /**
     * 标记所有未读消息为已读
     *
     * @param userId 当前登录用户ID
     * @return 标记为已读的消息数量
     */
    int markAllAsRead(Long userId);

    /**
     * 获取消息详情
     *
     * @param messageId 消息ID
     * @param userId    当前登录用户ID，管理员传 null 表示不限制归属
     * @param autoRead  是否自动标记已读
     * @return 消息详情，包含完整内容和防治建议
     * @throws com.citrus.common.exception.BizException 消息不存在或无权访问时抛出异常
     */
    MessageDetailVO getMessageDetail(Long messageId, Long userId, Boolean autoRead);

    /**
     * 创建预警消息并推送
     *
     * @param orchardId  果园ID
     * @param userId     接收用户ID
     * @param ruleId     触发规则ID
     * @param level      预警等级（1=L1, 2=L2, 3=L3）
     * @param title      消息标题
     * @param content    消息内容
     * @param advice     防治建议（来自 RAG 或规则模板）
     * @param messageType 消息类型：alert / calendar / system
     * @return 创建的消息ID
     */
    Long createAlertMessage(Long orchardId, Long userId, Integer ruleId, Integer level,
                            String title, String content, String advice, String messageType);

    /**
     * 创建检测触发的预警消息（含related字段）
     *
     * @param orchardId   果园ID
     * @param userId      接收用户ID
     * @param ruleId      触发规则ID（可选）
     * @param level       预警等级（1=L1, 2=L2, 3=L3）
     * @param title       消息标题
     * @param content     消息内容
     * @param advice      防治建议
     * @param relatedType 关联资源类型：detection / feedback
     * @param relatedId   关联资源ID
     * @return 创建的消息ID
     */
    Long createDetectionAlertMessage(Long orchardId, Long userId, Integer ruleId, Integer level,
                                     String title, String content, String advice,
                                     String relatedType, Long relatedId);

    /**
     * 上传预警处理图片
     *
     * @param messageId 消息ID
     * @param userId    当前登录用户ID
     * @param file      图片文件
     * @return 上传后的图片URL
     */
    String uploadTreatmentImage(Long messageId, Long userId, MultipartFile file);

    /**
     * 标记预警消息为已处理
     *
     * @param messageId 消息ID
     * @param userId    当前登录用户ID
     * @param dto       处理数据（含图片URL列表）
     */
    void markAsProcessed(Long messageId, Long userId, ProcessAlertDTO dto);
}
