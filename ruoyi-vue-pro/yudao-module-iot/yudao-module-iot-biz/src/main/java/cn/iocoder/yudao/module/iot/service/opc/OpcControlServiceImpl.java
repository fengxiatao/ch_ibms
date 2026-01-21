package cn.iocoder.yudao.module.iot.service.opc;

import cn.iocoder.yudao.module.iot.core.messagebus.core.IotMessageBus;
import cn.iocoder.yudao.module.iot.core.messagebus.topics.IotMessageTopics;
import cn.iocoder.yudao.module.iot.core.mq.message.opc.OpcControlCommand;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceDO;
import cn.iocoder.yudao.module.iot.service.device.IotDeviceService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * OPC 控制服务实现
 * 
 * @author 长辉信息科技有限公司
 */
@Service
@Slf4j
public class OpcControlServiceImpl implements OpcControlService {

    @Resource
    private IotDeviceService deviceService;

    @Resource
    private IotMessageBus messageBus;

    /**
     * 序列号生成器
     */
    private final AtomicInteger sequenceGenerator = new AtomicInteger(1);

    @Override
    public Boolean arm(Long deviceId) {
        return sendControlCommand(deviceId, 2, "布防");
    }

    @Override
    public Boolean disarm(Long deviceId) {
        return sendControlCommand(deviceId, 1, "撤防");
    }

    @Override
    public Boolean queryStatus(Long deviceId) {
        return sendControlCommand(deviceId, 0, "查询状态");
    }

    /**
     * 发送控制命令
     */
    private Boolean sendControlCommand(Long deviceId, Integer command, String commandName) {
        try {
            // 1. 查询设备
            IotDeviceDO device = deviceService.getDevice(deviceId);
            if (device == null) {
                log.warn("[sendControlCommand][设备不存在] deviceId={}", deviceId);
                return false;
            }

            // 2. 从 serialNumber 获取 account
            String serialNumber = device.getSerialNumber();
            if (serialNumber == null || serialNumber.isEmpty()) {
                log.warn("[sendControlCommand][设备未配置账号] deviceId={}", deviceId);
                return false;
            }

            Integer account;
            try {
                account = Integer.valueOf(serialNumber);
            } catch (NumberFormatException e) {
                log.warn("[sendControlCommand][设备账号格式错误] deviceId={}, serialNumber={}", 
                        deviceId, serialNumber);
                return false;
            }

            // 3. 生成序列号
            int sequence = sequenceGenerator.getAndIncrement();
            if (sequence > 9999) {
                sequenceGenerator.set(1);
                sequence = 1;
            }

            // 4. 构建控制命令
            OpcControlCommand controlCommand = OpcControlCommand.builder()
                    .requestId(java.util.UUID.randomUUID().toString())
                    .tenantId(device.getTenantId())
                    .account(account)
                    .command(command)
                    .area(0) // 全局操作
                    .password("1234") // TODO: 从配置读取
                    .sequence(sequence)
                    .deviceId(deviceId)
                    .deviceName(device.getDeviceName())
                    .build();

            // 5. 发布到消息总线（由Gateway消费并发送，使用统一通道）
            messageBus.post(IotMessageTopics.DEVICE_SERVICE_INVOKE, controlCommand);

            log.info("[sendControlCommand][发送控制命令] deviceId={}, command={}, account={}, sequence={}", 
                    deviceId, commandName, account, sequence);

            return true;

        } catch (Exception e) {
            log.error("[sendControlCommand][发送控制命令失败] deviceId={}, command={}", 
                    deviceId, commandName, e);
            return false;
        }
    }
}
