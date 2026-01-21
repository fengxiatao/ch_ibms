package cn.iocoder.yudao.module.iot.core.mq.message.alarm;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 报警主机控制响应消息
 * 
 * <p>用于 Gateway → Biz 的报警主机控制命令响应</p>
 * 
 * <p><b>字段类型约束（Requirements 4.3）：</b></p>
 * <ul>
 *   <li>responseType 使用 Integer 常量（参见 {@link ResponseType}）</li>
 * </ul>
 *
 * @author 长辉信息科技有限公司
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlarmControlResponseMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 设备ID
     */
    private Long deviceId;
    
    /**
     * 设备类型
     */
    private String deviceType;
    
    /**
     * 设备账号
     */
    private String account;

    /**
     * 响应类型
     * @see ResponseType
     */
    private Integer responseType;
    
    /**
     * 响应类型名称（用于显示）
     */
    private String responseTypeName;

    /**
     * 是否成功
     */
    private Boolean success;
    
    /**
     * 结果码
     */
    private Integer result;

    /**
     * 原始命令
     */
    private String originalCommand;
    
    /**
     * 序列号
     */
    private String sequence;

    /**
     * 错误码
     */
    private String errorCode;
    
    /**
     * 错误信息
     */
    private String errorMessage;

    /**
     * 事件时间戳（毫秒）
     */
    private Long timestamp;
    
    /**
     * 原始消息
     */
    private String rawMessage;

    /**
     * 租户ID
     */
    private Long tenantId;
    
    /**
     * 响应类型常量
     * <p>Requirements: 4.3 - 使用 Integer 常量而非 String</p>
     */
    public interface ResponseType {
        /** 确认响应 */
        int ACK = 1;
        /** 否定响应 */
        int NAK = 2;
        /** OPC 控制确认 */
        int OPC_CONTROL_ACK = 3;
    }
    
    /**
     * 获取响应类型名称
     * 
     * @return 响应类型名称
     */
    public String getResponseTypeName() {
        if (responseTypeName != null) {
            return responseTypeName;
        }
        if (responseType == null) {
            return null;
        }
        switch (responseType) {
            case ResponseType.ACK: return "ACK";
            case ResponseType.NAK: return "NAK";
            case ResponseType.OPC_CONTROL_ACK: return "OPC_CONTROL_ACK";
            default: return "UNKNOWN";
        }
    }
}
