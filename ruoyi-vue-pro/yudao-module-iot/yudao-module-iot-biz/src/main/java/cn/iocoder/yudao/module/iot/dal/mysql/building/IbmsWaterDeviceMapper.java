package cn.iocoder.yudao.module.iot.dal.mysql.building;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.iot.controller.admin.building.vo.bac.IbmsWaterDevicePageReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.building.IbmsWaterDeviceDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 给排水设备 Mapper
 *
 * @author 智慧楼宇系统
 */
@Mapper
public interface IbmsWaterDeviceMapper extends BaseMapperX<IbmsWaterDeviceDO> {

    default PageResult<IbmsWaterDeviceDO> selectPage(IbmsWaterDevicePageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<IbmsWaterDeviceDO>()
                .likeIfPresent(IbmsWaterDeviceDO::getDeviceCode, reqVO.getDeviceCode())
                .likeIfPresent(IbmsWaterDeviceDO::getDeviceName, reqVO.getDeviceName())
                .eqIfPresent(IbmsWaterDeviceDO::getDeviceType, reqVO.getDeviceType())
                .eqIfPresent(IbmsWaterDeviceDO::getAreaId, reqVO.getAreaId())
                .eqIfPresent(IbmsWaterDeviceDO::getStatus, reqVO.getStatus())
                .orderByDesc(IbmsWaterDeviceDO::getId));
    }

    default List<IbmsWaterDeviceDO> selectListByType(Integer deviceType) {
        return selectList(new LambdaQueryWrapperX<IbmsWaterDeviceDO>()
                .eqIfPresent(IbmsWaterDeviceDO::getDeviceType, deviceType)
                .orderByDesc(IbmsWaterDeviceDO::getId));
    }

    default long selectCountByStatus(Integer status) {
        return selectCount(new LambdaQueryWrapperX<IbmsWaterDeviceDO>()
                .eqIfPresent(IbmsWaterDeviceDO::getStatus, status));
    }

    default long selectCountByType(Integer deviceType) {
        return selectCount(new LambdaQueryWrapperX<IbmsWaterDeviceDO>()
                .eqIfPresent(IbmsWaterDeviceDO::getDeviceType, deviceType));
    }

}
