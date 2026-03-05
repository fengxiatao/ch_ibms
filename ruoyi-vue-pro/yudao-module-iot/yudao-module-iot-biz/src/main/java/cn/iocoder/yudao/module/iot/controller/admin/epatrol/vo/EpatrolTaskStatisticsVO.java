package cn.iocoder.yudao.module.iot.controller.admin.epatrol.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Schema(description = "管理后台 - 电子巡更任务统计 Response VO")
@Data
public class EpatrolTaskStatisticsVO {

    @Schema(description = "任务总数", example = "100")
    private Integer total;

    @Schema(description = "已完成任务数", example = "80")
    private Integer completed;

    @Schema(description = "未完成任务数", example = "20")
    private Integer pending;

    @Schema(description = "完成率（百分比）", example = "80")
    private Integer rate;

    @Schema(description = "今日任务数", example = "10")
    private Integer todayTotal;

    @Schema(description = "今日已完成", example = "8")
    private Integer todayCompleted;

    @Schema(description = "今日完成率", example = "80")
    private Integer todayRate;

    @Schema(description = "人员统计列表")
    private List<PersonStatistics> personStatistics;

    @Data
    @Schema(description = "人员统计")
    public static class PersonStatistics {

        @Schema(description = "人员ID", example = "1")
        private Long personId;

        @Schema(description = "人员姓名", example = "张三")
        private String personName;

        @Schema(description = "任务总数", example = "20")
        private Integer total;

        @Schema(description = "已完成任务数", example = "18")
        private Integer completed;

        @Schema(description = "未完成任务数", example = "2")
        private Integer pending;

        @Schema(description = "完成率（百分比）", example = "90")
        private Integer rate;

        @Schema(description = "准时完成数", example = "15")
        private Integer onTimeCount;

        @Schema(description = "迟到完成数", example = "2")
        private Integer lateCount;

        @Schema(description = "早到完成数", example = "1")
        private Integer earlyCount;

    }

}
