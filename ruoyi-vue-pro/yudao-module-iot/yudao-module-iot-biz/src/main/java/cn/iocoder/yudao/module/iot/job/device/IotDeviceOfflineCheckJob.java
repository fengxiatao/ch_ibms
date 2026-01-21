package cn.iocoder.yudao.module.iot.job.device;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.framework.quartz.core.enums.JobBusinessType;
import cn.iocoder.yudao.framework.quartz.core.enums.JobPriority;
import cn.iocoder.yudao.module.iot.core.enums.IotDeviceStateEnum;
import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceDO;
import cn.iocoder.yudao.module.iot.framework.iot.config.YudaoIotProperties;
import cn.iocoder.yudao.module.iot.framework.job.IotBusinessJobHandler;
import cn.iocoder.yudao.module.iot.service.device.IotDeviceService;
import cn.iocoder.yudao.module.iot.service.device.message.IotDeviceMessageService;
import cn.iocoder.yudao.module.iot.service.device.property.IotDevicePropertyService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * IoT 设备离线检查任务
 *
 * <p>检测逻辑：设备最后一条 {@link IotDeviceMessage} 消息超过一定时间，则认为设备离线
 *
 * <p>执行频率：建议每 5 分钟执行一次
 * <p>优先级：HIGH（高优先级，设备离线检测很重要）
 * <p>冲突策略：SKIP（如果上次检查还在进行，跳过本次）
 *
 * @see <a href="https://help.aliyun.com/zh/iot/support/faq-about-device-status#98f7056b2957y">阿里云 IoT —— 设备离线分析</a>
 * @author 长辉信息科技有限公司
 */
@Component
public class IotDeviceOfflineCheckJob extends IotBusinessJobHandler {

    @Resource
    private YudaoIotProperties iotProperties;

    @Resource
    private IotDeviceService deviceService;
    @Resource
    private IotDevicePropertyService devicePropertyService;
    @Resource
    private IotDeviceMessageService deviceMessageService;

    @Override
    public JobBusinessType getBusinessType() {
        return JobBusinessType.IOT_DEVICE_OFFLINE_CHECK;
    }
    
    @Override
    public int getPriority() {
        return JobPriority.HIGH; // 设备离线检测是高优先级任务
    }
    
    @Override
    public boolean isConcurrent() {
        return false; // 不允许并发执行，避免重复检测
    }

    @Override
    protected String doExecute(String param) {
        // 1.1 获得在线设备列表
        List<IotDeviceDO> devices = deviceService.getDeviceListByState(IotDeviceStateEnum.ONLINE.getState());
        if (CollUtil.isEmpty(devices)) {
            return JsonUtils.toJsonString(Collections.emptyList());
        }
        // 1.2 获取超时的设备集合
        Set<Long> timeoutDeviceIds = devicePropertyService.getDeviceIdListByReportTime(getTimeoutTime());

        // 2. 下线设备
        List<String[]> offlineDevices = CollUtil.newArrayList();
        for (IotDeviceDO device : devices) {
            if (!timeoutDeviceIds.contains(device.getId())) {
                continue;
            }
            offlineDevices.add(new String[]{device.getProductKey(), device.getDeviceName()});
            // 为什么不直接更新状态呢？因为通过 IotDeviceMessage 可以经过一系列的处理，例如说记录日志等等
            deviceMessageService.sendDeviceMessage(IotDeviceMessage.buildStateOffline().setDeviceId(device.getId()));
        }
        return JsonUtils.toJsonString(offlineDevices);
    }

    private LocalDateTime getTimeoutTime() {
        return LocalDateTime.now().minus(Duration.ofNanos(
                (long) (iotProperties.getKeepAliveTime().toNanos() * iotProperties.getKeepAliveFactor())));
    }

}
