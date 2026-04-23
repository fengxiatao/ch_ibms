package cn.iocoder.yudao.module.iot.dal.mysql.ibms;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.iot.dal.dataobject.ibms.IbmsProductPropertyDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * IBMS 产品属性定义 Mapper
 */
@Mapper
public interface IbmsProductPropertyMapper extends BaseMapperX<IbmsProductPropertyDO> {

    default List<IbmsProductPropertyDO> selectListByProductId(Long productId) {
        return selectList(new LambdaQueryWrapperX<IbmsProductPropertyDO>()
                .eq(IbmsProductPropertyDO::getProductId, productId)
                .orderByAsc(IbmsProductPropertyDO::getId));
    }

    default void deleteByProductId(Long productId) {
        delete(new LambdaQueryWrapperX<IbmsProductPropertyDO>()
                .eq(IbmsProductPropertyDO::getProductId, productId));
    }
}

