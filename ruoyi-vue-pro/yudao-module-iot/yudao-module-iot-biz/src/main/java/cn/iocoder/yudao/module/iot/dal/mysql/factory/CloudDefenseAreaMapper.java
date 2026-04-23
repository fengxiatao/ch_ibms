package cn.iocoder.yudao.module.iot.dal.mysql.factory;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.iot.dal.dataobject.factory.CloudDefenseAreaDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface CloudDefenseAreaMapper extends BaseMapperX<CloudDefenseAreaDO> {

    default List<CloudDefenseAreaDO> selectEnabledList() {
        return selectList(new LambdaQueryWrapperX<CloudDefenseAreaDO>()
                .eq(CloudDefenseAreaDO::getEnabled, 1)
                .orderByAsc(CloudDefenseAreaDO::getSort)
                .orderByAsc(CloudDefenseAreaDO::getId));
    }
}
