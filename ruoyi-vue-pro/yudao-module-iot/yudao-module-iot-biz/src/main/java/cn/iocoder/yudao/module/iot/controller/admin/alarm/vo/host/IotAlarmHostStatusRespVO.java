package cn.iocoder.yudao.module.iot.controller.admin.alarm.vo.host;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 报警主机状态 Response VO
 *
 * @author 芋道源码
 */
@Schema(description = "管理后台 - 报警主机状态 Response VO")
@Data
public class IotAlarmHostStatusRespVO {

    @Schema(description = "主机ID", example = "109")
    private Long hostId;

    @Schema(description = "主机账号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1234")
    private String account;

    @Schema(description = "系统状态：0-撤防，1-布防，2-居家布防", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    private Integer systemStatus;

    @Schema(description = "分区列表")
    private List<PartitionStatus> partitions;

    @Schema(description = "防区列表")
    private List<ZoneStatusVO> zones;

    @Schema(description = "查询时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime queryTime;

    @Schema(description = "最后查询时间")
    private LocalDateTime lastQueryTime;

    @Schema(description = "原始数据", example = "c1234,0,131ÉS0aaaaaaAB")
    private String rawData;

    /**
     * 分区状态
     */
    @Schema(description = "分区状态")
    @Data
    public static class PartitionStatus {
        @Schema(description = "分区编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
        private Integer partitionNo;

        @Schema(description = "布防状态：0-撤防，1-布防", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
        private Integer status;
    }

    /**
     * 防区状态
     */
    @Schema(description = "防区状态")
    @Data
    public static class ZoneStatus {
        @Schema(description = "防区编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
        private Integer zoneNo;

        @Schema(description = "布防状态：0-撤防，1-布防", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
        private Integer status;

        @Schema(description = "报警状态：0-正常，1-报警", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
        private Integer alarmStatus;
    }

    /**
     * 防区状态VO（扩展版，用于getHostStatus接口）
     */
    @Schema(description = "防区状态VO")
    @Data
    public static class ZoneStatusVO {
        @Schema(description = "防区编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
        private Integer zoneNo;

        @Schema(description = "防区名称", example = "防区1")
        private String zoneName;

        @Schema(description = "状态字符", example = "A")
        private String status;

        @Schema(description = "状态名称", example = "防区布防")
        private String statusName;

        @Schema(description = "布防状态枚举：0-撤防，1-布防，2-旁路", example = "1")
        private Integer armStatus;

        @Schema(description = "报警状态枚举：0-正常，1-报警中，11-17各类报警", example = "0")
        private Integer alarmStatus;

        @Schema(description = "最后报警时间")
        private LocalDateTime lastAlarmTime;
    }

}
