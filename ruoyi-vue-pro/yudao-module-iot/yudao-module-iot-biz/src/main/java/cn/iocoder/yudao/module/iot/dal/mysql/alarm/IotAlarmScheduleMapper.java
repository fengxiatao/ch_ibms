package cn.iocoder.yudao.module.iot.dal.mysql.alarm;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.iot.controller.admin.alarm.vo.schedule.IotAlarmSchedulePageReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.alarm.IotAlarmScheduleDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 定时布防任务 Mapper
 *
 * @author 长辉信息科技有限公司
 */
@Mapper
public interface IotAlarmScheduleMapper extends BaseMapperX<IotAlarmScheduleDO> {

    /**
     * 分页查询定时任务
     */
    default PageResult<IotAlarmScheduleDO> selectPage(IotAlarmSchedulePageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<IotAlarmScheduleDO>()
                .eqIfPresent(IotAlarmScheduleDO::getHostId, reqVO.getHostId())
                .likeIfPresent(IotAlarmScheduleDO::getTaskName, reqVO.getTaskName())
                .eqIfPresent(IotAlarmScheduleDO::getArmType, reqVO.getArmType())
                .eqIfPresent(IotAlarmScheduleDO::getStatus, reqVO.getStatus())
                .betweenIfPresent(IotAlarmScheduleDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(IotAlarmScheduleDO::getId));
    }

    /**
     * 根据主机ID查询所有任务
     */
    default List<IotAlarmScheduleDO> selectListByHostId(Long hostId) {
        return selectList(IotAlarmScheduleDO::getHostId, hostId);
    }

    /**
     * 查询所有启用的任务
     */
    default List<IotAlarmScheduleDO> selectEnabledList() {
        return selectList(IotAlarmScheduleDO::getStatus, 1);
    }
}
