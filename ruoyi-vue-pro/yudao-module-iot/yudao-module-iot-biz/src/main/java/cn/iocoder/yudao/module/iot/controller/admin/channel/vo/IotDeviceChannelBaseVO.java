package cn.iocoder.yudao.module.iot.controller.admin.channel.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.Map;

/**
 * IoT 设备通道 Base VO
 *
 * @author IBMS Team
 */
@Data
public class IotDeviceChannelBaseVO {

    @Schema(description = "所属设备ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "100")
    @NotNull(message = "所属设备ID不能为空")
    private Long deviceId;

    @Schema(description = "设备类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "NVR")
    @NotBlank(message = "设备类型不能为空")
    private String deviceType;

    @Schema(description = "产品ID", example = "4")
    private Long productId;

    @Schema(description = "通道号", requiredMode = Schema.RequiredMode.REQUIRED, example = "4")
    @NotNull(message = "通道号不能为空")
    private Integer channelNo;

    @Schema(description = "通道名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "前台全景相机")
    @NotBlank(message = "通道名称不能为空")
    private String channelName;

    @Schema(description = "通道编码", example = "CH-VIDEO-001")
    private String channelCode;

    @Schema(description = "通道类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "VIDEO")
    @NotBlank(message = "通道类型不能为空")
    private String channelType;

    @Schema(description = "通道子类型", example = "IPC")
    private String channelSubType;

    @Schema(description = "安装位置", example = "A栋1层前台大厅")
    private String location;

    @Schema(description = "所属建筑ID", example = "1")
    private Long buildingId;

    @Schema(description = "所属楼层ID", example = "1")
    private Long floorId;

    @Schema(description = "所属区域ID", example = "1")
    private Long areaId;

    @Schema(description = "所属空间ID", example = "1")
    private Long spaceId;

    @Schema(description = "目标设备ID", example = "206")
    private Long targetDeviceId;

    @Schema(description = "目标设备IP", example = "192.168.1.206")
    private String targetIp;

    @Schema(description = "目标设备端口", example = "554")
    private Integer targetPort;

    @Schema(description = "目标设备通道号", example = "1")
    private Integer targetChannelNo;

    @Schema(description = "协议类型", example = "ONVIF")
    private String protocol;

    @Schema(description = "登录用户名", example = "admin")
    private String username;

    @Schema(description = "登录密码", example = "admin123")
    private String password;

    // ========== 视频通道专用字段 ==========

    @Schema(description = "主码流地址", example = "rtsp://192.168.1.206:554/...")
    private String streamUrlMain;

    @Schema(description = "子码流地址", example = "rtsp://192.168.1.206:554/...")
    private String streamUrlSub;

    @Schema(description = "快照地址", example = "http://192.168.1.206/snapshot")
    private String snapshotUrl;

    @Schema(description = "是否支持云台", example = "true")
    private Boolean ptzSupport;

    @Schema(description = "是否支持音频", example = "true")
    private Boolean audioSupport;

    @Schema(description = "分辨率", example = "1920x1080")
    private String resolution;

    @Schema(description = "帧率FPS", example = "25")
    private Integer frameRate;

    @Schema(description = "码率Kbps", example = "4096")
    private Integer bitRate;

    // ========== 门禁通道专用字段 ==========

    @Schema(description = "门点名称", example = "前门")
    private String doorName;

    @Schema(description = "门方向", example = "IN")
    private String doorDirection;

    @Schema(description = "读卡器类型", example = "IC")
    private String cardReaderType;

    @Schema(description = "锁类型", example = "ELECTRIC")
    private String lockType;

    // ========== 消防通道专用字段 ==========

    @Schema(description = "探测器类型", example = "SMOKE")
    private String detectorType;

    @Schema(description = "报警级别", example = "3")
    private Integer alarmLevel;

    // ========== 能源通道专用字段 ==========

    @Schema(description = "表计类型", example = "ELECTRIC")
    private String meterType;

    @Schema(description = "回路名称", example = "照明回路1")
    private String circuitName;

    @Schema(description = "计量单位", example = "kWh")
    private String measurementUnit;

    // ========== 能力信息 ==========

    @Schema(description = "通道能力（JSON格式）")
    private Map<String, Object> capabilities;

    // ========== 状态信息 ==========

    @Schema(description = "在线状态（0:离线 1:在线 2:故障 3:未知）", example = "1")
    private Integer onlineStatus;

    @Schema(description = "启用状态（0:禁用 1:启用）", example = "1")
    private Integer enableStatus;

    @Schema(description = "报警状态（0:正常 1:报警 2:故障）", example = "0")
    private Integer alarmStatus;

    // ========== 业务配置 ==========

    @Schema(description = "是否录像：0-否，1-是", example = "0")
    private Integer isRecording;

    @Schema(description = "是否加入巡更：0-否，1-是", example = "0")
    private Integer isPatrol;

    @Schema(description = "是否加入监控墙：0-否，1-是", example = "0")
    private Integer isMonitor;

    @Schema(description = "巡更停留时长（秒）", example = "30")
    private Integer patrolDuration;

    @Schema(description = "监控墙位置（1-16）", example = "1")
    private Integer monitorPosition;

    // ========== 扩展配置 ==========

    @Schema(description = "扩展配置（JSON格式）")
    private Map<String, Object> config;

    @Schema(description = "描述", example = "前台区域全景监控")
    private String description;

    @Schema(description = "排序", example = "1")
    private Integer sort;

    @Schema(description = "标签", example = "重点,24小时,高清")
    private String tags;
}
