package com.citrus.orchard.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.citrus.annotation.OperationLog;
import com.citrus.common.amap.AmapClient;
import com.citrus.common.constant.Constants;
import com.citrus.common.exception.BizException;
import com.citrus.common.result.ResultCode;
import com.citrus.orchard.dto.AddMemberDTO;
import com.citrus.orchard.dto.CreateOrchardDTO;
import com.citrus.orchard.dto.UpdateOrchardDTO;
import com.citrus.orchard.entity.Orchard;
import com.citrus.orchard.entity.OrchardUser;
import com.citrus.orchard.mapper.OrchardMapper;
import com.citrus.orchard.mapper.OrchardUserMapper;
import com.citrus.orchard.service.OrchardService;
import com.citrus.orchard.vo.CreateOrchardVO;
import com.citrus.orchard.vo.OrchardDetailVO;
import com.citrus.orchard.vo.OrchardListVO;
import com.citrus.orchard.vo.OrchardMemberVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * 果园服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrchardServiceImpl implements OrchardService {

    private final OrchardMapper orchardMapper;
    private final OrchardUserMapper orchardUserMapper;
    private final AmapClient amapClient;//高德工具类

    @Override
    @Transactional
    @OperationLog(module = "orchard", action = "create", description = "创建果园")
    public CreateOrchardVO createOrchard(CreateOrchardDTO dto, Long userId) {
        // 0. 检查用户果园数量是否已达上限（只统计作为园主的）
        long count = orchardMapper.countByOwnerId(userId);
        if (count >= Constants.MAX_ORCHARD_PER_USER) {
            throw new BizException(ResultCode.ORCHARD_LIMIT_EXCEEDED);
        }

        // 1. 调用高德地图API进行地理编码，获取经纬度
        String location = geocodeAddress(dto.getFormattedAddress());

        // 2. 创建果园
        Orchard orchard = new Orchard();
        orchard.setOwnerId(userId);
        orchard.setName(dto.getName());
        orchard.setFormattedAddress(dto.getFormattedAddress());
        orchard.setLocation(location);
        orchard.setAreaMu(dto.getAreaMu());
        orchard.setTreeAge(dto.getTreeAge());
        orchard.setVariety(dto.getVariety() != null ? dto.getVariety() : "脐橙");
        orchard.setStatus(1);
        orchardMapper.insert(orchard);

        // 3. 创建果园用户关联（园主）
        OrchardUser orchardUser = new OrchardUser();
        orchardUser.setOrchardId(orchard.getId());
        orchardUser.setUserId(userId);
        orchardUser.setRoleType(Constants.ORCHARD_ROLE_OWNER);
        orchardUserMapper.insert(orchardUser);

        //日志打印输出
        log.info("用户[{}]创建果园[{}]成功，果园ID: {}", userId, orchard.getName(), orchard.getId());

        // 4. 返回响应
        CreateOrchardVO vo = new CreateOrchardVO();
        vo.setId(orchard.getId());
        vo.setName(orchard.getName());
        vo.setFormattedAddress(orchard.getFormattedAddress());
        vo.setLocation(orchard.getLocation());
        return vo;
    }

    @Override
    public List<OrchardListVO> listOrchards(Long userId, boolean isAdmin) {
        List<OrchardListVO> list;
        if (isAdmin) {
            list = orchardMapper.selectAllOrchardList();
        } else {
            list = orchardMapper.selectOrchardListByUserId(userId);
        }
        // 转换 location 格式
        list.forEach(vo -> vo.setLocation(convertWktToLocation(vo.getLocation())));
        return list;
    }

    @Override
    public OrchardDetailVO getOrchardDetail(Long orchardId, Long userId, boolean isAdmin) {
        // 1. 查询果园基本信息
        OrchardDetailVO detail = orchardMapper.selectOrchardDetail(orchardId);
        if (detail == null) {
            throw new BizException(ResultCode.ORCHARD_NOT_FOUND);
        }

        // 转换 location 格式
        detail.setLocation(convertWktToLocation(detail.getLocation()));

        // 2. 权限校验：管理员可以查看所有果园，普通用户只能查看关联的果园
        if (!isAdmin) {
            OrchardUser orchardUser = orchardUserMapper.selectOne(
                    new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<OrchardUser>()
                            .eq(OrchardUser::getOrchardId, orchardId)
                            .eq(OrchardUser::getUserId, userId)
            );
            if (orchardUser == null) {
                throw new BizException(ResultCode.ORCHARD_NO_PERMISSION);
            }
            detail.setRoleType(orchardUser.getRoleType());
        } else {
            detail.setRoleType("owner");
        }

        // 3. 查询成员列表
        List<OrchardMemberVO> members = orchardMapper.selectOrchardMembers(orchardId);
        detail.setMembers(members);

        // 4. 设备列表暂时为空
        detail.setDevices(new ArrayList<>());

        return detail;
    }

    @Override
    @Transactional
    public void addMember(Long orchardId, AddMemberDTO dto, Long operatorId) {
        // 1. 验证果园是否存在
        Orchard orchard = orchardMapper.selectById(orchardId);
        if (orchard == null) {
            throw new BizException(ResultCode.ORCHARD_NOT_FOUND);
        }

        // 2. 验证操作者权限（必须是园主或管理员）
        //查询 orchard_user 表中，果园 ID = 指定果园 ID 同时 用户 ID = 指定操作人 ID 的那条数据
        OrchardUser operator = orchardUserMapper.selectOne(
                new LambdaQueryWrapper<OrchardUser>()//创建专门给 OrchardUser 表用的条件封装对象，后面所有 .eq() 都是往里面叠加查询限制。
                        .eq(OrchardUser::getOrchardId, orchardId)//实体类的 get 方法，当果园的id等于查到的果园id
                        .eq(OrchardUser::getUserId, operatorId)//+操作者的id等于查到的用户id
        );
        if (operator == null ||
                (!operator.getRoleType().equals(Constants.ORCHARD_ROLE_OWNER) &&
                        !operator.getRoleType().equals(Constants.ORCHARD_ROLE_MANAGER))) {
            throw new BizException(ResultCode.ORCHARD_NO_PERMISSION);
        }

        // 3. 验证角色类型是否合法（只能添加 manager 或 viewer）
        if (!dto.getRoleType().equals(Constants.ORCHARD_ROLE_MANAGER) &&
                !dto.getRoleType().equals(Constants.ORCHARD_ROLE_VIEWER)) {
            throw new BizException(ResultCode.BAD_REQUEST, "只能添加管理员或查看者角色");
        }

        // 4. 管理员只能添加 viewer
        if (operator.getRoleType().equals(Constants.ORCHARD_ROLE_MANAGER) &&
                !dto.getRoleType().equals(Constants.ORCHARD_ROLE_VIEWER)) {
            throw new BizException(ResultCode.ORCHARD_NO_PERMISSION, "管理员只能添加查看者");
        }

        // 5. 检查用户是否已经是该果园的成员
        OrchardUser existingMember = orchardUserMapper.selectOne(
                new LambdaQueryWrapper<OrchardUser>()
                        .eq(OrchardUser::getOrchardId, orchardId)
                        .eq(OrchardUser::getUserId, dto.getUserId())
        );
        if (existingMember != null) {
            throw new BizException(ResultCode.CONFLICT, "该用户已经是果园成员");
        }

        // 6. 添加成员
        OrchardUser newMember = new OrchardUser();
        newMember.setOrchardId(orchardId);
        newMember.setUserId(dto.getUserId());
        newMember.setRoleType(dto.getRoleType());
        orchardUserMapper.insert(newMember);

        log.info("果园[{}]添加成员[{}]成功，角色: {}", orchardId, dto.getUserId(), dto.getRoleType());
    }

    @Override
    @Transactional
    public void removeMember(Long orchardId, Long targetUserId, Long operatorId) {
        // 1. 验证果园是否存在
        Orchard orchard = orchardMapper.selectById(orchardId);
        if (orchard == null) {
            throw new BizException(ResultCode.ORCHARD_NOT_FOUND);
        }

        // 2. 验证操作者权限（必须是园主或管理员）
        OrchardUser operator = orchardUserMapper.selectOne(
                new LambdaQueryWrapper<OrchardUser>()
                        .eq(OrchardUser::getOrchardId, orchardId)
                        .eq(OrchardUser::getUserId, operatorId)
        );
        if (operator == null ||
                (!operator.getRoleType().equals(Constants.ORCHARD_ROLE_OWNER) &&
                        !operator.getRoleType().equals(Constants.ORCHARD_ROLE_MANAGER))) {
            throw new BizException(ResultCode.ORCHARD_NO_PERMISSION);
        }

        // 3. 不能移除自己
        if (operatorId.equals(targetUserId)) {
            throw new BizException(ResultCode.BAD_REQUEST, "不能移除自己");
        }

        // 4. 验证被移除用户是否是该果园的成员
        OrchardUser targetMember = orchardUserMapper.selectOne(
                new LambdaQueryWrapper<OrchardUser>()
                        .eq(OrchardUser::getOrchardId, orchardId)
                        .eq(OrchardUser::getUserId, targetUserId)
        );
        if (targetMember == null) {
            throw new BizException(ResultCode.NOT_FOUND, "该用户不是果园成员");
        }

        // 5. 权限校验：管理员只能移除 viewer
        if (operator.getRoleType().equals(Constants.ORCHARD_ROLE_MANAGER) &&
                !targetMember.getRoleType().equals(Constants.ORCHARD_ROLE_VIEWER)) {
            throw new BizException(ResultCode.ORCHARD_NO_PERMISSION, "管理员只能移除查看者");
        }

        // 6. 移除成员
        orchardUserMapper.delete(
                new LambdaQueryWrapper<OrchardUser>()
                        .eq(OrchardUser::getOrchardId, orchardId)
                        .eq(OrchardUser::getUserId, targetUserId)
        );

        log.info("果园[{}]移除成员[{}]成功，操作者: {}", orchardId, targetUserId, operatorId);
    }

    @Override
    @Transactional
    @OperationLog(module = "orchard", action = "update", description = "更新果园")
    public void updateOrchard(Long orchardId, UpdateOrchardDTO dto, Long operatorId) {
        // 1. 验证果园是否存在
        Orchard orchard = orchardMapper.selectById(orchardId);
        if (orchard == null) {
            throw new BizException(ResultCode.ORCHARD_NOT_FOUND);
        }

        // 2. 验证操作者权限（必须是园主或管理员）
        OrchardUser operator = orchardUserMapper.selectOne(
                new LambdaQueryWrapper<OrchardUser>()
                        .eq(OrchardUser::getOrchardId, orchardId)
                        .eq(OrchardUser::getUserId, operatorId)
        );
        if (operator == null ||
                (!operator.getRoleType().equals(Constants.ORCHARD_ROLE_OWNER) &&
                        !operator.getRoleType().equals(Constants.ORCHARD_ROLE_MANAGER))) {
            throw new BizException(ResultCode.ORCHARD_NO_PERMISSION);
        }

        // 3. 更新果园信息（只更新非空字段）
        if (dto.getName() != null && !dto.getName().isEmpty()) {
            orchard.setName(dto.getName());
        }
        if (dto.getAreaMu() != null) {
            orchard.setAreaMu(dto.getAreaMu());
        }
        if (dto.getTreeAge() != null) {
            orchard.setTreeAge(dto.getTreeAge());
        }
        if (dto.getVariety() != null && !dto.getVariety().isEmpty()) {
            orchard.setVariety(dto.getVariety());
        }

        // 4. 如果传入了新地址，需要进行地理编码
        if (dto.getFormattedAddress() != null && !dto.getFormattedAddress().isEmpty()) {
            String location = geocodeAddress(dto.getFormattedAddress());
            orchard.setFormattedAddress(dto.getFormattedAddress());
            orchard.setLocation(location);
        }

        // 5. 保存更新
        orchardMapper.updateById(orchard);

        log.info("果园[{}]更新成功，操作者: {}", orchardId, operatorId);
    }

    @Override
    @Transactional
    @OperationLog(module = "orchard", action = "delete", description = "删除果园")
    public void deleteOrchard(Long orchardId, Long operatorId) {
        // 1. 验证果园是否存在
        Orchard orchard = orchardMapper.selectById(orchardId);
        if (orchard == null) {
            throw new BizException(ResultCode.ORCHARD_NOT_FOUND);
        }

        // 2. 验证操作者权限（必须是园主）
        OrchardUser operator = orchardUserMapper.selectOne(
                new LambdaQueryWrapper<OrchardUser>()
                        .eq(OrchardUser::getOrchardId, orchardId)
                        .eq(OrchardUser::getUserId, operatorId)
        );
        if (operator == null || !operator.getRoleType().equals(Constants.ORCHARD_ROLE_OWNER)) {
            throw new BizException(ResultCode.ORCHARD_NO_PERMISSION, "只有园主可以删除果园");
        }

        // 3. 软删除果园（MyBatis-Plus @TableLogic 会自动将 deleted 设置为 true）
        orchardMapper.deleteById(orchardId);

        log.info("果园[{}]删除成功，操作者: {}", orchardId, operatorId);
    }

    /**
     * 将 WKT 格式转换为 "经度,纬度" 格式
     * 输入: "POINT(114.933494 25.831139)"
     * 输出: "114.933494,25.831139"
     */
    private String convertWktToLocation(String wkt) {
        if (wkt == null || !wkt.startsWith("POINT")) {
            return null;
        }
        try {
            // 提取 POINT(...) 中的坐标
            String coords = wkt.substring(6, wkt.length() - 1);
            String[] parts = coords.split("\\s+");
            if (parts.length == 2) {
                return parts[0] + "," + parts[1];
            }
        } catch (Exception e) {
            log.warn("WKT格式转换失败: {}", wkt, e);
        }
        return null;
    }

    /**
     * 调用高德地图API进行地理编码
     * @param address 地址
     * @return 经纬度，格式：经度,纬度
     */
    private String geocodeAddress(String address) {
        try {
            AmapClient.GeoResult geoResult = amapClient.geo(address);
            if (geoResult != null && geoResult.getGeocodes() != null && !geoResult.getGeocodes().isEmpty()) {
                AmapClient.Geocode geocode = geoResult.getGeocodes().get(0);
                String location = geocode.getLocation();
                String level = geocode.getLevel();

                // 检查地址精度级别（至少需要乡镇级）
                if (!Constants.ALLOWED_GEOCODE_LEVELS.contains(level)) {
                    log.warn("地址[{}]精度不足，level: {}", address, level);
                    throw new BizException(ResultCode.GEOCODE_FAILED);
                }

                // 检查 location 是否有效
                if (location != null && !location.isEmpty() && location.contains(",")) {
                    log.info("地址[{}]地理编码成功，经纬度: {}, level: {}", address, location, level);
                    return location;
                }

                // location 为空，抛出异常
                log.warn("地址[{}]地理编码返回的经纬度为空", address);
                throw new BizException(ResultCode.GEOCODE_FAILED);
            }
            // 未获取到结果，抛出异常
            log.warn("地址[{}]地理编码失败，未获取到结果", address);
            throw new BizException(ResultCode.GEOCODE_FAILED);
        } catch (BizException e) {
            throw e;
        } catch (Exception e) {
            log.error("地址[{}]地理编码异常", address, e);
            throw new BizException(ResultCode.GEOCODE_FAILED);
        }
    }
}
