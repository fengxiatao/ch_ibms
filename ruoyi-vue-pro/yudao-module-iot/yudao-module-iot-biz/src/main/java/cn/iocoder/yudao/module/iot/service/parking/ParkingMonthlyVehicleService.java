package cn.iocoder.yudao.module.iot.service.parking;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.iot.controller.admin.parking.vo.monthlyvehicle.ParkingMonthlyVehiclePageReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.parking.vo.monthlyvehicle.ParkingMonthlyVehicleSaveReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.parking.ParkingMonthlyVehicleDO;
import jakarta.validation.Valid;

import java.math.BigDecimal;

/**
 * 月租车 Service 接口
 *
 * @author 芋道源码
 */
public interface ParkingMonthlyVehicleService {

    Long createMonthlyVehicle(@Valid ParkingMonthlyVehicleSaveReqVO createReqVO);

    void updateMonthlyVehicle(@Valid ParkingMonthlyVehicleSaveReqVO updateReqVO);

    void deleteMonthlyVehicle(Long id);

    ParkingMonthlyVehicleDO getMonthlyVehicle(Long id);

    PageResult<ParkingMonthlyVehicleDO> getMonthlyVehiclePage(ParkingMonthlyVehiclePageReqVO pageReqVO);

    ParkingMonthlyVehicleDO getMonthlyVehicleByPlateNumber(String plateNumber);

    /**
     * 月卡续费
     *
     * @param id 月租车ID
     * @param months 续费月数
     * @param paidAmount 实收金额
     * @param paymentMethod 支付方式
     */
    void rechargeMonthlyVehicle(Long id, Integer months, BigDecimal paidAmount, String paymentMethod);
}
