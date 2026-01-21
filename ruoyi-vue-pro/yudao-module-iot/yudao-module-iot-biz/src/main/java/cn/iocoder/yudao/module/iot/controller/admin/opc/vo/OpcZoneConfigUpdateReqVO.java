package cn.iocoder.yudao.module.iot.controller.admin.opc.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * OPC 防区配置更新 Request VO
 *
 * @author 长辉信息科技有限公司
 */
@Schema(description = "管理后台 - OPC 防区配置更新 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class OpcZoneConfigUpdateReqVO extends OpcZoneConfigBaseVO {

    @Schema(description = "配置ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "配置ID不能为空")
    private Long id;
}
