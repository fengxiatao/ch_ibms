package cn.iocoder.yudao.module.iot.dal.mysql.video;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.iot.controller.admin.video.vo.patrolschedule.VideoPatrolSchedulePageReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.video.VideoPatrolScheduleDO;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalTime;
import java.util.List;

/**
 * 视频定时轮巡计划 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface VideoPatrolScheduleMapper extends BaseMapperX<VideoPatrolScheduleDO> {

    default PageResult<VideoPatrolScheduleDO> selectPage(VideoPatrolSchedulePageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<VideoPatrolScheduleDO>()
                .likeIfPresent(VideoPatrolScheduleDO::getName, reqVO.getName())
                .eqIfPresent(VideoPatrolScheduleDO::getPatrolPlanId, reqVO.getPatrolPlanId())
                .eqIfPresent(VideoPatrolScheduleDO::getScheduleType, reqVO.getScheduleType())
                .eqIfPresent(VideoPatrolScheduleDO::getStatus, reqVO.getStatus())
                .betweenIfPresent(VideoPatrolScheduleDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(VideoPatrolScheduleDO::getId));
    }

    /**
     * 查询启用的定时计划
     *
     * @param currentTime 当前时间
     * @param dayOfWeek 星期几（1-7）
     * @return 定时计划列表
     */
    default List<VideoPatrolScheduleDO> selectActiveSchedules(LocalTime currentTime, Integer dayOfWeek) {
        // 将 LocalTime 转换为字符串格式 HH:mm:ss，确保格式一致
        String timeStr = String.format("%02d:%02d:%02d", 
                currentTime.getHour(), 
                currentTime.getMinute(), 
                currentTime.getSecond());
        
        return selectList(new LambdaQueryWrapperX<VideoPatrolScheduleDO>()
                .eq(VideoPatrolScheduleDO::getStatus, 1) // 启用状态
                .apply("start_time <= '" + timeStr + "'") // 开始时间 <= 当前时间
                .apply("end_time >= '" + timeStr + "'") // 结束时间 >= 当前时间
                .apply("(schedule_type = 1 OR (schedule_type = 2 AND FIND_IN_SET('" + dayOfWeek + "', week_days)))")); // 日计划或周计划
    }

}
