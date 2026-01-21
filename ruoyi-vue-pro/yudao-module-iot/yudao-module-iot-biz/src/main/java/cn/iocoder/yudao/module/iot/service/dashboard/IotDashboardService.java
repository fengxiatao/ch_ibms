package cn.iocoder.yudao.module.iot.service.dashboard;

import cn.iocoder.yudao.module.iot.controller.admin.dashboard.vo.AlertStatisticsRespVO;
import cn.iocoder.yudao.module.iot.controller.admin.dashboard.vo.DeviceStatisticsRespVO;
import cn.iocoder.yudao.module.iot.controller.admin.dashboard.vo.RealTimeMonitorRespVO;

/**
 * IoT 数据大屏 Service 接口
 *
 * @author 长辉信息科技有限公司
 */
public interface IotDashboardService {

    /**
     * 获取设备统计数据
     *
     * @return 设备统计数据
     */
    DeviceStatisticsRespVO getDeviceStatistics();

    /**
     * 获取告警统计数据
     *
     * @return 告警统计数据
     */
    AlertStatisticsRespVO getAlertStatistics();

    /**
     * 获取实时监控数据
     *
     * @return 实时监控数据
     */
    RealTimeMonitorRespVO getRealTimeMonitor();
}












