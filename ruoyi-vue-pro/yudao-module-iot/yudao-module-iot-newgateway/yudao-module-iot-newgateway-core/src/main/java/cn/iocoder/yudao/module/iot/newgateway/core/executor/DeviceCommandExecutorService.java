package cn.iocoder.yudao.module.iot.newgateway.core.executor;

import cn.iocoder.yudao.module.iot.core.messagebus.core.IotMessageBus;
import cn.iocoder.yudao.module.iot.core.messagebus.topics.IotMessageTopics;
import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.newgateway.core.handler.DeviceHandler;
import cn.iocoder.yudao.module.iot.newgateway.core.model.CommandResult;
import cn.iocoder.yudao.module.iot.newgateway.core.model.DeviceCommand;
import cn.iocoder.yudao.module.iot.newgateway.core.registry.DevicePluginRegistry;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import static cn.iocoder.yudao.framework.common.exception.enums.GlobalErrorCodeConstants.INTERNAL_SERVER_ERROR;
import static cn.iocoder.yudao.framework.common.exception.enums.GlobalErrorCodeConstants.SUCCESS;

/**
 * 设备命令执行服务
 * <p>
 * 负责异步执行设备命令，根据设备类型选择合适的线程池，
 * 实现 SDK 类设备与 TCP 类设备的线程隔离。
 * </p>
 *
 * <p>执行策略：</p>
 * <ul>
 *     <li>SDK 设备（门禁、NVR）：使用 sdkCommandExecutor 线程池</li>
 *     <li>TCP 设备（报警、长辉）：使用 deviceCommandExecutor 线程池</li>
 * </ul>
 *
 * <p>设计原则：</p>
 * <ul>
 *     <li>异步非阻塞：命令提交后立即返回</li>
 *     <li>线程隔离：SDK 阻塞不影响 TCP 命令</li>
 *     <li>结果回调：命令完成后自动发布结果</li>
 * </ul>
 *
 * @author IoT Gateway Team
 */
@Service
@Slf4j
public class DeviceCommandExecutorService {

    /**
     * SDK 类设备类型集合
     * <p>
     * 这些设备使用原生 SDK 调用，可能同步阻塞 5-10 秒。
     * </p>
     */
    private static final Set<String> SDK_DEVICE_TYPES = Set.of(
            "ACCESS_GEN1",  // 门禁一代
            "ACCESS_GEN2",  // 门禁二代
            "NVR"           // NVR 设备
    );

    /**
     * SDK 命令执行线程池
     */
    private final Executor sdkCommandExecutor;

    /**
     * 通用命令执行线程池
     */
    private final Executor deviceCommandExecutor;

    /**
     * 消息总线
     */
    private final IotMessageBus messageBus;

    /**
     * 可选指标（Actuator / Micrometer）
     */
    private final MeterRegistry meterRegistry;

    private final DevicePluginRegistry pluginRegistry;

    /**
     * 显式构造函数，确保 @Qualifier 正确生效
     * <p>
     * 注意：Lombok 的 @RequiredArgsConstructor 不会复制字段上的 @Qualifier 注解，
     * 所以必须使用显式构造函数来注入带有 Qualifier 的 Bean。
     * </p>
     */
    public DeviceCommandExecutorService(
            @Qualifier("sdkCommandExecutor") Executor sdkCommandExecutor,
            @Qualifier("deviceCommandExecutor") Executor deviceCommandExecutor,
            IotMessageBus messageBus,
            DevicePluginRegistry pluginRegistry,
            @Autowired(required = false) MeterRegistry meterRegistry) {
        this.sdkCommandExecutor = sdkCommandExecutor;
        this.deviceCommandExecutor = deviceCommandExecutor;
        this.messageBus = messageBus;
        this.pluginRegistry = pluginRegistry;
        this.meterRegistry = meterRegistry;
        log.info("[DeviceCommandExecutorService] 初始化完成，SDK线程池: {}, 设备线程池: {}",
                sdkCommandExecutor.getClass().getSimpleName(),
                deviceCommandExecutor.getClass().getSimpleName());
    }

