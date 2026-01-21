package cn.iocoder.yudao.module.iot.controller.admin.dashboard.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 实时监控数据 Response VO
 *
 * @author 长辉信息科技有限公司
 */
@Schema(description = "管理后台 - 实时监控数据 Response VO")
@Data
public class RealTimeMonitorRespVO {

    @Schema(description = "最新告警列表", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<LatestAlert> latestAlerts;

    @Schema(description = "设备状态变化列表", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<DeviceStatusChange> deviceStatusChanges;

    @Schema(description = "最新事件列表", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<LatestEvent> latestEvents;

    @Schema(description = "系统负载信息", requiredMode = Schema.RequiredMode.REQUIRED)
    private SystemLoad systemLoad;

    /**
     * 最新告警
     */
    @Schema(description = "最新告警")
    @Data
    public static class LatestAlert {
        @Schema(description = "告警ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
        private Long id;

        @Schema(description = "告警名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "运动检测告警")
        private String alertName;

        @Schema(description = "设备名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "摄像头-001")
        private String deviceName;

        @Schema(description = "告警级别", requiredMode = Schema.RequiredMode.REQUIRED, example = "WARNING")
        private String level;

        @Schema(description = "告警时间", requiredMode = Schema.RequiredMode.REQUIRED)
        private LocalDateTime alertTime;

        @Schema(description = "告警状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "UNHANDLED")
        private Integer status;
    }

    /**
     * 设备状态变化
     */
    @Schema(description = "设备状态变化")
    @Data
    public static class DeviceStatusChange {
        @Schema(description = "设备ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
        private Long deviceId;

        @Schema(description = "设备名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "摄像头-001")
        private String deviceName;

        @Schema(description = "旧状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "ONLINE")
        private String oldStatus;

        @Schema(description = "新状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "OFFLINE")
        private String newStatus;

        @Schema(description = "变化时间", requiredMode = Schema.RequiredMode.REQUIRED)
        private LocalDateTime changeTime;
    }

    /**
     * 最新事件
     */
    @Schema(description = "最新事件")
    @Data
    public static class LatestEvent {
        @Schema(description = "事件ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
        private Long id;

        @Schema(description = "事件类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "MOTION_DETECT")
        private String eventType;

        @Schema(description = "设备名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "摄像头-001")
        private String deviceName;

        @Schema(description = "事件时间", requiredMode = Schema.RequiredMode.REQUIRED)
        private LocalDateTime eventTime;

        @Schema(description = "事件数据", example = "{\"region\":\"zone1\"}")
        private String eventData;
    }

    /**
     * 系统负载
     */
    @Schema(description = "系统负载")
    @Data
    public static class SystemLoad {
        @Schema(description = "CPU使用率", requiredMode = Schema.RequiredMode.REQUIRED, example = "45.5")
        private Double cpuUsage;

        @Schema(description = "内存使用率", requiredMode = Schema.RequiredMode.REQUIRED, example = "60.2")
        private Double memoryUsage;

        @Schema(description = "磁盘使用率", requiredMode = Schema.RequiredMode.REQUIRED, example = "35.8")
        private Double diskUsage;

        @Schema(description = "消息队列积压数", requiredMode = Schema.RequiredMode.REQUIRED, example = "120")
        private Long messageQueueBacklog;

        @Schema(description = "数据库连接数", requiredMode = Schema.RequiredMode.REQUIRED, example = "25")
        private Integer databaseConnections;
    }
}












