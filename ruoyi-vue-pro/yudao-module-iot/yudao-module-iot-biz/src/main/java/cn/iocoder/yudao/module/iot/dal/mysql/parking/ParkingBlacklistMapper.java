package cn.iocoder.yudao.module.iot.dal.mysql.parking;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.iot.controller.admin.parking.vo.blacklist.ParkingBlacklistPageReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.parking.ParkingBlacklistDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 停车场黑名单 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface ParkingBlacklistMapper extends BaseMapperX<ParkingBlacklistDO> {

    default PageResult<ParkingBlacklistDO> selectPage(ParkingBlacklistPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<ParkingBlacklistDO>()
                .likeIfPresent(ParkingBlacklistDO::getPlateNumber, reqVO.getPlateNumber())
                .likeIfPresent(ParkingBlacklistDO::getReason, reqVO.getReason())
                .eqIfPresent(ParkingBlacklistDO::getLotId, reqVO.getLotId())
                .eqIfPresent(ParkingBlacklistDO::getStatus, reqVO.getStatus())
                .betweenIfPresent(ParkingBlacklistDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(ParkingBlacklistDO::getId));
    }

    default ParkingBlacklistDO selectByPlateNumber(String plateNumber) {
        return selectOne(new LambdaQueryWrapperX<ParkingBlacklistDO>()
                .eq(ParkingBlacklistDO::getPlateNumber, plateNumber)
                .eq(ParkingBlacklistDO::getStatus, 0)); // 只查询生效中的
    }

    default ParkingBlacklistDO selectByPlateNumberAndLotId(String plateNumber, Long lotId) {
        return selectOne(new LambdaQueryWrapperX<ParkingBlacklistDO>()
                .eq(ParkingBlacklistDO::getPlateNumber, plateNumber)
                .eq(ParkingBlacklistDO::getStatus, 0)
                .and(w -> w.isNull(ParkingBlacklistDO::getLotId)
                        .or().eq(ParkingBlacklistDO::getLotId, lotId)));
    }
}
