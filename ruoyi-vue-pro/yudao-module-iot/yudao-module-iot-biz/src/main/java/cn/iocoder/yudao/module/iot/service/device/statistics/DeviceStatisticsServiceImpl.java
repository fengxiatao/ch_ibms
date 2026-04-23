package cn.iocoder.yudao.module.iot.service.device.statistics;

import cn.iocoder.yudao.module.iot.controller.admin.device.vo.statistics.DeviceOnlineStatisticsRespVO;
import cn.iocoder.yudao.module.iot.controller.admin.device.vo.statistics.DeviceTrendReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.device.vo.statistics.DeviceTrendRespVO;
import cn.iocoder.yudao.module.iot.controller.admin.device.vo.statistics.DeviceTypeStatisticsRespVO;
import cn.iocoder.yudao.module.iot.core.enums.IotDeviceStateEnum;
import cn.iocoder.yudao.module.iot.dal.dataobject.ibms.IbmsDeviceDO;
import cn.iocoder.yudao.module.iot.dal.mysql.ibms.IbmsDeviceMapper;
import cn.iocoder.yudao.module.iot.dal.mysql.ibms.IbmsDeviceRuntimeMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 设备统计 Service 实现
 * 
 * <p>Requirements: 3.1, 3.2, 3.3, 3.4</p>
 *
 * @author 长辉信息科技有限公司
 */
@Service
@Slf4j
public class DeviceStatisticsServiceImpl implements DeviceStatisticsService {

    @Resource
    private IbmsDeviceMapper deviceMapper;
    @Resource
    private IbmsDeviceRuntimeMapper deviceRuntimeMapper;

    @Override
    public DeviceOnlineStatisticsRespVO getOnlineStatistics() {
        // 单台账：state 来自 ibms_device_runtime（缺失时视为 INACTIVE）
        List<IbmsDeviceDO> devices = deviceMapper.selectList();
        if (devices == null) {
            devices = List.of();
        }

        List<Long> ids = devices.stream().map(IbmsDeviceDO::getId).toList();
        Map<Long, Integer> stateMap = deviceRuntimeMapper.selectStateMapByDeviceIds(ids);

        int onlineCount = 0;
        int offlineCount = 0;
        int inactiveCount = 0;
        for (IbmsDeviceDO d : devices) {
            Integer state = stateMap.getOrDefault(d.getId(), IotDeviceStateEnum.INACTIVE.getState());
            if (IotDeviceStateEnum.ONLINE.getState().equals(state)) {
                onlineCount++;
            } else if (IotDeviceStateEnum.OFFLINE.getState().equals(state)) {
                offlineCount++;
            } else {
                inactiveCount++;
            }
        }

        int totalCount = onlineCount + offlineCount + inactiveCount;
        
        // 计算离线率
        BigDecimal offlineRate = calculateOfflineRate(offlineCount, totalCount);
        
        return DeviceOnlineStatisticsRespVO.builder()
                .totalCount(totalCount)
                .onlineCount(onlineCount)
                .offlineCount(offlineCount)
                .inactiveCount(inactiveCount)
                .offlineRate(offlineRate)
                .build();
    }

