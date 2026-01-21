package cn.iocoder.yudao.module.iot.service.parking;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.iot.controller.admin.parking.vo.presentvehicle.ParkingPresentVehiclePageReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.parking.vo.record.ParkingRecordPageReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.parking.ParkingPresentVehicleDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.parking.ParkingRecordDO;

import java.math.BigDecimal;

/**
 * 停车记录 Service 接口
 *
 * @author 芋道源码
 */
public interface ParkingRecordService {

    /**
     * 车辆入场
     *
     * @param plateNumber 车牌号
     * @param lotId 车场ID
     * @param laneId 车道ID
     * @param gateId 道闸ID
     * @param photoUrl 照片URL
     * @return 记录ID
     */
    Long vehicleEntry(String plateNumber, Long lotId, Long laneId, Long gateId, String photoUrl);

    /**
     * 车辆出场
     *
     * @param plateNumber 车牌号
     * @param lotId 车场ID
     * @param laneId 车道ID
     * @param gateId 道闸ID
     * @param photoUrl 照片URL
     * @param paidAmount 实收金额
     * @param paymentMethod 支付方式
     */
    void vehicleExit(String plateNumber, Long lotId, Long laneId, Long gateId, 
                     String photoUrl, BigDecimal paidAmount, String paymentMethod);

    /**
     * 强制出场
     *
     * @param presentVehicleId 在场车辆ID
     * @param remark 备注
     */
    void forceExit(Long presentVehicleId, String remark);

    /**
     * 获得在场车辆分页
     *
     * @param pageReqVO 分页查询
     * @return 在场车辆分页
     */
    PageResult<ParkingPresentVehicleDO> getPresentVehiclePage(ParkingPresentVehiclePageReqVO pageReqVO);

    /**
     * 获得停车记录分页
     *
     * @param pageReqVO 分页查询
     * @return 停车记录分页
     */
    PageResult<ParkingRecordDO> getRecordPage(ParkingRecordPageReqVO pageReqVO);

    /**
     * 获得停车记录
     *
     * @param id 记录ID
     * @return 停车记录
     */
    ParkingRecordDO getRecord(Long id);

    /**
     * 获得在场车辆
     *
     * @param id 在场车辆ID
     * @return 在场车辆
     */
    ParkingPresentVehicleDO getPresentVehicle(Long id);

    /**
     * 计算停车费用
     *
     * @param plateNumber 车牌号
     * @param lotId 车场ID
     * @return 停车费用
     */
    BigDecimal calculateParkingFee(String plateNumber, Long lotId);

    /**
     * 计算停车费用（支持按车型）
     *
     * @param plateNumber 车牌号
     * @param lotId 车场ID
     * @param vehicleType 收费车型：1-小型车，2-中型车，3-新能源车，4-大型车，5-超大型车
     * @return 停车费用
     */
    BigDecimal calculateParkingFee(String plateNumber, Long lotId, Integer vehicleType);

    /**
     * 判断车辆类型
     *
     * @param plateNumber 车牌号
     * @return 车辆类型：free-免费车，monthly-月租车，temporary-临时车
     */
    String getVehicleCategory(String plateNumber);

    /**
     * 获取适用的收费规则名称
     *
     * @param lotId 停车场ID
     * @param vehicleCategory 车辆类别
     * @return 收费规则名称
     */
    String getChargeRuleName(Long lotId, String vehicleCategory);

    /**
     * 获取适用的收费规则名称（支持按车型）
     *
     * @param lotId 停车场ID
     * @param vehicleCategory 车辆类别
     * @param vehicleType 收费车型
     * @return 收费规则名称
     */
    String getChargeRuleName(Long lotId, String vehicleCategory, Integer vehicleType);
}
