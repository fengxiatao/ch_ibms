package cn.iocoder.yudao.module.iot.mq.producer;

import cn.iocoder.yudao.framework.tenant.core.context.TenantContextHolder;
import cn.iocoder.yudao.module.iot.core.messagebus.core.IotMessageBus;
import cn.iocoder.yudao.module.iot.core.messagebus.topics.IotMessageTopics;
import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.mq.support.DeviceCommandBrandEnricher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 设备命令发布器
 * 
 * <p>统一的命令发布入口，所有设备命令通过此组件发送到 Gateway。</p>
 * 
 * <p>设计目标：</p>
 * <ul>
 *   <li>统一命令发送接口，所有设备类型使用相同的方法</li>
 *   <li>自动生成 requestId 用于关联响应</li>
 *   <li>确保消息格式一致性，包含 deviceType 和 commandType 字段</li>
 *   <li>所有命令发送到 DEVICE_SERVICE_INVOKE 主题</li>
 * </ul>
 * 
 * <p>Requirements: 3.1, 3.2, 3.3, 3.4, 3.5</p>
 *
 * @author 长辉信息科技有限公司
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DeviceCommandPublisher {

    /**
     * 消息总线（复用 iot-core）
     */
    private final IotMessageBus messageBus;

    /**
     * params 未带 brand 时按 ibms_device / iot_device.config 补全
     */
    private final DeviceCommandBrandEnricher brandEnricher;

    /**
     * 发布设备命令
     * 
     * <p>所有设备命令通过此方法发送到 DEVICE_SERVICE_INVOKE 主题。</p>
     * <p>消息格式遵循 IotDeviceMessage，params 中包含 deviceType 和 commandType。</p>
     * 
     * @param deviceType  设备类型 (ACCESS_GEN1, ACCESS_GEN2, ALARM, NVR, CHANGHUI)
     * @param deviceId    设备ID
     * @param commandType 命令类型 (OPEN_DOOR, ARM_ALL, PTZ_CONTROL 等)
     * @param params      命令参数（可为 null）
     * @return requestId  请求ID，用于关联响应
     * @throws IllegalArgumentException 如果 deviceType、deviceId 或 commandType 为空
     */
    public String publishCommand(String deviceType, Long deviceId,
                                  String commandType, Map<String, Object> params) {
        return publishCommand(deviceType, deviceId, commandType, params, null);
    }

    /**
     * 发布设备命令（可指定 requestId，用于与 {@link cn.iocoder.yudao.module.iot.mq.manager.DeviceCommandResponseManager} 预先注册一致）
     *
     * <p>tenantId 优先级（高 → 低）：当前 TenantContextHolder → null（由调用方使用下方 6 参重载显式传）</p>
     */
    public String publishCommand(String deviceType, Long deviceId,
                                  String commandType, Map<String, Object> params, String fixedRequestId) {
        return publishCommand(deviceType, deviceId, commandType, params, fixedRequestId, null);
    }

    /**
     * 发布设备命令（可指定 requestId 与 tenantId，v24 新增）
     *
     * <p>当调用方所在线程的 {@link TenantContextHolder} 不可靠（如某些过滤器豁免、@PreAuthorize 切换上下文导致为 null）时，
     * 可通过 explicitTenantId 显式传入设备所属租户（推荐从 ibms_device.tenant_id / iot_device.tenant_id 取）。</p>
     *
     * <p>tenantId 优先级（高 → 低）：explicitTenantId → TenantContextHolder.getTenantId() → null</p>
     *
     * @param explicitTenantId 显式租户 ID；若为 null，则回退到 TenantContextHolder
     */
    public String publishCommand(String deviceType, Long deviceId,
                                  String commandType, Map<String, Object> params,
                                  String fixedRequestId, Long explicitTenantId) {
        // 参数校验
        if (deviceType == null || deviceType.isEmpty()) {
            throw new IllegalArgumentException("deviceType 不能为空");
        }
        if (deviceId == null) {
            throw new IllegalArgumentException("deviceId 不能为空");
        }
        if (commandType == null || commandType.isEmpty()) {
            throw new IllegalArgumentException("commandType 不能为空");
        }

        String requestId = (fixedRequestId != null && !fixedRequestId.isEmpty())
                ? fixedRequestId
                : UUID.randomUUID().toString();

        // 构建消息参数，确保包含 deviceType 和 commandType
        Map<String, Object> messageParams = buildParams(deviceType, commandType, params);
        brandEnricher.enrichIfAbsent(deviceId, messageParams);

        // 构建 IotDeviceMessage
        IotDeviceMessage message = IotDeviceMessage.requestOf(requestId, commandType, messageParams);
        message.setDeviceId(deviceId);
        message.setMessageVersion(1);
        // v24 修复：tenantId 优先用显式传入值，回退到 TenantContextHolder
        // 避免 reply 路径下游 (IotDataRuleServiceImpl/IotHttpDataSinkAction) 因 tenantId == null 抛 NPE
        Long resolvedTenantId = explicitTenantId != null
                ? explicitTenantId
                : TenantContextHolder.getTenantId();
        message.setTenantId(resolvedTenantId);
        if (messageParams != null) {
            Object brand = messageParams.get("brand");
            if (brand != null) {
                message.setBrand(brand.toString());
            }
            Object vk = messageParams.get("vendorKey");
            if (vk != null) {
                message.setVendorKey(vk.toString());
            }
            Object pid = messageParams.get("pluginId");
            if (pid != null) {
                message.setPluginId(pid.toString());
            }
        }

        // 发送到统一主题 DEVICE_SERVICE_INVOKE
        try {
            messageBus.post(IotMessageTopics.DEVICE_SERVICE_INVOKE, message);
            
            log.info("[DeviceCommandPublisher] 命令已发布: deviceType={}, deviceId={}, commandType={}, requestId={}",
                    deviceType, deviceId, commandType, requestId);
        } catch (Exception e) {
            log.error("[DeviceCommandPublisher] 发布命令失败: deviceType={}, deviceId={}, commandType={}, requestId={}, error={}",
                    deviceType, deviceId, commandType, requestId, e.getMessage(), e);
            throw e;
        }

        return requestId;
    }

    /**
     * 发布设备命令（无额外参数）
     * 
     * @param deviceType  设备类型
     * @param deviceId    设备ID
     * @param commandType 命令类型
     * @return requestId  请求ID
     */
    public String publishCommand(String deviceType, Long deviceId, String commandType) {
        return publishCommand(deviceType, deviceId, commandType, null);
    }

    /**
     * 构建消息参数
     * 
     * <p>确保 params 中包含 deviceType 和 commandType 字段。</p>
     * 
     * @param deviceType  设备类型
     * @param commandType 命令类型
     * @param params      原始参数
     * @return 包含 deviceType 和 commandType 的参数 Map
     */
    Map<String, Object> buildParams(String deviceType, String commandType, 
                                            Map<String, Object> params) {
        Map<String, Object> result = new HashMap<>();
        
        // 复制原始参数
        if (params != null) {
            result.putAll(params);
        }
        
        // 添加 deviceType 和 commandType（覆盖原有值）
        result.put("deviceType", deviceType);
        result.put("commandType", commandType);
        
        return result;
    }

    /**
     * 获取消息总线（仅用于测试）
     * 
     * @return 消息总线实例
     */
    IotMessageBus getMessageBus() {
        return messageBus;
    }
}