    @Override
    public List<DeviceTypeStatisticsRespVO> getStatisticsByType() {
        // 单台账：以 ibms_device.deviceType 分组，state 来自 ibms_device_runtime（缺失时视为 INACTIVE）
        List<IbmsDeviceDO> devices = deviceMapper.selectList();
        if (devices == null) {
            devices = List.of();
        }
        List<Long> ids = devices.stream().map(IbmsDeviceDO::getId).toList();
        Map<Long, Integer> stateMap = deviceRuntimeMapper.selectStateMapByDeviceIds(ids);

        Map<Integer, Map<Integer, Integer>> typeStateCountMap = new java.util.HashMap<>();
        for (IbmsDeviceDO d : devices) {
            Integer deviceType = d.getDeviceType();
            if (deviceType == null) {
                deviceType = -1;
            }
            Integer state = stateMap.getOrDefault(d.getId(), IotDeviceStateEnum.INACTIVE.getState());
            typeStateCountMap
                    .computeIfAbsent(deviceType, k -> new java.util.HashMap<>())
                    .merge(state, 1, Integer::sum);
        }

        if (typeStateCountMap.isEmpty()) {
            return Collections.emptyList();
        }

        List<DeviceTypeStatisticsRespVO> result = new ArrayList<>();
        for (Map.Entry<Integer, Map<Integer, Integer>> entry : typeStateCountMap.entrySet()) {
            Integer deviceType = entry.getKey();
            Map<Integer, Integer> stateCountMap = entry.getValue();

            int onlineCount = stateCountMap.getOrDefault(IotDeviceStateEnum.ONLINE.getState(), 0);
            int offlineCount = stateCountMap.getOrDefault(IotDeviceStateEnum.OFFLINE.getState(), 0);
            int inactiveCount = stateCountMap.getOrDefault(IotDeviceStateEnum.INACTIVE.getState(), 0);
            int totalCount = onlineCount + offlineCount + inactiveCount;
            BigDecimal offlineRate = calculateOfflineRate(offlineCount, totalCount);

            result.add(DeviceTypeStatisticsRespVO.builder()
                    .deviceType(deviceType)
                    .deviceTypeName(getDeviceTypeName(deviceType))
                    .totalCount(totalCount)
                    .onlineCount(onlineCount)
                    .offlineCount(offlineCount)
                    .inactiveCount(inactiveCount)
                    .offlineRate(offlineRate)
                    .build());
        }

        return result;
    }

    @Override
    public BigDecimal getOfflineRate() {
        DeviceOnlineStatisticsRespVO statistics = getOnlineStatistics();
        return statistics.getOfflineRate();
    }

    @Override
    public List<DeviceTrendRespVO> getTrend(DeviceTrendReqVO reqVO) {
        // 当前实现：返回当前时刻的快照数据
        // 因为系统未记录历史状态变更，无法提供真正的历史趋势
        // 如需真正的历史趋势，需要额外的状态历史记录表
        
        DeviceOnlineStatisticsRespVO currentStats = getOnlineStatistics();
        
        // 返回单个数据点（当前时刻）
        DeviceTrendRespVO trendPoint = DeviceTrendRespVO.builder()
                .timestamp(LocalDateTime.now())
                .onlineCount(currentStats.getOnlineCount())
                .offlineCount(currentStats.getOfflineCount())
                .totalCount(currentStats.getTotalCount())
                .build();
        
        return Collections.singletonList(trendPoint);
    }

    /**
     * 从状态计数映射中获取指定状态的数量
     *
     * @param stateCountMap 状态计数映射
     * @param state 状态值
     * @return 数量，如果不存在则返回 0
     */
    /**
     * 计算离线率
     * 
     * <p>Property 6: Offline Rate Calculation - 离线率 = (离线数 / 总数) × 100%</p>
     *
     * @param offlineCount 离线设备数量
     * @param totalCount 设备总数
     * @return 离线率（百分比），保留两位小数
     */
    private BigDecimal calculateOfflineRate(int offlineCount, int totalCount) {
        if (totalCount == 0) {
            return BigDecimal.ZERO;
        }
        return BigDecimal.valueOf(offlineCount)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(totalCount), 2, RoundingMode.HALF_UP);
    }

    /**
     * 获取设备类型名称
     *
     * @param deviceType 设备类型
     * @return 设备类型名称
     */
    private String getDeviceTypeName(Integer deviceType) {
        if (deviceType == null) {
            return "未知";
        }
        // 根据设备类型返回名称
        // 这里可以根据实际的设备类型枚举进行映射
        switch (deviceType) {
            case 0: return "直连设备";
            case 1: return "网关设备";
            case 2: return "网关子设备";
            default: return "未知类型(" + deviceType + ")";
        }
    }

}
