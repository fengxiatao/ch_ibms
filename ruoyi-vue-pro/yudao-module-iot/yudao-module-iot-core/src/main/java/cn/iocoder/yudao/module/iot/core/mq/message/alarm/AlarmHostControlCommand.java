package cn.iocoder.yudao.module.iot.core.mq.message.alarm;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 报警主机控制命令消息
 *
 * @author 长辉信息科技有限公司
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlarmHostControlCommand implements Serializable {

    /**
     * 主机ID
     */
    private Long hostId;

    /**
     * 主机账号（用于Gateway识别设备）
     */
    private String account;

    /**
     * 命令类型
     * ARM_ALL - 全部布防
     * ARM_EMERGENCY - 紧急布防
     * DISARM - 撤防
     * CLEAR_ALARM - 消警
     * QUERY_STATUS - 查询状态
     */
    private String commandType;

    /**
     * 分区号（可选，用于分区级别的控制）
     */
    private Integer partitionNo;

    /**
     * 防区号（可选，用于防区级别的控制）
     */
    private Integer zoneNo;

    /**
     * 主机密码（用于执行控制命令）
     */
    private String password;

    /**
     * 租户ID
     */
    private Long tenantId;

    /**
     * 请求ID（用于追踪请求响应）
     */
    private String requestId;
}
