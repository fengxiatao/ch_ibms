package cn.iocoder.yudao.module.iot.dal.mysql.alarm;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.iot.dal.dataobject.alarm.IotAlarmEventDO;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 报警主机事件记录 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface IotAlarmEventMapper extends BaseMapperX<IotAlarmEventDO> {

    /**
     * 分页查询事件
     * @param status 处理状态：0-未处理，1-已处理，2-已忽略
     */
    default PageResult<IotAlarmEventDO> selectEventPage(cn.iocoder.yudao.framework.common.pojo.PageParam pageParam,
                                                          Long hostId, String eventType, 
                                                          Integer status, LocalDateTime[] createTime) {
        return selectPage(pageParam, 
                new LambdaQueryWrapperX<IotAlarmEventDO>()
                .eqIfPresent(IotAlarmEventDO::getHostId, hostId)
                .eqIfPresent(IotAlarmEventDO::getEventType, eventType)
                .eqIfPresent(IotAlarmEventDO::getStatus, status)
                .betweenIfPresent(IotAlarmEventDO::getCreateTime, createTime)
                .orderByDesc(IotAlarmEventDO::getCreateTime));
    }

    /**
     * 查询未处理的事件（status=0）
     */
    default List<IotAlarmEventDO> selectUnhandledEvents(Long hostId) {
        return selectList(new LambdaQueryWrapperX<IotAlarmEventDO>()
                .eq(IotAlarmEventDO::getHostId, hostId)
                .eq(IotAlarmEventDO::getStatus, 0)
                .orderByDesc(IotAlarmEventDO::getCreateTime));
    }

    /**
     * 查询指定时间范围内的事件
     */
    default List<IotAlarmEventDO> selectEventsByTimeRange(Long hostId, 
                                                           LocalDateTime startTime, 
                                                           LocalDateTime endTime) {
        return selectList(new LambdaQueryWrapperX<IotAlarmEventDO>()
                .eq(IotAlarmEventDO::getHostId, hostId)
                .between(IotAlarmEventDO::getCreateTime, startTime, endTime)
                .orderByDesc(IotAlarmEventDO::getCreateTime));
    }

    /**
     * 统计未处理事件数量（status=0）
     */
    default Long countUnhandledEvents(Long hostId) {
        return selectCount(new LambdaQueryWrapperX<IotAlarmEventDO>()
                .eq(IotAlarmEventDO::getHostId, hostId)
                .eq(IotAlarmEventDO::getStatus, 0));
    }

    /**
     * 按事件类型统计
     */
    default Long countByEventType(Long hostId, String eventType, LocalDateTime[] createTime) {
        return selectCount(new LambdaQueryWrapperX<IotAlarmEventDO>()
                .eq(IotAlarmEventDO::getHostId, hostId)
                .eq(IotAlarmEventDO::getEventType, eventType)
                .betweenIfPresent(IotAlarmEventDO::getCreateTime, createTime));
    }
}
