package com.citrus.collector.service;

import com.citrus.collector.vo.DetectionListVO;
import com.citrus.collector.vo.DetectionStatsVO;
import com.citrus.common.result.PageQuery;
import com.citrus.common.result.PageResult;

public interface DetectionQueryService {

    PageResult<DetectionListVO> getDetectionList(Long orchardId, String diseaseCode,
                                                  String startDate, String endDate,
                                                  PageQuery pageQuery, Long userId);

    DetectionStatsVO getDetectionStats(Long orchardId, String startDate, String endDate,
                                        Long userId);
}
