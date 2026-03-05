package cn.iocoder.yudao.module.iot.dal.dataobject.building;

import com.baomidou.mybatisplus.annotation.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 楼宇自控系统日志 DO
 *
 * @author 智慧楼宇系统
 */
@TableName("ibms_bac_system_log")
@KeySequence("ibms_bac_system_log_seq")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IbmsBacSystemLogDO {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 日志类型：1-远程控制 2-系统事件 3-设备状态变更
     */
    private Integer logType;

    /**
     * 设备类型：1-暖通 2-给排水
     */
    private Integer deviceType;

    /**
     * 设备ID
     */
    private Long deviceId;

    /**
     * 设备名称
     */
    private String deviceName;

    /**
     * 事件描述
     */
    private String eventDesc;

    /**
     * 数值/状态
     */
    private String eventValue;

    /**
     * 操作人
     */
    private String operator;

    /**
     * 日志时间
     */
    private LocalDateTime logTime;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 租户编号
     */
    private Long tenantId;

}
