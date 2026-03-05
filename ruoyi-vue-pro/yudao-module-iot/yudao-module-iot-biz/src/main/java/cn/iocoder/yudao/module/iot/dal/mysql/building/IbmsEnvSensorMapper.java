package cn.iocoder.yudao.module.iot.dal.mysql.building;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.iot.controller.admin.building.vo.env.IbmsEnvSensorPageReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.building.IbmsEnvSensorDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 环境传感器 Mapper
 *
 * @author 智慧楼宇系统
 */
@Mapper
public interface IbmsEnvSensorMapper extends BaseMapperX<IbmsEnvSensorDO> {

    default PageResult<IbmsEnvSensorDO> selectPage(IbmsEnvSensorPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<IbmsEnvSensorDO>()
                .likeIfPresent(IbmsEnvSensorDO::getSensorCode, reqVO.getSensorCode())
                .likeIfPresent(IbmsEnvSensorDO::getSensorName, reqVO.getSensorName())
                .eqIfPresent(IbmsEnvSensorDO::getSensorType, reqVO.getSensorType())
                .eqIfPresent(IbmsEnvSensorDO::getAreaId, reqVO.getAreaId())
                .eqIfPresent(IbmsEnvSensorDO::getStatus, reqVO.getStatus())
                .orderByDesc(IbmsEnvSensorDO::getId));
    }

    default List<IbmsEnvSensorDO> selectList(IbmsEnvSensorPageReqVO reqVO) {
        return selectList(new LambdaQueryWrapperX<IbmsEnvSensorDO>()
                .likeIfPresent(IbmsEnvSensorDO::getSensorCode, reqVO.getSensorCode())
                .likeIfPresent(IbmsEnvSensorDO::getSensorName, reqVO.getSensorName())
                .eqIfPresent(IbmsEnvSensorDO::getSensorType, reqVO.getSensorType())
                .eqIfPresent(IbmsEnvSensorDO::getAreaId, reqVO.getAreaId())
                .eqIfPresent(IbmsEnvSensorDO::getStatus, reqVO.getStatus())
                .orderByDesc(IbmsEnvSensorDO::getId));
    }

    default long selectCountByStatus(Integer status) {
        return selectCount(new LambdaQueryWrapperX<IbmsEnvSensorDO>()
                .eqIfPresent(IbmsEnvSensorDO::getStatus, status));
    }

    default long selectCountByType(Integer sensorType) {
        return selectCount(new LambdaQueryWrapperX<IbmsEnvSensorDO>()
                .eqIfPresent(IbmsEnvSensorDO::getSensorType, sensorType));
    }

}
