package cn.iocoder.yudao.module.iot.dal.dataobject.camera;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.time.LocalDateTime;

/**
 * IoT 摄像头抓图记录 DO
 *
 * @author 长辉信息
 */
@TableName("iot_camera_snapshot")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IotCameraSnapshotDO extends BaseDO {

    /**
     * ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 通道ID
     */
    private Long channelId;

    /**
     * 设备ID
     */
    private Long deviceId;

    /**
     * 通道名称（冗余字段，便于查询）
     */
    private String channelName;

    /**
     * 快照URL
     */
    private String snapshotUrl;

    /**
     * 快照文件路径
     */
    private String snapshotPath;

    /**
     * 文件大小(字节)
     */
    private Long fileSize;

    /**
     * 图片宽度
     */
    private Integer width;

    /**
     * 图片高度
     */
    private Integer height;

    /**
     * 抓拍时间
     */
    private LocalDateTime captureTime;

    /**
     * 抓图类型(1:手动抓图 2:定时抓图 3:报警抓图 4:移动侦测抓图)
     */
    private Integer snapshotType;

    /**
     * 触发事件（用于报警抓图）
     */
    private String triggerEvent;

    /**
     * 事件类型（motion_detected:移动侦测, alarm:报警等）
     */
    private String eventType;

    /**
     * 描述
     */
    private String description;

    /**
     * 是否已处理(0:未处理 1:已处理)
     */
    @TableField(typeHandler = cn.iocoder.yudao.framework.mybatis.core.type.BooleanToIntTypeHandler.class)
    private Boolean isProcessed;

    /**
     * 处理人
     */
    private String processor;

    /**
     * 处理时间
     */
    private LocalDateTime processTime;

    /**
     * 处理备注
     */
    private String processRemark;

}







