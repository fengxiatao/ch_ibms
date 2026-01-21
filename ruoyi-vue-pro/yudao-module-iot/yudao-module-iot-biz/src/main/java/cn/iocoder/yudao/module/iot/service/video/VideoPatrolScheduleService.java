package cn.iocoder.yudao.module.iot.service.video;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.iot.controller.admin.video.vo.patrolschedule.VideoPatrolSchedulePageReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.video.vo.patrolschedule.VideoPatrolScheduleSaveReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.video.VideoPatrolScheduleDO;

import jakarta.validation.Valid;
import java.time.LocalTime;
import java.util.List;

/**
 * 视频定时轮巡计划 Service 接口
 *
 * @author 芋道源码
 */
public interface VideoPatrolScheduleService {

    /**
     * 创建定时轮巡计划
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createSchedule(@Valid VideoPatrolScheduleSaveReqVO createReqVO);

    /**
     * 更新定时轮巡计划
     *
     * @param updateReqVO 更新信息
     */
    void updateSchedule(@Valid VideoPatrolScheduleSaveReqVO updateReqVO);

    /**
     * 删除定时轮巡计划
     *
     * @param id 编号
     */
    void deleteSchedule(Long id);

    /**
     * 获得定时轮巡计划
     *
     * @param id 编号
     * @return 定时轮巡计划
     */
    VideoPatrolScheduleDO getSchedule(Long id);

    /**
     * 获得定时轮巡计划分页
     *
     * @param pageReqVO 分页查询
     * @return 定时轮巡计划分页
     */
    PageResult<VideoPatrolScheduleDO> getSchedulePage(VideoPatrolSchedulePageReqVO pageReqVO);

    /**
     * 更新定时轮巡计划状态
     *
     * @param id 编号
     * @param status 状态
     */
    void updateScheduleStatus(Long id, Integer status);

    /**
     * 获取当前时间应该执行的定时计划
     *
     * @param currentTime 当前时间
     * @param dayOfWeek 星期几（1-7）
     * @return 定时计划列表
     */
    List<VideoPatrolScheduleDO> getActiveSchedules(LocalTime currentTime, Integer dayOfWeek);

}
