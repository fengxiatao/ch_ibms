package cn.iocoder.yudao.module.iot.service.building;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.iot.controller.admin.building.vo.energy.*;
import cn.iocoder.yudao.module.iot.dal.dataobject.building.*;
import cn.iocoder.yudao.module.iot.dal.mysql.building.*;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.iot.enums.ErrorCodeConstants.*;

/**
 * 能耗计量 Service 实现类
 *
 * @author 智慧楼宇系统
 */
@Service
@Validated
public class IbmsEnergyServiceImpl implements IbmsEnergyService {

    @Resource
    private IbmsEnergyMeterMapper meterMapper;

    @Resource
    private IbmsEnergyRecordMapper recordMapper;

    @Resource
    private IbmsEnergyStatisticsDailyMapper statisticsMapper;

    @Resource
    private IbmsEnergyAlarmMapper alarmMapper;

    @Resource
    private IbmsEnergyRateMapper rateMapper;

    @Resource
    private IbmsEnergyManualReadingMapper manualReadingMapper;

    // ======================= 仪表管理 =======================

    @Override
    public PageResult<IbmsEnergyMeterDO> getMeterPage(IbmsEnergyMeterPageReqVO pageReqVO) {
        return meterMapper.selectPage(pageReqVO);
    }

    @Override
    public IbmsEnergyMeterDO getMeter(Long id) {
        return meterMapper.selectById(id);
    }

    @Override
    public List<IbmsEnergyMeterDO> getMeterListByType(Integer meterType) {
        return meterMapper.selectListByType(meterType);
    }

    @Override
    public List<IbmsEnergyMeterDO> getMeterList() {
        return meterMapper.selectList();
    }

    @Override
    public Long createMeter(IbmsEnergyMeterSaveReqVO createReqVO) {
        IbmsEnergyMeterDO meter = BeanUtils.toBean(createReqVO, IbmsEnergyMeterDO.class);
        meterMapper.insert(meter);
        return meter.getId();
    }

    @Override
    public void updateMeter(IbmsEnergyMeterSaveReqVO updateReqVO) {
        // 校验存在
        validateMeterExists(updateReqVO.getId());
        // 更新
        IbmsEnergyMeterDO updateObj = BeanUtils.toBean(updateReqVO, IbmsEnergyMeterDO.class);
        meterMapper.updateById(updateObj);
    }

    @Override
    public void deleteMeter(Long id) {
        // 校验存在
        validateMeterExists(id);
        // 删除
        meterMapper.deleteById(id);
    }

    private void validateMeterExists(Long id) {
        if (meterMapper.selectById(id) == null) {
            throw exception(ENERGY_METER_NOT_EXISTS);
        }
    }

    // ======================= 采集记录 =======================

    @Override
    public PageResult<IbmsEnergyRecordDO> getRecordPage(IbmsEnergyRecordPageReqVO pageReqVO) {
        return recordMapper.selectPage(pageReqVO);
    }

    @Override
    public IbmsEnergyRecordDO getLatestRecord(Long meterId) {
        return recordMapper.selectLatestByMeterId(meterId);
    }

    // ======================= 统计分析 =======================

    @Override
    public PageResult<IbmsEnergyStatisticsDailyDO> getStatisticsPage(IbmsEnergyStatisticsPageReqVO pageReqVO) {
        return statisticsMapper.selectPage(pageReqVO);
    }

    @Override
    public List<IbmsEnergyStatisticsDailyDO> getStatisticsByDateRange(LocalDate startDate, LocalDate endDate) {
        return statisticsMapper.selectListByDateRange(startDate, endDate);
    }

    @Override
    public List<IbmsEnergyStatisticsDailyDO> getMeterStatisticsByDateRange(Long meterId, LocalDate startDate, LocalDate endDate) {
        return statisticsMapper.selectListByMeterIdAndDateRange(meterId, startDate, endDate);
    }

    @Override
    public List<IbmsEnergyStatisticsDailyDO> getStatisticsByTypeAndDateRange(Integer meterType, LocalDate startDate, LocalDate endDate) {
        return statisticsMapper.selectListByTypeAndDateRange(meterType, startDate, endDate);
    }

    // ======================= 告警管理 =======================

    @Override
    public PageResult<IbmsEnergyAlarmDO> getAlarmPage(IbmsEnergyAlarmPageReqVO pageReqVO) {
        return alarmMapper.selectPage(pageReqVO);
    }

