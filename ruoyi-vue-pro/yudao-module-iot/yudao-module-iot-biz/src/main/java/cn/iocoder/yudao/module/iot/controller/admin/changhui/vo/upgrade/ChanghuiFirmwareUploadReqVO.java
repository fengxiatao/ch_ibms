package cn.iocoder.yudao.module.iot.controller.admin.changhui.vo.upgrade;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 长辉固件上传 Request VO
 *
 * @author 长辉信息科技有限公司
 */
@Schema(description = "管理后台 - 长辉固件上传 Request VO")
@Data
public class ChanghuiFirmwareUploadReqVO {

    @Schema(description = "固件名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "测控一体化闸门固件")
    @NotBlank(message = "固件名称不能为空")
    private String name;

    @Schema(description = "版本号", requiredMode = Schema.RequiredMode.REQUIRED, example = "V1.0.0")
    @NotBlank(message = "版本号不能为空")
    private String version;

    @Schema(description = "适用设备类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "设备类型不能为空")
    private Integer deviceType;

    @Schema(description = "描述", example = "修复水位采集问题")
    private String description;

}
