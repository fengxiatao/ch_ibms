package cn.iocoder.yudao.module.iot.controller.admin.changhui.vo.control;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 长辉设备控制日志 Response VO
 *
 * @author 长辉信息科技有限公司
 */
@Schema(description = "管理后台 - 长辉设备控制日志 Response VO")
@Data
public class ChanghuiControlLogRespVO {

    @Schema(description = "日志ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    @Schema(description = "设备ID", example = "1")
    private Long deviceId;

    @Schema(description = "测站编码", requiredMode = Schema.RequiredMode.REQUIRED, example = "1234567890")
    private String stationCode;

    @Schema(description = "控制类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "MODE_SWITCH")
    private String controlType;

    @Schema(description = "控制类型名称", example = "模式切换")
    private String controlTypeName;

    @Schema(description = "控制参数(JSON)", example = "{\"action\":\"rise\"}")
    private String controlParams;

    @Schema(description = "结果：0-失败,1-成功", example = "1")
    private Integer result;

    @Schema(description = "结果名称", example = "成功")
    private String resultName;

    @Schema(description = "错误信息", example = "设备离线")
    private String errorMessage;

    @Schema(description = "操作员", example = "admin")
    private String operator;

    @Schema(description = "操作时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime operateTime;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

}
