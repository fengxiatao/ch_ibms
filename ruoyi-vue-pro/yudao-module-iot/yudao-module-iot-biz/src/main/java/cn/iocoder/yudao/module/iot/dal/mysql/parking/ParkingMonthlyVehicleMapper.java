package cn.iocoder.yudao.module.iot.dal.mysql.parking;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.iot.controller.admin.parking.vo.monthlyvehicle.ParkingMonthlyVehiclePageReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.parking.ParkingMonthlyVehicleDO;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 月租车 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface ParkingMonthlyVehicleMapper extends BaseMapperX<ParkingMonthlyVehicleDO> {

    default PageResult<ParkingMonthlyVehicleDO> selectPage(ParkingMonthlyVehiclePageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<ParkingMonthlyVehicleDO>()
                .likeIfPresent(ParkingMonthlyVehicleDO::getPlateNumber, reqVO.getPlateNumber())
                .likeIfPresent(ParkingMonthlyVehicleDO::getOwnerName, reqVO.getOwnerName())
                .likeIfPresent(ParkingMonthlyVehicleDO::getOwnerPhone, reqVO.getOwnerPhone())
                .eqIfPresent(ParkingMonthlyVehicleDO::getVehicleType, reqVO.getVehicleType())
                .eqIfPresent(ParkingMonthlyVehicleDO::getLotId, reqVO.getLotId())
                .eqIfPresent(ParkingMonthlyVehicleDO::getStatus, reqVO.getStatus())
                .betweenIfPresent(ParkingMonthlyVehicleDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(ParkingMonthlyVehicleDO::getId));
    }

    default ParkingMonthlyVehicleDO selectByPlateNumber(String plateNumber) {
        return selectOne(ParkingMonthlyVehicleDO::getPlateNumber, plateNumber);
    }

    default List<ParkingMonthlyVehicleDO> selectListByLotId(Long lotId) {
        return selectList(ParkingMonthlyVehicleDO::getLotId, lotId);
    }

    default List<ParkingMonthlyVehicleDO> selectListByStatus(Integer status) {
        return selectList(ParkingMonthlyVehicleDO::getStatus, status);
    }

    default List<ParkingMonthlyVehicleDO> selectExpiredVehicles(LocalDateTime now) {
        return selectList(new LambdaQueryWrapperX<ParkingMonthlyVehicleDO>()
                .lt(ParkingMonthlyVehicleDO::getValidEnd, now)
                .ne(ParkingMonthlyVehicleDO::getStatus, 2));
    }
}
