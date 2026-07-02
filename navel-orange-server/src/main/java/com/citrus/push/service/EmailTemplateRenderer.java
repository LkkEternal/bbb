package com.citrus.push.service;

import com.citrus.push.dto.AlertEmailDTO;
import com.citrus.push.entity.AlertMessage;

/**
 * 邮件模板渲染服务
 * <p>
 * 将 AlertMessage 数据渲染为 HTML 邮件字符串。
 */
public interface EmailTemplateRenderer {

    /**
     * 根据预警消息生成 HTML 邮件内容
     *
     * @param alert AlertMessage 实体（含 extraData）
     * @return HTML 字符串
     */
    String render(AlertMessage alert);

    /**
     * 根据 DTO 生成 HTML 邮件内容（直接传 DTO，灵活拼接）
     *
     * @param dto AlertEmailDTO
     * @return HTML 字符串
     */
    String render(AlertEmailDTO dto);
}
