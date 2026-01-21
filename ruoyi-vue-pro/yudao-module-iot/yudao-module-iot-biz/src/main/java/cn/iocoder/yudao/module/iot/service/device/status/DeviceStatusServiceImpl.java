package cn.iocoder.yudao.module.iot.service.device.status;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.iot.controller.admin.device.vo.status.DeviceStatusPageReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.device.vo.status.DeviceStatusRespVO;
import cn.iocoder.yudao.module.iot.core.enums.IotDeviceStateEnum;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceDO;
import cn.iocoder.yudao.module.iot.dal.mysql.device.IotDeviceMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;

/**
 * 设备状态查询 Service 实现
 * 
 * <p>Requirements: 5.1, 5.2, 5.3</p>
 *
 * @author 长辉信息科技有限公司
 */
@Service
@Slf4j
public class DeviceStatusServiceImpl implements DeviceStatusService {

    @Resource
    private IotDeviceMapper deviceMapper;

    @Override
    public DeviceStatusRespVO getDeviceStatus(Long deviceId) {
        IotDeviceDO device = deviceMapper.selectById(deviceId);
        
        // Requirements: 5.4 - 查询的设备不存在时返回 INACTIVE 状态
        if (device == null) {
            log.debug("[getDeviceStatus] 设备不存在，返回 INACTIVE 状态: deviceId={}", deviceId);
            return DeviceStatusRespVO.builder()
                    .deviceId(deviceId)
                    .state(IotDeviceStateEnum.INACTIVE.getState())
                    .stateName(IotDeviceStateEnum.INACTIVE.getName())
                    .build();
        }
        
        return convertToStatusVO(device);
    }

    @Override
    public List<DeviceStatusRespVO> batchGetDeviceStatus(List<Long> deviceIds) {
        if (deviceIds == null || deviceIds.isEmpty()) {
            return new ArrayList<>();
        }
        
        // 批量查询设备
        List<IotDeviceDO> devices = deviceMapper.selectBatchIds(deviceIds);
        Map<Long, IotDeviceDO> deviceMap = convertMap(devices, IotDeviceDO::getId);
        
        // 构建结果列表，保持请求顺序
        List<DeviceStatusRespVO> result = new ArrayList<>(deviceIds.size());
        for (Long deviceId : deviceIds) {
            IotDeviceDO device = deviceMap.get(deviceId);
            if (device != null) {
                result.add(convertToStatusVO(device));
            } else {
                // Requirements: 5.4 - 查询的设备不存在时返回 INACTIVE 状态
                result.add(DeviceStatusRespVO.builder()
                        .deviceId(deviceId)
                        .state(IotDeviceStateEnum.INACTIVE.getState())
                        .stateName(IotDeviceStateEnum.INACTIVE.getName())
                        .build());
            }
        }
        
        return result;
    }

    @Override
    public PageResult<DeviceStatusRespVO> getDeviceStatusPage(DeviceStatusPageReqVO pageReqVO) {
        // 构建查询条件
        LambdaQueryWrapperX<IotDeviceDO> wrapper = new LambdaQueryWrapperX<IotDeviceDO>()
                .likeIfPresent(IotDeviceDO::getDeviceName, pageReqVO.getDeviceName())
                .eqIfPresent(IotDeviceDO::getDeviceType, pageReqVO.getDeviceType())
                .eqIfPresent(IotDeviceDO::getState, pageReqVO.getState())
                .eqIfPresent(IotDeviceDO::getProductId, pageReqVO.getProductId())
                .orderByDesc(IotDeviceDO::getId);
        
        // 执行分页查询
        PageResult<IotDeviceDO> pageResult = deviceMapper.selectPage(pageReqVO, wrapper);
        
        // 转换为 VO
        List<DeviceStatusRespVO> voList = pageResult.getList().stream()
                .map(this::convertToStatusVO)
                .collect(Collectors.toList());
        
        return new PageResult<>(voList, pageResult.getTotal());
    }

    /**
     * 将设备 DO 转换为状态 VO
     * 
     * @param device 设备 DO
     * @return 设备状态 VO
     */
    private DeviceStatusRespVO convertToStatusVO(IotDeviceDO device) {
        IotDeviceStateEnum stateEnum = IotDeviceStateEnum.fromState(device.getState());
        
        // 计算最后活跃时间戳
        Long lastSeenTimestamp = calculateLastSeenTimestamp(device);
        
        return DeviceStatusRespVO.builder()
                .deviceId(device.getId())
                .deviceName(device.getDeviceName())
                .state(device.getState())
                .stateName(stateEnum != null ? stateEnum.getName() : "未知")
                .lastSeenTimestamp(lastSeenTimestamp)
                .onlineTime(device.getOnlineTime())
                .offlineTime(device.getOfflineTime())
                .deviceType(device.getDeviceType())
                .productId(device.getProductId())
                .build();
    }

    /**
     * 计算设备最后活跃时间戳
     * 
     * <p>优先使用 onlineTime，如果为空则使用 offlineTime</p>
     * 
     * @param device 设备 DO
     * @return 最后活跃时间戳（毫秒），如果都为空则返回 null
     */
    private Long calculateLastSeenTimestamp(IotDeviceDO device) {
        if (device.getOnlineTime() != null) {
            return device.getOnlineTime().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        }
        if (device.getOfflineTime() != null) {
            return device.getOfflineTime().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        }
        return null;
    }

}
