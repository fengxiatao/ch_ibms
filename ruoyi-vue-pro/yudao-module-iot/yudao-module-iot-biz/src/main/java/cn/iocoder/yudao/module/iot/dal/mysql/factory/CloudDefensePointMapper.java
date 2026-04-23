package cn.iocoder.yudao.module.iot.dal.mysql.factory;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.iot.dal.dataobject.factory.CloudDefensePointDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface CloudDefensePointMapper extends BaseMapperX<CloudDefensePointDO> {

    default List<CloudDefensePointDO> selectEnabledList() {
        return selectList(new LambdaQueryWrapperX<CloudDefensePointDO>()
                .eq(CloudDefensePointDO::getEnabled, 1)
                .orderByAsc(CloudDefensePointDO::getSort)
                .orderByAsc(CloudDefensePointDO::getId));
    }
}
