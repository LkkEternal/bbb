package com.citrus.common.result;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

/**
 * 分页查询参数
 */
@Data
public class PageQuery {

    @Min(value = 1, message = "页码最小为1")
    private int page = 1;

    @Min(value = 1, message = "每页数量最小为1")
    @Max(value = 100, message = "每页数量最大为100")
    private int size = 20;
}
