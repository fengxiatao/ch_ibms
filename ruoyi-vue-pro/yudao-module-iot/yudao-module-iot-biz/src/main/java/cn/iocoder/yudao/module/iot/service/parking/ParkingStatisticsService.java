package cn.iocoder.yudao.module.iot.service.parking;

import cn.iocoder.yudao.module.iot.controller.admin.parking.vo.statistics.ParkingOverviewStatisticsRespVO;
import cn.iocoder.yudao.module.iot.controller.admin.parking.vo.statistics.ParkingPresentStatisticsRespVO;

/**
 * 停车场统计 Service 接口
 *
 * @author 芋道源码
 */
public interface ParkingStatisticsService {

    /**
     * 获取在场车辆统计
     *
     * @param lotId 停车场ID（可选，不传则统计所有停车场）
     * @return 在场车辆统计
     */
    ParkingPresentStatisticsRespVO getPresentStatistics(Long lotId);

    /**
     * 获取停车场概览统计
     *
     * @return 停车场概览统计
     */
    ParkingOverviewStatisticsRespVO getOverviewStatistics();
}
