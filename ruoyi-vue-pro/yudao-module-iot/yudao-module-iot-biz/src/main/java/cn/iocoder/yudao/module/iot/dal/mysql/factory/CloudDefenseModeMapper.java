package cn.iocoder.yudao.module.iot.dal.mysql.factory;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.iot.dal.dataobject.factory.CloudDefenseModeDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface CloudDefenseModeMapper extends BaseMapperX<CloudDefenseModeDO> {

    default List<CloudDefenseModeDO> selectEnabledList() {
        return selectList(new LambdaQueryWrapperX<CloudDefenseModeDO>()
                .eq(CloudDefenseModeDO::getEnabled, 1)
                .orderByAsc(CloudDefenseModeDO::getSort)
                .orderByAsc(CloudDefenseModeDO::getId));
    }
}
