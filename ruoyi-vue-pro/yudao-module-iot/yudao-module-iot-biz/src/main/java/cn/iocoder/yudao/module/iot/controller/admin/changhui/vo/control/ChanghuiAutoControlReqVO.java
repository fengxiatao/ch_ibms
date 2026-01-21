package cn.iocoder.yudao.module.iot.controller.admin.changhui.vo.control;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * 长辉设备自动控制 Request VO
 *
 * @author 长辉信息科技有限公司
 */
@Schema(description = "管理后台 - 长辉设备自动控制 Request VO")
@Data
public class ChanghuiAutoControlReqVO {

    @Schema(description = "测站编码", requiredMode = Schema.RequiredMode.REQUIRED, example = "1234567890")
    @NotBlank(message = "测站编码不能为空")
    private String stationCode;

    @Schema(description = "控制模式：flow-流量, opening-开度, level-水位, volume-水量", 
            requiredMode = Schema.RequiredMode.REQUIRED, example = "flow")
    @NotBlank(message = "控制模式不能为空")
    @Pattern(regexp = "^(flow|opening|level|volume)$", message = "控制模式只能是 flow、opening、level 或 volume")
    private String controlMode;

    @Schema(description = "目标值", requiredMode = Schema.RequiredMode.REQUIRED, example = "1.5")
    @NotNull(message = "目标值不能为空")
    private Double targetValue;

}
