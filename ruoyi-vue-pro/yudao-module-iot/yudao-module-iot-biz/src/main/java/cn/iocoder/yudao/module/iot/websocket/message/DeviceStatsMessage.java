package cn.iocoder.yudao.module.iot.websocket.message;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 设备统计数据消息
 *
 * @author 芋道源码
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeviceStatsMessage {

    /**
     * 在线设备数
     */
    private Integer online;

    /**
     * 离线设备数
     */
    private Integer offline;

    /**
     * 告警设备数
     */
    private Integer alarm;

    /**
     * 设备总数
     */
    private Integer total;

    /**
     * 在线率（百分比）
     */
    private Double rate;
}









