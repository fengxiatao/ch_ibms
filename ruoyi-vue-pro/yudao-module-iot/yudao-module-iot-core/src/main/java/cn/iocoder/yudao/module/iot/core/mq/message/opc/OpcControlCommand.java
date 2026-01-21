package cn.iocoder.yudao.module.iot.core.mq.message.opc;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * OPC 控制命令消息
 * <p>
 * 用于从 Biz 层向 Gateway 层发送控制命令（C 消息）
 * 
 * @author 长辉信息科技有限公司
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OpcControlCommand implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 请求ID（用于链路追踪/关联）
     */
    private String requestId;

    /**
     * 租户ID
     */
    private Long tenantId;

    /**
     * 主机账号
     */
    private Integer account;

    /**
     * 命令代码
     * 0 - 查询状态
     * 1 - 撤防
     * 2 - 布防
     */
    private Integer command;

    /**
     * 防区号（0表示全局）
     */
    private Integer area;

    /**
     * 密码
     */
    private String password;

    /**
     * 序列号
     */
    private Integer sequence;

    /**
     * 设备ID（用于日志）
     */
    private Long deviceId;

    /**
     * 设备名称（用于日志）
     */
    private String deviceName;
}
