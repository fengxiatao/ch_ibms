package cn.iocoder.yudao.module.iot.dal.mysql.parking;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.iot.controller.admin.parking.vo.presentvehicle.ParkingPresentVehiclePageReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.parking.ParkingPresentVehicleDO;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 在场车辆 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface ParkingPresentVehicleMapper extends BaseMapperX<ParkingPresentVehicleDO> {

    default PageResult<ParkingPresentVehicleDO> selectPage(ParkingPresentVehiclePageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<ParkingPresentVehicleDO>()
                .likeIfPresent(ParkingPresentVehicleDO::getPlateNumber, reqVO.getPlateNumber())
                .eqIfPresent(ParkingPresentVehicleDO::getVehicleType, reqVO.getVehicleType())
                .eqIfPresent(ParkingPresentVehicleDO::getVehicleCategory, reqVO.getVehicleCategory())
                .eqIfPresent(ParkingPresentVehicleDO::getLotId, reqVO.getLotId())
                .eqIfPresent(ParkingPresentVehicleDO::getLongTermFlag, reqVO.getLongTermFlag())
                .betweenIfPresent(ParkingPresentVehicleDO::getEntryTime, reqVO.getEntryTime())
                .orderByDesc(ParkingPresentVehicleDO::getEntryTime));
    }

    default ParkingPresentVehicleDO selectByPlateNumber(String plateNumber) {
        return selectOne(ParkingPresentVehicleDO::getPlateNumber, plateNumber);
    }

    default ParkingPresentVehicleDO selectByPlateNumberAndLotId(String plateNumber, Long lotId) {
        return selectOne(new LambdaQueryWrapperX<ParkingPresentVehicleDO>()
                .eq(ParkingPresentVehicleDO::getPlateNumber, plateNumber)
                .eq(ParkingPresentVehicleDO::getLotId, lotId));
    }

    default List<ParkingPresentVehicleDO> selectListByLotId(Long lotId) {
        return selectList(ParkingPresentVehicleDO::getLotId, lotId);
    }

    default Long selectCountByLotId(Long lotId) {
        return selectCount(ParkingPresentVehicleDO::getLotId, lotId);
    }

    default Long selectCountByLongTermFlag(Integer longTermFlag) {
        return selectCount(ParkingPresentVehicleDO::getLongTermFlag, longTermFlag);
    }

    default List<ParkingPresentVehicleDO> selectLongTermVehicles(LocalDateTime threshold) {
        return selectList(new LambdaQueryWrapperX<ParkingPresentVehicleDO>()
                .lt(ParkingPresentVehicleDO::getEntryTime, threshold));
    }
}
