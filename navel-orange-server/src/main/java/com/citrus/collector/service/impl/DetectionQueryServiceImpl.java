package com.citrus.collector.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.citrus.collector.entity.DiseaseCatalog;
import com.citrus.collector.entity.DiseaseDetection;
import com.citrus.collector.mapper.DiseaseCatalogMapper;
import com.citrus.collector.mapper.DiseaseDetectionMapper;
import com.citrus.collector.service.DetectionQueryService;
import com.citrus.collector.vo.DetectionListVO;
import com.citrus.collector.vo.DetectionStatsVO;
import com.citrus.common.constant.Constants;
import com.citrus.common.exception.BizException;
import com.citrus.common.result.PageQuery;
import com.citrus.common.result.PageResult;
import com.citrus.common.result.ResultCode;
import com.citrus.orchard.entity.OrchardUser;
import com.citrus.orchard.mapper.OrchardUserMapper;
import com.citrus.auth.model.LoginUser;
import com.citrus.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DetectionQueryServiceImpl implements DetectionQueryService {

    private final DiseaseDetectionMapper diseaseDetectionMapper;
    private final DiseaseCatalogMapper diseaseCatalogMapper;
    private final OrchardUserMapper orchardUserMapper;
    private final SecurityUtil securityUtil;

    @Override
    public PageResult<DetectionListVO> getDetectionList(Long orchardId, String diseaseCode,
                                                         String startDate, String endDate,
                                                         PageQuery pageQuery, Long userId) {
        List<Long> accessibleOrchardIds = getAccessibleOrchardIds(orchardId, userId);

        if (accessibleOrchardIds.isEmpty()) {
            return PageResult.of(new ArrayList<>(), 0, pageQuery.getPage(), pageQuery.getSize());
        }

        LambdaQueryWrapper<DiseaseDetection> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(DiseaseDetection::getOrchardId, accessibleOrchardIds);

        if (diseaseCode != null && !diseaseCode.isEmpty()) {
            wrapper.eq(DiseaseDetection::getTopClassCode, diseaseCode);
        }

        if (startDate != null && !startDate.isEmpty()) {
            LocalDateTime start = LocalDate.parse(startDate, DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                    .atStartOfDay();
            wrapper.ge(DiseaseDetection::getDeviceTimestamp, start);
        }

        if (endDate != null && !endDate.isEmpty()) {
            LocalDateTime end = LocalDate.parse(endDate, DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                    .atTime(LocalTime.MAX);
            wrapper.le(DiseaseDetection::getDeviceTimestamp, end);
        }

        wrapper.orderByDesc(DiseaseDetection::getDeviceTimestamp);

        Page<DiseaseDetection> page = new Page<>(pageQuery.getPage(), pageQuery.getSize());
        IPage<DiseaseDetection> pageResult = diseaseDetectionMapper.selectPage(page, wrapper);

        List<DetectionListVO> voList = pageResult.getRecords().stream()
                .map(this::convertToListVO)
                .collect(Collectors.toList());

        return PageResult.of(voList, pageResult.getTotal(), pageQuery.getPage(), pageQuery.getSize());
    }

    @Override
    public DetectionStatsVO getDetectionStats(Long orchardId, String startDate, String endDate,
                                               Long userId) {
        List<Long> accessibleOrchardIds = getAccessibleOrchardIds(orchardId, userId);

        if (accessibleOrchardIds.isEmpty()) {
            return DetectionStatsVO.builder()
                    .totalCount(0L)
                    .alertCount(0L)
                    .diseaseDistribution(new ArrayList<>())
                    .dailyTrend(new ArrayList<>())
                    .build();
        }

        LocalDateTime start = LocalDate.parse(startDate, DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                .atStartOfDay();
        LocalDateTime end = LocalDate.parse(endDate, DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                .atTime(LocalTime.MAX);

        // 查询总数
        LambdaQueryWrapper<DiseaseDetection> countWrapper = new LambdaQueryWrapper<>();
        countWrapper.in(DiseaseDetection::getOrchardId, accessibleOrchardIds)
                .ge(DiseaseDetection::getDeviceTimestamp, start)
                .le(DiseaseDetection::getDeviceTimestamp, end);
        Long totalCount = diseaseDetectionMapper.selectCount(countWrapper);

        // 查询所有符合条件的记录用于统计
        LambdaQueryWrapper<DiseaseDetection> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(DiseaseDetection::getOrchardId, accessibleOrchardIds)
                .ge(DiseaseDetection::getDeviceTimestamp, start)
                .le(DiseaseDetection::getDeviceTimestamp, end)
                .select(DiseaseDetection::getTopClassCode, DiseaseDetection::getDiseaseCatalogId,
                        DiseaseDetection::getDeviceTimestamp);
        List<DiseaseDetection> detections = diseaseDetectionMapper.selectList(queryWrapper);

        // 计算病害分布
        List<DetectionStatsVO.DiseaseDistribution> distribution = calculateDistribution(detections, totalCount);

        // 计算每日趋势
        List<DetectionStatsVO.DailyTrend> dailyTrend = calculateDailyTrend(detections, startDate, endDate);

        return DetectionStatsVO.builder()
                .totalCount(totalCount)
                .alertCount(0L)
                .diseaseDistribution(distribution)
                .dailyTrend(dailyTrend)
                .build();
    }

    private List<Long> getAccessibleOrchardIds(Long orchardId, Long userId) {
        LoginUser loginUser = securityUtil.getCurrentUser();
        boolean isAdmin = loginUser.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals(Constants.ROLE_ADMIN));

        List<Long> orchardIds = new ArrayList<>();

        if (isAdmin) {
            if (orchardId != null) {
                orchardIds.add(orchardId);
            } else {
                LambdaQueryWrapper<DiseaseDetection> wrapper = new LambdaQueryWrapper<>();
                wrapper.select(DiseaseDetection::getOrchardId)
                        .groupBy(DiseaseDetection::getOrchardId);
                List<DiseaseDetection> allDetections = diseaseDetectionMapper.selectList(wrapper);
                orchardIds = allDetections.stream()
                        .map(DiseaseDetection::getOrchardId)
                        .collect(Collectors.toList());
            }
        } else {
            if (orchardId != null) {
                OrchardUser orchardUser = orchardUserMapper.selectOne(
                        new LambdaQueryWrapper<OrchardUser>()
                                .eq(OrchardUser::getOrchardId, orchardId)
                                .eq(OrchardUser::getUserId, userId)
                );
                if (orchardUser == null) {
                    throw new BizException(ResultCode.ORCHARD_NO_PERMISSION);
                }
                orchardIds.add(orchardId);
            } else {
                List<OrchardUser> orchardUsers = orchardUserMapper.selectList(
                        new LambdaQueryWrapper<OrchardUser>()
                                .eq(OrchardUser::getUserId, userId)
                                .select(OrchardUser::getOrchardId)
                );
                orchardIds = orchardUsers.stream()
                        .map(OrchardUser::getOrchardId)
                        .collect(Collectors.toList());
            }
        }

        return orchardIds;
    }

    private List<DetectionStatsVO.DiseaseDistribution> calculateDistribution(
            List<DiseaseDetection> detections, Long totalCount) {
        // 按病害编码分组计数
        Map<String, Long> countMap = detections.stream()
                .collect(Collectors.groupingBy(DiseaseDetection::getTopClassCode, Collectors.counting()));

        // 查询病害名称映射
        Map<String, String> nameMap = getDiseaseNameMap();

        List<DetectionStatsVO.DiseaseDistribution> distribution = new ArrayList<>();
        for (Map.Entry<String, Long> entry : countMap.entrySet()) {
            String code = entry.getKey();
            Long count = entry.getValue();
            BigDecimal percentage = totalCount > 0 ?
                    BigDecimal.valueOf(count * 100.0 / totalCount).setScale(1, RoundingMode.HALF_UP) :
                    BigDecimal.ZERO;

            distribution.add(DetectionStatsVO.DiseaseDistribution.builder()
                    .diseaseCode(code)
                    .diseaseName(nameMap.getOrDefault(code, code))
                    .count(count)
                    .percentage(percentage)
                    .build());
        }

        // 按数量降序排序
        distribution.sort((a, b) -> Long.compare(b.getCount(), a.getCount()));

        return distribution;
    }

    private List<DetectionStatsVO.DailyTrend> calculateDailyTrend(
            List<DiseaseDetection> detections, String startDate, String endDate) {
        LocalDate start = LocalDate.parse(startDate);
        LocalDate end = LocalDate.parse(endDate);

        // 按日期分组计数
        Map<String, Long> countMap = detections.stream()
                .collect(Collectors.groupingBy(
                        d -> d.getDeviceTimestamp().toLocalDate().toString(),
                        Collectors.counting()));

        List<DetectionStatsVO.DailyTrend> dailyTrend = new ArrayList<>();
        for (LocalDate date = start; !date.isAfter(end); date = date.plusDays(1)) {
            String dateStr = date.toString();
            Long count = countMap.getOrDefault(dateStr, 0L);

            dailyTrend.add(DetectionStatsVO.DailyTrend.builder()
                    .date(dateStr)
                    .count(count)
                    .alertCount(0L)
                    .build());
        }

        return dailyTrend;
    }

    private Map<String, String> getDiseaseNameMap() {
        List<DiseaseCatalog> catalogs = diseaseCatalogMapper.selectList(
                new LambdaQueryWrapper<DiseaseCatalog>()
                        .select(DiseaseCatalog::getDiseaseCode, DiseaseCatalog::getDiseaseName)
        );
        Map<String, String> nameMap = new HashMap<>();
        for (DiseaseCatalog catalog : catalogs) {
            nameMap.put(catalog.getDiseaseCode(), catalog.getDiseaseName());
        }
        return nameMap;
    }

    private DetectionListVO convertToListVO(DiseaseDetection entity) {
        return DetectionListVO.builder()
                .id(entity.getId())
                .orchardId(entity.getOrchardId())
                .imageUrl(entity.getImageUrl())
                .topClassCode(entity.getTopClassCode())
                .topClassName(entity.getDiseaseNameRaw())
                .topProbability(entity.getTopProbability())
                .allProbabilities(entity.getAllProbabilities())
                .deviceTimestamp(entity.getDeviceTimestamp() != null ?
                        entity.getDeviceTimestamp().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : null)
                .createdAt(entity.getCreatedAt() != null ?
                        entity.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : null)
                .build();
    }
}
