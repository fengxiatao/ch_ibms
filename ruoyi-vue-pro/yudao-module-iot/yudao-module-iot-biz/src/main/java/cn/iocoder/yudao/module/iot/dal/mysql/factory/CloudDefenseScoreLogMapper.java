package cn.iocoder.yudao.module.iot.dal.mysql.factory;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.iot.dal.dataobject.factory.CloudDefenseScoreLogDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CloudDefenseScoreLogMapper extends BaseMapperX<CloudDefenseScoreLogDO> {

    default CloudDefenseScoreLogDO selectLatest() {
        return selectOne(new LambdaQueryWrapperX<CloudDefenseScoreLogDO>()
                .orderByDesc(CloudDefenseScoreLogDO::getScoreTime)
                .orderByDesc(CloudDefenseScoreLogDO::getId)
                .last("LIMIT 1"));
    }
}
