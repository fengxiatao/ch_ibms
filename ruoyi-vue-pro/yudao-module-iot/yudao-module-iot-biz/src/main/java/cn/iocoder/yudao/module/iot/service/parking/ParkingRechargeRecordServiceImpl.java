package cn.iocoder.yudao.module.iot.service.parking;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.iot.controller.admin.parking.vo.rechargerecord.ParkingRechargeRecordPageReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.parking.vo.rechargerecord.ParkingRechargeStatisticsVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.parking.ParkingRechargeRecordDO;
import cn.iocoder.yudao.module.iot.dal.mysql.parking.ParkingRechargeRecordMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * 月卡充值记录 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
@Slf4j
public class ParkingRechargeRecordServiceImpl implements ParkingRechargeRecordService {

    @Resource
    private ParkingRechargeRecordMapper parkingRechargeRecordMapper;

    @Override
    public PageResult<ParkingRechargeRecordDO> getRechargeRecordPage(ParkingRechargeRecordPageReqVO pageReqVO) {
        return parkingRechargeRecordMapper.selectPage(pageReqVO);
    }

    @Override
    public ParkingRechargeStatisticsVO getRechargeStatistics() {
        ParkingRechargeStatisticsVO statistics = new ParkingRechargeStatisticsVO();
        
        // 今日时间范围
        LocalDateTime todayStart = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        LocalDateTime todayEnd = LocalDateTime.of(LocalDate.now().plusDays(1), LocalTime.MIN);
        
        // 本月时间范围
        LocalDateTime monthStart = LocalDateTime.of(LocalDate.now().withDayOfMonth(1), LocalTime.MIN);
        LocalDateTime monthEnd = LocalDateTime.of(LocalDate.now().plusMonths(1).withDayOfMonth(1), LocalTime.MIN);
        
        // 今日统计
        statistics.setTodayCount(parkingRechargeRecordMapper.selectCountByTimeRange(todayStart, todayEnd));
        statistics.setTodayAmount(parkingRechargeRecordMapper.selectSumAmountByTimeRange(todayStart, todayEnd));
        
        // 本月统计
        statistics.setMonthCount(parkingRechargeRecordMapper.selectCountByTimeRange(monthStart, monthEnd));
        statistics.setMonthAmount(parkingRechargeRecordMapper.selectSumAmountByTimeRange(monthStart, monthEnd));
        
        // 累计统计
        statistics.setTotalCount(parkingRechargeRecordMapper.selectTotalCount());
        statistics.setTotalAmount(parkingRechargeRecordMapper.selectTotalAmount());
        
        return statistics;
    }
}
