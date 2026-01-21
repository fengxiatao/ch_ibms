package cn.iocoder.yudao.module.iot.service.access;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.iot.dal.dataobject.access.IotAccessPermissionGroupDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.access.IotAccessPermissionGroupDeviceDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.access.IotAccessPermissionGroupPersonDO;
import cn.iocoder.yudao.module.iot.dal.mysql.access.IotAccessPermissionGroupDeviceMapper;
import cn.iocoder.yudao.module.iot.dal.mysql.access.IotAccessPermissionGroupMapper;
import cn.iocoder.yudao.module.iot.dal.mysql.access.IotAccessPermissionGroupPersonMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import jakarta.annotation.Resource;
import org.springframework.context.annotation.Lazy;
import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.iot.enums.ErrorCodeConstants.*;

/**
 * 门禁权限组 Service 实现类
 *
 * @author 芋道源码
 */
@Slf4j
@Service
@Validated
public class IotAccessPermissionGroupServiceImpl implements IotAccessPermissionGroupService {

    @Resource
    private IotAccessPermissionGroupMapper groupMapper;

    @Resource
    private IotAccessPermissionGroupDeviceMapper groupDeviceMapper;

    @Resource
    private IotAccessPermissionGroupPersonMapper groupPersonMapper;

    @Resource
    private cn.iocoder.yudao.module.iot.dal.mysql.device.IotDeviceMapper deviceMapper;

    @Resource
    private cn.iocoder.yudao.module.iot.dal.mysql.channel.IotDeviceChannelMapper channelMapper;

    @Resource
    private cn.iocoder.yudao.module.iot.dal.mysql.access.IotAccessPersonMapper personMapper;

    @Resource
    private cn.iocoder.yudao.module.iot.dal.mysql.access.IotAccessDepartmentMapper departmentMapper;

    @Resource
    @Lazy  // 使用延迟加载打破循环依赖
    private IotAccessAuthDispatchService authDispatchService;

    // ========== 权限组管理 ==========

    @Override
    public Long createPermissionGroup(IotAccessPermissionGroupDO group) {
        groupMapper.insert(group);
        return group.getId();
    }

