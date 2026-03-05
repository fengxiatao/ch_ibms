package cn.iocoder.yudao.module.iot.controller.admin.dashboard.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 首页大屏统计 Response VO
 *
 * @author 长辉信息科技有限公司
 */
@Schema(description = "管理后台 - 首页大屏统计 Response VO")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HomeScreenRespVO {

    // ==================== 顶部统计卡片 ====================

    @Schema(description = "设备接入总数", example = "2856")
    private Long deviceTotal;

    @Schema(description = "本月能耗(标煤)", example = "142.8")
    private BigDecimal monthlyEnergy;

    @Schema(description = "实时异常设备数", example = "5")
    private Long abnormalDevices;

    @Schema(description = "今日告警事件数", example = "12")
    private Long todayAlerts;

    // ==================== 设备实时状态统计 ====================

    @Schema(description = "设备状态统计")
    private DeviceStatusStats deviceStatusStats;

    // ==================== 智慧安防 ====================

    @Schema(description = "安防数据")
    private SecurityData securityData;

    // ==================== 智慧通行 ====================

    @Schema(description = "通行数据")
    private AccessData accessData;

    // ==================== 智慧能源 ====================

    @Schema(description = "能源数据")
    private EnergyData energyData;

    // ==================== 智慧楼宇 ====================

    @Schema(description = "楼宇环境数据")
    private BuildingEnvData buildingEnvData;

    // ==================== 实时告警列表 ====================

    @Schema(description = "最新告警列表")
    private List<LatestAlarm> latestAlarms;

    // ==================== 内部类定义 ====================

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DeviceStatusStats {
        @Schema(description = "设备总数", example = "323")
        private Long total;

        @Schema(description = "在线数", example = "243")
        private Long online;

        @Schema(description = "离线数", example = "42")
        private Long offline;

        @Schema(description = "告警数", example = "4")
        private Long alarm;

        @Schema(description = "故障数", example = "34")
        private Long fault;

        @Schema(description = "在线率(%)", example = "75.2")
        private Double onlineRate;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SecurityData {
        // 通道总览
        @Schema(description = "通道在线率(%)", example = "98.0")
        private Double channelOnlineRate;

        @Schema(description = "通道在线数", example = "46")
        private Long channelOnline;

        @Schema(description = "通道离线数", example = "2")
        private Long channelOffline;

        // 存储设备
        @Schema(description = "存储设备在线率(%)", example = "100.0")
        private Double storageOnlineRate;

        @Schema(description = "存储设备在线数", example = "21")
        private Long storageOnline;

        @Schema(description = "存储设备离线数", example = "0")
        private Long storageOffline;

        // 服务器
        @Schema(description = "服务器在线率(%)")
        private Double serverOnlineRate;

        @Schema(description = "服务器在线数", example = "0")
        private Long serverOnline;

        @Schema(description = "服务器离线数", example = "0")
        private Long serverOffline;

        // 入侵报警
        @Schema(description = "未处理报警数", example = "2")
        private Long unhandledAlarms;

        @Schema(description = "报警趋势(小时统计)")
        private List<Integer> alarmTrend;

        @Schema(description = "最近报警列表")
        private List<AlarmItem> recentAlarms;

        // 电子巡更
        @Schema(description = "巡更完成率(%)", example = "80.0")
        private Double patrolRate;

        @Schema(description = "巡更已完成数", example = "16")
        private Integer patrolCompleted;

        @Schema(description = "巡更总数", example = "20")
        private Integer patrolTotal;

        @Schema(description = "巡更异常数", example = "1")
        private Integer patrolAbnormal;

        @Schema(description = "巡更漏检数", example = "3")
        private Integer patrolMissed;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AlarmItem {
        @Schema(description = "告警标题")
        private String title;

        @Schema(description = "告警位置")
        private String location;

        @Schema(description = "告警时间")
        private String time;

        @Schema(description = "告警级别: danger/warning")
        private String level;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AccessData {
        // 门禁管理
        @Schema(description = "门禁状态: 正常/异常")
        private String doorStatus;

        @Schema(description = "今日进入人数", example = "3428")
        private Long todayEntry;

        @Schema(description = "今日离开人数", example = "3156")
        private Long todayExit;

        @Schema(description = "进出趋势数据")
        private List<AccessTrend> accessTrend;

        // 访客预约
        @Schema(description = "预约访客数", example = "24")
        private Long visitorBooked;

        @Schema(description = "在访访客数", example = "18")
        private Long visitorVisiting;

        @Schema(description = "已离访客数", example = "156")
        private Long visitorLeft;

        // 停车场
        @Schema(description = "车位占用率(%)", example = "82.0")
        private Double parkingRate;

        @Schema(description = "已用车位", example = "328")
        private Long parkingUsed;

        @Schema(description = "剩余车位", example = "72")
        private Long parkingRemaining;

        @Schema(description = "车位总数", example = "400")
        private Long parkingTotal;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AccessTrend {
        @Schema(description = "时间点")
        private String time;

        @Schema(description = "进入人数")
        private Long entry;

        @Schema(description = "离开人数")
        private Long exit;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EnergyData {
        // 用电
        @Schema(description = "今日用电量(kWh)", example = "8650")
        private BigDecimal todayElectricity;

        @Schema(description = "用电环比(%)", example = "-5.2")
        private Double electricityChange;

        @Schema(description = "用电趋势")
        private List<BigDecimal> electricityTrend;

        // 用水
        @Schema(description = "今日用水量(m³)", example = "1240")
        private BigDecimal todayWater;

        @Schema(description = "用水环比(%)", example = "-5.2")
        private Double waterChange;

        // 费用
        @Schema(description = "电费(元)", example = "6920")
        private BigDecimal electricityCost;

        @Schema(description = "水费(元)", example = "3720")
        private BigDecimal waterCost;

        @Schema(description = "燃气费(元)", example = "1450")
        private BigDecimal gasCost;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BuildingEnvData {
        @Schema(description = "环境温度(°C)", example = "24.5")
        private BigDecimal temperature;

        @Schema(description = "环境湿度(%)", example = "58")
        private Integer humidity;

        @Schema(description = "空气质量等级: 优/良/中/差")
        private String airQuality;

        @Schema(description = "PM2.5(μg/m³)", example = "35")
        private Integer pm25;

        @Schema(description = "CO2(ppm)", example = "450")
        private Integer co2;

        @Schema(description = "设备在线率(%)", example = "96.2")
        private Double deviceOnlineRate;

        @Schema(description = "设备负载率(%)", example = "68.0")
        private Double deviceLoadRate;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LatestAlarm {
        @Schema(description = "告警ID")
        private Long id;

        @Schema(description = "告警标题")
        private String title;

        @Schema(description = "设备名称")
        private String deviceName;

        @Schema(description = "告警位置")
        private String location;

        @Schema(description = "告警级别: critical/warning/info")
        private String level;

        @Schema(description = "告警时间")
        private LocalDateTime time;
    }
}