    @Override
    public void handleAlarm(Long id, String handler, String handleRemark) {
        IbmsEnergyAlarmDO alarm = alarmMapper.selectById(id);
        if (alarm == null) {
            throw exception(ENERGY_ALARM_NOT_EXISTS);
        }

        IbmsEnergyAlarmDO updateObj = new IbmsEnergyAlarmDO();
        updateObj.setId(id);
        updateObj.setStatus(1); // 已确认
        updateObj.setHandler(handler);
        updateObj.setHandleRemark(handleRemark);
        updateObj.setHandleTime(LocalDateTime.now());
        alarmMapper.updateById(updateObj);
    }

    @Override
    public void ignoreAlarm(Long id, String handler, String handleRemark) {
        IbmsEnergyAlarmDO alarm = alarmMapper.selectById(id);
        if (alarm == null) {
            throw exception(ENERGY_ALARM_NOT_EXISTS);
        }

        IbmsEnergyAlarmDO updateObj = new IbmsEnergyAlarmDO();
        updateObj.setId(id);
        updateObj.setStatus(2); // 已忽略
        updateObj.setHandler(handler);
        updateObj.setHandleRemark(handleRemark);
        updateObj.setHandleTime(LocalDateTime.now());
        alarmMapper.updateById(updateObj);
    }

    @Override
    public IbmsEnergyAlarmDO getAlarm(Long id) {
        return alarmMapper.selectById(id);
    }

    @Override
    public long getTodayAlarmCount() {
        return alarmMapper.selectCountByToday();
    }

    @Override
    public long getUnhandledAlarmCount() {
        return alarmMapper.selectCountByStatus(0);
    }

    // ======================= 费率设置 =======================

    @Override
    public PageResult<IbmsEnergyRateDO> getRatePage(IbmsEnergyRatePageReqVO pageReqVO) {
        return rateMapper.selectPage(pageReqVO);
    }

    @Override
    public IbmsEnergyRateDO getRate(Long id) {
        return rateMapper.selectById(id);
    }

    @Override
    public List<IbmsEnergyRateDO> getRateListByEnergyType(Integer energyType) {
        return rateMapper.selectListByEnergyType(energyType);
    }

    @Override
    public Long createRate(IbmsEnergyRateSaveReqVO createReqVO) {
        IbmsEnergyRateDO rate = BeanUtils.toBean(createReqVO, IbmsEnergyRateDO.class);
        rateMapper.insert(rate);
        return rate.getId();
    }

    @Override
    public void updateRate(IbmsEnergyRateSaveReqVO updateReqVO) {
        // 校验存在
        if (rateMapper.selectById(updateReqVO.getId()) == null) {
            throw exception(ENERGY_RATE_NOT_EXISTS);
        }
        // 更新
        IbmsEnergyRateDO updateObj = BeanUtils.toBean(updateReqVO, IbmsEnergyRateDO.class);
        rateMapper.updateById(updateObj);
    }

    @Override
    public void deleteRate(Long id) {
        // 校验存在
        if (rateMapper.selectById(id) == null) {
            throw exception(ENERGY_RATE_NOT_EXISTS);
        }
        // 删除
        rateMapper.deleteById(id);
    }

    // ======================= 人工抄表 =======================

    @Override
    public PageResult<IbmsEnergyManualReadingDO> getManualReadingPage(IbmsEnergyManualReadingPageReqVO pageReqVO) {
        return manualReadingMapper.selectPage(pageReqVO);
    }

    @Override
    public List<IbmsEnergyManualReadingDO> getTodayManualReadings() {
        return manualReadingMapper.selectListByDate(LocalDate.now());
    }

