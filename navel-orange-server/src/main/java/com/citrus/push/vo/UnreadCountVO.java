package com.citrus.push.vo;

import lombok.Data;

import java.util.Map;

/**
 * 未读消息数量VO（View Object）
 *
 * 用于未读数量接口的响应数据
 * 包含未读消息的总数和按预警等级分类的数量
 */
@Data
public class UnreadCountVO {

    /**
     * 未读消息总数
     */
    private Long total;

    /**
     * 按预警等级分类的未读数量
     * key: L1, L2, L3
     * value: 对应等级的未读数量
     *
     * 示例：
     * {
     *   "L1": 10,  // 低风险未读10条
     *   "L2": 3,   // 中风险未读3条
     *   "L3": 2    // 高风险未读2条
     * }
     */
    private Map<String, Long> byLevel;
}
