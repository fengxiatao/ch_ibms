package cn.iocoder.yudao.module.iot.controller.admin.video.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 大华 SDK 录像文件查询请求 VO
 *
 * @author 长辉信息
 */
@Schema(description = "管理后台 - 大华SDK录像文件查询请求")
@Data
public class QueryDahuaRecordingReqVO {

    @Schema(description = "通道ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "通道ID不能为空")
    private Long channelId;

    @Schema(description = "开始时间，格式: yyyy-MM-dd HH:mm:ss", requiredMode = Schema.RequiredMode.REQUIRED, example = "2024-01-01 00:00:00")
    @NotNull(message = "开始时间不能为空")
    private String startTime;

    @Schema(description = "结束时间，格式: yyyy-MM-dd HH:mm:ss", requiredMode = Schema.RequiredMode.REQUIRED, example = "2024-01-01 23:59:59")
    @NotNull(message = "结束时间不能为空")
    private String endTime;

    @Schema(description = "录像类型: 0=所有录像, 1=外部报警, 2=动态监测报警, 3=所有报警", example = "0")
    private Integer recordType;

}
