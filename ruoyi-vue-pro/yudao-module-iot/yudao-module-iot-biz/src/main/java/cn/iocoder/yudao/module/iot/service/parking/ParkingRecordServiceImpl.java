package cn.iocoder.yudao.module.iot.service.parking;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.iot.controller.admin.parking.vo.presentvehicle.ParkingPresentVehiclePageReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.parking.vo.record.ParkingRecordPageReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.parking.*;
import cn.iocoder.yudao.module.iot.dal.mysql.parking.*;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.iot.enums.ErrorCodeConstants.*;

@Service
@Validated
@Slf4j
public class ParkingRecordServiceImpl implements ParkingRecordService {

    @Resource
    private ParkingRecordMapper parkingRecordMapper;

    @Resource
    private ParkingPresentVehicleMapper parkingPresentVehicleMapper;

    @Resource
    private ParkingFreeVehicleMapper parkingFreeVehicleMapper;

    @Resource
    private ParkingMonthlyVehicleMapper parkingMonthlyVehicleMapper;

    @Resource
    private ParkingChargeRuleMapper parkingChargeRuleMapper;

    @Resource
    private ParkingChargeRuleApplyMapper parkingChargeRuleApplyMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long vehicleEntry(String plateNumber, Long lotId, Long laneId, Long gateId, String photoUrl) {
        LocalDateTime now = LocalDateTime.now();
        
        // 判断车辆类型
        String vehicleCategory = determineVehicleCategory(plateNumber);
        
        // 创建在场车辆记录
        ParkingPresentVehicleDO presentVehicle = ParkingPresentVehicleDO.builder()
                .plateNumber(plateNumber)
                .vehicleCategory(vehicleCategory)
                .lotId(lotId)
                .entryLaneId(laneId)
                .entryGateId(gateId)
                .entryTime(now)
                .entryPhotoUrl(photoUrl)
                .status(0)
                .build();
        parkingPresentVehicleMapper.insert(presentVehicle);
        
        // 创建进出记录
        ParkingRecordDO record = ParkingRecordDO.builder()
                .plateNumber(plateNumber)
                .vehicleCategory(vehicleCategory)
                .lotId(lotId)
                .entryLaneId(laneId)
                .entryGateId(gateId)
                .entryTime(now)
                .entryPhotoUrl(photoUrl)
                .recordStatus(1) // 在场
                .paymentStatus("free".equals(vehicleCategory) || "monthly".equals(vehicleCategory) ? 2 : 0)
                .build();
        parkingRecordMapper.insert(record);
        
        return record.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void vehicleExit(String plateNumber, Long lotId, Long laneId, Long gateId, 
                           String photoUrl, BigDecimal paidAmount, String paymentMethod) {
        LocalDateTime now = LocalDateTime.now();
        
        // 查找在场车辆
        ParkingPresentVehicleDO presentVehicle = parkingPresentVehicleMapper.selectByPlateNumberAndLotId(plateNumber, lotId);
        if (presentVehicle == null) {
            throw exception(PARKING_RECORD_NOT_IN_LOT);
        }
        
        // 计算停车时长
        int parkingDuration = (int) Duration.between(presentVehicle.getEntryTime(), now).toMinutes();
        
        // 更新进出记录
        ParkingRecordDO record = parkingRecordMapper.selectLatestByPlateNumber(plateNumber);
        if (record != null && record.getRecordStatus() == 1) {
            record.setExitLaneId(laneId);
            record.setExitGateId(gateId);
            record.setExitTime(now);
            record.setExitPhotoUrl(photoUrl);
            record.setParkingDuration(parkingDuration);
            record.setPaidAmount(paidAmount != null ? paidAmount : BigDecimal.ZERO);
            record.setPaymentMethod(paymentMethod);
            record.setPaymentTime(now);
            record.setPaymentStatus(1);
            record.setRecordStatus(2); // 已出场
            record.setExitType("normal");
            parkingRecordMapper.updateById(record);
        }
        
        // 删除在场车辆
        parkingPresentVehicleMapper.deleteById(presentVehicle.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void forceExit(Long presentVehicleId, String remark) {
        ParkingPresentVehicleDO presentVehicle = parkingPresentVehicleMapper.selectById(presentVehicleId);
        if (presentVehicle == null) {
            throw exception(PARKING_PRESENT_VEHICLE_NOT_EXISTS);
        }
        
        LocalDateTime now = LocalDateTime.now();
        int parkingDuration = (int) Duration.between(presentVehicle.getEntryTime(), now).toMinutes();
        
        // 更新进出记录
        ParkingRecordDO record = parkingRecordMapper.selectLatestByPlateNumber(presentVehicle.getPlateNumber());
        if (record != null && record.getRecordStatus() == 1) {
            record.setExitTime(now);
            record.setParkingDuration(parkingDuration);
            record.setRecordStatus(2);
            record.setExitType("force");
            record.setRemark(remark);
            parkingRecordMapper.updateById(record);
        }
        
        // 删除在场车辆
        parkingPresentVehicleMapper.deleteById(presentVehicleId);
    }

    @Override
    public PageResult<ParkingPresentVehicleDO> getPresentVehiclePage(ParkingPresentVehiclePageReqVO pageReqVO) {
        return parkingPresentVehicleMapper.selectPage(pageReqVO);
    }

    @Override
    public PageResult<ParkingRecordDO> getRecordPage(ParkingRecordPageReqVO pageReqVO) {
        return parkingRecordMapper.selectPage(pageReqVO);
    }

    @Override
    public ParkingRecordDO getRecord(Long id) {
        return parkingRecordMapper.selectById(id);
    }

    @Override
    public ParkingPresentVehicleDO getPresentVehicle(Long id) {
        return parkingPresentVehicleMapper.selectById(id);
    }

    @Override
    public BigDecimal calculateParkingFee(String plateNumber, Long lotId) {
        ParkingPresentVehicleDO presentVehicle = parkingPresentVehicleMapper.selectByPlateNumberAndLotId(plateNumber, lotId);
        if (presentVehicle == null) {
            return BigDecimal.ZERO;
        }
        
        // 1. 判断车辆类型：免费车和月租车不收费
        String category = determineVehicleCategory(plateNumber);
        if ("free".equals(category)) {
            log.info("[calculateParkingFee] 车牌号：{}，类型：免费车，费用：0", plateNumber);
            return BigDecimal.ZERO;
        }
        if ("monthly".equals(category)) {
            // 检查月租车是否在有效期内
            ParkingMonthlyVehicleDO monthlyVehicle = parkingMonthlyVehicleMapper.selectByPlateNumber(plateNumber);
            if (monthlyVehicle != null && monthlyVehicle.getValidEnd() != null 
                    && LocalDateTime.now().isBefore(monthlyVehicle.getValidEnd())) {
                log.info("[calculateParkingFee] 车牌号：{}，类型：月租车（有效期至：{}），费用：0", 
                        plateNumber, monthlyVehicle.getValidEnd());
                return BigDecimal.ZERO;
            }
            // 月租车过期，按临时车收费
            log.info("[calculateParkingFee] 车牌号：{}，类型：月租车已过期，按临时车收费", plateNumber);
            category = "temporary";
        }
        
        // 2. 查询适用的收费规则
        ParkingChargeRuleDO chargeRule = findApplicableChargeRule(lotId, category);
        if (chargeRule == null) {
            log.warn("[calculateParkingFee] 未找到适用的收费规则，使用默认规则。停车场ID：{}，车辆类别：{}", lotId, category);
            return calculateDefaultFee(presentVehicle.getEntryTime());
        }
        
        // 3. 根据收费规则计算费用
        BigDecimal fee = calculateFeeByRule(presentVehicle.getEntryTime(), chargeRule);
        log.info("[calculateParkingFee] 车牌号：{}，收费规则：{}，计算费用：{}", 
                plateNumber, chargeRule.getRuleName(), fee);
        return fee;
    }

    /**
     * 查找适用的收费规则
     */
    private ParkingChargeRuleDO findApplicableChargeRule(Long lotId, String vehicleCategory) {
        return findApplicableChargeRule(lotId, vehicleCategory, null);
    }

    /**
     * 查找适用的收费规则（支持按车型）
     */
    private ParkingChargeRuleDO findApplicableChargeRule(Long lotId, String vehicleCategory, Integer vehicleType) {
        // 查询适用于该停车场、车辆类别和车型的收费规则应用
        List<ParkingChargeRuleApplyDO> applies;
        if (vehicleType != null) {
            applies = parkingChargeRuleApplyMapper.selectByLotIdAndCategoryAndVehicleType(lotId, vehicleCategory, vehicleType);
        } else {
            applies = parkingChargeRuleApplyMapper.selectByLotIdAndCategory(lotId, vehicleCategory);
        }
        
        if (CollUtil.isEmpty(applies)) {
            return null;
        }
        
        // 按优先级取第一个（已按优先级降序排序）
        ParkingChargeRuleApplyDO apply = applies.get(0);
        return parkingChargeRuleMapper.selectById(apply.getRuleId());
    }

    /**
     * 根据收费规则计算费用
     */
    private BigDecimal calculateFeeByRule(LocalDateTime entryTime, ParkingChargeRuleDO rule) {
        LocalDateTime now = LocalDateTime.now();
        int parkingMinutes = (int) Duration.between(entryTime, now).toMinutes();
        
        // 免费时长检查
        int freeMinutes = rule.getFreeMinutes() != null ? rule.getFreeMinutes() : 0;
        if (parkingMinutes <= freeMinutes) {
            return BigDecimal.ZERO;
        }
        
        BigDecimal fee = BigDecimal.ZERO;
        
        // 收费模式：1-按次收费，2-按时收费
        if (rule.getChargeMode() != null && rule.getChargeMode() == 1) {
            // 按次收费
            fee = rule.getPerTimeFee() != null ? rule.getPerTimeFee() : BigDecimal.ZERO;
        } else {
            // 按时收费（默认）
            int chargeableMinutes = parkingMinutes - freeMinutes;
            int hours = (chargeableMinutes + 59) / 60; // 向上取整
            
            if (hours >= 1) {
                // 首小时费用
                BigDecimal firstHourFee = rule.getFirstHourFee() != null ? rule.getFirstHourFee() : new BigDecimal("5.00");
                fee = firstHourFee;
                
                // 超出部分费用
                if (hours > 1) {
                    BigDecimal extraHourFee = rule.getExtraHourFee() != null ? rule.getExtraHourFee() : new BigDecimal("3.00");
                    fee = fee.add(extraHourFee.multiply(new BigDecimal(hours - 1)));
                }
            }
            
            // 夜间折扣处理
            fee = applyNightDiscount(fee, entryTime, now, rule);
            
            // 每日最高收费
            BigDecimal maxDailyFee = rule.getMaxDailyFee();
            if (maxDailyFee != null && fee.compareTo(maxDailyFee) > 0) {
                fee = maxDailyFee;
            }
        }
        
        return fee.setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * 应用夜间折扣
     */
    private BigDecimal applyNightDiscount(BigDecimal fee, LocalDateTime entryTime, LocalDateTime exitTime, 
                                          ParkingChargeRuleDO rule) {
        if (rule.getNightDiscount() == null || rule.getNightStartTime() == null || rule.getNightEndTime() == null) {
            return fee;
        }
        
        // 简化处理：如果当前时间在夜间时段，应用折扣
        LocalTime currentTime = LocalTime.now();
        LocalTime nightStart = rule.getNightStartTime();
        LocalTime nightEnd = rule.getNightEndTime();
        
        boolean isNightTime;
        if (nightStart.isBefore(nightEnd)) {
            // 夜间时段不跨天（例如 20:00 - 22:00）
            isNightTime = !currentTime.isBefore(nightStart) && currentTime.isBefore(nightEnd);
        } else {
            // 夜间时段跨天（例如 22:00 - 06:00）
            isNightTime = !currentTime.isBefore(nightStart) || currentTime.isBefore(nightEnd);
        }
        
        if (isNightTime) {
            return fee.multiply(rule.getNightDiscount()).setScale(2, RoundingMode.HALF_UP);
        }
        return fee;
    }

    /**
     * 默认收费规则（未配置时使用）
     */
    private BigDecimal calculateDefaultFee(LocalDateTime entryTime) {
        int parkingMinutes = (int) Duration.between(entryTime, LocalDateTime.now()).toMinutes();
        
        // 默认：15分钟免费
        if (parkingMinutes <= 15) {
            return BigDecimal.ZERO;
        }
        
        // 默认：首小时5元，之后每小时3元，每日封顶50元
        int hours = (parkingMinutes + 59) / 60;
        BigDecimal fee = BigDecimal.ZERO;
        if (hours >= 1) {
            fee = new BigDecimal("5.00");
            if (hours > 1) {
                fee = fee.add(new BigDecimal("3.00").multiply(new BigDecimal(hours - 1)));
            }
        }
        
        BigDecimal maxDailyFee = new BigDecimal("50.00");
        return fee.compareTo(maxDailyFee) > 0 ? maxDailyFee : fee;
    }

    @Override
    public BigDecimal calculateParkingFee(String plateNumber, Long lotId, Integer vehicleType) {
        ParkingPresentVehicleDO presentVehicle = parkingPresentVehicleMapper.selectByPlateNumberAndLotId(plateNumber, lotId);
        if (presentVehicle == null) {
            return BigDecimal.ZERO;
        }
        
        // 1. 判断车辆类型：免费车和月租车不收费
        String category = determineVehicleCategory(plateNumber);
        if ("free".equals(category)) {
            log.info("[calculateParkingFee] 车牌号：{}，类型：免费车，费用：0", plateNumber);
            return BigDecimal.ZERO;
        }
        if ("monthly".equals(category)) {
            // 检查月租车是否在有效期内
            ParkingMonthlyVehicleDO monthlyVehicle = parkingMonthlyVehicleMapper.selectByPlateNumber(plateNumber);
            if (monthlyVehicle != null && monthlyVehicle.getValidEnd() != null 
                    && LocalDateTime.now().isBefore(monthlyVehicle.getValidEnd())) {
                log.info("[calculateParkingFee] 车牌号：{}，类型：月租车（有效期至：{}），费用：0", 
                        plateNumber, monthlyVehicle.getValidEnd());
                return BigDecimal.ZERO;
            }
            // 月租车过期，按临时车收费
            log.info("[calculateParkingFee] 车牌号：{}，类型：月租车已过期，按临时车收费", plateNumber);
            category = "temporary";
        }
        
        // 2. 查询适用的收费规则（考虑车型）
        ParkingChargeRuleDO chargeRule = findApplicableChargeRule(lotId, category, vehicleType);
        if (chargeRule == null) {
            log.warn("[calculateParkingFee] 未找到适用的收费规则，使用默认规则。停车场ID：{}，车辆类别：{}，车型：{}", 
                    lotId, category, vehicleType);
            return calculateDefaultFee(presentVehicle.getEntryTime());
        }
        
        // 3. 根据收费规则计算费用
        BigDecimal fee = calculateFeeByRule(presentVehicle.getEntryTime(), chargeRule);
        log.info("[calculateParkingFee] 车牌号：{}，收费规则：{}，车型：{}，计算费用：{}", 
                plateNumber, chargeRule.getRuleName(), vehicleType, fee);
        return fee;
    }

    @Override
    public String getVehicleCategory(String plateNumber) {
        return determineVehicleCategory(plateNumber);
    }

    @Override
    public String getChargeRuleName(Long lotId, String vehicleCategory) {
        ParkingChargeRuleDO rule = findApplicableChargeRule(lotId, vehicleCategory);
        return rule != null ? rule.getRuleName() : "默认收费规则";
    }

    @Override
    public String getChargeRuleName(Long lotId, String vehicleCategory, Integer vehicleType) {
        ParkingChargeRuleDO rule = findApplicableChargeRule(lotId, vehicleCategory, vehicleType);
        return rule != null ? rule.getRuleName() : "默认收费规则";
    }

    private String determineVehicleCategory(String plateNumber) {
        // 检查是否为免费车
        ParkingFreeVehicleDO freeVehicle = parkingFreeVehicleMapper.selectByPlateNumber(plateNumber);
        if (freeVehicle != null && freeVehicle.getStatus() == 0) {
            LocalDateTime now = LocalDateTime.now();
            if ((freeVehicle.getValidStart() == null || !now.isBefore(freeVehicle.getValidStart())) &&
                (freeVehicle.getValidEnd() == null || !now.isAfter(freeVehicle.getValidEnd()))) {
                return "free";
            }
        }
        
        // 检查是否为月租车
        ParkingMonthlyVehicleDO monthlyVehicle = parkingMonthlyVehicleMapper.selectByPlateNumber(plateNumber);
        if (monthlyVehicle != null && monthlyVehicle.getStatus() == 0) {
            LocalDateTime now = LocalDateTime.now();
            if (monthlyVehicle.getValidEnd() != null && now.isBefore(monthlyVehicle.getValidEnd())) {
                return "monthly";
            }
        }
        
        return "temporary";
    }
}
