package cn.iocoder.yudao.module.iot.dal.mysql.alarm;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.iot.controller.admin.alarm.vo.host.IotAlarmHostPageReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.alarm.IotAlarmHostDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 报警主机 Mapper
 *
 * @author 长辉信息科技有限公司
 */
@Mapper
public interface IotAlarmHostMapper extends BaseMapperX<IotAlarmHostDO> {

    /**
     * 分页查询报警主机
     */
    default PageResult<IotAlarmHostDO> selectPage(IotAlarmHostPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<IotAlarmHostDO>()
                .likeIfPresent(IotAlarmHostDO::getHostName, reqVO.getHostName())
                .eqIfPresent(IotAlarmHostDO::getDeviceId, reqVO.getDeviceId())
                .eqIfPresent(IotAlarmHostDO::getOnlineStatus, reqVO.getOnlineStatus())
                .eqIfPresent(IotAlarmHostDO::getArmStatus, reqVO.getArmStatus())
                .eqIfPresent(IotAlarmHostDO::getAlarmStatus, reqVO.getAlarmStatus())
                .betweenIfPresent(IotAlarmHostDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(IotAlarmHostDO::getId));
    }

    /**
     * 根据设备ID查询报警主机列表
     */
    default List<IotAlarmHostDO> selectListByDeviceId(Long deviceId) {
        return selectList(IotAlarmHostDO::getDeviceId, deviceId);
    }

    /**
     * 统计在线主机数量
     */
    default Long selectOnlineCount() {
        return selectCount(IotAlarmHostDO::getOnlineStatus, 1);
    }
}
