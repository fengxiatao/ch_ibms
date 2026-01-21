package cn.iocoder.yudao.module.iot.dal.dataobject.channel;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * IoT 设备通道 DO
 * 
 * 通道是IBMS系统中的核心抽象概念，代表一个可访问的数据流或控制端点
 * 
 * @author IBMS Team
 */
@TableName(value = "iot_device_channel", autoResultMap = true)
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IotDeviceChannelDO extends BaseDO {

    /**
     * 通道ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    // ========== 设备关联信息 ==========
    
    /**
     * 所属设备ID（关联 iot_device.id）
     */
    private Long deviceId;
    
    /**
     * 设备类型（NVR、DVR、ACCESS_CONTROLLER、FIRE_PANEL、METER、BROADCAST）
     */
    private String deviceType;
    
    /**
     * 产品ID（关联 iot_product.id）
     */
    private Long productId;

    // ========== 通道基本信息 ==========
    
    /**
     * 通道号（从1开始，设备上的物理通道号）
     */
    private Integer channelNo;
    
    /**
     * 通道名称（如：前台全景相机、前门读卡器、1号烟感）
     */
    private String channelName;
    
    /**
     * 通道编码（如：CH-VIDEO-001、CH-ACCESS-001）
     */
    private String channelCode;
    
    /**
     * 通道类型（VIDEO、AUDIO、ACCESS、FIRE、ENERGY、BROADCAST）
     */
    private String channelType;
    
    /**
     * 通道子类型（如视频：IPC、PTZ；门禁：CARD_READER、FINGERPRINT）
     */
    private String channelSubType;

    // ========== 位置信息 ==========
    
    /**
     * 安装位置（如：A栋1层前台大厅）
     */
    private String location;
    
    /**
     * 所属建筑ID
     */
    private Long buildingId;
    
    /**
     * 所属楼层ID
     */
    private Long floorId;
    
    /**
     * 所属区域ID
     */
    private Long areaId;
    
    /**
     * 所属空间ID
     */
    private Long spaceId;

    // ========== 目标设备信息（用于通道映射） ==========
    
    /**
     * 目标设备ID（如NVR通道关联的摄像头设备ID）
     */
    private Long targetDeviceId;
    
    /**
     * 目标设备IP（如摄像头IP：192.168.1.206）
     */
    private String targetIp;
    
    /**
     * 目标设备端口
     */
    private Integer targetPort;
    
    /**
     * 目标设备通道号（如摄像头的物理通道号）
     */
    private Integer targetChannelNo;

    // ========== 连接信息 ==========
    
    /**
     * 协议类型（ONVIF、RTSP、GB28181、Wiegand、Modbus）
     */
    private String protocol;
    
    /**
     * 登录用户名
     */
    private String username;
    
    /**
     * 登录密码（加密存储）
     */
    private String password;

    // ========== 视频通道专用字段 ==========
    
    /**
     * 主码流地址（视频通道）
     */
    private String streamUrlMain;
    
    /**
     * 子码流地址（视频通道）
     */
    private String streamUrlSub;
    
    /**
     * 快照地址（视频通道）
     */
    private String snapshotUrl;
    
    /**
     * 是否支持云台（视频通道）
     */
    @TableField(typeHandler = cn.iocoder.yudao.framework.mybatis.core.type.BooleanToIntTypeHandler.class)
    private Boolean ptzSupport;
    
    /**
     * 是否支持音频（视频通道）
     */
    @TableField(typeHandler = cn.iocoder.yudao.framework.mybatis.core.type.BooleanToIntTypeHandler.class)
    private Boolean audioSupport;
    
    /**
     * 分辨率（视频通道）
     */
    private String resolution;
    
    /**
     * 帧率FPS（视频通道）
     */
    private Integer frameRate;
    
    /**
     * 码率Kbps（视频通道）
     */
    private Integer bitRate;

    // ========== 门禁通道专用字段 ==========
    
    /**
     * 门点名称（门禁通道）
     */
    private String doorName;
    
    /**
     * 门方向（IN、OUT、BOTH）
     */
    private String doorDirection;
    
    /**
     * 读卡器类型（IC、ID、FINGERPRINT、FACE）
     */
    private String cardReaderType;
    
    /**
     * 锁类型（ELECTRIC、MAGNETIC、MOTOR）
     */
    private String lockType;

    // ========== 消防通道专用字段 ==========
    
    /**
     * 探测器类型（SMOKE、HEAT、GAS、MANUAL）
     */
    private String detectorType;
    
    /**
     * 报警级别（1-5）
     */
    private Integer alarmLevel;

    // ========== 能源通道专用字段 ==========
    
    /**
     * 表计类型（ELECTRIC、WATER、GAS、HEAT）
     */
    private String meterType;
    
    /**
     * 回路名称（电表通道）
     */
    private String circuitName;
    
    /**
     * 计量单位（kWh、m³、L）
     */
    private String measurementUnit;

    // ========== 能力信息 ==========
    
    /**
     * 通道能力（JSON格式，存储通道支持的功能列表）
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> capabilities;

    // ========== 状态信息 ==========
    
    /**
     * 在线状态（0:离线 1:在线 2:故障 3:未知）
     */
    private Integer onlineStatus;
    
    /**
     * 启用状态（0:禁用 1:启用）
     */
    private Integer enableStatus;
    
    /**
     * 报警状态（0:正常 1:报警 2:故障）
     */
    private Integer alarmStatus;
    
    /**
     * 最后在线时间
     */
    private LocalDateTime lastOnlineTime;
    
    /**
     * 最后同步时间
     */
    private LocalDateTime lastSyncTime;

    // ========== 业务配置 ==========
    
    /**
     * 是否录像（视频通道）：0-否，1-是
     */
    private Integer isRecording;
    
    /**
     * 是否加入巡更：0-否，1-是
     */
    private Integer isPatrol;
    
    /**
     * 是否加入监控墙：0-否，1-是
     */
    private Integer isMonitor;
    
    /**
     * 巡更停留时长（秒）
     */
    private Integer patrolDuration;
    
    /**
     * 监控墙位置（1-16）
     */
    private Integer monitorPosition;

    // ========== 扩展配置 ==========
    
    /**
     * 扩展配置（JSON格式，存储其他自定义配置）
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> config;
    
    /**
     * 描述
     */
    private String description;
    
    /**
     * 排序
     */
    private Integer sort;
    
    /**
     * 标签（逗号分隔，如：重点,24小时,高清）
     */
    private String tags;
}
