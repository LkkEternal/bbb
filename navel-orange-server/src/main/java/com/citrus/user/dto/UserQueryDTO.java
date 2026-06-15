package com.citrus.user.dto;

import com.citrus.common.result.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用户查询参数
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class UserQueryDTO extends PageQuery {

    /**
     * 搜索关键词（手机号/昵称）
     */
    private String keyword;
}
