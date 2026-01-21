package cn.iocoder.yudao.module.iot.service.access;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.iot.controller.admin.access.vo.doorgroup.*;
import cn.iocoder.yudao.module.iot.dal.dataobject.access.DoorGroupDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.access.DoorGroupDeviceDO;
import cn.iocoder.yudao.module.iot.dal.mysql.access.DoorGroupDeviceMapper;
import cn.iocoder.yudao.module.iot.dal.mysql.access.DoorGroupMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import jakarta.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.iot.enums.ErrorCodeConstants.*;

/**
 * 门组 Service 实现类
 *
 * @author 智能化系统
 */
@Service
@Validated
@Slf4j
public class DoorGroupServiceImpl implements DoorGroupService {

    @Resource
    private DoorGroupMapper doorGroupMapper;
    
    @Resource
    private DoorGroupDeviceMapper doorGroupDeviceMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createDoorGroup(DoorGroupCreateReqVO createReqVO) {
        // 插入门组
        DoorGroupDO doorGroup = BeanUtils.toBean(createReqVO, DoorGroupDO.class);
        doorGroupMapper.insert(doorGroup);

        // 插入门组设备关联
        saveDoorGroupDevices(doorGroup.getId(), createReqVO.getDeviceIds());

        return doorGroup.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateDoorGroup(DoorGroupUpdateReqVO updateReqVO) {
        // 校验存在
        validateDoorGroupExists(updateReqVO.getId());

        // 更新门组
        DoorGroupDO updateObj = BeanUtils.toBean(updateReqVO, DoorGroupDO.class);
        doorGroupMapper.updateById(updateObj);

        // 更新门组设备关联
        doorGroupDeviceMapper.deleteByGroupId(updateReqVO.getId());
        saveDoorGroupDevices(updateReqVO.getId(), updateReqVO.getDeviceIds());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteDoorGroup(Long id) {
        // 校验存在
        validateDoorGroupExists(id);

        // TODO: 校验是否正在被使用

        // 删除门组
        doorGroupMapper.deleteById(id);

        // 删除门组设备关联
        doorGroupDeviceMapper.deleteByGroupId(id);
    }

    @Override
    public DoorGroupRespVO getDoorGroup(Long id) {
        DoorGroupDO doorGroup = doorGroupMapper.selectById(id);
        if (doorGroup == null) {
            return null;
        }

        DoorGroupRespVO respVO = BeanUtils.toBean(doorGroup, DoorGroupRespVO.class);

        // 填充设备ID列表
        List<DoorGroupDeviceDO> devices = doorGroupDeviceMapper.selectListByGroupId(id);
        respVO.setDeviceIds(devices.stream()
                .map(DoorGroupDeviceDO::getDeviceId)
                .collect(Collectors.toList()));

        return respVO;
    }

    @Override
    public PageResult<DoorGroupRespVO> getDoorGroupPage(DoorGroupPageReqVO pageReqVO) {
        PageResult<DoorGroupDO> pageResult = doorGroupMapper.selectPage(pageReqVO);
        return BeanUtils.toBean(pageResult, DoorGroupRespVO.class);
    }

    @Override
    public List<DoorGroupRespVO> getDoorGroupList() {
        List<DoorGroupDO> list = doorGroupMapper.selectList();
        return BeanUtils.toBean(list, DoorGroupRespVO.class);
    }

    // ==================== 私有方法 ====================

    private void validateDoorGroupExists(Long id) {
        if (doorGroupMapper.selectById(id) == null) {
            throw exception(DOOR_GROUP_NOT_EXISTS);
        }
    }

    private void saveDoorGroupDevices(Long groupId, List<Long> deviceIds) {
        if (CollUtil.isEmpty(deviceIds)) {
            return;
        }

        List<DoorGroupDeviceDO> devices = deviceIds.stream()
                .map(deviceId -> DoorGroupDeviceDO.builder()
                        .groupId(groupId)
                        .deviceId(deviceId)
                        .createTime(LocalDateTime.now())
                        .build())
                .collect(Collectors.toList());

        devices.forEach(doorGroupDeviceMapper::insert);
    }

}


























