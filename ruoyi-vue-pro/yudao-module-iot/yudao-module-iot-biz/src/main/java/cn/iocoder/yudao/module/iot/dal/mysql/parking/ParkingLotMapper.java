package cn.iocoder.yudao.module.iot.dal.mysql.parking;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.iot.controller.admin.parking.vo.lot.ParkingLotPageReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.parking.ParkingLotDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 停车场/车场 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface ParkingLotMapper extends BaseMapperX<ParkingLotDO> {

    default PageResult<ParkingLotDO> selectPage(ParkingLotPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<ParkingLotDO>()
                .likeIfPresent(ParkingLotDO::getLotName, reqVO.getLotName())
                .likeIfPresent(ParkingLotDO::getLotCode, reqVO.getLotCode())
                .eqIfPresent(ParkingLotDO::getLotType, reqVO.getLotType())
                .eqIfPresent(ParkingLotDO::getStatus, reqVO.getStatus())
                .betweenIfPresent(ParkingLotDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(ParkingLotDO::getId));
    }

    default ParkingLotDO selectByLotCode(String lotCode) {
        return selectOne(ParkingLotDO::getLotCode, lotCode);
    }

    default ParkingLotDO selectByLotName(String lotName) {
        return selectOne(ParkingLotDO::getLotName, lotName);
    }

    default List<ParkingLotDO> selectListByStatus(Integer status) {
        return selectList(ParkingLotDO::getStatus, status);
    }
}
