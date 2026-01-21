package cn.iocoder.yudao.module.iot.websocket.message;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 告警事件消息
 *
 * @author 芋道源码
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlarmEventMessage {

    /**
     * 告警ID
     */
    private Long id;

    /**
     * 告警类型（personnel/device/behavior/fire/intrusion）
     * 或报警主机事件类型（ALARM/RESTORE/ARM/DISARM等）
     */
    private String type;

    /**
     * 告警级别（high/warning/info）
     * 或报警主机事件级别（CRITICAL/WARNING/INFO）
     */
    private String level;

    /**
     * 告警标题
     */
    private String title;

    /**
     * 告警内容
     */
    private String content;

    /**
     * 关联设备ID
     */
    private Long deviceId;

    /**
     * 关联设备名称
     */
    private String deviceName;

    /**
     * 告警位置
     */
    private String location;

    /**
     * 时间戳
     */
    private Long timestamp;
    
    // ========== 报警主机相关字段 ==========
    
    /**
     * 报警主机ID
     */
    private Long hostId;
    
    /**
     * 报警主机名称
     */
    private String hostName;
    
    /**
     * 事件代码（CID格式，如1401表示撤防）
     */
    private String eventCode;
    
    /**
     * 事件类型（ALARM/RESTORE/ARM/DISARM等）
     */
    private String eventType;
    
    /**
     * 分区号
     */
    private Integer areaNo;
    
    /**
     * 防区号
     */
    private Integer zoneNo;
    
    /**
     * 是否为新事件
     */
    private Boolean isNewEvent;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}









