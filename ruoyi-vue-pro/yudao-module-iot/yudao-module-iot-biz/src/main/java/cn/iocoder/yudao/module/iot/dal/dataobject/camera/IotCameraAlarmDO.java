package cn.iocoder.yudao.module.iot.dal.dataobject.camera;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.time.LocalDateTime;

/**
 * IoT 摄像头报警记录 DO
 *
 * @author 长辉信息
 */
@TableName("iot_camera_alarm")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IotCameraAlarmDO extends BaseDO {

    /**
     * ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 摄像头ID
     */
    private Long cameraId;

    /**
     * 设备名称（冗余字段，便于查询）
     */
    private String deviceName;

    /**
     * 报警类型
     */
    private String alarmType;

    /**
     * 报警时间
     */
    private LocalDateTime alarmTime;

    /**
     * 报警级别(1:低 2:中 3:高 4:严重)
     */
    private Integer alarmLevel;

    /**
     * 快照地址
     */
    private String snapshotUrl;

    /**
     * 视频片段地址
     */
    private String videoUrl;

    /**
     * 描述
     */
    private String description;

    /**
     * 状态(0:未处理 1:已确认 2:已处理 3:误报 4:已忽略)
     */
    private Integer status;

    /**
     * 处理人
     */
    private String handler;

    /**
     * 处理时间
     */
    private LocalDateTime handleTime;

    /**
     * 处理备注
     */
    private String handleRemark;

    /**
     * 是否自动处理(0:否 1:是)
     */
    private Boolean autoHandled;

}

