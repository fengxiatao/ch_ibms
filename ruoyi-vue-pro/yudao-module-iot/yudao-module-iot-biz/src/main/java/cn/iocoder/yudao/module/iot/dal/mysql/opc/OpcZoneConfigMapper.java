package cn.iocoder.yudao.module.iot.dal.mysql.opc;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.iot.controller.admin.opc.vo.OpcZoneConfigPageReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.opc.OpcZoneConfigDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * OPC 防区配置 Mapper
 *
 * @author 长辉信息科技有限公司
 */
@Mapper
public interface OpcZoneConfigMapper extends BaseMapperX<OpcZoneConfigDO> {

    default PageResult<OpcZoneConfigDO> selectPage(OpcZoneConfigPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<OpcZoneConfigDO>()
                .eqIfPresent(OpcZoneConfigDO::getDeviceId, reqVO.getDeviceId())
                .eqIfPresent(OpcZoneConfigDO::getArea, reqVO.getArea())
                .eqIfPresent(OpcZoneConfigDO::getPoint, reqVO.getPoint())
                .likeIfPresent(OpcZoneConfigDO::getZoneName, reqVO.getZoneName())
                .eqIfPresent(OpcZoneConfigDO::getEnabled, reqVO.getEnabled())
                .orderByAsc(OpcZoneConfigDO::getArea)
                .orderByAsc(OpcZoneConfigDO::getPoint));
    }

    default OpcZoneConfigDO selectByDeviceAndAreaPoint(Long deviceId, Integer area, Integer point) {
        return selectOne(OpcZoneConfigDO::getDeviceId, deviceId,
                OpcZoneConfigDO::getArea, area,
                OpcZoneConfigDO::getPoint, point);
    }
}
