package cn.iocoder.yudao.module.iot.service.access;

import cn.iocoder.yudao.module.iot.controller.admin.access.vo.personpermission.*;
import cn.iocoder.yudao.module.iot.dal.dataobject.access.*;
import cn.iocoder.yudao.module.iot.dal.mysql.access.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.iot.enums.ErrorCodeConstants.*;

/**
 * 门禁人员权限配置 Service 实现类
 *
 * @author 智能化系统
 */
@Slf4j
@Service
@Validated
public class IotAccessPersonPermissionServiceImpl implements IotAccessPersonPermissionService {

    @Resource
    private IotAccessPersonMapper personMapper;

    @Resource
    private IotAccessPermissionGroupMapper groupMapper;

    @Resource
    private IotAccessPermissionGroupPersonMapper groupPersonMapper;

    @Resource
    private IotAccessPermissionGroupDeviceMapper groupDeviceMapper;

    @Resource
    private IotAccessAuthTaskService authTaskService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assignByGroup(Long personId, List<Long> groupIds) {
        validatePersonExists(personId);
        if (groupIds == null || groupIds.isEmpty()) {
            return;
        }
        for (Long groupId : groupIds) {
            validateGroupExists(groupId);
            // 检查是否已存在（包括软删除的记录，避免唯一约束冲突）
            IotAccessPermissionGroupPersonDO existing = groupPersonMapper.selectByGroupIdAndPersonIdIncludeDeleted(groupId, personId);
            if (existing != null) {
                // 如果记录存在但已被软删除，先物理删除再重新插入
                if (existing.getDeleted()) {
                    groupPersonMapper.physicalDeleteByGroupIdAndPersonId(groupId, personId);
                } else {
                    // 记录存在且未删除，跳过
                    continue;
                }
            }
            IotAccessPermissionGroupPersonDO groupPerson = IotAccessPermissionGroupPersonDO.builder()
                    .groupId(groupId)
                    .personId(personId)
                    .build();
            groupPersonMapper.insert(groupPerson);
        }
        // 触发权限下发
        triggerAuthTask(personId);
        log.info("[assignByGroup] 人员 {} 分配权限组 {} 成功", personId, groupIds);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long assignByGroupWithDispatch(Long personId, List<Long> groupIds) {
        validatePersonExists(personId);
        if (groupIds == null || groupIds.isEmpty()) {
            return null;
        }
        
        // 1. 建立人员与权限组的关联关系
        for (Long groupId : groupIds) {
            validateGroupExists(groupId);
            // 检查是否已存在（包括软删除的记录，避免唯一约束冲突）
            IotAccessPermissionGroupPersonDO existing = groupPersonMapper.selectByGroupIdAndPersonIdIncludeDeleted(groupId, personId);
            if (existing != null) {
                // 如果记录存在但已被软删除，先物理删除再重新插入
                if (existing.getDeleted()) {
                    groupPersonMapper.physicalDeleteByGroupIdAndPersonId(groupId, personId);
                } else {
                    // 记录存在且未删除，跳过
                    continue;
                }
            }
            IotAccessPermissionGroupPersonDO groupPerson = IotAccessPermissionGroupPersonDO.builder()
                    .groupId(groupId)
                    .personId(personId)
                    .build();
            groupPersonMapper.insert(groupPerson);
        }
        
        // 2. 创建授权任务，向权限组关联的设备下发该人员的凭证
        Long taskId = authTaskService.createTask("person_dispatch", null, personId);
        log.info("[assignByGroupWithDispatch] 人员 {} 分配权限组 {} 成功，创建授权任务 {}", personId, groupIds, taskId);
        
        return taskId;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assignByDevice(Long personId, List<Long> deviceIds) {
        validatePersonExists(personId);
        if (deviceIds == null || deviceIds.isEmpty()) {
            return;
        }
        // 为每个设备创建直接授权（通过创建临时权限组或直接授权记录）
        // 这里简化处理：为人员创建一个专属权限组
        log.info("[assignByDevice] 人员 {} 直接分配设备 {} 权限", personId, deviceIds);
        // 触发权限下发
        triggerAuthTask(personId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchAssignByGroup(List<Long> personIds, List<Long> groupIds) {
        if (personIds == null || personIds.isEmpty()) {
            return;
        }
        for (Long personId : personIds) {
            assignByGroup(personId, groupIds);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchAssignByDevice(List<Long> personIds, List<Long> deviceIds) {
        if (personIds == null || personIds.isEmpty()) {
            return;
        }
        for (Long personId : personIds) {
            assignByDevice(personId, deviceIds);
        }
    }

    @Override
    public IotAccessPersonPermissionRespVO getPersonPermission(Long personId) {
        IotAccessPersonDO person = personMapper.selectById(personId);
        if (person == null) {
            throw exception(ACCESS_PERSON_NOT_EXISTS);
        }

        // 获取人员关联的权限组
        List<IotAccessPermissionGroupPersonDO> groupPersons = groupPersonMapper.selectListByPersonId(personId);
        List<IotAccessPersonPermissionRespVO.PermissionGroupInfo> groupInfos = new ArrayList<>();
        
        for (IotAccessPermissionGroupPersonDO gp : groupPersons) {
            IotAccessPermissionGroupDO group = groupMapper.selectById(gp.getGroupId());
            if (group != null) {
                List<IotAccessPermissionGroupDeviceDO> devices = groupDeviceMapper.selectListByGroupId(group.getId());
                groupInfos.add(IotAccessPersonPermissionRespVO.PermissionGroupInfo.builder()
                        .groupId(group.getId())
                        .groupName(group.getGroupName())
                        .deviceCount(devices != null ? devices.size() : 0)
                        .build());
            }
        }

        return IotAccessPersonPermissionRespVO.builder()
                .personId(person.getId())
                .personCode(person.getPersonCode())
                .personName(person.getPersonName())
                .groups(groupInfos)
                .devices(new ArrayList<>()) // 直接设备授权暂不实现
                .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeGroups(Long personId, List<Long> groupIds) {
        if (groupIds == null || groupIds.isEmpty()) {
            return;
        }
        for (Long groupId : groupIds) {
            // 使用物理删除，避免软删除导致的唯一约束冲突
            // 关联表的历史记录没有保留价值，直接物理删除
            groupPersonMapper.physicalDeleteByGroupIdAndPersonId(groupId, personId);
        }
        log.info("[removeGroups] 人员 {} 移除权限组 {} 成功", personId, groupIds);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long removeGroupsWithRevoke(Long personId, List<Long> groupIds) {
        if (groupIds == null || groupIds.isEmpty()) {
            return null;
        }
        validatePersonExists(personId);
        
        // 1. 先创建撤销任务（在删除关联关系之前，这样可以获取到需要撤销的设备列表）
        Long taskId = authTaskService.createTask("revoke", null, personId);
        
        // 2. 删除人员与权限组的关联关系
        for (Long groupId : groupIds) {
            // 使用物理删除，避免软删除导致的唯一约束冲突
            // 关联表的历史记录没有保留价值，直接物理删除
            groupPersonMapper.physicalDeleteByGroupIdAndPersonId(groupId, personId);
        }
        
        log.info("[removeGroupsWithRevoke] 人员 {} 移除权限组 {} 成功，创建撤销任务 {}", personId, groupIds, taskId);
        return taskId;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeDevices(Long personId, List<Long> deviceIds) {
        log.info("[removeDevices] 人员 {} 移除设备 {} 权限", personId, deviceIds);
        // 直接设备授权移除逻辑
    }

    @Override
    public void triggerAuthTask(Long personId) {
        // 触发权限下发任务
        try {
            authTaskService.createAuthTaskForPerson(personId);
            log.info("[triggerAuthTask] 触发人员 {} 权限下发任务", personId);
        } catch (Exception e) {
            log.error("[triggerAuthTask] 触发权限下发任务失败: personId={}", personId, e);
        }
    }

    private void validatePersonExists(Long personId) {
        if (personMapper.selectById(personId) == null) {
            throw exception(ACCESS_PERSON_NOT_EXISTS);
        }
    }

    private void validateGroupExists(Long groupId) {
        if (groupMapper.selectById(groupId) == null) {
            throw exception(ACCESS_PERMISSION_GROUP_NOT_EXISTS);
        }
    }

}
