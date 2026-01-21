package cn.iocoder.yudao.module.iot.controller.admin.access.vo.authtask;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 门禁授权任务 Response VO
 */
@Schema(description = "管理后台 - 门禁授权任务 Response VO")
@Data
public class IotAccessAuthTaskRespVO {

    @Schema(description = "任务ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    @Schema(description = "任务类型（ADD-新增权限，UPDATE-更新权限，DELETE-删除权限，SYNC-同步权限）", example = "ADD")
    private String taskType;

    @Schema(description = "权限组ID", example = "1")
    private Long groupId;

    @Schema(description = "权限组名称", example = "研发部门禁权限")
    private String groupName;

    @Schema(description = "设备ID", example = "110")
    private Long deviceId;

    @Schema(description = "设备名称", example = "大门门禁")
    private String deviceName;

    @Schema(description = "总数量", example = "100")
    private Integer totalCount;

    @Schema(description = "成功数量", example = "95")
    private Integer successCount;

    @Schema(description = "失败数量", example = "5")
    private Integer failCount;

    @Schema(description = "任务状态（0待执行 1执行中 2已完成 3部分失败 4全部失败）", example = "2")
    private Integer status;

    @Schema(description = "开始时间")
    private LocalDateTime startTime;

    @Schema(description = "结束时间")
    private LocalDateTime endTime;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "进度百分比", example = "95")
    private Integer progress;

    @Schema(description = "任务明细列表")
    private List<DetailVO> details;

    @Schema(description = "任务明细")
    @Data
    public static class DetailVO {
        @Schema(description = "明细ID", example = "1")
        private Long id;

        @Schema(description = "人员ID", example = "1")
        private Long personId;

        @Schema(description = "人员编号", example = "EMP001")
        private String personCode;

        @Schema(description = "人员姓名", example = "张三")
        private String personName;

        @Schema(description = "设备ID", example = "110")
        private Long deviceId;

        @Schema(description = "设备名称", example = "大门门禁")
        private String deviceName;

        @Schema(description = "凭证类型（逗号分隔，如：FACE,CARD,FINGERPRINT）", example = "FACE,CARD")
        private String credentialTypes;

        @Schema(description = "状态（0待执行 1成功 2失败）", example = "1")
        private Integer status;

        @Schema(description = "错误信息", example = "设备离线")
        private String errorMessage;

        @Schema(description = "执行时间")
        private LocalDateTime executeTime;
    }

}
