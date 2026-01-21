package cn.iocoder.yudao.module.iot.controller.admin.access.vo.dashboard;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 门禁管理 Dashboard 统计数据 Response VO
 *
 * @author 智能化系统
 */
@Schema(description = "管理后台 - 门禁管理 Dashboard 统计数据 Response VO")
@Data
public class AccessDashboardStatisticsRespVO {

    @Schema(description = "今日通行次数", example = "1256")
    private Long todayAccessCount;

    @Schema(description = "今日访客数", example = "89")
    private Long todayVisitorCount;

    @Schema(description = "今日车辆数", example = "456")
    private Long todayVehicleCount;

    @Schema(description = "今日告警数", example = "3")
    private Long todayAlarmCount;

    @Schema(description = "在线设备数", example = "15")
    private Long onlineDeviceCount;

    @Schema(description = "设备总数", example = "18")
    private Long totalDeviceCount;

    @Schema(description = "当前访客数", example = "23")
    private Long currentVisitorCount;

    @Schema(description = "已占用停车位", example = "45")
    private Long occupiedParkingSpaces;

    @Schema(description = "总停车位", example = "100")
    private Long totalParkingSpaces;

    @Schema(description = "通行次数增长率（百分比）", example = "12.5")
    private Double accessCountGrowth;

    @Schema(description = "访客数增长率（百分比）", example = "8.3")
    private Double visitorCountGrowth;

    @Schema(description = "车辆数增长率（百分比）", example = "-2.1")
    private Double vehicleCountGrowth;

    @Schema(description = "设备状态分布")
    private DeviceStatusDistribution deviceStatusDistribution;

    @Schema(description = "通行类型分布")
    private AccessTypeDistribution accessTypeDistribution;

    @Data
    @Schema(description = "设备状态分布")
    public static class DeviceStatusDistribution {
        @Schema(description = "在线", example = "15")
        private Long online;
        @Schema(description = "离线", example = "2")
        private Long offline;
        @Schema(description = "维护中", example = "1")
        private Long maintenance;
        @Schema(description = "故障", example = "0")
        private Long fault;
    }

    @Data
    @Schema(description = "通行类型分布")
    public static class AccessTypeDistribution {
        @Schema(description = "员工", example = "65")
        private Long employee;
        @Schema(description = "访客", example = "20")
        private Long visitor;
        @Schema(description = "车辆", example = "12")
        private Long vehicle;
        @Schema(description = "电梯", example = "3")
        private Long elevator;
    }

}



















