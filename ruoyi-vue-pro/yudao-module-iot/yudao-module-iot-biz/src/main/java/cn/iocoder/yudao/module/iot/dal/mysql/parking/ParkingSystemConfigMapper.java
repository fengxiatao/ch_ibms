package cn.iocoder.yudao.module.iot.dal.mysql.parking;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.iot.dal.dataobject.parking.ParkingSystemConfigDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ParkingSystemConfigMapper extends BaseMapperX<ParkingSystemConfigDO> {

    default ParkingSystemConfigDO selectByLotIdOrFirst(Long lotId) {
        if (lotId != null) {
            ParkingSystemConfigDO config = selectOne(ParkingSystemConfigDO::getLotId, lotId);
            if (config != null) {
                return config;
            }
        }
        return selectOne(new LambdaQueryWrapperX<ParkingSystemConfigDO>()
                .orderByAsc(ParkingSystemConfigDO::getId)
                .last("limit 1"));
    }
}

