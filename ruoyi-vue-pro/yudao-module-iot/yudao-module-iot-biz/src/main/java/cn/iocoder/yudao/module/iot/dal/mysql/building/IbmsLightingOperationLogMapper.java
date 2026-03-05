package cn.iocoder.yudao.module.iot.dal.mysql.building;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.iot.controller.admin.building.vo.lighting.IbmsLightingOperationLogPageReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.building.IbmsLightingOperationLogDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 照明操作日志 Mapper
 *
 * @author 智慧楼宇系统
 */
@Mapper
public interface IbmsLightingOperationLogMapper extends BaseMapperX<IbmsLightingOperationLogDO> {

    default PageResult<IbmsLightingOperationLogDO> selectPage(IbmsLightingOperationLogPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<IbmsLightingOperationLogDO>()
                .eqIfPresent(IbmsLightingOperationLogDO::getOperationType, reqVO.getOperationType())
                .eqIfPresent(IbmsLightingOperationLogDO::getTargetType, reqVO.getTargetType())
                .likeIfPresent(IbmsLightingOperationLogDO::getTargetName, reqVO.getTargetName())
                .likeIfPresent(IbmsLightingOperationLogDO::getOperator, reqVO.getOperator())
                .betweenIfPresent(IbmsLightingOperationLogDO::getOperateTime, reqVO.getStartTime(), reqVO.getEndTime())
                .orderByDesc(IbmsLightingOperationLogDO::getOperateTime));
    }

}
