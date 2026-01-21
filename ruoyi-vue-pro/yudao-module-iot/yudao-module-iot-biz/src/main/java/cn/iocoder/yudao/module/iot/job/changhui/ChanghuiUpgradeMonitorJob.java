package cn.iocoder.yudao.module.iot.job.changhui;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.quartz.core.enums.JobBusinessType;
import cn.iocoder.yudao.framework.quartz.core.enums.JobPriority;
import cn.iocoder.yudao.module.iot.dal.dataobject.changhui.ChanghuiUpgradeTaskDO;
import cn.iocoder.yudao.module.iot.dal.mysql.changhui.ChanghuiUpgradeTaskMapper;
import cn.iocoder.yudao.module.iot.enums.changhui.ChanghuiUpgradeStatusEnum;
import cn.iocoder.yudao.module.iot.framework.job.IotBusinessJobHandler;
import cn.iocoder.yudao.module.iot.service.changhui.upgrade.ChanghuiUpgradeService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 长辉设备升级任务监控 Job
 * 
 * <p>针对4G网络不稳定场景，定期检查升级任务状态：</p>
 * <ul>
 *   <li>检查长时间无进度更新的任务</li>
 *   <li>清理超时的升级任务</li>
 *   <li>记录异常任务日志</li>
 * </ul>
 * 
 * <p>执行频率：建议每 5 分钟执行一次</p>
 * <p>优先级：NORMAL（普通优先级）</p>
 * <p>允许并发：false（不允许并发）</p>
 *
 * @author 长辉信息科技有限公司
 */
@Component
@Slf4j
public class ChanghuiUpgradeMonitorJob extends IotBusinessJobHandler {

    private static final String LOG_PREFIX = "[ChanghuiUpgradeMonitorJob]";

    /**
     * 升级任务无更新超时时间（毫秒）
     * 如果任务进度超过此时间未更新，记录告警日志
     */
    private static final long NO_UPDATE_TIMEOUT_MS = 5 * 60 * 1000; // 5分钟

    /**
     * 升级任务总超时时间（小时）
     */
    private static final int TOTAL_TIMEOUT_HOURS = 24;

    @Resource
    private ChanghuiUpgradeTaskMapper upgradeTaskMapper;

    @Resource
    private ChanghuiUpgradeService changhuiUpgradeService;

    @Override
    public JobBusinessType getBusinessType() {
        return JobBusinessType.IOT_CHANGHUI_UPGRADE_MONITOR;
    }

    @Override
    public int getPriority() {
        return JobPriority.NORMAL;
    }

    @Override
    public boolean isConcurrent() {
        return false;
    }

    @Override
    protected String doExecute(String param) throws Exception {
        int cleanedCount = 0;
        int warningCount = 0;
        int inProgressCount = 0;

        // 1. 查询所有进行中的升级任务
        List<ChanghuiUpgradeTaskDO> inProgressTasks = upgradeTaskMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<ChanghuiUpgradeTaskDO>()
                        .eq(ChanghuiUpgradeTaskDO::getStatus, ChanghuiUpgradeStatusEnum.IN_PROGRESS.getCode())
        );

        if (CollUtil.isNotEmpty(inProgressTasks)) {
            inProgressCount = inProgressTasks.size();
            for (ChanghuiUpgradeTaskDO task : inProgressTasks) {
                try {
                    // 检查任务是否超过总超时时间
                    if (isTaskTotalTimeout(task)) {
                        log.warn("{} 升级任务总超时，标记为失败: taskId={}, stationCode={}, startTime={}", 
                                LOG_PREFIX, task.getId(), task.getStationCode(), task.getStartTime());
                        changhuiUpgradeService.completeUpgrade(task.getId(), false, 
                                "升级任务超时（超过" + TOTAL_TIMEOUT_HOURS + "小时）");
                        cleanedCount++;
                        continue;
                    }

                    // 检查任务是否长时间无进度更新
                    if (isTaskNoUpdate(task)) {
                        log.warn("{} 升级任务长时间无进度更新: taskId={}, stationCode={}, progress={}%, lastUpdate={}", 
                                LOG_PREFIX, task.getId(), task.getStationCode(), 
                                task.getProgress(), task.getUpdateTime());
                        warningCount++;
                        // 不自动失败，只记录告警，可能设备正在下载/烧录
                    }
                } catch (Exception e) {
                    log.error("{} 检查升级任务失败: taskId={}", LOG_PREFIX, task.getId(), e);
                }
            }
        }

        // 2. 清理超时的待执行任务
        int pendingCleanedCount = changhuiUpgradeService.cleanupTimeoutTasks();
        cleanedCount += pendingCleanedCount;

        String result = StrUtil.format("进行中任务: {}, 告警任务: {}, 清理超时任务: {}", 
                inProgressCount, warningCount, cleanedCount);
        log.info("{} 监控完成: {}", LOG_PREFIX, result);
        
        return result;
    }

    /**
     * 检查任务是否超过总超时时间
     */
    private boolean isTaskTotalTimeout(ChanghuiUpgradeTaskDO task) {
        LocalDateTime startTime = task.getStartTime();
        if (startTime == null) {
            startTime = task.getCreateTime();
        }
        if (startTime == null) {
            return false;
        }
        return startTime.plusHours(TOTAL_TIMEOUT_HOURS).isBefore(LocalDateTime.now());
    }

    /**
     * 检查任务是否长时间无进度更新
     */
    private boolean isTaskNoUpdate(ChanghuiUpgradeTaskDO task) {
        LocalDateTime updateTime = task.getUpdateTime();
        if (updateTime == null) {
            updateTime = task.getStartTime();
        }
        if (updateTime == null) {
            return false;
        }
        
        long elapsedMs = java.time.Duration.between(updateTime, LocalDateTime.now()).toMillis();
        return elapsedMs > NO_UPDATE_TIMEOUT_MS;
    }
}