    @Override
    public IbmsEnergyManualReadingDO getLatestManualReading(Long meterId) {
        return manualReadingMapper.selectLatestByMeterId(meterId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createManualReading(IbmsEnergyManualReadingSaveReqVO createReqVO) {
        // 查询仪表信息
        IbmsEnergyMeterDO meter = meterMapper.selectById(createReqVO.getMeterId());
        if (meter == null) {
            throw exception(ENERGY_METER_NOT_EXISTS);
        }

        // 获取上期读数
        IbmsEnergyManualReadingDO lastReading = manualReadingMapper.selectLatestByMeterId(createReqVO.getMeterId());
        BigDecimal lastReadingValue = lastReading != null ? lastReading.getCurrentReading() : meter.getCurrentReading();

        // 计算用量
        BigDecimal consumption = createReqVO.getCurrentReading().subtract(lastReadingValue != null ? lastReadingValue : BigDecimal.ZERO);

        // 创建抄表记录
        IbmsEnergyManualReadingDO reading = new IbmsEnergyManualReadingDO();
        reading.setMeterId(createReqVO.getMeterId());
        reading.setMeterCode(meter.getMeterCode());
        reading.setMeterName(meter.getMeterName());
        reading.setReadingDate(createReqVO.getReadingDate());
        reading.setReadingTime(LocalDateTime.now());
        reading.setLastReading(lastReadingValue);
        reading.setCurrentReading(createReqVO.getCurrentReading());
        reading.setConsumption(consumption);
        reading.setReader(createReqVO.getReader());
        reading.setStatus(1); // 已确认
        reading.setRemark(createReqVO.getRemark());

        manualReadingMapper.insert(reading);

        // 更新仪表当前读数
        IbmsEnergyMeterDO updateMeter = new IbmsEnergyMeterDO();
        updateMeter.setId(meter.getId());
        updateMeter.setCurrentReading(createReqVO.getCurrentReading());
        updateMeter.setLastReadingTime(LocalDateTime.now());
        meterMapper.updateById(updateMeter);

        return reading.getId();
    }

    @Override
    public void reviewManualReading(Long id, String reviewer) {
        IbmsEnergyManualReadingDO reading = manualReadingMapper.selectById(id);
        if (reading == null) {
            throw exception(ENERGY_MANUAL_READING_NOT_EXISTS);
        }

        IbmsEnergyManualReadingDO updateObj = new IbmsEnergyManualReadingDO();
        updateObj.setId(id);
        updateObj.setStatus(1); // 已确认
        updateObj.setReviewer(reviewer);
        updateObj.setReviewTime(LocalDateTime.now());
        manualReadingMapper.updateById(updateObj);
    }

    @Override
    public void voidManualReading(Long id) {
        IbmsEnergyManualReadingDO reading = manualReadingMapper.selectById(id);
        if (reading == null) {
            throw exception(ENERGY_MANUAL_READING_NOT_EXISTS);
        }

        IbmsEnergyManualReadingDO updateObj = new IbmsEnergyManualReadingDO();
        updateObj.setId(id);
        updateObj.setStatus(2); // 已作废
        manualReadingMapper.updateById(updateObj);
    }

    // ======================= 总览 =======================

    @Override
    public IbmsEnergyOverviewVO getOverview() {
        IbmsEnergyOverviewVO vo = new IbmsEnergyOverviewVO();
        // 仪表统计
        vo.setMeterTotalCount(meterMapper.selectCount());
        vo.setMeterOnlineCount(meterMapper.selectCountByStatus(1));
        vo.setMeterOfflineCount(meterMapper.selectCountByStatus(0));
        vo.setMeterFaultCount(meterMapper.selectCountByStatus(2));

        // 按类型统计
        vo.setElectricMeterCount(meterMapper.selectCountByType(1));
        vo.setWaterMeterCount(meterMapper.selectCountByType(2));
        vo.setGasMeterCount(meterMapper.selectCountByType(3));
        vo.setColdMeterCount(meterMapper.selectCountByType(4));
        vo.setHeatMeterCount(meterMapper.selectCountByType(5));

        // 今日能耗（从统计表获取）
        LocalDate today = LocalDate.now();
        List<IbmsEnergyStatisticsDailyDO> todayStats = statisticsMapper.selectListByDateRange(today, today);
        
        BigDecimal todayElec = BigDecimal.ZERO;
        BigDecimal todayWater = BigDecimal.ZERO;
        BigDecimal todayGas = BigDecimal.ZERO;
        BigDecimal todayCold = BigDecimal.ZERO;
        BigDecimal todayHeat = BigDecimal.ZERO;
        
        for (IbmsEnergyStatisticsDailyDO stat : todayStats) {
            if (stat.getMeterType() == 1) todayElec = todayElec.add(stat.getConsumption());
            if (stat.getMeterType() == 2) todayWater = todayWater.add(stat.getConsumption());
            if (stat.getMeterType() == 3) todayGas = todayGas.add(stat.getConsumption());
            if (stat.getMeterType() == 4) todayCold = todayCold.add(stat.getConsumption());
            if (stat.getMeterType() == 5) todayHeat = todayHeat.add(stat.getConsumption());
        }
        
        vo.setTodayElectricity(todayElec);
        vo.setTodayWater(todayWater);
        vo.setTodayGas(todayGas);
        vo.setTodayCold(todayCold);
        vo.setTodayHeat(todayHeat);

        // 本月能耗
        LocalDate monthStart = today.withDayOfMonth(1);
        List<IbmsEnergyStatisticsDailyDO> monthStats = statisticsMapper.selectListByDateRange(monthStart, today);
        
        BigDecimal monthElec = BigDecimal.ZERO;
        BigDecimal monthWater = BigDecimal.ZERO;
        BigDecimal monthGas = BigDecimal.ZERO;
        BigDecimal monthCold = BigDecimal.ZERO;
        BigDecimal monthHeat = BigDecimal.ZERO;
        
        for (IbmsEnergyStatisticsDailyDO stat : monthStats) {
            if (stat.getMeterType() == 1) monthElec = monthElec.add(stat.getConsumption());
            if (stat.getMeterType() == 2) monthWater = monthWater.add(stat.getConsumption());
            if (stat.getMeterType() == 3) monthGas = monthGas.add(stat.getConsumption());
            if (stat.getMeterType() == 4) monthCold = monthCold.add(stat.getConsumption());
            if (stat.getMeterType() == 5) monthHeat = monthHeat.add(stat.getConsumption());
        }
        
        vo.setMonthElectricity(monthElec);
        vo.setMonthWater(monthWater);
        vo.setMonthGas(monthGas);
        vo.setMonthCold(monthCold);
        vo.setMonthHeat(monthHeat);

        // 同环比（暂时使用示例值）
        vo.setElectricityYoy(new BigDecimal("-5.2"));
        vo.setElectricityMom(new BigDecimal("3.8"));
        vo.setWaterYoy(new BigDecimal("-2.1"));
        vo.setWaterMom(new BigDecimal("1.5"));

        // 告警统计
        vo.setTodayAlarmCount(alarmMapper.selectCountByToday());
        vo.setUnhandledAlarmCount(alarmMapper.selectCountByStatus(0));

        return vo;
    }

    @Override
    public IbmsEnergyOverviewVO getOverviewByRange(LocalDate startDate, LocalDate endDate) {
        IbmsEnergyOverviewVO vo = new IbmsEnergyOverviewVO();

        // 仪表统计（与 getOverview 保持一致，便于前端展示总表数等信息）
        vo.setMeterTotalCount(meterMapper.selectCount());
        vo.setMeterOnlineCount(meterMapper.selectCountByStatus(1));
        vo.setMeterOfflineCount(meterMapper.selectCountByStatus(0));
        vo.setMeterFaultCount(meterMapper.selectCountByStatus(2));

        vo.setElectricMeterCount(meterMapper.selectCountByType(1));
        vo.setWaterMeterCount(meterMapper.selectCountByType(2));
        vo.setGasMeterCount(meterMapper.selectCountByType(3));
        vo.setColdMeterCount(meterMapper.selectCountByType(4));
        vo.setHeatMeterCount(meterMapper.selectCountByType(5));

        // 指定日期范围内的能耗汇总
        List<IbmsEnergyStatisticsDailyDO> stats = statisticsMapper.selectListByDateRange(startDate, endDate);

        BigDecimal totalElec = BigDecimal.ZERO;
        BigDecimal totalWater = BigDecimal.ZERO;
        BigDecimal totalGas = BigDecimal.ZERO;
        BigDecimal totalCold = BigDecimal.ZERO;
        BigDecimal totalHeat = BigDecimal.ZERO;

        for (IbmsEnergyStatisticsDailyDO stat : stats) {
            if (stat.getMeterType() == 1) {
                totalElec = totalElec.add(stat.getConsumption());
            } else if (stat.getMeterType() == 2) {
                totalWater = totalWater.add(stat.getConsumption());
            } else if (stat.getMeterType() == 3) {
                totalGas = totalGas.add(stat.getConsumption());
            } else if (stat.getMeterType() == 4) {
                totalCold = totalCold.add(stat.getConsumption());
            } else if (stat.getMeterType() == 5) {
                totalHeat = totalHeat.add(stat.getConsumption());
            }
        }

        // 这里复用 today 字段来表示“当前选择周期”的累计值，方便前端复用 VO
        vo.setTodayElectricity(totalElec);
        vo.setTodayWater(totalWater);
        vo.setTodayGas(totalGas);
        vo.setTodayCold(totalCold);
        vo.setTodayHeat(totalHeat);

        // 同环比、告警统计等暂不按范围计算，保持为空或由前端使用其它接口展示
        return vo;
    }

}
