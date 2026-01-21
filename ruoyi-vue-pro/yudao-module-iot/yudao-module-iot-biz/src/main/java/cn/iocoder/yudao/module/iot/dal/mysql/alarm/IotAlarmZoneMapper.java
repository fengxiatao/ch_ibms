package cn.iocoder.yudao.module.iot.dal.mysql.alarm;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.iot.controller.admin.alarm.vo.zone.IotAlarmZonePageReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.alarm.IotAlarmZoneDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 防区 Mapper
 *
 * @author 长辉信息科技有限公司
 */
@Mapper
public interface IotAlarmZoneMapper extends BaseMapperX<IotAlarmZoneDO> {

    /**
     * 分页查询防区
     */
    default PageResult<IotAlarmZoneDO> selectPage(IotAlarmZonePageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<IotAlarmZoneDO>()
                .eqIfPresent(IotAlarmZoneDO::getHostId, reqVO.getHostId())
                .likeIfPresent(IotAlarmZoneDO::getZoneName, reqVO.getZoneName())
                .eqIfPresent(IotAlarmZoneDO::getZoneType, reqVO.getZoneType())
                .eqIfPresent(IotAlarmZoneDO::getZoneStatus, reqVO.getZoneStatus())
                .eqIfPresent(IotAlarmZoneDO::getOnlineStatus, reqVO.getOnlineStatus())
                .eqIfPresent(IotAlarmZoneDO::getIsImportant, reqVO.getIsImportant())
                .betweenIfPresent(IotAlarmZoneDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(IotAlarmZoneDO::getId));
    }

    /**
     * 根据主机ID查询所有防区
     */
    default List<IotAlarmZoneDO> selectListByHostId(Long hostId) {
        return selectList(IotAlarmZoneDO::getHostId, hostId);
    }

    /**
     * 根据主机ID和防区编号查询
     */
    default IotAlarmZoneDO selectByHostIdAndZoneNo(Long hostId, Integer zoneNo) {
        return selectOne(IotAlarmZoneDO::getHostId, hostId, 
                        IotAlarmZoneDO::getZoneNo, zoneNo);
    }

    /**
     * 统计主机的防区数量
     */
    default Long selectCountByHostId(Long hostId) {
        return selectCount(IotAlarmZoneDO::getHostId, hostId);
    }
}
