package cn.iocoder.yudao.module.iot.framework.tdengine.config;

import cn.iocoder.yudao.module.iot.service.device.message.IotDeviceMessageService;
import cn.iocoder.yudao.module.iot.service.opc.OpcAlarmRecordService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * IoT 表初始化的 Configuration
 * 
 * 注意：已从 TDEngine 迁移到 MySQL，此Runner现在用于检查MySQL表是否存在
 *
 * @author alwayssuper
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class TDengineTableInitRunner implements ApplicationRunner {

    private final IotDeviceMessageService deviceMessageService;
    private final OpcAlarmRecordService opcAlarmRecordService;

    /**
     * 是否启用表初始化检查，默认 false（禁用）
     * 在 application.yaml 中配置：iot.tdengine.enabled=true 来启用
     * 
     * MySQL版本：表已通过SQL脚本预先创建，此配置用于控制是否进行启动时检查
     */
    @Value("${iot.tdengine.enabled:false}")
    private boolean tdengineEnabled;

    @Override
    public void run(ApplicationArguments args) {
        if (!tdengineEnabled) {
            log.info("[run][IoT表初始化检查已禁用（使用MySQL，表已预先创建）。如需启用检查请配置 iot.tdengine.enabled=true]");
            return;
        }

        // MySQL版本：仅检查表是否存在，不进行创建
        try {
            // 检查设备消息表
            deviceMessageService.defineDeviceMessageStable();
            log.info("[run][MySQL设备消息表检查完成]");
        } catch (Exception ex) {
            log.warn("[run][MySQL设备消息表检查失败，请确保已执行SQL脚本创建表]", ex);
        }

        try {
            // 检查OPC报警记录表
            opcAlarmRecordService.initOpcAlarmStable();
            log.info("[run][MySQL OPC报警记录表检查完成]");
        } catch (Exception ex) {
            log.warn("[run][MySQL OPC报警记录表检查失败，请确保已执行SQL脚本创建表]", ex);
        }
    }

}