    /**
     * 异步执行设备命令
     * <p>
     * 根据设备类型选择合适的线程池执行命令，命令完成后自动发布结果。
     * </p>
     *
     * @param handler    设备处理器
     * @param deviceId   设备ID
     * @param tenantId   租户ID（v24 新增），从请求透传到 reply，避免下游 NPE
     * @param deviceType 设备类型
     * @param command    设备命令
     * @param requestId  请求ID
     * @param method     原始方法
     * @param params     原始参数
     * @return CompletableFuture，可用于等待或链式处理
     */
    public CompletableFuture<CommandResult> executeAsync(
            DeviceHandler handler,
            Long deviceId,
            Long tenantId,
            String deviceType,
            DeviceCommand command,
            String requestId,
            String method,
            Map<String, Object> params) {

        // 选择合适的线程池
        Executor executor = selectExecutor(deviceType);

        log.debug("[CommandExecutor] 提交异步命令: deviceId={}, deviceType={}, commandType={}, executor={}",
                deviceId, deviceType, command.getCommandType(),
                isSdkDeviceType(deviceType) ? "sdk-cmd" : "dev-cmd");

        // 异步执行
        return CompletableFuture.supplyAsync(() -> {
            try {
                log.debug("[CommandExecutor] 开始执行命令: deviceId={}, commandType={}",
                        deviceId, command.getCommandType());

                // 执行命令
                CommandResult result = handler.executeCommand(deviceId, command);

                log.debug("[CommandExecutor] 命令执行完成: deviceId={}, success={}",
                        deviceId, result.isSuccess());

                return result;
            } catch (Exception e) {
                log.error("[CommandExecutor] 命令执行异常: deviceId={}, commandType={}",
                        deviceId, command.getCommandType(), e);
                return CommandResult.failure("命令执行异常: " + e.getMessage());
            }
        }, executor).whenComplete((result, throwable) -> {
            // 发布命令结果
            CommandResult finalResult = result;
            if (throwable != null) {
                log.error("[CommandExecutor] 异步执行异常: deviceId={}", deviceId, throwable);
                finalResult = CommandResult.failure("异步执行异常: " + throwable.getMessage());
            }
            recordCommandMetric(deviceType, command.getCommandType(), finalResult, params);
            publishCommandResult(requestId, deviceId, tenantId, method, params, finalResult);
        });
    }

    /**
     * 同步执行设备命令（用于需要等待结果的场景）
     *
     * @param handler    设备处理器
     * @param deviceId   设备ID
     * @param deviceType 设备类型
     * @param command    设备命令
     * @return 命令执行结果
     */
    public CommandResult executeSync(
            DeviceHandler handler,
            Long deviceId,
            String deviceType,
            DeviceCommand command) {

        try {
            log.debug("[CommandExecutor] 同步执行命令: deviceId={}, deviceType={}, commandType={}",
                    deviceId, deviceType, command.getCommandType());

            return handler.executeCommand(deviceId, command);
        } catch (Exception e) {
            log.error("[CommandExecutor] 同步执行异常: deviceId={}, commandType={}",
                    deviceId, command.getCommandType(), e);
            return CommandResult.failure("命令执行异常: " + e.getMessage());
        }
    }

    /**
     * 选择执行线程池
     *
     * @param deviceType 设备类型
     * @return 执行线程池
     */
    private Executor selectExecutor(String deviceType) {
        if (isSdkDeviceType(deviceType)) {
            return sdkCommandExecutor;
        }
        return deviceCommandExecutor;
    }

    /**
     * 判断是否为 SDK 类设备
     *
     * @param deviceType 设备类型
     * @return true=SDK 设备，false=TCP 设备
     */
    public boolean isSdkDeviceType(String deviceType) {
        return deviceType != null && SDK_DEVICE_TYPES.contains(deviceType.toUpperCase());
    }

