package cn.iocoder.yudao.module.iot.dal.mysql.building;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.iot.controller.admin.building.vo.lighting.IbmsLightingDevicePageReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.building.IbmsLightingControllerDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 照明控制器 Mapper
 *
 * @author 智慧楼宇系统
 */
@Mapper
public interface IbmsLightingControllerMapper extends BaseMapperX<IbmsLightingControllerDO> {

    default PageResult<IbmsLightingControllerDO> selectPage(IbmsLightingDevicePageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<IbmsLightingControllerDO>()
                .likeIfPresent(IbmsLightingControllerDO::getControllerCode, reqVO.getDeviceCode())
                .likeIfPresent(IbmsLightingControllerDO::getControllerName, reqVO.getDeviceName())
                .eqIfPresent(IbmsLightingControllerDO::getStatus, reqVO.getStatus())
                .orderByDesc(IbmsLightingControllerDO::getId));
    }

    default List<IbmsLightingControllerDO> selectSimpleList() {
        return selectList(new LambdaQueryWrapperX<IbmsLightingControllerDO>()
                .orderByDesc(IbmsLightingControllerDO::getId));
    }

}
