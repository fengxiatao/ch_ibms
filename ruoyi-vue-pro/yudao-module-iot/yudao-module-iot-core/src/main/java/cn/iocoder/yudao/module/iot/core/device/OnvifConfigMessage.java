package cn.iocoder.yudao.module.iot.core.device;

/**
 * ONVIF 设备配置消息
 *
 * <p>用于 Biz ↔ Gateway 的配置同步/应用请求与结果透传。</p>
 */
public class OnvifConfigMessage {

    /** 请求ID（用于链路追踪/关联） */
    private String requestId;

    /** 租户ID（用于多租户支持） */
    private Long tenantId;

    private Long deviceId;

    /** 动作：sync_from_device / apply_to_device 等 */
    private String action;

    /** 配置内容（JSON字符串） */
    private String configData;

    /** 结果：true 成功 / false 失败（用于 *_RESULT topic） */
    private Boolean success;

    /** 错误信息（当 success=false 时） */
    private String errorMessage;

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public Long getTenantId() {
        return tenantId;
    }

    public void setTenantId(Long tenantId) {
        this.tenantId = tenantId;
    }

    public Long getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(Long deviceId) {
        this.deviceId = deviceId;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getConfigData() {
        return configData;
    }

    public void setConfigData(String configData) {
        this.configData = configData;
    }

    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}
