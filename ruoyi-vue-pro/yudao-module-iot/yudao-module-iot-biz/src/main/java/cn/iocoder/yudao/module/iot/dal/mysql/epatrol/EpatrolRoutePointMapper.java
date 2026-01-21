package cn.iocoder.yudao.module.iot.dal.mysql.epatrol;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.iot.dal.dataobject.epatrol.EpatrolRoutePointDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 电子巡更 - 路线点位关联 Mapper
 *
 * @author 长辉信息
 */
@Mapper
public interface EpatrolRoutePointMapper extends BaseMapperX<EpatrolRoutePointDO> {

    default List<EpatrolRoutePointDO> selectByRouteId(Long routeId) {
        return selectList(new LambdaQueryWrapperX<EpatrolRoutePointDO>()
                .eq(EpatrolRoutePointDO::getRouteId, routeId)
                .orderByAsc(EpatrolRoutePointDO::getSort));
    }

    default void deleteByRouteId(Long routeId) {
        delete(new LambdaQueryWrapperX<EpatrolRoutePointDO>()
                .eq(EpatrolRoutePointDO::getRouteId, routeId));
    }

}
