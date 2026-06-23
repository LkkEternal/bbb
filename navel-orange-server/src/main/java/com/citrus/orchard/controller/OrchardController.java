package com.citrus.orchard.controller;

import com.citrus.common.constant.Constants;
import com.citrus.common.exception.BizException;
import com.citrus.common.result.Result;
import com.citrus.common.result.ResultCode;
import com.citrus.orchard.dto.AddMemberDTO;
import com.citrus.orchard.dto.CreateOrchardDTO;
import com.citrus.orchard.dto.UpdateOrchardDTO;
import com.citrus.orchard.service.OrchardService;
import com.citrus.orchard.vo.CreateOrchardVO;
import com.citrus.orchard.vo.OrchardDetailVO;
import com.citrus.orchard.vo.OrchardListVO;
import com.citrus.auth.model.LoginUser;
import com.citrus.util.SecurityUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 果园管理控制器
 */
@RestController
@RequestMapping("/api/orchards")
@RequiredArgsConstructor
@Tag(name = "果园管理", description = "果园CRUD接口")
public class OrchardController {

    private final OrchardService orchardService;
    private final SecurityUtil securityUtil;

    /**
     * 创建果园
     * 创建者自动成为园主
     * 系统管理员不能创建果园
     */
    @PostMapping
    @Operation(summary = "创建果园")
    public Result<CreateOrchardVO> createOrchard(@Valid @RequestBody CreateOrchardDTO dto) {
        LoginUser loginUser = securityUtil.getCurrentUser();

        // 系统管理员不能创建果园
        boolean isAdmin = loginUser.getAuthorities().stream()//当前登录用户获取其角色权限集合再把集合转为转为 Java8 流式操作，方便遍历判断
                //anyMatch（）流式操作只要有一个满足条件就返回true，全部不满足返回false
                //a为遍历的权限对象 GrantedAuthority。->表示分开函数与参数。a的权限对象是系统管理员
                .anyMatch(a -> a.getAuthority().equals(Constants.ROLE_ADMIN));
        if (isAdmin) {
            throw new BizException(ResultCode.FORBIDDEN, "系统管理员不能创建果园");
        }

        Long userId = loginUser.getUserId();
        CreateOrchardVO vo = orchardService.createOrchard(dto, userId);
        return Result.success("创建成功", vo);
    }

    /**
     * 获取果园列表
     * 普通用户：只返回关联的果园
     * 管理员：返回所有果园
     */
    @GetMapping
    @Operation(summary = "我的果园列表")
    public Result<List<OrchardListVO>> listOrchards() {
        LoginUser loginUser = securityUtil.getCurrentUser();
        Long userId = loginUser.getUserId();
        boolean isAdmin = loginUser.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals(Constants.ROLE_ADMIN));
        List<OrchardListVO> list = orchardService.listOrchards(userId, isAdmin);
        return Result.success(list);
    }

    /**
     * 获取果园详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "果园详情")
    public Result<OrchardDetailVO> getOrchardDetail(@PathVariable("id") Long id) {
        LoginUser loginUser = securityUtil.getCurrentUser();
        Long userId = loginUser.getUserId();
        boolean isAdmin = loginUser.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals(Constants.ROLE_ADMIN));
        OrchardDetailVO detail = orchardService.getOrchardDetail(id, userId, isAdmin);
        return Result.success(detail);
    }

    /**
     * 更新果园
     * 园主和管理员可以更新果园信息
     */
    @PutMapping("/{id}")
    @Operation(summary = "更新果园")
    public Result<Void> updateOrchard(
            @PathVariable("id") Long id,
            @Valid @RequestBody UpdateOrchardDTO dto) {
        Long userId = securityUtil.getCurrentUserId();
        orchardService.updateOrchard(id, dto, userId);
        return Result.success("更新成功", null);
    }

    /**
     * 删除果园（软删除）
     * 只有园主可以删除果园
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除果园")
    public Result<Void> deleteOrchard(@PathVariable("id") Long id) {
        Long userId = securityUtil.getCurrentUserId();
        orchardService.deleteOrchard(id, userId);
        return Result.success("删除成功", null);
    }

    /**
     * 添加果园成员
     * 园主可以添加管理员和查看者
     * 管理员只能添加查看者
     */
    @PostMapping("/{id}/members")
    @Operation(summary = "添加成员")
    public Result<Void> addMember(
            @PathVariable("id") Long id,
            @Valid @RequestBody AddMemberDTO dto) {
        Long userId = securityUtil.getCurrentUserId();
        orchardService.addMember(id, dto, userId);
        return Result.success("添加成功", null);
    }

    /**
     * 移除果园成员
     * 园主可以移除管理员和查看者
     * 管理员只能移除查看者
     */
    @DeleteMapping("/{id}/members/{userId}")
    @Operation(summary = "移除成员")
    public Result<Void> removeMember(
            @PathVariable("id") Long id,
            @PathVariable("userId") Long userId) {
        Long operatorId = securityUtil.getCurrentUserId();
        orchardService.removeMember(id, userId, operatorId);
        return Result.success("移除成功", null);
    }
}
