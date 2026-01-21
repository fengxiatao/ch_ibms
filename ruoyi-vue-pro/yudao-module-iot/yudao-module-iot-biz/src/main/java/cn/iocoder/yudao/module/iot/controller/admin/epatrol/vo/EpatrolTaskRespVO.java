package cn.iocoder.yudao.module.iot.controller.admin.epatrol.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "管理后台 - 电子巡更任务 Response VO")
@Data
public class EpatrolTaskRespVO {

    @Schema(description = "主键ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    @Schema(description = "任务编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "RW20240101001")
    private String taskCode;

    @Schema(description = "计划ID", example = "1")
    private Long planId;

    @Schema(description = "计划名称", example = "日常巡更计划")
    private String planName;

    @Schema(description = "路线ID", example = "1")
    private Long routeId;

    @Schema(description = "路线名称", example = "白班巡更路线")
    private String routeName;

    @Schema(description = "任务日期", example = "2024-01-01")
    private LocalDate taskDate;

    @Schema(description = "计划开始时间", example = "2024-01-01 08:00:00")
    private LocalDateTime plannedStartTime;

    @Schema(description = "计划结束时间", example = "2024-01-01 09:00:00")
    private LocalDateTime plannedEndTime;

    @Schema(description = "计划时间描述", example = "08:00-09:00")
    private String plannedTimeDesc;

    @Schema(description = "巡更人员ID列表", example = "[1,2]")
    private List<Long> personIds;

    @Schema(description = "巡更人员姓名列表", example = "[\"张三\",\"李四\"]")
    private List<String> personNames;

    @Schema(description = "任务状态：0-未巡，1-已巡", example = "0")
    private Integer status;

    @Schema(description = "提交时间", example = "2024-01-01 09:05:00")
    private LocalDateTime submitTime;

    @Schema(description = "备注", example = "备注")
    private String remark;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

    @Schema(description = "任务记录列表（用于详情展示）")
    private List<TaskRecordRespVO> records;

    @Data
    @Schema(description = "任务记录 Response VO")
    public static class TaskRecordRespVO {

        @Schema(description = "记录ID", example = "1")
        private Long id;

        @Schema(description = "点位ID", example = "1")
        private Long pointId;

        @Schema(description = "点位编号", example = "XGD001")
        private String pointNo;

        @Schema(description = "点位名称", example = "1号楼大厅")
        private String pointName;

        @Schema(description = "巡更人员ID", example = "1")
        private Long personId;

        @Schema(description = "巡更人员姓名", example = "张三")
        private String personName;

        @Schema(description = "预期顺序", example = "1")
        private Integer expectedSort;

        @Schema(description = "实际顺序", example = "1")
        private Integer actualSort;

        @Schema(description = "计划到达时间", example = "2024-01-01 08:10:00")
        private LocalDateTime plannedTime;

        @Schema(description = "实际到达时间", example = "2024-01-01 08:12:00")
        private LocalDateTime actualTime;

        @Schema(description = "巡更状态：1-准时，2-早到，3-晚到，4-未到，5-顺序错", example = "1")
        private Integer patrolStatus;

        @Schema(description = "巡更状态描述", example = "准时")
        private String patrolStatusDesc;

        @Schema(description = "时间差（秒）", example = "120")
        private Integer timeDiffSeconds;

    }

}
