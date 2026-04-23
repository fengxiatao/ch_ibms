package cn.iocoder.yudao.module.iot.job.unified.executor;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.module.iot.core.enums.IotDeviceStateEnum;
import cn.iocoder.yudao.module.iot.dal.dataobject.ibms.IbmsDeviceDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.ibms.IbmsDeviceRuntimeDO;
import cn.iocoder.yudao.module.iot.dal.mysql.ibms.IbmsDeviceMapper;
import cn.iocoder.yudao.module.iot.job.unified.model.ScheduledTask;
import cn.iocoder.yudao.module.iot.service.ibms.device.IbmsDeviceRuntimeService;
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
    private IbmsDeviceMapper ibmsDeviceMapper;
    @Resource
    private IbmsDeviceRuntimeService ibmsDeviceRuntimeService;
    
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
            List<IbmsDeviceDO> devices = ibmsDeviceMapper.selectListByIbmsProductId(task.getEntityId());

            if (CollUtil.isEmpty(devices)) {
                return "该产品下无设备";
            }

            for (IbmsDeviceDO device : devices) {
                totalDevices++;
                int state = resolveIbmsDeviceState(device.getId());
                if (IotDeviceStateEnum.isOffline(state)) {
                    offlineDevices++;
                    log.warn("检测到离线设备: id={}, name={}, state={}",
                            device.getId(), device.getName(), state);

                    if (Boolean.TRUE.equals(task.getConfig().getNotifyOnOffline())) {
                        notifyDeviceOffline(device);
                    }
                }
            }

        } else if ("DEVICE".equals(task.getEntityType())) {
            IbmsDeviceDO device = ibmsDeviceMapper.selectById(task.getEntityId());

            if (device == null) {
                return "设备不存在";
            }

            totalDevices = 1;
            int state = resolveIbmsDeviceState(device.getId());

            if (IotDeviceStateEnum.isOffline(state)) {
                offlineDevices = 1;
                log.warn("检测到离线设备: id={}, name={}", device.getId(), device.getName());

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
    private void notifyDeviceOffline(IbmsDeviceDO device) {
        // TODO: 实现通知逻辑（短信、邮件、站内信等）
        log.info("发送离线通知: 设备[{}]已离线", device.getName());
    }

    private int resolveIbmsDeviceState(Long ibmsDeviceId) {
        IbmsDeviceRuntimeDO rt = ibmsDeviceRuntimeService.getByDeviceId(ibmsDeviceId);
        if (rt != null && rt.getState() != null) {
            return rt.getState();
        }
        return IotDeviceStateEnum.INACTIVE.getState();
    }
}

