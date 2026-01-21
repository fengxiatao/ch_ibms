package cn.iocoder.yudao.module.iot.job.unified.executor;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.module.iot.core.enums.IotDeviceStateEnum;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceDO;
import cn.iocoder.yudao.module.iot.job.unified.model.ScheduledTask;
import cn.iocoder.yudao.module.iot.service.device.IotDeviceService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 离线检查执行器
 * 
 * <p>负责执行设备离线检查任务
 * 
 * @author IBMS Team
 */
@Component
@Slf4j
public class OfflineCheckExecutor implements JobExecutor {
    
    @Resource
    private IotDeviceService deviceService;
    
    @Override
    public String getJobType() {
        return "offlineCheck";
    }
    
    @Override
    public String execute(ScheduledTask task) throws Exception {
        log.info("执行离线检查: entityType={}, entityId={}", 
                task.getEntityType(), task.getEntityId());
        
        int totalDevices = 0;
        int offlineDevices = 0;
        
        if ("PRODUCT".equals(task.getEntityType())) {
            // 产品级：检查该产品下所有设备
            List<IotDeviceDO> devices = deviceService.getDeviceListByProductId(task.getEntityId());
            
            if (CollUtil.isEmpty(devices)) {
                return "该产品下无设备";
            }
            
            for (IotDeviceDO device : devices) {
                totalDevices++;
                
                // 判断设备是否离线
                if (IotDeviceStateEnum.isOffline(device.getState())) {
                    offlineDevices++;
                    log.warn("检测到离线设备: id={}, name={}, state={}", 
                            device.getId(), device.getDeviceName(), device.getState());
                    
                    // 如果配置了通知，发送离线通知
                    if (Boolean.TRUE.equals(task.getConfig().getNotifyOnOffline())) {
                        notifyDeviceOffline(device);
                    }
                }
            }
            
        } else if ("DEVICE".equals(task.getEntityType())) {
            // 设备级：只检查该设备
            IotDeviceDO device = deviceService.getDevice(task.getEntityId());
            
            if (device == null) {
                return "设备不存在";
            }
            
            totalDevices = 1;
            
            if (IotDeviceStateEnum.isOffline(device.getState())) {
                offlineDevices = 1;
                log.warn("检测到离线设备: id={}, name={}", device.getId(), device.getDeviceName());
                
                if (Boolean.TRUE.equals(task.getConfig().getNotifyOnOffline())) {
                    notifyDeviceOffline(device);
                }
            }
        }
        
        return String.format("检查完成: 总设备数=%d, 离线设备数=%d", totalDevices, offlineDevices);
    }
    
    /**
     * 发送设备离线通知
     */
    private void notifyDeviceOffline(IotDeviceDO device) {
        // TODO: 实现通知逻辑（短信、邮件、站内信等）
        log.info("发送离线通知: 设备[{}]已离线", device.getDeviceName());
    }
}

