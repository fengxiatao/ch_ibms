package cn.iocoder.yudao.module.iot.service.video;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.iot.controller.admin.video.vo.patrolschedule.VideoPatrolSchedulePageReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.video.vo.patrolschedule.VideoPatrolScheduleSaveReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.video.VideoPatrolScheduleDO;
import cn.iocoder.yudao.module.iot.dal.mysql.video.VideoPatrolScheduleMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.time.LocalTime;
import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.iot.enums.ErrorCodeConstants.VIDEO_PATROL_SCHEDULE_NOT_EXISTS;

/**
 * 视频定时轮巡计划 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
@Slf4j
public class VideoPatrolScheduleServiceImpl implements VideoPatrolScheduleService {

    @Resource
    private VideoPatrolScheduleMapper scheduleMapper;

    @Override
    public Long createSchedule(VideoPatrolScheduleSaveReqVO createReqVO) {
        // 插入
        VideoPatrolScheduleDO schedule = VideoPatrolScheduleDO.builder()
                .name(createReqVO.getName())
                .patrolPlanId(createReqVO.getPatrolPlanId())
                .scheduleType(createReqVO.getScheduleType())
                .startTime(createReqVO.getStartTime())
                .endTime(createReqVO.getEndTime())
                .weekDays(createReqVO.getWeekDays())
                .status(createReqVO.getStatus())
                .remark(createReqVO.getRemark())
                .build();
        scheduleMapper.insert(schedule);
        
        log.info("[createSchedule][创建定时轮巡计划成功, id={}]", schedule.getId());
        return schedule.getId();
    }

    @Override
    public void updateSchedule(VideoPatrolScheduleSaveReqVO updateReqVO) {
        // 校验存在
        validateScheduleExists(updateReqVO.getId());
        
        // 更新
        VideoPatrolScheduleDO updateObj = VideoPatrolScheduleDO.builder()
                .id(updateReqVO.getId())
                .name(updateReqVO.getName())
                .patrolPlanId(updateReqVO.getPatrolPlanId())
                .scheduleType(updateReqVO.getScheduleType())
                .startTime(updateReqVO.getStartTime())
                .endTime(updateReqVO.getEndTime())
                .weekDays(updateReqVO.getWeekDays())
                .status(updateReqVO.getStatus())
                .remark(updateReqVO.getRemark())
                .build();
        scheduleMapper.updateById(updateObj);
        
        log.info("[updateSchedule][更新定时轮巡计划成功, id={}]", updateReqVO.getId());
    }

    @Override
    public void deleteSchedule(Long id) {
        // 校验存在
        validateScheduleExists(id);
        
        // 删除
        scheduleMapper.deleteById(id);
        
        log.info("[deleteSchedule][删除定时轮巡计划成功, id={}]", id);
    }

    private void validateScheduleExists(Long id) {
        if (scheduleMapper.selectById(id) == null) {
            throw exception(VIDEO_PATROL_SCHEDULE_NOT_EXISTS);
        }
    }

    @Override
    public VideoPatrolScheduleDO getSchedule(Long id) {
        return scheduleMapper.selectById(id);
    }

    @Override
    public PageResult<VideoPatrolScheduleDO> getSchedulePage(VideoPatrolSchedulePageReqVO pageReqVO) {
        return scheduleMapper.selectPage(pageReqVO);
    }

    @Override
    public void updateScheduleStatus(Long id, Integer status) {
        // 校验存在
        validateScheduleExists(id);
        
        // 更新状态
        VideoPatrolScheduleDO updateObj = VideoPatrolScheduleDO.builder()
                .id(id)
                .status(status)
                .build();
        scheduleMapper.updateById(updateObj);
        
        log.info("[updateScheduleStatus][更新定时轮巡计划状态成功, id={}, status={}]", id, status);
    }

    @Override
    public List<VideoPatrolScheduleDO> getActiveSchedules(LocalTime currentTime, Integer dayOfWeek) {
        log.info("[getActiveSchedules] 查询参数: currentTime={}, dayOfWeek={}", currentTime, dayOfWeek);
        List<VideoPatrolScheduleDO> schedules = scheduleMapper.selectActiveSchedules(currentTime, dayOfWeek);
        log.info("[getActiveSchedules] 查询结果: 找到 {} 个计划", schedules.size());
        if (!schedules.isEmpty()) {
            schedules.forEach(s -> log.info("[getActiveSchedules] 计划详情: id={}, name={}, type={}, startTime={}, endTime={}, weekDays={}, status={}", 
                s.getId(), s.getName(), s.getScheduleType(), s.getStartTime(), s.getEndTime(), s.getWeekDays(), s.getStatus()));
        }
        return schedules;
    }

}
