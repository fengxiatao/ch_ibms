package cn.iocoder.yudao.module.iot.controller.admin.factory.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "管理后台 - 智慧工厂总览 Response VO")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FactoryOverviewRespVO {

    @Schema(description = "核心指标")
    private Kpis kpis;

    @Schema(description = "楼层列表")
    private List<FloorItem> floors;

    @Schema(description = "设备状态列表")
    private List<DeviceStatusItem> deviceStatusList;

    @Schema(description = "最新告警列表")
    private List<AlertItem> latestAlerts;

    @Schema(description = "主视图区信息")
    private SceneInfo scene;

    @Schema(description = "视频快照")
    private VideoSnapshot videoSnapshot;

    @Schema(description = "能耗趋势")
    private List<EnergyTrendItem> energyTrend;

    @Schema(description = "环境快照")
    private EnvironmentSnapshot environmentSnapshot;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Kpis {

        private MetricItem deviceOnlineRate;

        private MetricItem alarmCount;

        private MetricItem environmentComplianceRate;

        private MetricItem todayEnergy;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MetricItem {

        private BigDecimal value;

        private String unit;

        private BigDecimal trend;

        private Long total;

        private Long online;

        private Long offline;

        private Long inactive;

        private Long unhandled;

        private Long handled;

        private Long qualified;

        private BigDecimal electricity;

        private BigDecimal water;

        private BigDecimal gas;

        private LocalDate statDate;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FloorItem {

        private Long id;

        private String code;

        private String name;

        private Integer sort;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DeviceStatusItem {

        private Long id;

        private String name;

        private String nickname;

        private String location;

        private Long floorId;

        private String floorCode;

        private String floorName;

        private String status;

        private Boolean online;

        private String systemCode;

        private String deviceTypeCode;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AlertItem {

        private Long id;

        private String title;

        private Integer level;

        private String levelLabel;

        private Long deviceId;

        private String deviceName;

        private String location;

        private Long floorId;

        private String floorCode;

        private String floorName;

        private Boolean handled;

        private LocalDateTime alarmTime;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SceneInfo {

        private Long currentFloorId;

        private String currentFloorName;

        private String title;

        private String description;

        private List<SceneAction> actions;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SceneAction {

        private String key;

        private String label;

        private Boolean enabled;

        private String actionType;

        private String target;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VideoSnapshot {

        private Long total;

        private Long online;

        private VideoSource primarySource;

        private List<VideoSource> sources;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VideoSource {

        private Long id;

        private Long deviceId;

        private String name;

        private String location;

        private Long floorId;

        private String floorCode;

        private String floorName;

        private String status;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EnergyTrendItem {

        private LocalDate date;

        private BigDecimal electricity;

        private BigDecimal water;

        private BigDecimal gas;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EnvironmentSnapshot {

        private BigDecimal temperature;

        private BigDecimal humidity;

        private BigDecimal pm25;

        private BigDecimal co2;

        private BigDecimal differentialPressure;

        private String cleanliness;

        private Long qualified;

        private Long total;

        private LocalDateTime collectedAt;

        private String location;
    }
}
