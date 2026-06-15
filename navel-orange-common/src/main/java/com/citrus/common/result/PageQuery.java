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
    private Integer page;

    @Min(value = 1, message = "每页数量最小为1")
    @Max(value = 20, message = "每页数量最大为100")
    private Integer size;

    public int getPage() {
        return page != null ? page : 1;
    }

    public int getSize() {
        return size != null ? size : 20;
    }
}
