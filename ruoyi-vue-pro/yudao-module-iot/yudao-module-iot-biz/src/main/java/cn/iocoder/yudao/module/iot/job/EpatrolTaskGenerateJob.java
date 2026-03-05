package cn.iocoder.yudao.module.iot.job;

import cn.iocoder.yudao.framework.tenant.core.util.TenantUtils;
import cn.iocoder.yudao.module.iot.service.epatrol.EpatrolPlanService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;

/**
 * 电子巡更任务自动生成定时任务
 * 
 * 每日凌晨自动根据巡更计划生成当天的巡更任务
 *
 * @author 长辉信息
 */
@Component
@Slf4j
public class EpatrolTaskGenerateJob {

    @Resource
    private EpatrolPlanService planService;

    /**
     * 每天凌晨 00:01 自动生成当天的巡更任务
     */
    @Scheduled(cron = "0 1 0 * * ?")
    public void generateDailyTasks() {
        log.info("[电子巡更] 开始自动生成每日巡更任务");
        
        // 使用 TenantUtils.execute 在租户上下文中执行任务
        // TODO: 当前使用默认租户ID = 1，未来如需支持多租户，需要遍历所有租户
        TenantUtils.execute(1L, () -> {
            try {
                planService.generateDailyTasks();
                log.info("[电子巡更] 每日巡更任务生成完成");
            } catch (Exception e) {
                log.error("[电子巡更] 生成每日巡更任务异常", e);
            }
        });
    }

    /**
     * 每小时检查一次计划状态，更新过期计划
     * 状态定义：0-未开始, 1-执行中, 2-已过期
     */
    @Scheduled(cron = "0 0 * * * ?")
    public void updatePlanStatus() {
        log.info("[电子巡更] 开始更新计划状态");
        
        TenantUtils.execute(1L, () -> {
            try {
                planService.updateAllPlanStatus();
                log.info("[电子巡更] 计划状态更新完成");
            } catch (Exception e) {
                log.error("[电子巡更] 更新计划状态异常", e);
            }
        });
    }

}
