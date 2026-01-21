package cn.iocoder.yudao.module.iot.controller.admin.changhui.vo.control;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * 长辉设备模式切换 Request VO
 *
 * @author 长辉信息科技有限公司
 */
@Schema(description = "管理后台 - 长辉设备模式切换 Request VO")
@Data
public class ChanghuiSwitchModeReqVO {

    @Schema(description = "测站编码", requiredMode = Schema.RequiredMode.REQUIRED, example = "1234567890")
    @NotBlank(message = "测站编码不能为空")
    private String stationCode;

    @Schema(description = "目标模式：manual-手动, auto-自动", requiredMode = Schema.RequiredMode.REQUIRED, example = "manual")
    @NotBlank(message = "目标模式不能为空")
    @Pattern(regexp = "^(manual|auto)$", message = "目标模式只能是 manual 或 auto")
    private String mode;

}
