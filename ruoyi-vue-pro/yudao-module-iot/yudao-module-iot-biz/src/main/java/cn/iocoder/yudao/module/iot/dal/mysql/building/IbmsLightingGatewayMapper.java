package cn.iocoder.yudao.module.iot.dal.mysql.building;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.iot.controller.admin.building.vo.lighting.IbmsLightingDevicePageReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.building.IbmsLightingGatewayDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 照明网关 Mapper
 *
 * @author 智慧楼宇系统
 */
@Mapper
public interface IbmsLightingGatewayMapper extends BaseMapperX<IbmsLightingGatewayDO> {

    default PageResult<IbmsLightingGatewayDO> selectPage(IbmsLightingDevicePageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<IbmsLightingGatewayDO>()
                .likeIfPresent(IbmsLightingGatewayDO::getGatewayCode, reqVO.getDeviceCode())
                .likeIfPresent(IbmsLightingGatewayDO::getGatewayName, reqVO.getDeviceName())
                .eqIfPresent(IbmsLightingGatewayDO::getStatus, reqVO.getStatus())
                .orderByDesc(IbmsLightingGatewayDO::getId));
    }

    default List<IbmsLightingGatewayDO> selectSimpleList() {
        return selectList(new LambdaQueryWrapperX<IbmsLightingGatewayDO>()
                .orderByDesc(IbmsLightingGatewayDO::getId));
    }

}
