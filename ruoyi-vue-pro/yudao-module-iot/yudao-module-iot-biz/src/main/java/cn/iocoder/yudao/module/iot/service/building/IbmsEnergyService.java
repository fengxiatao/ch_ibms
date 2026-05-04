package cn.iocoder.yudao.module.iot.service.building;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.iot.controller.admin.building.vo.energy.*;
import cn.iocoder.yudao.module.iot.dal.dataobject.building.*;

import java.time.LocalDate;
import java.util.List;

/**
 * 能耗计量 Service 接口
 *
 * @author 智慧楼宇系统
 */
public interface IbmsEnergyService {

    // ======================= 仪表管理 =======================

    /**
     * 获得能耗仪表分页
     */
    PageResult<IbmsEnergyMeterDO> getMeterPage(IbmsEnergyMeterPageReqVO pageReqVO);

    /**
     * 获得能耗仪表
     */
    IbmsEnergyMeterDO getMeter(Long id);

    /**
     * 获得仪表列表（按类型）
     */
    List<IbmsEnergyMeterDO> getMeterListByType(Integer meterType);

    /**
     * 获得全部仪表列表
     */
    List<IbmsEnergyMeterDO> getMeterList();

    /**
     * 创建能耗仪表
     */
    Long createMeter(IbmsEnergyMeterSaveReqVO createReqVO);

    /**
     * 更新能耗仪表
     */
    void updateMeter(IbmsEnergyMeterSaveReqVO updateReqVO);

    /**
     * 删除能耗仪表
     */
    void deleteMeter(Long id);

    /**
     * 将能耗仪表绑定到 IBMS 设备台账（幂等）。
     * <p>{@code ibmsDeviceId} 为 null 表示解绑。</p>
     *
     * @param meterId       仪表 ID
     * @param ibmsDeviceId  IBMS 设备台账 ID；传 null 表示解绑
     */
    void bindDevice(Long meterId, Long ibmsDeviceId);

    // ======================= 采集记录 =======================

    /**
     * 获得能耗采集记录分页
     */
    PageResult<IbmsEnergyRecordDO> getRecordPage(IbmsEnergyRecordPageReqVO pageReqVO);

    /**
     * 获得仪表最新采集记录
     */
    IbmsEnergyRecordDO getLatestRecord(Long meterId);

    // ======================= 统计分析 =======================

    /**
     * 获得能耗日统计分页
     */
    PageResult<IbmsEnergyStatisticsDailyDO> getStatisticsPage(IbmsEnergyStatisticsPageReqVO pageReqVO);

    /**
     * 获得指定日期范围的能耗统计
     */
    List<IbmsEnergyStatisticsDailyDO> getStatisticsByDateRange(LocalDate startDate, LocalDate endDate);

    /**
     * 获得仪表指定日期范围的能耗统计
     */
    List<IbmsEnergyStatisticsDailyDO> getMeterStatisticsByDateRange(Long meterId, LocalDate startDate, LocalDate endDate);

    /**
     * 按类型获得指定日期范围的能耗统计
     */
    List<IbmsEnergyStatisticsDailyDO> getStatisticsByTypeAndDateRange(Integer meterType, LocalDate startDate, LocalDate endDate);

    // ======================= 告警管理 =======================

    /**
     * 获得能耗告警分页
     */
    PageResult<IbmsEnergyAlarmDO> getAlarmPage(IbmsEnergyAlarmPageReqVO pageReqVO);

    /**
     * 处理告警（确认）
     */
    void handleAlarm(Long id, String handler, String handleRemark);

    /**
     * 忽略告警
     */
    void ignoreAlarm(Long id, String handler, String handleRemark);

    /**
     * 获得告警详情
     */
    IbmsEnergyAlarmDO getAlarm(Long id);

    /**
     * 获得今日告警数量
     */
    long getTodayAlarmCount();

    /**
     * 获得未处理告警数量
     */
    long getUnhandledAlarmCount();

    // ======================= 费率设置 =======================

    /**
     * 获得费率设置分页
     */
    PageResult<IbmsEnergyRateDO> getRatePage(IbmsEnergyRatePageReqVO pageReqVO);

    /**
     * 获得费率设置详情
     */
    IbmsEnergyRateDO getRate(Long id);

    /**
     * 获得费率列表（按能源类型）
     */
    List<IbmsEnergyRateDO> getRateListByEnergyType(Integer energyType);

    /**
     * 创建费率设置
     */
    Long createRate(IbmsEnergyRateSaveReqVO createReqVO);

    /**
     * 更新费率设置
     */
    void updateRate(IbmsEnergyRateSaveReqVO updateReqVO);

    /**
     * 删除费率设置
     */
    void deleteRate(Long id);

    // ======================= 人工抄表 =======================

    /**
     * 获得人工抄表记录分页
     */
    PageResult<IbmsEnergyManualReadingDO> getManualReadingPage(IbmsEnergyManualReadingPageReqVO pageReqVO);

    /**
     * 获得今日人工抄表记录
     */
    List<IbmsEnergyManualReadingDO> getTodayManualReadings();

    /**
     * 获得仪表最新人工抄表记录
     */
    IbmsEnergyManualReadingDO getLatestManualReading(Long meterId);

    /**
     * 创建人工抄表记录
     */
    Long createManualReading(IbmsEnergyManualReadingSaveReqVO createReqVO);

    /**
     * 复核人工抄表记录
     */
    void reviewManualReading(Long id, String reviewer);

    /**
     * 作废人工抄表记录
     */
    void voidManualReading(Long id);

    // ======================= 总览 =======================

    /**
     * 获得能耗总览数据
     */
    IbmsEnergyOverviewVO getOverview();

    /**
     * 按日期范围获得能耗总览数据
     *
     * @param startDate 起始日期（含）
     * @param endDate   结束日期（含）
     */
    IbmsEnergyOverviewVO getOverviewByRange(LocalDate startDate, LocalDate endDate);

}
