package cn.iocoder.yudao.module.iot.service.parking;

import cn.iocoder.yudao.module.iot.controller.admin.parking.vo.statistics.ParkingOverviewStatisticsRespVO;
import cn.iocoder.yudao.module.iot.controller.admin.parking.vo.statistics.ParkingPresentStatisticsRespVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.parking.ParkingLotDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.parking.ParkingPresentVehicleDO;
import cn.iocoder.yudao.module.iot.dal.mysql.parking.*;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 停车场统计 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Slf4j
public class ParkingStatisticsServiceImpl implements ParkingStatisticsService {

    @Resource
    private ParkingLotMapper parkingLotMapper;

    @Resource
    private ParkingPresentVehicleMapper parkingPresentVehicleMapper;

    @Resource
    private ParkingRecordMapper parkingRecordMapper;

    @Resource
    private ParkingMonthlyVehicleMapper parkingMonthlyVehicleMapper;

    @Resource
    private ParkingFreeVehicleMapper parkingFreeVehicleMapper;

    @Override
    public ParkingPresentStatisticsRespVO getPresentStatistics(Long lotId) {
        // 1. 查询在场车辆
        LambdaQueryWrapper<ParkingPresentVehicleDO> queryWrapper = new LambdaQueryWrapper<>();
        if (lotId != null) {
            queryWrapper.eq(ParkingPresentVehicleDO::getLotId, lotId);
        }
        List<ParkingPresentVehicleDO> presentVehicles = parkingPresentVehicleMapper.selectList(queryWrapper);

        // 2. 统计各类型车辆数量
        long totalCount = presentVehicles.size();
        long temporaryCount = presentVehicles.stream()
                .filter(v -> "temporary".equals(v.getVehicleCategory()))
                .count();
        long monthlyCount = presentVehicles.stream()
                .filter(v -> "monthly".equals(v.getVehicleCategory()))
                .count();
        long freeCount = presentVehicles.stream()
                .filter(v -> "free".equals(v.getVehicleCategory()))
                .count();
        long longTermCount = presentVehicles.stream()
                .filter(v -> v.getLongTermFlag() != null && v.getLongTermFlag() == 1)
                .count();

        // 3. 查询车位信息
        int totalSpaces = 0;
        if (lotId != null) {
            ParkingLotDO lot = parkingLotMapper.selectById(lotId);
            if (lot != null && lot.getTotalSpaces() != null) {
                totalSpaces = lot.getTotalSpaces();
            }
        } else {
            // 统计所有停车场的总车位数
            List<ParkingLotDO> lots = parkingLotMapper.selectList(new LambdaQueryWrapper<ParkingLotDO>()
                    .eq(ParkingLotDO::getStatus, 0));
            totalSpaces = lots.stream()
                    .filter(l -> l.getTotalSpaces() != null)
                    .mapToInt(ParkingLotDO::getTotalSpaces)
                    .sum();
        }

        // 4. 计算剩余车位和使用率
        int availableSpaces = Math.max(0, totalSpaces - (int) totalCount);
        double occupancyRate = totalSpaces > 0 
                ? BigDecimal.valueOf(totalCount * 100.0 / totalSpaces)
                        .setScale(1, RoundingMode.HALF_UP).doubleValue()
                : 0.0;

        return ParkingPresentStatisticsRespVO.builder()
                .totalCount(totalCount)
                .temporaryCount(temporaryCount)
                .monthlyCount(monthlyCount)
                .freeCount(freeCount)
                .totalSpaces(totalSpaces)
                .availableSpaces(availableSpaces)
                .occupancyRate(occupancyRate)
                .longTermCount(longTermCount)
                .build();
    }

    @Override
    public ParkingOverviewStatisticsRespVO getOverviewStatistics() {
        // 1. 查询所有正常状态的停车场
        List<ParkingLotDO> lots = parkingLotMapper.selectList(new LambdaQueryWrapper<ParkingLotDO>()
                .eq(ParkingLotDO::getStatus, 0));
        long lotCount = lots.size();

        // 2. 统计总车位数
        int totalSpaces = lots.stream()
                .filter(l -> l.getTotalSpaces() != null)
                .mapToInt(ParkingLotDO::getTotalSpaces)
                .sum();

        // 3. 查询在场车辆总数
        Long presentVehicleCount = parkingPresentVehicleMapper.selectCount();

        // 4. 计算剩余车位和使用率
        int availableSpaces = Math.max(0, totalSpaces - presentVehicleCount.intValue());
        double overallOccupancyRate = totalSpaces > 0
                ? BigDecimal.valueOf(presentVehicleCount * 100.0 / totalSpaces)
                        .setScale(1, RoundingMode.HALF_UP).doubleValue()
                : 0.0;

        // 5. 查询今日入场/出场车辆数和收入
        LocalDateTime todayStart = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        LocalDateTime todayEnd = LocalDateTime.of(LocalDate.now(), LocalTime.MAX);
        
        Long todayEntryCount = parkingRecordMapper.selectCountByEntryTime(todayStart, todayEnd);
        Long todayExitCount = parkingRecordMapper.selectCountByExitTime(todayStart, todayEnd);
        BigDecimal todayIncome = parkingRecordMapper.selectTodayIncome(todayStart, todayEnd);
        if (todayIncome == null) {
            todayIncome = BigDecimal.ZERO;
        }

        // 6. 查询月租车和免费车总数
        Long monthlyVehicleCount = parkingMonthlyVehicleMapper.selectCount(new LambdaQueryWrapper<>());
        Long freeVehicleCount = parkingFreeVehicleMapper.selectCount(new LambdaQueryWrapper<>());

        // 7. 各停车场统计
        List<ParkingOverviewStatisticsRespVO.LotStatistics> lotStatisticsList = new ArrayList<>();
        for (ParkingLotDO lot : lots) {
            Long presentCount = parkingPresentVehicleMapper.selectCountByLotId(lot.getId());
            int lotTotalSpaces = lot.getTotalSpaces() != null ? lot.getTotalSpaces() : 0;
            int lotAvailableSpaces = Math.max(0, lotTotalSpaces - presentCount.intValue());
            double lotOccupancyRate = lotTotalSpaces > 0
                    ? BigDecimal.valueOf(presentCount * 100.0 / lotTotalSpaces)
                            .setScale(1, RoundingMode.HALF_UP).doubleValue()
                    : 0.0;

            lotStatisticsList.add(ParkingOverviewStatisticsRespVO.LotStatistics.builder()
                    .lotId(lot.getId())
                    .lotName(lot.getLotName())
                    .totalSpaces(lotTotalSpaces)
                    .presentCount(presentCount)
                    .availableSpaces(lotAvailableSpaces)
                    .occupancyRate(lotOccupancyRate)
                    .build());
        }

        return ParkingOverviewStatisticsRespVO.builder()
                .lotCount(lotCount)
                .totalSpaces(totalSpaces)
                .presentVehicleCount(presentVehicleCount)
                .availableSpaces(availableSpaces)
                .overallOccupancyRate(overallOccupancyRate)
                .todayEntryCount(todayEntryCount != null ? todayEntryCount : 0L)
                .todayExitCount(todayExitCount != null ? todayExitCount : 0L)
                .todayIncome(todayIncome)
                .monthlyVehicleCount(monthlyVehicleCount)
                .freeVehicleCount(freeVehicleCount)
                .lotStatisticsList(lotStatisticsList)
                .build();
    }
}
