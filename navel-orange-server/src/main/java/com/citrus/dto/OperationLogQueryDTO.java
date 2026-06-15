package com.citrus.dto;

import com.citrus.common.result.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 操作日志查询参数
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class OperationLogQueryDTO extends PageQuery {

    /**
     * 操作类型：login/create/update/delete/export
     */
    private String action;

    /**
     * 操作用户ID
     */
    private Long userId;
}
