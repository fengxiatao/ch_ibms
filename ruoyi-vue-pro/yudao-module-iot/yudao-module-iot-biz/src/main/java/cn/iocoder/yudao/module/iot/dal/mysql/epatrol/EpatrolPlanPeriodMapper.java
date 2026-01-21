package cn.iocoder.yudao.module.iot.dal.mysql.epatrol;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.iot.dal.dataobject.epatrol.EpatrolPlanPeriodDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 电子巡更 - 计划时段 Mapper
 *
 * @author 长辉信息
 */
@Mapper
public interface EpatrolPlanPeriodMapper extends BaseMapperX<EpatrolPlanPeriodDO> {

    default List<EpatrolPlanPeriodDO> selectByPlanId(Long planId) {
        return selectList(new LambdaQueryWrapperX<EpatrolPlanPeriodDO>()
                .eq(EpatrolPlanPeriodDO::getPlanId, planId)
                .orderByAsc(EpatrolPlanPeriodDO::getStartTime));
    }

    default void deleteByPlanId(Long planId) {
        delete(new LambdaQueryWrapperX<EpatrolPlanPeriodDO>()
                .eq(EpatrolPlanPeriodDO::getPlanId, planId));
    }

}