    @Override
    public void updatePermissionGroup(IotAccessPermissionGroupDO group) {
        validateGroupExists(group.getId());
        groupMapper.updateById(group);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deletePermissionGroup(Long id) {
        validateGroupExists(id);
        // 删除关联的设备
        groupDeviceMapper.deleteByGroupId(id);
        // 删除关联的人员
        groupPersonMapper.deleteByGroupId(id);
        // 删除权限组
        groupMapper.deleteById(id);
    }

    @Override
    public IotAccessPermissionGroupDO getPermissionGroup(Long id) {
        return groupMapper.selectById(id);
    }

    @Override
    public PageResult<IotAccessPermissionGroupDO> getPermissionGroupPage(String groupName, Integer status,
                                                                          Integer pageNo, Integer pageSize) {
        return groupMapper.selectPage(groupName, status, pageNo, pageSize);
    }

    @Override
    public List<IotAccessPermissionGroupDO> getEnabledPermissionGroups() {
        return groupMapper.selectListByStatus(0); // 0-正常
    }


    // ========== 设备关联 ==========

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void setGroupDevices(Long groupId, List<Long> deviceIds) {
        validateGroupExists(groupId);
        // 删除原有关联
        groupDeviceMapper.deleteByGroupId(groupId);
        // 添加新关联
        if (deviceIds != null && !deviceIds.isEmpty()) {
            for (Long deviceId : deviceIds) {
                IotAccessPermissionGroupDeviceDO groupDevice = IotAccessPermissionGroupDeviceDO.builder()
                        .groupId(groupId)
                        .deviceId(deviceId)
                        .build();
                groupDeviceMapper.insert(groupDevice);
            }
        }
    }

    @Override
    public void addDeviceToGroup(Long groupId, Long deviceId, Long channelId) {
        validateGroupExists(groupId);
        // 检查是否已存在（未删除的记录）
        IotAccessPermissionGroupDeviceDO existing = groupDeviceMapper.selectByGroupIdAndDeviceIdAndChannelId(groupId, deviceId, channelId);
        if (existing != null) {
            return; // 已存在，不重复添加
        }
        // 先物理删除可能存在的软删除记录，避免唯一约束冲突
        // 关联表不需要保留软删除记录
        groupDeviceMapper.physicalDeleteByGroupIdAndDeviceIdAndChannelId(groupId, deviceId, channelId);
        
        IotAccessPermissionGroupDeviceDO groupDevice = IotAccessPermissionGroupDeviceDO.builder()
                .groupId(groupId)
                .deviceId(deviceId)
                .channelId(channelId)
                .build();
        groupDeviceMapper.insert(groupDevice);
    }

    @Override
    public void removeDeviceFromGroup(Long groupId, Long deviceId) {
        groupDeviceMapper.delete(new cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX<IotAccessPermissionGroupDeviceDO>()
                .eq(IotAccessPermissionGroupDeviceDO::getGroupId, groupId)
                .eq(IotAccessPermissionGroupDeviceDO::getDeviceId, deviceId));
    }

    @Override
    public List<IotAccessPermissionGroupDeviceDO> getGroupDevices(Long groupId) {
        return groupDeviceMapper.selectListByGroupId(groupId);
    }

    @Override
    public List<IotAccessPermissionGroupDeviceDO> getDeviceGroups(Long deviceId) {
        return groupDeviceMapper.selectListByDeviceId(deviceId);
    }

    // ========== 人员关联 ==========

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addPersonsToGroup(Long groupId, List<Long> personIds) {
        validateGroupExists(groupId);
        if (personIds == null || personIds.isEmpty()) {
            return;
        }
        for (Long personId : personIds) {
            // 检查是否已存在（未删除的记录）
            IotAccessPermissionGroupPersonDO existing = groupPersonMapper.selectByGroupIdAndPersonId(groupId, personId);
            if (existing != null) {
                continue; // 已存在，跳过
            }
            // 先物理删除可能存在的软删除记录，避免唯一约束冲突
            // 关联表不需要保留软删除记录
            groupPersonMapper.physicalDeleteByGroupIdAndPersonId(groupId, personId);
            
            IotAccessPermissionGroupPersonDO groupPerson = IotAccessPermissionGroupPersonDO.builder()
                    .groupId(groupId)
                    .personId(personId)
                    .build();
            groupPersonMapper.insert(groupPerson);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removePersonsFromGroup(Long groupId, List<Long> personIds) {
        if (personIds == null || personIds.isEmpty()) {
            return;
        }
        for (Long personId : personIds) {
            groupPersonMapper.delete(new cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX<IotAccessPermissionGroupPersonDO>()
                    .eq(IotAccessPermissionGroupPersonDO::getGroupId, groupId)
                    .eq(IotAccessPermissionGroupPersonDO::getPersonId, personId));
        }
    }

    @Override
    public List<IotAccessPermissionGroupPersonDO> getGroupPersons(Long groupId) {
        return groupPersonMapper.selectListByGroupId(groupId);
    }

    @Override
    public List<IotAccessPermissionGroupPersonDO> getPersonGroups(Long personId) {
        return groupPersonMapper.selectListByPersonId(personId);
    }

    @Override
    public Long getGroupPersonCount(Long groupId) {
        return groupPersonMapper.selectCountByGroupId(groupId);
    }

    private void validateGroupExists(Long id) {
        if (groupMapper.selectById(id) == null) {
            throw exception(ACCESS_PERMISSION_GROUP_NOT_EXISTS);
        }
    }

    // ========== 便捷方法 ==========

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addDevices(Long groupId, List<Long> deviceIds, List<Long> channelIds) {
        validateGroupExists(groupId);
        if (deviceIds == null || deviceIds.isEmpty()) {
            return;
        }
        for (int i = 0; i < deviceIds.size(); i++) {
            Long deviceId = deviceIds.get(i);
            Long channelId = (channelIds != null && i < channelIds.size()) ? channelIds.get(i) : null;
            addDeviceToGroup(groupId, deviceId, channelId);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateDevices(Long groupId, List<Long> deviceIds, List<Long> channelIds) {
        validateGroupExists(groupId);
        // 删除原有关联
        groupDeviceMapper.deleteByGroupId(groupId);
        // 添加新关联
        addDevices(groupId, deviceIds, channelIds);
    }

    @Override
    public Integer getDeviceCount(Long groupId) {
        return groupDeviceMapper.selectCountByGroupId(groupId).intValue();
    }

    @Override
    public Integer getPersonCount(Long groupId) {
        return groupPersonMapper.selectCountByGroupId(groupId).intValue();
    }

    @Override
    public List<IotAccessPermissionGroupDO> getPermissionGroupList(String groupName, Integer status) {
        return groupMapper.selectList(new cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX<IotAccessPermissionGroupDO>()
                .likeIfPresent(IotAccessPermissionGroupDO::getGroupName, groupName)
                .eqIfPresent(IotAccessPermissionGroupDO::getStatus, status)
                .orderByDesc(IotAccessPermissionGroupDO::getId));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addPersons(Long groupId, List<Long> personIds) {
        addPersonsToGroup(groupId, personIds);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removePersons(Long groupId, List<Long> personIds) {
        removePersonsFromGroup(groupId, personIds);
    }

    @Override
    public List<cn.iocoder.yudao.module.iot.controller.admin.access.vo.permissiongroup.IotAccessPermissionGroupDeviceRespVO> getGroupDevicesWithDetail(Long groupId) {
        List<IotAccessPermissionGroupDeviceDO> devices = groupDeviceMapper.selectListByGroupId(groupId);
        List<cn.iocoder.yudao.module.iot.controller.admin.access.vo.permissiongroup.IotAccessPermissionGroupDeviceRespVO> result = new java.util.ArrayList<>();
        for (IotAccessPermissionGroupDeviceDO device : devices) {
            cn.iocoder.yudao.module.iot.controller.admin.access.vo.permissiongroup.IotAccessPermissionGroupDeviceRespVO vo = 
                new cn.iocoder.yudao.module.iot.controller.admin.access.vo.permissiongroup.IotAccessPermissionGroupDeviceRespVO();
            vo.setDeviceId(device.getDeviceId());
            vo.setChannelId(device.getChannelId());
            // 查询设备信息
            if (device.getDeviceId() != null) {
                cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceDO deviceDO = deviceMapper.selectById(device.getDeviceId());
                if (deviceDO != null) {
                    vo.setDeviceName(deviceDO.getDeviceName());
                    // 从config中获取IP（需要转换为AccessDeviceConfig）
                    if (deviceDO.getConfig() != null && deviceDO.getConfig() instanceof cn.iocoder.yudao.module.iot.dal.dataobject.device.config.AccessDeviceConfig) {
                        cn.iocoder.yudao.module.iot.dal.dataobject.device.config.AccessDeviceConfig accessConfig = 
                            (cn.iocoder.yudao.module.iot.dal.dataobject.device.config.AccessDeviceConfig) deviceDO.getConfig();
                        vo.setDeviceIp(accessConfig.getIpAddress());
                    }
                }
            }
            // 查询通道信息
            if (device.getChannelId() != null) {
                cn.iocoder.yudao.module.iot.dal.dataobject.channel.IotDeviceChannelDO channelDO = channelMapper.selectById(device.getChannelId());
                if (channelDO != null) {
                    vo.setChannelNo(channelDO.getChannelNo());
                    vo.setChannelName(channelDO.getChannelName());
                }
            }
            result.add(vo);
        }
        return result;
    }

    @Override
    public List<cn.iocoder.yudao.module.iot.controller.admin.access.vo.permissiongroup.IotAccessPermissionGroupPersonRespVO> getGroupPersonsWithDetail(Long groupId) {
        List<IotAccessPermissionGroupPersonDO> persons = groupPersonMapper.selectListByGroupId(groupId);
        List<cn.iocoder.yudao.module.iot.controller.admin.access.vo.permissiongroup.IotAccessPermissionGroupPersonRespVO> result = new java.util.ArrayList<>();
        for (IotAccessPermissionGroupPersonDO person : persons) {
            cn.iocoder.yudao.module.iot.controller.admin.access.vo.permissiongroup.IotAccessPermissionGroupPersonRespVO vo = 
                new cn.iocoder.yudao.module.iot.controller.admin.access.vo.permissiongroup.IotAccessPermissionGroupPersonRespVO();
            vo.setPersonId(person.getPersonId());
            // 查询人员信息
            if (person.getPersonId() != null) {
                cn.iocoder.yudao.module.iot.dal.dataobject.access.IotAccessPersonDO personDO = personMapper.selectById(person.getPersonId());
                if (personDO != null) {
                    vo.setPersonCode(personDO.getPersonCode());
                    vo.setPersonName(personDO.getPersonName());
                    vo.setDeptId(personDO.getDeptId());
                    // 查询部门名称
                    if (personDO.getDeptId() != null) {
                        cn.iocoder.yudao.module.iot.dal.dataobject.access.IotAccessDepartmentDO deptDO = departmentMapper.selectById(personDO.getDeptId());
                        if (deptDO != null) {
                            vo.setDeptName(deptDO.getDeptName());
                        }
                    }
                }
            }
            result.add(vo);
        }
        return result;
    }

    // ========== 带授权下发的人员管理 (Requirements: 1.1, 2.1) ==========

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long addPersonsWithDispatch(Long groupId, List<Long> personIds) {
        // 1. 保存人员关联
        addPersonsToGroup(groupId, personIds);
        
        // 2. 触发授权下发
        log.info("[addPersonsWithDispatch] 权限组 {} 添加人员 {}，触发授权下发", groupId, personIds);
        return authDispatchService.dispatchPermissionGroupPersons(groupId, personIds);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long removePersonsWithRevoke(Long groupId, List<Long> personIds) {
        // 1. 删除人员关联
        removePersonsFromGroup(groupId, personIds);
        
        // 2. 触发权限撤销（检查是否在其他组有相同设备权限）
        log.info("[removePersonsWithRevoke] 权限组 {} 移除人员 {}，触发权限撤销", groupId, personIds);
        return authDispatchService.revokePermissionGroupPersons(groupId, personIds);
    }

}
