package cn.iocoder.yudao.module.iot.controller.admin.task.vo.config;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Schema(description = "管理后台 - 任务监控分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
public class TaskMonitorPageReqVO extends PageParam {

    @Schema(description = "任务分类", example = "IOT")
    private String category;

    @Schema(description = "实体类型", example = "DEVICE")
    private String entityType;

    @Schema(description = "任务类型", example = "IOT_DEVICE_OFFLINE_CHECK")
    private String jobType;

    @Schema(description = "是否启用")
    private Boolean enabled;

    @Schema(description = "搜索关键词（实体名称或任务名称）", example = "摄像头")
    private String keyword;

}




