package cn.iocoder.yudao.module.iot.controller.admin.video;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.iot.controller.admin.video.vo.patrolschedule.VideoPatrolSchedulePageReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.video.vo.patrolschedule.VideoPatrolScheduleRespVO;
import cn.iocoder.yudao.module.iot.controller.admin.video.vo.patrolschedule.VideoPatrolScheduleSaveReqVO;
import cn.iocoder.yudao.module.iot.convert.video.VideoPatrolScheduleConvert;
import cn.iocoder.yudao.module.iot.dal.dataobject.video.VideoPatrolScheduleDO;
import cn.iocoder.yudao.module.iot.service.video.VideoPatrolScheduleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import jakarta.validation.Valid;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

/**
 * 视频定时轮巡计划 Controller
 *
 * @author 芋道源码
 */
@Tag(name = "管理后台 - 视频定时轮巡计划")
@RestController
@RequestMapping("/iot/video-patrol-schedule")
@Validated
public class VideoPatrolScheduleController {

    @Resource
    private VideoPatrolScheduleService scheduleService;

    @PostMapping("/create")
    @Operation(summary = "创建定时轮巡计划")
    @PreAuthorize("@ss.hasPermission('iot:video-patrol-schedule:create')")
    public CommonResult<Long> createSchedule(@Valid @RequestBody VideoPatrolScheduleSaveReqVO createReqVO) {
        return success(scheduleService.createSchedule(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新定时轮巡计划")
    @PreAuthorize("@ss.hasPermission('iot:video-patrol-schedule:update')")
    public CommonResult<Boolean> updateSchedule(@Valid @RequestBody VideoPatrolScheduleSaveReqVO updateReqVO) {
        scheduleService.updateSchedule(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除定时轮巡计划")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('iot:video-patrol-schedule:delete')")
    public CommonResult<Boolean> deleteSchedule(@RequestParam("id") Long id) {
        scheduleService.deleteSchedule(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得定时轮巡计划")
    @Parameter(name = "id", description = "编号", required = true, example = "1")
    @PreAuthorize("@ss.hasPermission('iot:video-patrol-schedule:query')")
    public CommonResult<VideoPatrolScheduleRespVO> getSchedule(@RequestParam("id") Long id) {
        VideoPatrolScheduleDO schedule = scheduleService.getSchedule(id);
        return success(VideoPatrolScheduleConvert.INSTANCE.convert(schedule));
    }

    @GetMapping("/page")
    @Operation(summary = "获得定时轮巡计划分页")
    @PreAuthorize("@ss.hasPermission('iot:video-patrol-schedule:query')")
    public CommonResult<PageResult<VideoPatrolScheduleRespVO>> getSchedulePage(@Valid VideoPatrolSchedulePageReqVO pageReqVO) {
        PageResult<VideoPatrolScheduleDO> pageResult = scheduleService.getSchedulePage(pageReqVO);
        return success(VideoPatrolScheduleConvert.INSTANCE.convertPage(pageResult));
    }

    @PutMapping("/update-status")
    @Operation(summary = "更新定时轮巡计划状态")
    @PreAuthorize("@ss.hasPermission('iot:video-patrol-schedule:update')")
    public CommonResult<Boolean> updateScheduleStatus(@RequestParam("id") Long id,
                                                       @RequestParam("status") Integer status) {
        scheduleService.updateScheduleStatus(id, status);
        return success(true);
    }

}