    /**
     * 发布命令执行结果
     */
    private void recordCommandMetric(String deviceType, String commandType, CommandResult result,
                                     Map<String, Object> params) {
        if (meterRegistry == null) {
            return;
        }
        String dt = metricTag(deviceType);
        String ct = metricTag(commandType);
        String ok = result != null && result.isSuccess() ? "success" : "failure";
        String pluginId = metricTag(pluginRegistry.resolvePluginIdForDeviceType(deviceType));
        String defaultVendor = metricTag(pluginRegistry.resolveAnnotationVendorForDeviceType(deviceType));
        String vendor = resolveVendorFromParams(params, defaultVendor);
        meterRegistry.counter("gateway.command.completed",
                "deviceType", dt,
                "commandType", ct,
                "result", ok,
                "pluginId", pluginId,
                "vendor", vendor).increment();
    }

    private static String resolveVendorFromParams(Map<String, Object> params, String defaultVendor) {
        if (params != null) {
            Object v = firstNonBlankParam(params.get("brand"), params.get("vendorKey"), params.get("manufacturer"));
            if (v != null) {
                return metricTag(v.toString());
            }
        }
        return defaultVendor;
    }

    private static Object firstNonBlankParam(Object... vals) {
        for (Object v : vals) {
            if (v != null && !v.toString().isBlank()) {
                return v;
            }
        }
        return null;
    }

    /** 限制标签基数与非法空白，避免 Micrometer/Prometheus 问题 */
    private static String metricTag(String raw) {
        if (raw == null || raw.isBlank()) {
            return "unknown";
        }
        String t = raw.trim();
        if (t.length() > 64) {
            t = t.substring(0, 64);
        }
        StringBuilder sb = new StringBuilder(t.length());
        for (int i = 0; i < t.length(); i++) {
            char c = t.charAt(i);
            sb.append(Character.isWhitespace(c) ? '_' : c);
        }
        return sb.toString();
    }

    /**
     * 发布命令执行结果。
     *
     * <p>v24 修复：增加 {@code tenantId} 参数，从请求透传到 reply 消息，
     * 避免 reply 路径下游（如 {@code IotHttpDataSinkAction}）因 {@code tenantId == null} 抛 NPE。</p>
     */
    private void publishCommandResult(String requestId, Long deviceId, Long tenantId, String method,
                                      Map<String, Object> params, CommandResult result) {
        try {
            // 统一补齐 code/msg，避免下游判空导致结果丢失
            int code = result != null && result.isSuccess() ? SUCCESS.getCode() : INTERNAL_SERVER_ERROR.getCode();
            String msg = result != null ? result.getMessage() : null;
            if (msg == null || msg.isEmpty()) {
                msg = result != null && result.isSuccess() ? SUCCESS.getMsg() : INTERNAL_SERVER_ERROR.getMsg();
            }

            IotDeviceMessage resultMessage = IotDeviceMessage.builder()
                    .requestId(requestId)
                    .deviceId(deviceId)
                    .tenantId(tenantId)
                    .method(method)
                    .params(params)
                    .code(code)
                    .msg(msg)
                    .data(result != null ? result.getData() : null)
                    .build();

            messageBus.post(IotMessageTopics.DEVICE_SERVICE_RESULT, resultMessage);
            
            if (result != null && result.isSuccess()) {
                log.info("[CommandExecutor] ✅ 命令执行成功: deviceId={}, requestId={}", deviceId, requestId);
            } else {
                log.warn("[CommandExecutor] 命令执行失败: deviceId={}, requestId={}, error={}",
                        deviceId, requestId, result != null ? result.getMessage() : "未知错误");
            }
        } catch (Exception e) {
            log.error("[CommandExecutor] 发布命令结果失败: deviceId={}, requestId={}", deviceId, requestId, e);
        }
    }
}
