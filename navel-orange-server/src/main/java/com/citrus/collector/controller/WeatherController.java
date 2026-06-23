package com.citrus.collector.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.citrus.collector.service.WeatherService;
import com.citrus.collector.vo.WeatherDailyVO;
import com.citrus.collector.vo.WeatherHourlyVO;
import com.citrus.collector.vo.WeatherNowVO;
import com.citrus.common.constant.Constants;
import com.citrus.common.exception.BizException;
import com.citrus.common.result.Result;
import com.citrus.common.result.ResultCode;
import com.citrus.auth.model.LoginUser;
import com.citrus.orchard.entity.OrchardUser;
import com.citrus.orchard.mapper.OrchardUserMapper;
import com.citrus.util.SecurityUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 天气数据控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/weather")
@RequiredArgsConstructor
@Tag(name = "天气数据", description = "天气数据查询接口")
public class WeatherController {

    private final WeatherService weatherService;
    private final OrchardUserMapper orchardUserMapper;
    private final SecurityUtil securityUtil;

    /**
     * 获取实时天气
     * 系统管理员或果园关联用户可访问
     */
    @GetMapping("/now")
    @Operation(summary = "实时天气", description = "获取指定经纬度的实时天气数据")
    public Result<WeatherNowVO> getWeatherNow(
            @Parameter(description = "果园ID", required = true)
            @RequestParam("orchardId") Long orchardId,
            @Parameter(description = "果园经纬度，格式：经度,纬度（如：114.93,25.38）", required = true, example = "114.93,25.38")
            @RequestParam("location") String location) {

        // 格式化location参数（将空格替换为逗号）
        String formattedLocation = formatLocation(location);

        // 获取当前登录用户信息
        LoginUser loginUser = securityUtil.getCurrentUser();
        Long userId = loginUser.getUserId();
        boolean isSysAdmin = loginUser.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals(Constants.ROLE_ADMIN));

        // 系统管理员可直接访问，农户需要果园关联
        if (!isSysAdmin) {
            boolean hasOrchard = orchardUserMapper.exists(
                    new LambdaQueryWrapper<OrchardUser>()
                            .eq(OrchardUser::getOrchardId, orchardId)
                            .eq(OrchardUser::getUserId, userId)
            );
            if (!hasOrchard) {
                throw new BizException(ResultCode.FORBIDDEN, "仅果园关联用户可访问天气数据");
            }
        }

        // 调用天气服务获取实时天气
        WeatherNowVO response = weatherService.getWeatherNow(formattedLocation);
        
        // 保存天气数据到环境数据表
        try {
            weatherService.fetchAndSaveWeatherNow(orchardId, formattedLocation);
        } catch (Exception e) {
            log.warn("保存天气数据失败: {}", e.getMessage());
        }
        
        return Result.success(response);
    }

    /**
     * 获取24小时天气预报
     * 系统管理员或果园关联用户可访问
     */
    @GetMapping("/24h")
    @Operation(summary = "24小时天气预报", description = "获取指定经纬度的24小时逐小时天气预报")
    public Result<WeatherHourlyVO> getWeatherHourly(
            @Parameter(description = "果园经纬度，格式：经度,纬度（如：114.93,25.38）", required = true, example = "114.93,25.38")
            @RequestParam("location") String location) {

        // 格式化location参数（将空格替换为逗号）
        String formattedLocation = formatLocation(location);

        // 获取当前登录用户信息
        LoginUser loginUser = securityUtil.getCurrentUser();
        Long userId = loginUser.getUserId();
        boolean isSysAdmin = loginUser.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals(Constants.ROLE_ADMIN));

        // 系统管理员可直接访问，农户需要果园关联
        if (!isSysAdmin) {
            boolean hasOrchard = orchardUserMapper.exists(
                    new LambdaQueryWrapper<OrchardUser>()
                            .eq(OrchardUser::getUserId, userId)
            );
            if (!hasOrchard) {
                throw new BizException(ResultCode.FORBIDDEN, "仅果园关联用户可访问天气数据");
            }
        }

        // 调用天气服务获取24小时预报
        WeatherHourlyVO response = weatherService.getWeatherHourly(formattedLocation);
        return Result.success(response);
    }

    /**
     * 获取7天天气预报并保存到天气预报表
     * 系统管理员或果园关联用户可访问
     */
    @GetMapping("/7d")
    @Operation(summary = "7天天气预报", description = "获取指定经纬度的7天每日天气预报并保存到数据库")
    public Result<WeatherDailyVO> getWeather7d(
            @Parameter(description = "果园ID", required = true)
            @RequestParam("orchardId") Long orchardId,
            @Parameter(description = "果园经纬度，格式：经度,纬度（如：114.93,25.38）", required = true, example = "114.93,25.38")
            @RequestParam("location") String location) {

        // 格式化location参数（将空格替换为逗号）
        String formattedLocation = formatLocation(location);

        // 获取当前登录用户信息
        LoginUser loginUser = securityUtil.getCurrentUser();
        Long userId = loginUser.getUserId();
        boolean isSysAdmin = loginUser.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals(Constants.ROLE_ADMIN));

        // 系统管理员可直接访问，农户需要果园关联
        if (!isSysAdmin) {
            boolean hasOrchard = orchardUserMapper.exists(
                    new LambdaQueryWrapper<OrchardUser>()
                            .eq(OrchardUser::getOrchardId, orchardId)
                            .eq(OrchardUser::getUserId, userId)
            );
            if (!hasOrchard) {
                throw new BizException(ResultCode.FORBIDDEN, "仅果园关联用户可访问天气数据");
            }
        }

        // 调用天气服务获取7天预报
        WeatherDailyVO response = weatherService.getWeather7d(formattedLocation);
        
        // 保存7天预报数据到天气预报表
        try {
            weatherService.fetchAndSaveWeather7d(orchardId, formattedLocation);
        } catch (Exception e) {
            log.warn("保存7天天气预报数据失败: {}", e.getMessage());
        }
        
        return Result.success(response);
    }

    /**
     * 获取天气预报（前端兼容，同 /7d）
     */
    @GetMapping("/forecast")
    @Operation(summary = "天气预报", description = "获取指定经纬度的每日天气预报")
    public Result<WeatherDailyVO> getWeatherForecast(
            @RequestParam("orchardId") Long orchardId,
            @RequestParam("location") String location) {
        return getWeather7d(orchardId, location);
    }

    /**
     * 格式化location参数
     * 将空格替换为逗号，确保格式为"经度,纬度"
     */
    private String formatLocation(String location) {
        if (location == null) {
            throw new BizException(ResultCode.BAD_REQUEST, "location参数不能为空");
        }
        // 将空格替换为逗号
        return location.trim().replace(" ", ",");
    }
}
