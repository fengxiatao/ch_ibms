package cn.iocoder.yudao.module.iot.dal.mysql.parking;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.iot.controller.admin.parking.vo.gate.ParkingGatePageReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.parking.ParkingGateDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 道闸设备 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface ParkingGateMapper extends BaseMapperX<ParkingGateDO> {

    default PageResult<ParkingGateDO> selectPage(ParkingGatePageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<ParkingGateDO>()
                .likeIfPresent(ParkingGateDO::getGateName, reqVO.getGateName())
                .likeIfPresent(ParkingGateDO::getGateCode, reqVO.getGateCode())
                .eqIfPresent(ParkingGateDO::getLotId, reqVO.getLotId())
                .eqIfPresent(ParkingGateDO::getLaneId, reqVO.getLaneId())
                .eqIfPresent(ParkingGateDO::getGateType, reqVO.getGateType())
                .eqIfPresent(ParkingGateDO::getDirection, reqVO.getDirection())
                .eqIfPresent(ParkingGateDO::getOnlineStatus, reqVO.getOnlineStatus())
                .eqIfPresent(ParkingGateDO::getStatus, reqVO.getStatus())
                .betweenIfPresent(ParkingGateDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(ParkingGateDO::getId));
    }

    default ParkingGateDO selectByGateCode(String gateCode) {
        return selectOne(ParkingGateDO::getGateCode, gateCode);
    }

    default List<ParkingGateDO> selectListByLotId(Long lotId) {
        return selectList(ParkingGateDO::getLotId, lotId);
    }

    default List<ParkingGateDO> selectListByLaneId(Long laneId) {
        return selectList(ParkingGateDO::getLaneId, laneId);
    }

    default ParkingGateDO selectByIpAddress(String ipAddress) {
        return selectOne(ParkingGateDO::getIpAddress, ipAddress);
    }
}
