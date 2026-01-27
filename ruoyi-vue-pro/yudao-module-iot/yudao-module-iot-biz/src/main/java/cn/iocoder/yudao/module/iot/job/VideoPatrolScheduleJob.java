package cn.iocoder.yudao.module.iot.job;

import cn.iocoder.yudao.framework.tenant.core.context.TenantContextHolder;
import cn.iocoder.yudao.framework.tenant.core.util.TenantUtils;
import cn.iocoder.yudao.module.iot.dal.dataobject.video.VideoPatrolScheduleDO;
import cn.iocoder.yudao.module.iot.service.video.VideoPatrolScheduleService;
import cn.iocoder.yudao.module.iot.websocket.IotWebSocketHandler;
import cn.iocoder.yudao.module.iot.websocket.message.IotMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 视频定时轮巡计划定时任务
 *
 * @author 芋道源码
 */
@Component
@Slf4j
public class VideoPatrolScheduleJob {

    @Resource
    private VideoPatrolScheduleService scheduleService;

    @Resource(name = "iotWebSocketHandler")
    private IotWebSocketHandler webSocketHandler;

    /**
     * 记录正在执行的计划ID
     */
    private final Map<Long, Boolean> runningSchedules = new ConcurrentHashMap<>();

    /**
     * 每分钟检查一次定时计划
     */
    @Scheduled(cron = "0 * * * * ?")
    public void checkAndExecuteSchedules() {
        // 使用 TenantUtils.execute 在租户上下文中执行任务
        // TODO: 当前使用默认租户ID = 1，未来如需支持多租户，需要遍历所有租户
        TenantUtils.execute(1L, () -> {
            try {
                // 获取当前时间，并截断到秒级精度（去除纳秒）
                LocalTime now = LocalTime.now().withNano(0);
                int dayOfWeek = LocalDate.now().getDayOfWeek().getValue();

                log.info("[定时轮巡] 检查定时计划，当前时间: {}, 星期: {}", 
                        now, dayOfWeek);

                // 获取当前时间应该执行的计划
                List<VideoPatrolScheduleDO> schedules = scheduleService.getActiveSchedules(now, dayOfWeek);

//                log.info("[定时轮巡] 查询到 {} 个符合条件的计划", schedules.size());
                
                if (schedules.isEmpty()) {
                    log.debug("[定时轮巡] 当前时间段没有需要执行的计划");
                    return;
                }

                for (VideoPatrolScheduleDO schedule : schedules) {
                    processSchedule(schedule, now);
                }

            } catch (Exception e) {
                log.error("[定时轮巡] 检查定时计划异常", e);
            }
        });
    }

    /**
     * 处理单个定时计划
     */
    private void processSchedule(VideoPatrolScheduleDO schedule, LocalTime now) {
        Long scheduleId = schedule.getId();
        LocalTime startTime = schedule.getStartTime();
        LocalTime endTime = schedule.getEndTime();

        // 判断是否在执行时间范围内
        boolean inTimeRange = isInTimeRange(now, startTime, endTime);

        if (inTimeRange) {
            // 应该执行
            if (!runningSchedules.containsKey(scheduleId)) {
                // 首次进入时间范围，启动轮巡
                startPatrol(schedule);
                runningSchedules.put(scheduleId, true);
            }
        } else {
            // 不在时间范围内
            if (runningSchedules.containsKey(scheduleId)) {
                // 之前在执行，现在应该停止
                stopPatrol(schedule);
                runningSchedules.remove(scheduleId);
            }
        }
    }

    /**
     * 判断当前时间是否在时间范围内
     */
    private boolean isInTimeRange(LocalTime now, LocalTime startTime, LocalTime endTime) {
        if (startTime.isBefore(endTime)) {
            // 正常情况：08:00-18:00
            return !now.isBefore(startTime) && !now.isAfter(endTime);
        } else {
            // 跨天情况：23:00-01:00
            return !now.isBefore(startTime) || !now.isAfter(endTime);
        }
    }

    /**
     * 启动轮巡
     */
    private void startPatrol(VideoPatrolScheduleDO schedule) {
        log.info("[定时轮巡] 启动轮巡计划: {}", schedule.getName());

        try {
            // 构建WebSocket消息数据
            Map<String, Object> data = new HashMap<>();
            data.put("scheduleId", schedule.getId());
            data.put("scheduleName", schedule.getName());
            data.put("patrolPlanId", schedule.getPatrolPlanId());

            // 创建IoT消息并广播
            IotMessage message = new IotMessage();
            message.setType("START_PATROL");
            message.setData(data);
            
            webSocketHandler.broadcast(message);

            log.info("[定时轮巡] 已发送启动消息: scheduleId={}, patrolPlanId={}", 
                    schedule.getId(), schedule.getPatrolPlanId());

        } catch (Exception e) {
            log.error("[定时轮巡] 启动轮巡失败: {}", schedule.getName(), e);
        }
    }

    /**
     * 停止轮巡
     */
    private void stopPatrol(VideoPatrolScheduleDO schedule) {
        log.info("[定时轮巡] 停止轮巡计划: {}", schedule.getName());

        try {
            // 构建WebSocket消息数据
            Map<String, Object> data = new HashMap<>();
            data.put("scheduleId", schedule.getId());
            data.put("scheduleName", schedule.getName());

            // 创建IoT消息并广播
            IotMessage message = new IotMessage();
            message.setType("STOP_PATROL");
            message.setData(data);
            
            webSocketHandler.broadcast(message);

            log.info("[定时轮巡] 已发送停止消息: scheduleId={}", schedule.getId());

        } catch (Exception e) {
            log.error("[定时轮巡] 停止轮巡失败: {}", schedule.getName(), e);
        }
    }

}
