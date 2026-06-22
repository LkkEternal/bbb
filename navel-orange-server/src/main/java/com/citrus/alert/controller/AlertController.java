package com.citrus.alert.controller;

import com.citrus.alert.service.AlertService;
import com.citrus.alert.vo.RiskAssessmentVO;
import com.citrus.common.constant.Constants;
import com.citrus.common.exception.BizException;
import com.citrus.common.result.Result;
import com.citrus.common.result.ResultCode;
import com.citrus.orchard.entity.OrchardUser;
import com.citrus.orchard.mapper.OrchardUserMapper;
import com.citrus.auth.model.LoginUser;
import com.citrus.util.SecurityUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 风险评估控制器
 *
 * 提供果园风险评估接口（需登录）
 * - 管理员可以查看所有果园
 * - 普通用户只能查看关联的果园
 */
@RestController
@RequestMapping("/api/alert")
@RequiredArgsConstructor
@Tag(name = "风险评估", description = "果园风险评估接口")
public class AlertController {

    private final AlertService alertService;
    private final SecurityUtil securityUtil;
    private final OrchardUserMapper orchardUserMapper;

    /**
     * 查询当前果园风险等级
     *
     * 权限说明：
     * - 管理员可以查看所有果园
     * - 普通用户只能查看关联的果园（作为园主或成员）
     */
    @GetMapping("/risk")
    @Operation(summary = "查询果园风险等级", description = "评估指定果园的风险等级")
    public Result<RiskAssessmentVO> assessRisk(
            @Parameter(description = "果园ID", required = true)
            @RequestParam("orchardId") Long orchardId) {
        
        // 获取当前登录用户
        Long userId = securityUtil.getCurrentUserId();
        LoginUser loginUser = securityUtil.getCurrentUser();
        
        // 检查用户角色
        boolean isAdmin = loginUser.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals(Constants.ROLE_ADMIN));
        
        // 如果不是管理员，需要检查是否有权限访问该果园
        if (!isAdmin) {
            OrchardUser orchardUser = orchardUserMapper.selectOne(
                    new LambdaQueryWrapper<OrchardUser>()
                            .eq(OrchardUser::getOrchardId, orchardId)
                            .eq(OrchardUser::getUserId, userId)
            );
            if (orchardUser == null) {
                throw new BizException(ResultCode.ORCHARD_NO_PERMISSION);
            }
        }
        
        RiskAssessmentVO result = alertService.assessRisk(orchardId);
        return Result.success(result);
    }
}
