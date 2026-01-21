package cn.iocoder.yudao.module.iot.core.mq.message.access;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 门禁设备控制命令
 * 
 * 用于 Biz 层向 Gateway 层发送设备控制命令
 * 
 * @author 长辉信息科技有限公司
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Deprecated
public class AccessControlDeviceCommand implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 请求ID（用于关联请求和响应）
     */
    private String requestId;

    /**
     * 设备ID
     */
    private Long deviceId;

    /**
     * 命令类型
     * - ACTIVATE: 激活设备（登录）
     * - DISCOVER_CHANNELS: 发现通道
     * - OPEN_DOOR: 开门
     * - CLOSE_DOOR: 关门
     * - LOCK: 上锁
     * - UNLOCK: 解锁
     * - QUERY_STATUS: 查询状态
     */
    private String commandType;

    /**
     * 设备IP地址
     */
    private String ipAddress;

    /**
     * 设备端口
     */
    private Integer port;

    /**
     * 登录用户名
     */
    private String username;

    /**
     * 登录密码
     */
    private String password;

    /**
     * 通道号（用于开门、关门等操作）
     */
    private Integer channelNo;

    /**
     * 租户ID
     */
    private Long tenantId;
}