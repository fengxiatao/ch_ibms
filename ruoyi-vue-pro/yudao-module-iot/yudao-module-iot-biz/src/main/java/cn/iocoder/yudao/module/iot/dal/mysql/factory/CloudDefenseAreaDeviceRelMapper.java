package cn.iocoder.yudao.module.iot.dal.mysql.factory;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.iot.dal.dataobject.factory.CloudDefenseAreaDeviceRelDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.List;

@Mapper
public interface CloudDefenseAreaDeviceRelMapper extends BaseMapperX<CloudDefenseAreaDeviceRelDO> {

    default List<CloudDefenseAreaDeviceRelDO> selectListByAreaIds(Collection<Long> areaIds) {
        return selectList(new LambdaQueryWrapperX<CloudDefenseAreaDeviceRelDO>()
                .inIfPresent(CloudDefenseAreaDeviceRelDO::getAreaId, areaIds)
                .orderByAsc(CloudDefenseAreaDeviceRelDO::getSort)
                .orderByAsc(CloudDefenseAreaDeviceRelDO::getId));
    }
}
