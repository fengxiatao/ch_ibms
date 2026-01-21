package cn.iocoder.yudao.module.iot.service.access;

import cn.iocoder.yudao.module.iot.controller.admin.access.vo.dashboard.AccessDashboardStatisticsRespVO;
import cn.iocoder.yudao.module.iot.controller.admin.access.vo.dashboard.RealTimeAccessRespVO;

/**
 * 门禁管理 Dashboard Service 接口
 *
 * @author 智能化系统
 */
public interface AccessDashboardService {

    /**
     * 获取门禁管理统计数据
     *
     * @return 统计数据
     */
    AccessDashboardStatisticsRespVO getStatistics();

    /**
     * 获取实时通行数据
     *
     * @param pageSize 每页数量
     * @return 实时通行数据
     */
    RealTimeAccessRespVO getRealTimeAccess(Integer pageSize);

    /**
     * 获取通行趋势数据
     *
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param type 类型
     * @return 趋势数据
     */
    Object getAccessTrend(String startTime, String endTime, String type);

    /**
     * 获取设备状态概览
     *
     * @return 设备状态
     */
    Object getDeviceStatusOverview();

    /**
     * 获取热力图数据
     *
     * @param date 日期
     * @param type 类型
     * @return 热力图数据
     */
    Object getAccessHeatmap(String date, String type);

    /**
     * 获取异常事件列表
     *
     * @param pageSize 每页数量
     * @param level 级别
     * @return 异常事件列表
     */
    Object getAbnormalEventList(Integer pageSize, String level);

}



















