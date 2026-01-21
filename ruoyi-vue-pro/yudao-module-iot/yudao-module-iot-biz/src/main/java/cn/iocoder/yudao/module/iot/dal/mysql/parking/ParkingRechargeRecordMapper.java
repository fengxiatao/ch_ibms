package cn.iocoder.yudao.module.iot.dal.mysql.parking;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.iot.controller.admin.parking.vo.rechargerecord.ParkingRechargeRecordPageReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.parking.ParkingRechargeRecordDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 月卡充值记录 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface ParkingRechargeRecordMapper extends BaseMapperX<ParkingRechargeRecordDO> {

    default PageResult<ParkingRechargeRecordDO> selectPage(ParkingRechargeRecordPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<ParkingRechargeRecordDO>()
                .likeIfPresent(ParkingRechargeRecordDO::getPlateNumber, reqVO.getPlateNumber())
                .likeIfPresent(ParkingRechargeRecordDO::getOwnerName, reqVO.getOwnerName())
                .likeIfPresent(ParkingRechargeRecordDO::getOwnerPhone, reqVO.getOwnerPhone())
                .eqIfPresent(ParkingRechargeRecordDO::getMonthlyVehicleId, reqVO.getMonthlyVehicleId())
                .eqIfPresent(ParkingRechargeRecordDO::getPaymentStatus, reqVO.getPaymentStatus())
                .betweenIfPresent(ParkingRechargeRecordDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(ParkingRechargeRecordDO::getId));
    }

    /**
     * 统计指定时间范围内的充值笔数
     */
    @Select("SELECT COUNT(*) FROM iot_parking_monthly_recharge WHERE payment_status = 1 AND create_time >= #{startTime} AND create_time < #{endTime} AND deleted = 0")
    Long selectCountByTimeRange(LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 统计指定时间范围内的充值金额
     */
    @Select("SELECT COALESCE(SUM(paid_amount), 0) FROM iot_parking_monthly_recharge WHERE payment_status = 1 AND create_time >= #{startTime} AND create_time < #{endTime} AND deleted = 0")
    BigDecimal selectSumAmountByTimeRange(LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 统计累计充值笔数
     */
    @Select("SELECT COUNT(*) FROM iot_parking_monthly_recharge WHERE payment_status = 1 AND deleted = 0")
    Long selectTotalCount();

    /**
     * 统计累计充值金额
     */
    @Select("SELECT COALESCE(SUM(paid_amount), 0) FROM iot_parking_monthly_recharge WHERE payment_status = 1 AND deleted = 0")
    BigDecimal selectTotalAmount();
}
