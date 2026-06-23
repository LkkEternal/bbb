package com.citrus.collector.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.citrus.collector.entity.EnvironmentData;
import com.citrus.collector.mapper.EnvironmentDataMapper;
import com.citrus.collector.service.EnvironmentService;
import com.citrus.collector.vo.EnvironmentListVO;
import com.citrus.collector.vo.EnvironmentTrendVO;
import com.citrus.auth.model.LoginUser;
import com.citrus.common.constant.Constants;
import com.citrus.common.exception.BizException;
import com.citrus.common.result.PageQuery;
import com.citrus.common.result.PageResult;
import com.citrus.common.result.ResultCode;
import com.citrus.orchard.entity.OrchardUser;
import com.citrus.orchard.mapper.OrchardUserMapper;
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
import java.util.List;
import java.util.stream.Collectors;

/**
 * 环境数据服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EnvironmentServiceImpl implements EnvironmentService {

    private final EnvironmentDataMapper environmentDataMapper;
    private final OrchardUserMapper orchardUserMapper;
    private final SecurityUtil securityUtil;

    @Override
    public PageResult<EnvironmentListVO> getEnvironmentList(Long orchardId, String startDate, String endDate,
                                                             PageQuery pageQuery, Long userId) {
        // 校验用户是否有权限访问该果园
        checkOrchardPermission(orchardId, userId);

        // 构建查询条件
        LambdaQueryWrapper<EnvironmentData> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(EnvironmentData::getOrchardId, orchardId);

        // 日期筛选
        if (startDate != null && !startDate.isEmpty()) {
            LocalDateTime start = LocalDate.parse(startDate, DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                    .atStartOfDay();
            wrapper.ge(EnvironmentData::getRecordTime, start);
        }
        if (endDate != null && !endDate.isEmpty()) {
            LocalDateTime end = LocalDate.parse(endDate, DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                    .atTime(LocalTime.MAX);
            wrapper.le(EnvironmentData::getRecordTime, end);
        }

        wrapper.orderByDesc(EnvironmentData::getRecordTime);

        // 分页查询
        Page<EnvironmentData> page = new Page<>(pageQuery.getPage(), pageQuery.getSize());
        IPage<EnvironmentData> pageResult = environmentDataMapper.selectPage(page, wrapper);

        // 转换为VO
        List<EnvironmentListVO> voList = pageResult.getRecords().stream()
                .map(this::convertToListVO)
                .collect(Collectors.toList());

        return PageResult.of(voList, pageResult.getTotal(), pageQuery.getPage(), pageQuery.getSize());
    }

    @Override
    public EnvironmentTrendVO getEnvironmentTrend(Long orchardId, Integer days, Long userId) {
        // 校验用户是否有权限访问该果园
        checkOrchardPermission(orchardId, userId);

        // 默认7天
        if (days == null || days <= 0) {
            days = 7;
        }

        // 计算开始时间
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(days - 1);

        // 查询环境数据
        LambdaQueryWrapper<EnvironmentData> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(EnvironmentData::getOrchardId, orchardId)
                .ge(EnvironmentData::getRecordTime, startDate.atStartOfDay())
                .le(EnvironmentData::getRecordTime, endDate.atTime(LocalTime.MAX))
                .orderByAsc(EnvironmentData::getRecordTime);

        List<EnvironmentData> dataList = environmentDataMapper.selectList(wrapper);

        // 按日期分组并计算每日平均值
        List<String> dates = new ArrayList<>();
        List<BigDecimal> tempValues = new ArrayList<>();
        List<BigDecimal> humidityValues = new ArrayList<>();
        List<BigDecimal> precipValues = new ArrayList<>();
        List<BigDecimal> windSpeedValues = new ArrayList<>();
        BigDecimal totalPrecip = BigDecimal.ZERO;

        for (int i = 0; i < days; i++) {
            LocalDate currentDate = startDate.plusDays(i);
            dates.add(currentDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));

            // 筛选当天的数据
            LocalDate finalCurrentDate = currentDate;
            List<EnvironmentData> dayData = dataList.stream()
                    .filter(d -> d.getRecordTime().toLocalDate().equals(finalCurrentDate))
                    .collect(Collectors.toList());

            if (dayData.isEmpty()) {
                // 无数据时填充0
                tempValues.add(BigDecimal.ZERO);
                humidityValues.add(BigDecimal.ZERO);
                precipValues.add(BigDecimal.ZERO);
                windSpeedValues.add(BigDecimal.ZERO);
            } else {
                // 计算平均值
                tempValues.add(calculateAverage(dayData, EnvironmentData::getTemp));
                humidityValues.add(calculateAverage(dayData, EnvironmentData::getHumidity));
                
                // 计算降水量总和
                BigDecimal dayPrecip = dayData.stream()
                        .map(EnvironmentData::getPrecip)
                        .filter(p -> p != null)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                precipValues.add(dayPrecip);
                totalPrecip = totalPrecip.add(dayPrecip);
                
                windSpeedValues.add(calculateAverage(dayData, EnvironmentData::getWindSpeed));
            }
        }

        // 构建降水数据
        EnvironmentTrendVO.PrecipData precipData = EnvironmentTrendVO.PrecipData.builder()
                .daily(precipValues)
                .total(totalPrecip)
                .build();

        return EnvironmentTrendVO.builder()
                .dates(dates)
                .temp(tempValues)
                .humidity(humidityValues)
                .precip(precipData)
                .windSpeed(windSpeedValues)
                .build();
    }

    /**
     * 校验用户是否有权限访问果园
     * 系统管理员可直接访问，农户需要果园关联
     */
    private void checkOrchardPermission(Long orchardId, Long userId) {
        // 系统管理员可直接访问
        LoginUser loginUser = securityUtil.getCurrentUser();
        boolean isSysAdmin = loginUser.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals(Constants.ROLE_ADMIN));
        if (isSysAdmin) {
            return;
        }

        // 农户需要果园关联
        boolean hasPermission = orchardUserMapper.exists(
                new LambdaQueryWrapper<OrchardUser>()
                        .eq(OrchardUser::getOrchardId, orchardId)
                        .eq(OrchardUser::getUserId, userId)
        );
        if (!hasPermission) {
            throw new BizException(ResultCode.ORCHARD_NO_PERMISSION);
        }
    }

    /**
     * 转换为列表VO
     */
    private EnvironmentListVO convertToListVO(EnvironmentData entity) {
        return EnvironmentListVO.builder()
                .id(entity.getId())
                .orchardId(entity.getOrchardId())
                .temp(entity.getTemp())
                .humidity(entity.getHumidity())
                .precip(entity.getPrecip())
                .windSpeed(entity.getWindSpeed())
                .dataSource(entity.getDataSource())
                .recordTime(entity.getRecordTime() != null ? 
                        entity.getRecordTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : null)
                .build();
    }

    /**
     * 计算平均值
     */
    private BigDecimal calculateAverage(List<EnvironmentData> data, 
            java.util.function.Function<EnvironmentData, BigDecimal> getter) {
        return data.stream()
                .map(getter)
                .filter(v -> v != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(data.size()), 1, RoundingMode.HALF_UP);
    }
}
