package cn.iocoder.yudao.module.iot.dal.mysql.building;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.iot.controller.admin.building.vo.bac.IbmsBacSystemLogPageReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.building.IbmsBacSystemLogDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 楼宇自控系统日志 Mapper
 *
 * @author 智慧楼宇系统
 */
@Mapper
public interface IbmsBacSystemLogMapper extends BaseMapperX<IbmsBacSystemLogDO> {

    default PageResult<IbmsBacSystemLogDO> selectPage(IbmsBacSystemLogPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<IbmsBacSystemLogDO>()
                .eqIfPresent(IbmsBacSystemLogDO::getLogType, reqVO.getLogType())
                .eqIfPresent(IbmsBacSystemLogDO::getDeviceType, reqVO.getDeviceType())
                .likeIfPresent(IbmsBacSystemLogDO::getDeviceName, reqVO.getDeviceName())
                .likeIfPresent(IbmsBacSystemLogDO::getOperator, reqVO.getOperator())
                .betweenIfPresent(IbmsBacSystemLogDO::getLogTime, reqVO.getStartTime(), reqVO.getEndTime())
                .orderByDesc(IbmsBacSystemLogDO::getLogTime));
    }

}
