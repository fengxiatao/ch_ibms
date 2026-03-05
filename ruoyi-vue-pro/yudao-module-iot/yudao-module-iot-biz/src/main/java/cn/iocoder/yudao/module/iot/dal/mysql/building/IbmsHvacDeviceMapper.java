package cn.iocoder.yudao.module.iot.dal.mysql.building;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.iot.controller.admin.building.vo.bac.IbmsHvacDevicePageReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.building.IbmsHvacDeviceDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 暖通空调设备 Mapper
 *
 * @author 智慧楼宇系统
 */
@Mapper
public interface IbmsHvacDeviceMapper extends BaseMapperX<IbmsHvacDeviceDO> {

    default PageResult<IbmsHvacDeviceDO> selectPage(IbmsHvacDevicePageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<IbmsHvacDeviceDO>()
                .likeIfPresent(IbmsHvacDeviceDO::getDeviceCode, reqVO.getDeviceCode())
                .likeIfPresent(IbmsHvacDeviceDO::getDeviceName, reqVO.getDeviceName())
                .eqIfPresent(IbmsHvacDeviceDO::getDeviceType, reqVO.getDeviceType())
                .eqIfPresent(IbmsHvacDeviceDO::getAreaId, reqVO.getAreaId())
                .eqIfPresent(IbmsHvacDeviceDO::getStatus, reqVO.getStatus())
                .orderByDesc(IbmsHvacDeviceDO::getId));
    }

    default List<IbmsHvacDeviceDO> selectListByType(Integer deviceType) {
        return selectList(new LambdaQueryWrapperX<IbmsHvacDeviceDO>()
                .eqIfPresent(IbmsHvacDeviceDO::getDeviceType, deviceType)
                .orderByDesc(IbmsHvacDeviceDO::getId));
    }

    default long selectCountByStatus(Integer status) {
        return selectCount(new LambdaQueryWrapperX<IbmsHvacDeviceDO>()
                .eqIfPresent(IbmsHvacDeviceDO::getStatus, status));
    }

    default long selectCountByType(Integer deviceType) {
        return selectCount(new LambdaQueryWrapperX<IbmsHvacDeviceDO>()
                .eqIfPresent(IbmsHvacDeviceDO::getDeviceType, deviceType));
    }

    /**
     * 按运行模式统计数量
     */
    default long selectCountByRunMode(Integer runMode) {
        return selectCount(new LambdaQueryWrapperX<IbmsHvacDeviceDO>()
                .eqIfPresent(IbmsHvacDeviceDO::getRunMode, runMode));
    }

}
