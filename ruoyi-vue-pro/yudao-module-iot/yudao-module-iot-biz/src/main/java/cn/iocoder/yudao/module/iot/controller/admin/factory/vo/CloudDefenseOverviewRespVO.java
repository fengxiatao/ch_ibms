package cn.iocoder.yudao.module.iot.controller.admin.factory.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "管理后台 - 立体化云防总览 Response VO")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CloudDefenseOverviewRespVO {

    private LocalDateTime updatedAt;

    private MetricSummary metrics;

    private List<ModeItem> modes;

    private String activeModeCode;

    private Topology topology;

    private List<DeviceItem> deviceList;

    private List<ZoneCardItem> zoneList;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MetricSummary {

        private Long armedAreaCount;

        private Long totalAreaCount;

        private Long onlineDeviceCount;

        private Long totalDeviceCount;

        private Long todayAlertCount;

        private Integer safetyScore;

        private String safetyLevel;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ModeItem {

        private Long id;

        private String code;

        private String name;

        private String icon;

        private String statusText;

        private Boolean enabled;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Topology {

        private String title;

        private List<LegendItem> legends;

        private List<AreaItem> areas;

        private List<PointItem> points;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LegendItem {

        private String key;

        private String label;

        private String color;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AreaItem {

        private Long id;

        private String code;

        private String name;

        private String type;

        private BigDecimal x;

        private BigDecimal y;

        private BigDecimal width;

        private BigDecimal height;

        private Boolean armed;

        private Boolean alarming;

        private Long deviceCount;

        private Long zoneCount;

        private String detailText;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PointItem {

        private Long id;

        private Long areaId;

        private Long deviceId;

        private Long channelId;

        private String code;

        private String name;

        private String type;

        private BigDecimal x;

        private BigDecimal y;

        private Boolean armed;

        private Boolean alarming;

        private Boolean online;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DeviceItem {

        private Long id;

        private Long areaId;

        private String areaName;

        private String name;

        private String typeLabel;

        private String location;

        private Boolean online;

        private List<String> capabilityTags;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ZoneCardItem {

        private Long id;

        private Long areaId;

        private String name;

        private Long deviceCount;

        private Long onlineDeviceCount;

        private Long zoneCount;

        private Long armedZoneCount;

        private Long alarmingZoneCount;

        private String statusText;

        private String healthText;

        private String actionText;
    }
}
