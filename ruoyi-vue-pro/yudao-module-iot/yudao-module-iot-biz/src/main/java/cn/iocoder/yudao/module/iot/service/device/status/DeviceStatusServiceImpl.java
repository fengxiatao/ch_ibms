package cn.iocoder.yudao.module.iot.service.device.status;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.iot.controller.admin.device.vo.status.DeviceStatusPageReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.device.vo.status.DeviceStatusRespVO;
import cn.iocoder.yudao.module.iot.core.enums.IotDeviceStateEnum;
import cn.iocoder.yudao.module.iot.dal.dataobject.ibms.IbmsDeviceDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.ibms.IbmsDeviceRuntimeDO;
import cn.iocoder.yudao.module.iot.dal.mysql.ibms.IbmsDeviceMapper;
import cn.iocoder.yudao.module.iot.dal.mysql.ibms.IbmsDeviceRuntimeMapper;
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
    private IbmsDeviceMapper deviceMapper;
    @Resource
    private IbmsDeviceRuntimeMapper deviceRuntimeMapper;

    @Override
    public DeviceStatusRespVO getDeviceStatus(Long deviceId) {
        IbmsDeviceDO device = deviceMapper.selectById(deviceId);
        IbmsDeviceRuntimeDO runtime = deviceId != null ? deviceRuntimeMapper.selectById(deviceId) : null;
        
        // Requirements: 5.4 - 查询的设备不存在时返回 INACTIVE 状态
        if (device == null) {
            log.debug("[getDeviceStatus] 设备不存在，返回 INACTIVE 状态: deviceId={}", deviceId);
            return DeviceStatusRespVO.builder()
                    .deviceId(deviceId)
                    .state(IotDeviceStateEnum.INACTIVE.getState())
                    .stateName(IotDeviceStateEnum.INACTIVE.getName())
                    .build();
        }
        
        return convertToStatusVO(device, runtime);
    }

    @Override
    public List<DeviceStatusRespVO> batchGetDeviceStatus(List<Long> deviceIds) {
        if (deviceIds == null || deviceIds.isEmpty()) {
            return new ArrayList<>();
        }
        
        // 批量查询设备
        List<IbmsDeviceDO> devices = deviceMapper.selectBatchIds(deviceIds);
        Map<Long, IbmsDeviceDO> deviceMap = convertMap(devices, IbmsDeviceDO::getId);

        // 批量查询运行态（onlineTime/offlineTime/state）
        List<IbmsDeviceRuntimeDO> runtimes = deviceRuntimeMapper.selectList(
                new LambdaQueryWrapperX<IbmsDeviceRuntimeDO>()
                        .in(IbmsDeviceRuntimeDO::getDeviceId, deviceIds)
                        .select(IbmsDeviceRuntimeDO::getDeviceId, IbmsDeviceRuntimeDO::getState,
                                IbmsDeviceRuntimeDO::getOnlineTime, IbmsDeviceRuntimeDO::getOfflineTime));
        Map<Long, IbmsDeviceRuntimeDO> runtimeMap = convertMap(runtimes, IbmsDeviceRuntimeDO::getDeviceId);
        
        // 构建结果列表，保持请求顺序
        List<DeviceStatusRespVO> result = new ArrayList<>(deviceIds.size());
        for (Long deviceId : deviceIds) {
            IbmsDeviceDO device = deviceMap.get(deviceId);
            IbmsDeviceRuntimeDO runtime = runtimeMap.get(deviceId);
            if (device != null) {
                result.add(convertToStatusVO(device, runtime));
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
        int pageNo = pageReqVO.getPageNo();
        int pageSize = pageReqVO.getPageSize();
        if (pageSize <= 0) {
            pageSize = 10;
        }

        // 1) 查询设备候选（不做 state 条件过滤，state 来自 ibms_device_runtime）
        LambdaQueryWrapperX<IbmsDeviceDO> wrapper = new LambdaQueryWrapperX<IbmsDeviceDO>()
                .likeIfPresent(IbmsDeviceDO::getName, pageReqVO.getDeviceName())
                .eqIfPresent(IbmsDeviceDO::getDeviceType, pageReqVO.getDeviceType())
                .eqIfPresent(IbmsDeviceDO::getIbmsProductId, pageReqVO.getProductId())
                .orderByDesc(IbmsDeviceDO::getId);

        // 为了保证 state 过滤准确，先查足够范围再二次过滤与分页（G4 下数据规模通常可控）
        List<IbmsDeviceDO> candidates = deviceMapper.selectList(wrapper);
        if (candidates == null) {
            candidates = List.of();
        }

        Map<Long, IbmsDeviceRuntimeDO> runtimeMap = new java.util.HashMap<>();
        if (candidates.size() > 0) {
            List<Long> ids = candidates.stream().map(IbmsDeviceDO::getId).toList();
            List<IbmsDeviceRuntimeDO> runtimes = deviceRuntimeMapper.selectList(
                    new LambdaQueryWrapperX<IbmsDeviceRuntimeDO>()
                            .in(IbmsDeviceRuntimeDO::getDeviceId, ids)
                            .select(IbmsDeviceRuntimeDO::getDeviceId, IbmsDeviceRuntimeDO::getState,
                                    IbmsDeviceRuntimeDO::getOnlineTime, IbmsDeviceRuntimeDO::getOfflineTime));
            runtimeMap = convertMap(runtimes, IbmsDeviceRuntimeDO::getDeviceId);
        }

        List<DeviceStatusRespVO> all = new java.util.ArrayList<>(candidates.size());
        for (IbmsDeviceDO d : candidates) {
            IbmsDeviceRuntimeDO rt = runtimeMap.get(d.getId());
            int state = rt != null && rt.getState() != null ? rt.getState() : IotDeviceStateEnum.INACTIVE.getState();
            if (pageReqVO.getState() != null && !pageReqVO.getState().equals(state)) {
                continue;
            }
            all.add(convertToStatusVO(d, rt));
        }

        int total = all.size();
        int fromIndex = (pageNo - 1) * pageSize;
        int toIndex = Math.min(fromIndex + pageSize, total);

        List<DeviceStatusRespVO> pageList = (fromIndex >= total) ? List.of() : all.subList(fromIndex, toIndex);
        return new PageResult<>(pageList, (long) total);
    }

    /**
     * 将设备 DO 转换为状态 VO
     * 
     * @param device 设备 DO
     * @return 设备状态 VO
     */
    private DeviceStatusRespVO convertToStatusVO(IbmsDeviceDO device, IbmsDeviceRuntimeDO runtime) {
        int state = runtime != null && runtime.getState() != null
                ? runtime.getState() : IotDeviceStateEnum.INACTIVE.getState();
        IotDeviceStateEnum stateEnum = IotDeviceStateEnum.fromState(state);

        // 计算最后活跃时间戳
        Long lastSeenTimestamp = calculateLastSeenTimestamp(runtime);
        
        return DeviceStatusRespVO.builder()
                .deviceId(device.getId())
                .deviceName(device.getName())
                .state(state)
                .stateName(stateEnum != null ? stateEnum.getName() : "未知")
                .lastSeenTimestamp(lastSeenTimestamp)
                .onlineTime(runtime != null ? runtime.getOnlineTime() : null)
                .offlineTime(runtime != null ? runtime.getOfflineTime() : null)
                .deviceType(device.getDeviceType())
                .productId(device.getIbmsProductId())
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
    private Long calculateLastSeenTimestamp(IbmsDeviceRuntimeDO runtime) {
        if (runtime == null) {
            return null;
        }
        if (runtime.getOnlineTime() != null) {
            return runtime.getOnlineTime().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        }
        if (runtime.getOfflineTime() != null) {
            return runtime.getOfflineTime().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        }
        return null;
    }

}
