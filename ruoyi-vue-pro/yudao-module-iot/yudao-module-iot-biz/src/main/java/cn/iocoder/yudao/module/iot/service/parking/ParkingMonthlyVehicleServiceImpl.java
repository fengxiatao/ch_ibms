package cn.iocoder.yudao.module.iot.service.parking;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.iot.controller.admin.parking.vo.monthlyvehicle.ParkingMonthlyVehiclePageReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.parking.vo.monthlyvehicle.ParkingMonthlyVehicleSaveReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.parking.ParkingMonthlyRechargeDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.parking.ParkingMonthlyVehicleDO;
import cn.iocoder.yudao.module.iot.dal.mysql.parking.ParkingMonthlyRechargeMapper;
import cn.iocoder.yudao.module.iot.dal.mysql.parking.ParkingMonthlyVehicleMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.iot.enums.ErrorCodeConstants.*;

@Service
@Validated
public class ParkingMonthlyVehicleServiceImpl implements ParkingMonthlyVehicleService {

    @Resource
    private ParkingMonthlyVehicleMapper parkingMonthlyVehicleMapper;

    @Resource
    private ParkingMonthlyRechargeMapper parkingMonthlyRechargeMapper;

    @Override
    public Long createMonthlyVehicle(ParkingMonthlyVehicleSaveReqVO createReqVO) {
        validatePlateNumberUnique(null, createReqVO.getPlateNumber());
        ParkingMonthlyVehicleDO monthlyVehicle = BeanUtils.toBean(createReqVO, ParkingMonthlyVehicleDO.class);
        parkingMonthlyVehicleMapper.insert(monthlyVehicle);
        return monthlyVehicle.getId();
    }

    @Override
    public void updateMonthlyVehicle(ParkingMonthlyVehicleSaveReqVO updateReqVO) {
        validateMonthlyVehicleExists(updateReqVO.getId());
        validatePlateNumberUnique(updateReqVO.getId(), updateReqVO.getPlateNumber());
        ParkingMonthlyVehicleDO updateObj = BeanUtils.toBean(updateReqVO, ParkingMonthlyVehicleDO.class);
        parkingMonthlyVehicleMapper.updateById(updateObj);
    }

    @Override
    public void deleteMonthlyVehicle(Long id) {
        validateMonthlyVehicleExists(id);
        parkingMonthlyVehicleMapper.deleteById(id);
    }

    private void validateMonthlyVehicleExists(Long id) {
        if (parkingMonthlyVehicleMapper.selectById(id) == null) {
            throw exception(PARKING_MONTHLY_VEHICLE_NOT_EXISTS);
        }
    }

    private void validatePlateNumberUnique(Long id, String plateNumber) {
        ParkingMonthlyVehicleDO vehicle = parkingMonthlyVehicleMapper.selectByPlateNumber(plateNumber);
        if (vehicle == null) {
            return;
        }
        if (id == null || !id.equals(vehicle.getId())) {
            throw exception(PARKING_MONTHLY_VEHICLE_PLATE_EXISTS);
        }
    }

    @Override
    public ParkingMonthlyVehicleDO getMonthlyVehicle(Long id) {
        return parkingMonthlyVehicleMapper.selectById(id);
    }

    @Override
    public PageResult<ParkingMonthlyVehicleDO> getMonthlyVehiclePage(ParkingMonthlyVehiclePageReqVO pageReqVO) {
        return parkingMonthlyVehicleMapper.selectPage(pageReqVO);
    }

    @Override
    public ParkingMonthlyVehicleDO getMonthlyVehicleByPlateNumber(String plateNumber) {
        return parkingMonthlyVehicleMapper.selectByPlateNumber(plateNumber);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void rechargeMonthlyVehicle(Long id, Integer months, BigDecimal paidAmount, String paymentMethod) {
        ParkingMonthlyVehicleDO vehicle = validateMonthlyVehicleExistsAndGet(id);
        
        // 计算新的有效期
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime validStart = vehicle.getValidEnd() != null && vehicle.getValidEnd().isAfter(now) 
                ? vehicle.getValidEnd() : now;
        LocalDateTime validEnd = validStart.plusMonths(months);
        
        // 计算应收金额
        BigDecimal chargeAmount = vehicle.getMonthlyFee() != null 
                ? vehicle.getMonthlyFee().multiply(new BigDecimal(months)) 
                : BigDecimal.ZERO;
        
        // 更新月租车信息
        vehicle.setValidStart(vehicle.getValidEnd() != null && vehicle.getValidEnd().isAfter(now) 
                ? vehicle.getValidStart() : now);
        vehicle.setValidEnd(validEnd);
        vehicle.setLastChargeTime(now);
        vehicle.setLastChargeMonths(months);
        vehicle.setStatus(0); // 正常状态
        parkingMonthlyVehicleMapper.updateById(vehicle);
        
        // 创建充值记录
        ParkingMonthlyRechargeDO recharge = ParkingMonthlyRechargeDO.builder()
                .monthlyVehicleId(id)
                .plateNumber(vehicle.getPlateNumber())
                .ownerName(vehicle.getOwnerName())
                .ownerPhone(vehicle.getOwnerPhone())
                .rechargeMonths(months)
                .validStart(validStart)
                .validEnd(validEnd)
                .chargeAmount(chargeAmount)
                .paidAmount(paidAmount)
                .paymentMethod(paymentMethod)
                .paymentTime(now)
                .paymentStatus(1)
                .build();
        parkingMonthlyRechargeMapper.insert(recharge);
    }

    private ParkingMonthlyVehicleDO validateMonthlyVehicleExistsAndGet(Long id) {
        ParkingMonthlyVehicleDO vehicle = parkingMonthlyVehicleMapper.selectById(id);
        if (vehicle == null) {
            throw exception(PARKING_MONTHLY_VEHICLE_NOT_EXISTS);
        }
        return vehicle;
    }
}
