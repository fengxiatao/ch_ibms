package cn.iocoder.yudao.module.iot.job;

import cn.iocoder.yudao.framework.quartz.core.handler.JobHandler;
import cn.iocoder.yudao.framework.tenant.core.job.TenantJob;
import cn.iocoder.yudao.module.iot.service.channel.IotDeviceChannelService;
import cn.iocoder.yudao.module.iot.service.channel.SyncResult;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * NVR通道自动同步定时任务
 * 
 * 功能：每天凌晨自动同步所有NVR设备的通道信息
 * 
 * 配置示例（在定时任务管理中配置）：
 * - 任务名称：NVR通道自动同步
 * - Cron表达式：0 0 2 * * ?  （每天凌晨2点执行）
 * - 处理器名称：nvrChannelSyncJob
 * 
 * @author IBMS Team
 */
@Slf4j
@Component
public class NvrChannelSyncJob implements JobHandler {

    @Resource
    private IotDeviceChannelService channelService;

    @Override
    @TenantJob
    public String execute(String param) throws Exception {
        log.info("[NVR通道自动同步] 开始执行...");
        
        long startTime = System.currentTimeMillis();
        
        try {
            // 执行批量同步
            SyncResult result = channelService.batchSyncAllNvrChannels();
            result.setDuration(System.currentTimeMillis() - startTime);
            
            // 记录同步结果
            log.info("[NVR通道自动同步] {}", result.getSummary());
            
            // 如果有错误，记录详细信息
            if (!result.getErrors().isEmpty()) {
                log.warn("[NVR通道自动同步] 同步过程中出现 {} 个错误：", result.getErrors().size());
                result.getErrors().forEach(error -> log.warn("  - {}", error));
            }
            
            return result.getSummary();
            
        } catch (Exception e) {
            log.error("[NVR通道自动同步] 执行失败", e);
            throw e;
        }
    }
}
