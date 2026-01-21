package cn.iocoder.yudao.module.iot.service.device.statistics;

import cn.iocoder.yudao.module.iot.controller.admin.device.vo.statistics.DeviceOnlineStatisticsRespVO;
import cn.iocoder.yudao.module.iot.controller.admin.device.vo.statistics.DeviceTrendReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.device.vo.statistics.DeviceTrendRespVO;
import cn.iocoder.yudao.module.iot.controller.admin.device.vo.statistics.DeviceTypeStatisticsRespVO;

import java.math.BigDecimal;
import java.util.List;

/**
 * 设备统计 Service 接口
 * 
 * <p>Requirements: 3.1, 3.2, 3.3, 3.4</p>
 *
 * @author 长辉信息科技有限公司
 */
public interface DeviceStatisticsService {

    /**
     * 获取设备在线统计
     * 
     * <p>Requirements: 3.1, 3.2, 3.3</p>
     *
     * @return 设备在线统计数据
     */
    DeviceOnlineStatisticsRespVO getOnlineStatistics();

    /**
     * 按设备类型获取统计数据
     * 
     * <p>Requirements: 3.2</p>
     *
     * @return 按设备类型分组的统计数据列表
     */
    List<DeviceTypeStatisticsRespVO> getStatisticsByType();

    /**
     * 获取设备离线率
     * 
     * <p>Requirements: 3.3</p>
     *
     * @return 离线率（百分比）
     */
    BigDecimal getOfflineRate();

    /**
     * 获取设备历史趋势数据
     * 
     * <p>Requirements: 3.4</p>
     * <p>注意：当前实现返回当前时刻的快照数据，因为系统未记录历史状态变更。
     * 如需真正的历史趋势，需要额外的状态历史记录表。</p>
     *
     * @param reqVO 查询请求参数
     * @return 趋势数据列表
     */
    List<DeviceTrendRespVO> getTrend(DeviceTrendReqVO reqVO);

}
