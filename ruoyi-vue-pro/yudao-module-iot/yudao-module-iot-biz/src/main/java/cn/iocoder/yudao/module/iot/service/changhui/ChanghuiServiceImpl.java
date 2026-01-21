package cn.iocoder.yudao.module.iot.service.changhui;

import cn.iocoder.yudao.module.iot.enums.device.ChanghuiDeviceTypeConstants;
import cn.iocoder.yudao.module.iot.mq.producer.DeviceCommandPublisher;
import cn.iocoder.yudao.framework.tenant.core.context.TenantContextHolder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.HashMap;
import java.util.Map;

/**
 * 长辉设备服务实现类
 * 
 * <p>使用统一的 DeviceCommandPublisher 发送设备命令到 DEVICE_SERVICE_INVOKE 主题</p>
 * <p>所有命令都设置 deviceType=CHANGHUI，由 newgateway 的 ChanghuiPlugin 处理</p>
 *
 * @author 长辉信息科技有限公司
 * @see DeviceCommandPublisher
 * @see ChanghuiDeviceTypeConstants
 */
@Service
@Validated
@Slf4j
@RequiredArgsConstructor
public class ChanghuiServiceImpl implements ChanghuiService {

    /**
     * 统一命令发布器
     */
    private final DeviceCommandPublisher deviceCommandPublisher;

    @Override
    public String triggerUpgrade(Long deviceId) {
        // 参数校验
        if (deviceId == null) {
            throw new IllegalArgumentException("deviceId 不能为空");
        }

        log.info("[triggerUpgrade][开始触发设备升级] deviceId={}", deviceId);

        // 构建命令参数
        Map<String, Object> params = buildBaseParams();

        // 使用统一命令发布器发送升级触发命令
        String requestId = deviceCommandPublisher.publishCommand(
                ChanghuiDeviceTypeConstants.CHANGHUI,
                deviceId,
                ChanghuiDeviceTypeConstants.COMMAND_UPGRADE_TRIGGER,
                params
        );

        log.info("[triggerUpgrade][升级触发命令已发送] deviceId={}, requestId={}", deviceId, requestId);
        return requestId;
    }

    @Override
    public String sendUpgradeUrl(Long deviceId, String url) {
        // 参数校验
        if (deviceId == null) {
            throw new IllegalArgumentException("deviceId 不能为空");
        }
        if (url == null || url.isEmpty()) {
            throw new IllegalArgumentException("url 不能为空");
        }

        log.info("[sendUpgradeUrl][开始下发升级URL] deviceId={}, url={}", deviceId, url);

        // 构建命令参数
        Map<String, Object> params = buildBaseParams();
        params.put(ChanghuiDeviceTypeConstants.PARAM_URL, url);

        // 使用统一命令发布器发送升级URL命令
        String requestId = deviceCommandPublisher.publishCommand(
                ChanghuiDeviceTypeConstants.CHANGHUI,
                deviceId,
                ChanghuiDeviceTypeConstants.COMMAND_UPGRADE_URL,
                params
        );

        log.info("[sendUpgradeUrl][升级URL命令已发送] deviceId={}, url={}, requestId={}", 
                deviceId, url, requestId);
        return requestId;
    }

    @Override
    public String queryStatus(Long deviceId) {
        // 参数校验
        if (deviceId == null) {
            throw new IllegalArgumentException("deviceId 不能为空");
        }

        log.info("[queryStatus][开始查询设备状态] deviceId={}", deviceId);

        // 构建命令参数
        Map<String, Object> params = buildBaseParams();

        // 使用统一命令发布器发送查询状态命令
        String requestId = deviceCommandPublisher.publishCommand(
                ChanghuiDeviceTypeConstants.CHANGHUI,
                deviceId,
                ChanghuiDeviceTypeConstants.COMMAND_QUERY_STATUS,
                params
        );

        log.info("[queryStatus][查询状态命令已发送] deviceId={}, requestId={}", deviceId, requestId);
        return requestId;
    }

    @Override
    public String executeUpgrade(Long deviceId, String url) {
        // 参数校验
        if (deviceId == null) {
            throw new IllegalArgumentException("deviceId 不能为空");
        }
        if (url == null || url.isEmpty()) {
            throw new IllegalArgumentException("url 不能为空");
        }

        log.info("[executeUpgrade][开始执行完整升级流程] deviceId={}, url={}", deviceId, url);

        // 1. 先触发升级
        String triggerRequestId = triggerUpgrade(deviceId);
        log.info("[executeUpgrade][升级触发完成] deviceId={}, triggerRequestId={}", 
                deviceId, triggerRequestId);

        // 2. 下发升级URL
        String urlRequestId = sendUpgradeUrl(deviceId, url);
        log.info("[executeUpgrade][升级URL下发完成] deviceId={}, urlRequestId={}", 
                deviceId, urlRequestId);

        // 返回最后一个请求ID（URL下发的请求ID）
        return urlRequestId;
    }

    /**
     * 构建基础参数
     * 
     * @return 包含租户ID的基础参数Map
     */
    private Map<String, Object> buildBaseParams() {
        Map<String, Object> params = new HashMap<>();
        // 添加租户ID
        Long tenantId = TenantContextHolder.getTenantId();
        if (tenantId != null) {
            params.put(ChanghuiDeviceTypeConstants.PARAM_TENANT_ID, tenantId);
        }
        return params;
    }
}
