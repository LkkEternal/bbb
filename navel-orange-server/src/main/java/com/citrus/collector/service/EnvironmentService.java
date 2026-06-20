package com.citrus.collector.service;

import com.citrus.collector.vo.EnvironmentListVO;
import com.citrus.collector.vo.EnvironmentTrendVO;
import com.citrus.common.result.PageQuery;
import com.citrus.common.result.PageResult;

/**
 * 环境数据服务接口
 */
public interface EnvironmentService {

    /**
     * 获取环境数据列表（分页）
     *
     * @param orchardId  果园ID
     * @param startDate  开始日期（可选）
     * @param endDate    结束日期（可选）
     * @param pageQuery  分页参数
     * @param userId     当前用户ID
     * @return 分页结果
     */
    PageResult<EnvironmentListVO> getEnvironmentList(Long orchardId, String startDate, String endDate, 
                                                      PageQuery pageQuery, Long userId);

    /**
     * 获取环境趋势数据
     *
     * @param orchardId 果园ID
     * @param days      查询天数（默认7天）
     * @param userId    当前用户ID
     * @return 趋势数据
     */
    EnvironmentTrendVO getEnvironmentTrend(Long orchardId, Integer days, Long userId);
}
