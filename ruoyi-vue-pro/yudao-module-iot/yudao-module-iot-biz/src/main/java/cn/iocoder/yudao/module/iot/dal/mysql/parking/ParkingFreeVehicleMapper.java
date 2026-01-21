package cn.iocoder.yudao.module.iot.dal.mysql.parking;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.iot.controller.admin.parking.vo.freevehicle.ParkingFreeVehiclePageReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.parking.ParkingFreeVehicleDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 免费车 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface ParkingFreeVehicleMapper extends BaseMapperX<ParkingFreeVehicleDO> {

    default PageResult<ParkingFreeVehicleDO> selectPage(ParkingFreeVehiclePageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<ParkingFreeVehicleDO>()
                .likeIfPresent(ParkingFreeVehicleDO::getPlateNumber, reqVO.getPlateNumber())
                .likeIfPresent(ParkingFreeVehicleDO::getOwnerName, reqVO.getOwnerName())
                .likeIfPresent(ParkingFreeVehicleDO::getOwnerPhone, reqVO.getOwnerPhone())
                .eqIfPresent(ParkingFreeVehicleDO::getVehicleType, reqVO.getVehicleType())
                .eqIfPresent(ParkingFreeVehicleDO::getSpecialType, reqVO.getSpecialType())
                .eqIfPresent(ParkingFreeVehicleDO::getStatus, reqVO.getStatus())
                .betweenIfPresent(ParkingFreeVehicleDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(ParkingFreeVehicleDO::getId));
    }

    default ParkingFreeVehicleDO selectByPlateNumber(String plateNumber) {
        return selectOne(ParkingFreeVehicleDO::getPlateNumber, plateNumber);
    }

    default List<ParkingFreeVehicleDO> selectListByStatus(Integer status) {
        return selectList(ParkingFreeVehicleDO::getStatus, status);
    }
}
