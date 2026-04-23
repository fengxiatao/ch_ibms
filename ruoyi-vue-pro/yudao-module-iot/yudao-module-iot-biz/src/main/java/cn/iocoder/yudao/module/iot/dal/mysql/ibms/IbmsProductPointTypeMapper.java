package cn.iocoder.yudao.module.iot.dal.mysql.ibms;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.iot.dal.dataobject.ibms.IbmsProductPointTypeDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * IBMS 产品点位类型定义 Mapper
 */
@Mapper
public interface IbmsProductPointTypeMapper extends BaseMapperX<IbmsProductPointTypeDO> {

    default List<IbmsProductPointTypeDO> selectListByProductId(Long productId) {
        return selectList(new LambdaQueryWrapperX<IbmsProductPointTypeDO>()
                .eq(IbmsProductPointTypeDO::getProductId, productId)
                .orderByAsc(IbmsProductPointTypeDO::getId));
    }

    default void deleteByProductId(Long productId) {
        delete(new LambdaQueryWrapperX<IbmsProductPointTypeDO>()
                .eq(IbmsProductPointTypeDO::getProductId, productId));
    }
}

