package cn.iocoder.yudao.module.iot.service.access;

import cn.iocoder.yudao.module.iot.controller.admin.access.vo.dashboard.AccessDashboardStatisticsRespVO;
import cn.iocoder.yudao.module.iot.controller.admin.access.vo.dashboard.RealTimeAccessRespVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.access.AccessRecordDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.access.AccessAlarmDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceDO;
import cn.iocoder.yudao.module.iot.dal.mysql.access.AccessRecordMapper;
import cn.iocoder.yudao.module.iot.dal.mysql.access.AccessAlarmMapper;
import cn.iocoder.yudao.module.iot.dal.mysql.device.IotDeviceMapper;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;

/**
 * 门禁管理 Dashboard Service 实现类
 *
 * @author 智能化系统
 */
@Service
@Slf4j
public class AccessDashboardServiceImpl implements AccessDashboardService {

    @Resource
    private AccessRecordMapper accessRecordMapper;

    @Resource
    private AccessAlarmMapper accessAlarmMapper;

    @Resource
    private IotDeviceMapper iotDeviceMapper;

    @Override
    public AccessDashboardStatisticsRespVO getStatistics() {
        LocalDate today = LocalDate.now();
        LocalDateTime startOfToday = today.atStartOfDay();
        LocalDateTime endOfToday = today.atTime(LocalTime.MAX);

        // 今日通行次数
        Long todayAccessCount = accessRecordMapper.selectCount(
                new cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX<AccessRecordDO>()
                        .between(AccessRecordDO::getAccessTime, startOfToday, endOfToday));

        // 今日告警数
        Long todayAlarmCount = accessAlarmMapper.selectCount(
                new cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX<AccessAlarmDO>()
                        .between(AccessAlarmDO::getAlarmTime, startOfToday, endOfToday));

        // 设备统计（门禁设备）
        List<IotDeviceDO> devices = iotDeviceMapper.selectList(
                new cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX<IotDeviceDO>()
                        .like(IotDeviceDO::getProductKey, "access") // 门禁设备
                        .or()
                        .like(IotDeviceDO::getProductKey, "door")); // 门禁设备

        Long totalDeviceCount = (long) devices.size();
        Long onlineDeviceCount = devices.stream()
                .filter(d -> cn.iocoder.yudao.module.iot.core.enums.IotDeviceStateEnum.isOnline(d.getState())) // 使用统一的状态判断方法
                .count();

        // 构建响应对象
        AccessDashboardStatisticsRespVO respVO = new AccessDashboardStatisticsRespVO();
        respVO.setTodayAccessCount(todayAccessCount);
        respVO.setTodayVisitorCount(0L); // TODO: 从访客表统计
        respVO.setTodayVehicleCount(0L); // TODO: 从车辆表统计
        respVO.setTodayAlarmCount(todayAlarmCount);
        respVO.setOnlineDeviceCount(onlineDeviceCount);
        respVO.setTotalDeviceCount(totalDeviceCount);
        respVO.setCurrentVisitorCount(0L); // TODO: 从访客表统计
        respVO.setOccupiedParkingSpaces(0L); // TODO: 从车辆表统计
        respVO.setTotalParkingSpaces(100L); // TODO: 从配置获取
        respVO.setAccessCountGrowth(0.0); // TODO: 计算同比
        respVO.setVisitorCountGrowth(0.0);
        respVO.setVehicleCountGrowth(0.0);

        // 设备状态分布
        AccessDashboardStatisticsRespVO.DeviceStatusDistribution deviceStatus = 
                new AccessDashboardStatisticsRespVO.DeviceStatusDistribution();
        deviceStatus.setOnline(onlineDeviceCount);
        deviceStatus.setOffline(totalDeviceCount - onlineDeviceCount);
        deviceStatus.setMaintenance(0L);
        deviceStatus.setFault(0L);
        respVO.setDeviceStatusDistribution(deviceStatus);

        // 通行类型分布（从通行记录统计）
        AccessDashboardStatisticsRespVO.AccessTypeDistribution accessType = 
                new AccessDashboardStatisticsRespVO.AccessTypeDistribution();
        // TODO: 根据实际业务逻辑统计
        accessType.setEmployee(todayAccessCount);
        accessType.setVisitor(0L);
        accessType.setVehicle(0L);
        accessType.setElevator(0L);
        respVO.setAccessTypeDistribution(accessType);

        return respVO;
    }

    @Override
    public RealTimeAccessRespVO getRealTimeAccess(Integer pageSize) {
        // 查询最近的通行记录
        List<AccessRecordDO> records = accessRecordMapper.selectList(
                new cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX<AccessRecordDO>()
                        .orderByDesc(AccessRecordDO::getAccessTime)
                        .last("LIMIT " + pageSize));

        RealTimeAccessRespVO respVO = new RealTimeAccessRespVO();
        respVO.setRecords(convertList(records, record -> {
            RealTimeAccessRespVO.RealTimeAccessItem item = new RealTimeAccessRespVO.RealTimeAccessItem();
            item.setId(record.getId());
            item.setType("access");
            item.setUserName(record.getPersonName());
            item.setDeviceName(record.getDeviceName());
//            item.setLocation(record.getLocation());
            item.setTime(record.getAccessTime());
            item.setResult(record.getOpenResult() == 1 ? "success" : "failed");
//            item.setPhoto(record.getImageUrl());
            return item;
        }));

        return respVO;
    }

    @Override
    public Object getAccessTrend(String startTime, String endTime, String type) {
        // TODO: 实现通行趋势统计
        return new java.util.ArrayList<>();
    }

    @Override
    public Object getDeviceStatusOverview() {
        // TODO: 实现设备状态概览
        return new java.util.HashMap<>();
    }

    @Override
    public Object getAccessHeatmap(String date, String type) {
        // TODO: 实现热力图数据
        return new java.util.ArrayList<>();
    }

    @Override
    public Object getAbnormalEventList(Integer pageSize, String level) {
        // TODO: 实现异常事件列表
        return new java.util.ArrayList<>();
    }

}

