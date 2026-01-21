package cn.iocoder.yudao.module.iot.service.access;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.iot.controller.admin.access.vo.doorpost.*;
import cn.iocoder.yudao.module.iot.dal.dataobject.access.DoorPostDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.access.DoorPostDeviceDO;
import cn.iocoder.yudao.module.iot.dal.mysql.access.DoorPostDeviceMapper;
import cn.iocoder.yudao.module.iot.dal.mysql.access.DoorPostMapper;
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
 * 门岗 Service 实现类
 *
 * @author 智能化系统
 */
@Service
@Validated
@Slf4j
public class DoorPostServiceImpl implements DoorPostService {

    @Resource
    private DoorPostMapper doorPostMapper;
    
    @Resource
    private DoorPostDeviceMapper doorPostDeviceMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createDoorPost(DoorPostCreateReqVO createReqVO) {
        // 插入门岗
        DoorPostDO doorPost = BeanUtils.toBean(createReqVO, DoorPostDO.class);
        doorPostMapper.insert(doorPost);

        // 插入门岗设备关联
        saveDoorPostDevices(doorPost.getId(), createReqVO.getDevices());

        return doorPost.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateDoorPost(DoorPostUpdateReqVO updateReqVO) {
        // 校验存在
        validateDoorPostExists(updateReqVO.getId());

        // 更新门岗
        DoorPostDO updateObj = BeanUtils.toBean(updateReqVO, DoorPostDO.class);
        doorPostMapper.updateById(updateObj);

        // 更新门岗设备关联
        doorPostDeviceMapper.deleteByPostId(updateReqVO.getId());
        saveDoorPostDevices(updateReqVO.getId(), updateReqVO.getDevices());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteDoorPost(Long id) {
        // 校验存在
        validateDoorPostExists(id);

        // 删除门岗
        doorPostMapper.deleteById(id);

        // 删除门岗设备关联
        doorPostDeviceMapper.deleteByPostId(id);
    }

    @Override
    public DoorPostRespVO getDoorPost(Long id) {
        DoorPostDO doorPost = doorPostMapper.selectById(id);
        if (doorPost == null) {
            return null;
        }

        DoorPostRespVO respVO = BeanUtils.toBean(doorPost, DoorPostRespVO.class);

        // 填充设备列表
        List<DoorPostDeviceDO> devices = doorPostDeviceMapper.selectListByPostId(id);
        respVO.setDevices(devices.stream()
                .map(device -> {
                    DoorPostRespVO.DoorPostDeviceRespVO deviceResp = new DoorPostRespVO.DoorPostDeviceRespVO();
                    deviceResp.setDeviceId(device.getDeviceId());
                    deviceResp.setDeviceType(device.getDeviceType());
                    // TODO: 填充 deviceName
                    return deviceResp;
                })
                .collect(Collectors.toList()));

        return respVO;
    }

    @Override
    public PageResult<DoorPostRespVO> getDoorPostPage(DoorPostPageReqVO pageReqVO) {
        PageResult<DoorPostDO> pageResult = doorPostMapper.selectPage(pageReqVO);
        return BeanUtils.toBean(pageResult, DoorPostRespVO.class);
    }

    @Override
    public List<DoorPostRespVO> getDoorPostList() {
        List<DoorPostDO> list = doorPostMapper.selectList();
        return BeanUtils.toBean(list, DoorPostRespVO.class);
    }

    // ==================== 私有方法 ====================

    private void validateDoorPostExists(Long id) {
        if (doorPostMapper.selectById(id) == null) {
            throw exception(DOOR_POST_NOT_EXISTS);
        }
    }

    private void saveDoorPostDevices(Long postId, List<DoorPostCreateReqVO.DoorPostDeviceVO> devices) {
        if (CollUtil.isEmpty(devices)) {
            return;
        }

        List<DoorPostDeviceDO> deviceDOs = devices.stream()
                .map(device -> DoorPostDeviceDO.builder()
                        .postId(postId)
                        .deviceId(device.getDeviceId())
                        .deviceType(device.getDeviceType())
                        .createTime(LocalDateTime.now())
                        .build())
                .collect(Collectors.toList());

        deviceDOs.forEach(doorPostDeviceMapper::insert);
    }

}


























