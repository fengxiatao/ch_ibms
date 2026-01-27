package cn.iocoder.yudao.module.iot.newgateway.core.executor;

import cn.iocoder.yudao.module.iot.core.messagebus.core.IotMessageBus;
import cn.iocoder.yudao.module.iot.core.messagebus.topics.IotMessageTopics;
import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.newgateway.core.handler.DeviceHandler;
import cn.iocoder.yudao.module.iot.newgateway.core.model.CommandResult;
import cn.iocoder.yudao.module.iot.newgateway.core.model.DeviceCommand;
import lombok.extern.slf4j.Slf4j;
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
     * 显式构造函数，确保 @Qualifier 正确生效
     * <p>
     * 注意：Lombok 的 @RequiredArgsConstructor 不会复制字段上的 @Qualifier 注解，
     * 所以必须使用显式构造函数来注入带有 Qualifier 的 Bean。
     * </p>
     */
    public DeviceCommandExecutorService(
            @Qualifier("sdkCommandExecutor") Executor sdkCommandExecutor,
            @Qualifier("deviceCommandExecutor") Executor deviceCommandExecutor,
            IotMessageBus messageBus) {
        this.sdkCommandExecutor = sdkCommandExecutor;
        this.deviceCommandExecutor = deviceCommandExecutor;
        this.messageBus = messageBus;
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
            publishCommandResult(requestId, deviceId, method, params, finalResult);
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
    private void publishCommandResult(String requestId, Long deviceId, String method,
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
