package cn.iocoder.yudao.module.iot.core.mq.message.access;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 门禁设备控制响应
 * 
 * 用于 Gateway 层向 Biz 层返回命令执行结果
 * 
 * @author 长辉信息科技有限公司
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Deprecated
public class AccessControlDeviceResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 请求ID（关联原始请求）
     */
    private String requestId;

    /**
     * 设备ID
     */
    private Long deviceId;

    /**
     * 命令类型
     */
    private String commandType;

    /**
     * 是否成功
     */
    private Boolean success;

    /**
     * 响应消息
     */
    private String message;

    /**
     * 响应数据（可选）
     * 例如：通道列表、设备状态等
     */
    private Map<String, Object> data;

    /**
     * 响应时间
     */
    private LocalDateTime responseTime;

    /**
     * 租户ID
     */
    private Long tenantId;
}