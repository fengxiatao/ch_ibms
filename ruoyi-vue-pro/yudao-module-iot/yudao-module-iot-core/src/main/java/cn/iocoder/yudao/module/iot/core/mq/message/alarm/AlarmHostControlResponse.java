package cn.iocoder.yudao.module.iot.core.mq.message.alarm;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 报警主机控制命令响应消息
 * Gateway执行控制命令后，通过消息总线返回执行结果
 *
 * @author 长辉信息科技有限公司
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlarmHostControlResponse implements Serializable {

    /**
     * 请求ID（与命令的requestId对应）
     */
    private String requestId;

    /**
     * 主机ID
     */
    private Long hostId;

    /**
     * 主机账号
     */
    private String account;

    /**
     * 命令类型
     */
    private String commandType;

    /**
     * 执行是否成功
     */
    private Boolean success;

    /**
     * 错误消息（失败时）
     */
    private String errorMessage;

    /**
     * 设备响应的原始数据
     */
    private String rawResponse;

    /**
     * 响应时间
     */
    private LocalDateTime responseTime;

    /**
     * 租户ID
     */
    private Long tenantId;
}
