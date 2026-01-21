package cn.iocoder.yudao.module.iot.dal.mysql.parking;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.iot.controller.admin.parking.vo.monthlyrecharge.ParkingMonthlyRechargePageReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.parking.ParkingMonthlyRechargeDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 月卡充值记录 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface ParkingMonthlyRechargeMapper extends BaseMapperX<ParkingMonthlyRechargeDO> {

    default PageResult<ParkingMonthlyRechargeDO> selectPage(ParkingMonthlyRechargePageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<ParkingMonthlyRechargeDO>()
                .likeIfPresent(ParkingMonthlyRechargeDO::getPlateNumber, reqVO.getPlateNumber())
                .likeIfPresent(ParkingMonthlyRechargeDO::getOwnerName, reqVO.getOwnerName())
                .likeIfPresent(ParkingMonthlyRechargeDO::getOwnerPhone, reqVO.getOwnerPhone())
                .eqIfPresent(ParkingMonthlyRechargeDO::getMonthlyVehicleId, reqVO.getMonthlyVehicleId())
                .eqIfPresent(ParkingMonthlyRechargeDO::getPaymentStatus, reqVO.getPaymentStatus())
                .betweenIfPresent(ParkingMonthlyRechargeDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(ParkingMonthlyRechargeDO::getId));
    }

    default List<ParkingMonthlyRechargeDO> selectListByMonthlyVehicleId(Long monthlyVehicleId) {
        return selectList(new LambdaQueryWrapperX<ParkingMonthlyRechargeDO>()
                .eq(ParkingMonthlyRechargeDO::getMonthlyVehicleId, monthlyVehicleId)
                .orderByDesc(ParkingMonthlyRechargeDO::getCreateTime));
    }

    default List<ParkingMonthlyRechargeDO> selectListByPlateNumber(String plateNumber) {
        return selectList(new LambdaQueryWrapperX<ParkingMonthlyRechargeDO>()
                .eq(ParkingMonthlyRechargeDO::getPlateNumber, plateNumber)
                .orderByDesc(ParkingMonthlyRechargeDO::getCreateTime));
    }
}
