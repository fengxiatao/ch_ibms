package cn.iocoder.yudao.module.iot.core.mq.message.opc;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * OPC报警事件消息
 * <p>
 * 用于在消息总线中传递IP9500报警主机的事件数据
 *
 * @author 长辉信息科技有限公司
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OpcAlarmEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 事件ID（幂等键）
     * <p>推荐使用 account + sequence 组合；若为空，则由消息总线 Envelope 回退生成</p>
     */
    private String eventId;

    /**
     * 事件发生时间（epoch milli）
     */
    private Long timestamp;

    /**
     * 账号（主机标识）
     */
    private Integer account;

    /**
     * 事件代码（4位）
     * <p>
     * 常见事件代码：
     * - 0: 链路测试
     * - 1100-1199: 防区相关事件
     * - 1200-1299: 报警相关事件
     * - 1300-1399: 设备状态事件
     */
    private Integer eventCode;

    /**
     * 防区号（2位，00-99）
     */
    private Integer area;

    /**
     * 点位号（3位，000-999）
     */
    private Integer point;

    /**
     * 序列号（4位）
     */
    private Integer sequence;

    /**
     * 事件描述
     */
    private String eventDescription;

    /**
     * 事件级别
     * <p>
     * - info: 信息
     * - warning: 警告
     * - error: 错误
     * - critical: 严重
     */
    private String level;

    /**
     * 事件类型
     * <p>
     * - alarm: 报警
     * - restore: 恢复
     * - status: 状态
     * - test: 测试
     */
    private String type;

    /**
     * 接收时间
     */
    private LocalDateTime receiveTime;

    /**
     * 远程地址
     */
    private String remoteAddress;

    /**
     * 远程端口
     */
    private Integer remotePort;

    /**
     * 原始消息
     */
    private String rawMessage;

    /**
     * 租户ID
     */
    private Long tenantId;

    /**
     * 设备ID（关联到系统中的设备）
     */
    private Long deviceId;

    /**
     * 设备名称
     */
    private String deviceName;

    /**
     * 防区名称
     */
    private String areaName;

    /**
     * 点位名称
     */
    private String pointName;

    /**
     * 位置信息
     */
    private String location;

    /**
     * 防区名称（从防区配置表获取）
     */
    private String zoneName;

    /**
     * 关联摄像头ID（用于视频联动）
     */
    private Long cameraId;

    /**
     * 是否已处理
     */
    private Boolean handled;

    /**
     * 处理时间
     */
    private LocalDateTime handleTime;

    /**
     * 处理人
     */
    private String handleBy;

    /**
     * 处理备注
     */
    private String handleRemark;
}
