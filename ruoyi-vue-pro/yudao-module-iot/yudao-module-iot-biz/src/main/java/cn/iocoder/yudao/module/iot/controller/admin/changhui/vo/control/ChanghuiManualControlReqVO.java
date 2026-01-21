package cn.iocoder.yudao.module.iot.controller.admin.changhui.vo.control;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * 长辉设备手动控制 Request VO
 *
 * @author 长辉信息科技有限公司
 */
@Schema(description = "管理后台 - 长辉设备手动控制 Request VO")
@Data
public class ChanghuiManualControlReqVO {

    @Schema(description = "测站编码", requiredMode = Schema.RequiredMode.REQUIRED, example = "1234567890")
    @NotBlank(message = "测站编码不能为空")
    private String stationCode;

    @Schema(description = "控制动作：rise-升, fall-降, stop-停", requiredMode = Schema.RequiredMode.REQUIRED, example = "rise")
    @NotBlank(message = "控制动作不能为空")
    @Pattern(regexp = "^(rise|fall|stop)$", message = "控制动作只能是 rise、fall 或 stop")
    private String action;

}
