package cn.iocoder.yudao.module.iot.newgateway.core.router;

import cn.iocoder.yudao.module.iot.newgateway.core.handler.ActiveDeviceHandler;
import cn.iocoder.yudao.module.iot.newgateway.core.handler.DeviceHandler;
import cn.iocoder.yudao.module.iot.newgateway.core.model.CommandResult;
import cn.iocoder.yudao.module.iot.newgateway.core.model.DeviceCommand;
import cn.iocoder.yudao.module.iot.newgateway.core.model.DeviceConnectionInfo;
import cn.iocoder.yudao.module.iot.newgateway.core.model.LoginResult;
import cn.iocoder.yudao.module.iot.newgateway.core.registry.DevicePluginRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 设备命令路由器
 * <p>
 * 统一的命令路由入口，根据设备类型将命令路由到对应的插件处理器。
 * 所有消费者（AccessControlCommandConsumer、DeviceCommandConsumer等）
 * 都应该委托给这个路由器来执行命令。
 * </p>
 *
 * <p>设计目标：</p>
 * <ul>
 *     <li>统一命令执行逻辑，避免代码重复</li>
 *     <li>支持即时登录（设备未连接时自动登录）</li>
 *     <li>提供统一的错误处理</li>
 * </ul>
 *
 * @author IoT Gateway Team
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DeviceCommandRouter {

    private static final String LOG_PREFIX = "[DeviceCommandRouter]";

    private final DevicePluginRegistry pluginRegistry;

    /**
     * 路由并执行命令
     *
     * @param deviceType     设备类型
     * @param deviceId       设备ID
     * @param command        设备命令
     * @param connectionInfo 连接信息（用于即时登录，可为null）
     * @return 命令执行结果
     */
    public CommandResult routeAndExecute(String deviceType, Long deviceId, 
                                          DeviceCommand command, 
                                          DeviceConnectionInfo connectionInfo) {
        if (deviceType == null || deviceType.isEmpty()) {
            return CommandResult.failure("设备类型不能为空");
        }
        if (deviceId == null) {
            return CommandResult.failure("设备ID不能为空");
        }
        if (command == null) {
            return CommandResult.failure("命令不能为空");
        }

        log.info("{} 路由命令: deviceType={}, deviceId={}, commandType={}",
                LOG_PREFIX, deviceType, deviceId, command.getCommandType());

        // 获取设备处理器
        DeviceHandler handler = pluginRegistry.getHandler(deviceType);
        if (handler == null) {
            log.error("{} 未找到设备处理器: deviceType={}", LOG_PREFIX, deviceType);
            return CommandResult.failure("未找到设备处理器: " + deviceType);
        }

        // 如果是主动连接设备，检查是否需要即时登录
        if (handler instanceof ActiveDeviceHandler && connectionInfo != null) {
            ActiveDeviceHandler activeHandler = (ActiveDeviceHandler) handler;
            var status = activeHandler.queryStatus(deviceId);
            
            if (status == null || !status.isOnline()) {
                log.info("{} 设备未连接，尝试即时登录: deviceId={}", LOG_PREFIX, deviceId);
                LoginResult loginResult = activeHandler.login(connectionInfo);
                if (!loginResult.isSuccess()) {
                    return CommandResult.failure("设备登录失败: " + loginResult.getErrorMessage());
                }
            }
        }

        // 执行命令
        try {
            return handler.executeCommand(deviceId, command);
        } catch (Exception e) {
            log.error("{} 命令执行异常: deviceId={}, commandType={}",
                    LOG_PREFIX, deviceId, command.getCommandType(), e);
            return CommandResult.failure("命令执行异常: " + e.getMessage());
        }
    }

    /**
     * 执行设备登录
     */
    public LoginResult login(String deviceType, DeviceConnectionInfo connectionInfo) {
        if (deviceType == null || deviceType.isEmpty()) {
            return LoginResult.failure("设备类型不能为空");
        }

        DeviceHandler handler = pluginRegistry.getHandler(deviceType);
        if (handler == null) {
            return LoginResult.failure("未找到设备处理器: " + deviceType);
        }

        if (!(handler instanceof ActiveDeviceHandler)) {
            return LoginResult.failure("设备处理器不支持主动连接");
        }

        return ((ActiveDeviceHandler) handler).login(connectionInfo);
    }

    /**
     * 执行设备登出
     */
    public void logout(String deviceType, Long deviceId) {
        if (deviceType == null || deviceId == null) {
            return;
        }

        DeviceHandler handler = pluginRegistry.getHandler(deviceType);
        if (handler instanceof ActiveDeviceHandler) {
            ((ActiveDeviceHandler) handler).logout(deviceId);
        }
    }

    /**
     * 查询设备状态
     */
    public boolean isDeviceOnline(String deviceType, Long deviceId) {
        if (deviceType == null || deviceId == null) {
            return false;
        }

        DeviceHandler handler = pluginRegistry.getHandler(deviceType);
        if (handler == null) {
            return false;
        }

        var status = handler.queryStatus(deviceId);
        return status != null && status.isOnline();
    }

    /**
     * 构建设备命令
     */
    public static DeviceCommand buildCommand(String commandType, Map<String, Object> params) {
        return DeviceCommand.builder()
                .commandType(commandType)
                .params(params != null ? params : Map.of())
                .build();
    }
}
