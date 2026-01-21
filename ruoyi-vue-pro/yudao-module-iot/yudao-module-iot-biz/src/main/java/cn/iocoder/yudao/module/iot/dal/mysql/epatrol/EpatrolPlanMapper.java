package cn.iocoder.yudao.module.iot.dal.mysql.epatrol;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.iot.controller.admin.epatrol.vo.EpatrolPlanPageReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.epatrol.EpatrolPlanDO;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDate;
import java.util.List;

/**
 * 电子巡更 - 巡更计划 Mapper
 *
 * @author 长辉信息
 */
@Mapper
public interface EpatrolPlanMapper extends BaseMapperX<EpatrolPlanDO> {

    default PageResult<EpatrolPlanDO> selectPage(EpatrolPlanPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<EpatrolPlanDO>()
                .likeIfPresent(EpatrolPlanDO::getPlanCode, reqVO.getPlanCode())
                .likeIfPresent(EpatrolPlanDO::getPlanName, reqVO.getPlanName())
                .eqIfPresent(EpatrolPlanDO::getRouteId, reqVO.getRouteId())
                .eqIfPresent(EpatrolPlanDO::getStatus, reqVO.getStatus())
                .orderByDesc(EpatrolPlanDO::getId));
    }

    default EpatrolPlanDO selectByPlanCode(String planCode) {
        return selectOne(EpatrolPlanDO::getPlanCode, planCode);
    }

    default List<EpatrolPlanDO> selectListByStatus(Integer status) {
        return selectList(new LambdaQueryWrapperX<EpatrolPlanDO>()
                .eq(EpatrolPlanDO::getStatus, status)
                .orderByDesc(EpatrolPlanDO::getId));
    }

    /**
     * 查询需要生成任务的计划（执行中且在有效期内）
     */
    default List<EpatrolPlanDO> selectActivePlans(LocalDate date) {
        return selectList(new LambdaQueryWrapperX<EpatrolPlanDO>()
                .eq(EpatrolPlanDO::getStatus, 1) // 执行中
                .le(EpatrolPlanDO::getStartDate, date)
                .and(wrapper -> wrapper
                        .isNull(EpatrolPlanDO::getEndDate)
                        .or()
                        .ge(EpatrolPlanDO::getEndDate, date)));
    }

}
