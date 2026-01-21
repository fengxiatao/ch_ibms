package cn.iocoder.yudao.module.iot.dal.mysql.parking;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.iot.controller.admin.parking.vo.lane.ParkingLanePageReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.parking.ParkingLaneDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 车道 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface ParkingLaneMapper extends BaseMapperX<ParkingLaneDO> {

    default PageResult<ParkingLaneDO> selectPage(ParkingLanePageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<ParkingLaneDO>()
                .likeIfPresent(ParkingLaneDO::getLaneName, reqVO.getLaneName())
                .likeIfPresent(ParkingLaneDO::getLaneCode, reqVO.getLaneCode())
                .eqIfPresent(ParkingLaneDO::getLotId, reqVO.getLotId())
                .eqIfPresent(ParkingLaneDO::getDirection, reqVO.getDirection())
                .eqIfPresent(ParkingLaneDO::getStatus, reqVO.getStatus())
                .betweenIfPresent(ParkingLaneDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(ParkingLaneDO::getId));
    }

    default ParkingLaneDO selectByLaneCode(String laneCode) {
        return selectOne(ParkingLaneDO::getLaneCode, laneCode);
    }

    default List<ParkingLaneDO> selectListByLotId(Long lotId) {
        return selectList(ParkingLaneDO::getLotId, lotId);
    }

    default List<ParkingLaneDO> selectListByDirection(Integer direction) {
        return selectList(ParkingLaneDO::getDirection, direction);
    }
}
