package cn.iocoder.yudao.module.iot.websocket.message;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 授权任务进度消息
 * 
 * 用于通过 WebSocket 实时推送授权任务的执行进度
 * 
 * Requirements: 6.2
 *
 * @author Kiro
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "授权任务进度消息")
public class AuthTaskProgressMessage {

    @Schema(description = "任务ID", example = "1")
    private Long taskId;

    @Schema(description = "任务类型（group_dispatch/person_dispatch/revoke）", example = "group_dispatch")
    private String taskType;

    @Schema(description = "权限组ID", example = "1")
    private Long groupId;

    @Schema(description = "权限组名称", example = "研发部门禁权限")
    private String groupName;

    @Schema(description = "总数量", example = "100")
    private Integer totalCount;

    @Schema(description = "成功数量", example = "50")
    private Integer successCount;

    @Schema(description = "失败数量", example = "5")
    private Integer failCount;

    @Schema(description = "进度百分比（0-100）", example = "55")
    private Integer progress;

    @Schema(description = "任务状态（0待执行 1执行中 2已完成 3部分失败 4全部失败）", example = "1")
    private Integer status;

    @Schema(description = "状态描述", example = "执行中")
    private String statusDesc;

    @Schema(description = "当前处理的人员ID", example = "1")
    private Long currentPersonId;

    @Schema(description = "当前处理的人员姓名", example = "张三")
    private String currentPersonName;

    @Schema(description = "当前处理的设备ID", example = "1")
    private Long currentDeviceId;

    @Schema(description = "当前处理的设备名称", example = "大门门禁")
    private String currentDeviceName;

    @Schema(description = "最新错误信息", example = "设备离线")
    private String latestError;

    @Schema(description = "开始时间")
    private LocalDateTime startTime;

    @Schema(description = "结束时间")
    private LocalDateTime endTime;

    /**
     * 计算进度百分比
     */
    public static Integer calculateProgress(Integer totalCount, Integer successCount, Integer failCount) {
        if (totalCount == null || totalCount <= 0) {
            return 0;
        }
        int completed = (successCount != null ? successCount : 0) + (failCount != null ? failCount : 0);
        return completed * 100 / totalCount;
    }

    /**
     * 获取状态描述
     */
    public static String getStatusDescription(Integer status) {
        if (status == null) {
            return "未知";
        }
        switch (status) {
            case 0: return "待执行";
            case 1: return "执行中";
            case 2: return "已完成";
            case 3: return "部分失败";
            case 4: return "全部失败";
            default: return "未知";
        }
    }
}
