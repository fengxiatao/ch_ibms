package cn.iocoder.yudao.module.iot.service.subsystem;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.tenant.core.aop.TenantIgnore;
import cn.iocoder.yudao.module.iot.controller.admin.subsystem.vo.SubsystemRespVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.product.IotProductDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.subsystem.SubsystemDO;
import cn.iocoder.yudao.module.iot.dal.mysql.device.IotDeviceMapper;
import cn.iocoder.yudao.module.iot.dal.mysql.product.IotProductMapper;
import cn.iocoder.yudao.module.iot.dal.mysql.subsystem.SubsystemMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * IoT子系统 Service 实现类
 * 
 * <p>注意：子系统定义是全局共享的，不区分租户
 *
 * @author IBMS Team
 */
@Service
@Slf4j
public class SubsystemServiceImpl implements SubsystemService {

    @Resource
    private SubsystemMapper subsystemMapper;

    @Resource
    private IotProductMapper productMapper;

    @Resource
    private IotDeviceMapper deviceMapper;

    @Override
    @TenantIgnore // 子系统是全局共享的，忽略租户过滤
    public List<SubsystemRespVO> getSubsystemList() {
        List<SubsystemDO> list = subsystemMapper.selectList(
            new LambdaQueryWrapper<SubsystemDO>()
                .eq(SubsystemDO::getEnabled, true)
                .orderByAsc(SubsystemDO::getSort)
        );

        return list.stream().map(this::convertToVO).collect(Collectors.toList());
    }

    @Override
    @TenantIgnore // 子系统是全局共享的，忽略租户过滤
    public List<SubsystemRespVO> getSubsystemTree() {
        log.info("[子系统查询] 开始查询子系统树，当前租户忽略状态: @TenantIgnore");
        
        // 获取所有子系统
        List<SubsystemDO> allSubsystems = subsystemMapper.selectList(
            new LambdaQueryWrapper<SubsystemDO>()
               .eq(SubsystemDO::getEnabled, true)
               .orderByAsc(SubsystemDO::getSort)
        );
        
        log.info("[子系统查询] 查询结果: 共 {} 条记录", allSubsystems == null ? 0 : allSubsystems.size());

        // 转换为VO
        List<SubsystemRespVO> allVOs = allSubsystems.stream()
            .map(this::convertToVO)
            .collect(Collectors.toList());

        // 构建树形结构
        List<SubsystemRespVO> tree = new ArrayList<>();
        Map<String, SubsystemRespVO> codeMap = new HashMap<>();

        // 先放入Map
        for (SubsystemRespVO vo : allVOs) {
            codeMap.put(vo.getCode(), vo);
            vo.setChildren(new ArrayList<>());
        }

        // 构建父子关系
        for (SubsystemRespVO vo : allVOs) {
            if (vo.getLevel() == 1) {
                // 一级系统，直接加入树
                tree.add(vo);
            } else if (vo.getLevel() == 2 && vo.getParentCode() != null) {
                // 二级系统，添加到父系统的children中
                SubsystemRespVO parent = codeMap.get(vo.getParentCode());
                if (parent != null) {
                    parent.getChildren().add(vo);
                }
            }
        }

        return tree;
    }

    @Override
    @TenantIgnore // 子系统是全局共享的，忽略租户过滤
    public SubsystemRespVO getSubsystemByCode(String code) {
        SubsystemDO subsystem = subsystemMapper.selectOne(
            new LambdaQueryWrapper<SubsystemDO>()
                .eq(SubsystemDO::getCode, code)
        );

        return subsystem != null ? convertToVO(subsystem) : null;
    }

    @Override
    @TenantIgnore // 子系统是全局共享的，忽略租户过滤
    public List<SubsystemRespVO> getSubsystemListByParent(String parentCode) {
        List<SubsystemDO> list = subsystemMapper.selectList(
            new LambdaQueryWrapper<SubsystemDO>()
                .eq(SubsystemDO::getParentCode, parentCode)
                .eq(SubsystemDO::getEnabled, true)
                .orderByAsc(SubsystemDO::getSort)
        );

        return list.stream().map(this::convertToVO).collect(Collectors.toList());
    }

    @Override
    @TenantIgnore // 子系统是全局共享的，忽略租户过滤
    public List<SubsystemRespVO> getSubsystemStatistics() {
        // 获取所有二级子系统
        List<SubsystemDO> subsystems = subsystemMapper.selectList(
            new LambdaQueryWrapper<SubsystemDO>()
                .eq(SubsystemDO::getLevel, 2)
                .eq(SubsystemDO::getEnabled, true)
                .orderByAsc(SubsystemDO::getSort)
        );

        // 新架构：产品/设备不再使用 subsystemCode，已改为菜单关联
        // 暂时返回0，后续可以基于菜单关联重新实现统计
        List<SubsystemRespVO> result = new ArrayList<>();
        for (SubsystemDO subsystem : subsystems) {
            SubsystemRespVO vo = convertToVO(subsystem);
            vo.setProductCount(0);
            vo.setDeviceCount(0);
            result.add(vo);
        }

        return result;
        
        // 旧实现已注释（保留用于参考）：
        // Long productCount = productMapper.selectCount(
        //     new LambdaQueryWrapper<IotProductDO>()
        //         .eq(IotProductDO::getSubsystemCode, subsystem.getCode())
        // );
        // vo.setProductCount(productCount.intValue());
        // Long deviceCount = deviceMapper.selectCount(
        //     new LambdaQueryWrapper<IotDeviceDO>()
        //         .eq(IotDeviceDO::getSubsystemCode, subsystem.getCode())
        // );
        // vo.setDeviceCount(deviceCount.intValue());
    }

    /**
     * 转换为VO
     */
    private SubsystemRespVO convertToVO(SubsystemDO subsystem) {
        SubsystemRespVO vo = new SubsystemRespVO();
        BeanUtils.copyProperties(subsystem, vo);
        return vo;
    }
}


