package cn.iocoder.yudao.module.iot.controller.admin.opc.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDateTime;

/**
 * OPC 防区配置 Response VO
 *
 * @author 长辉信息科技有限公司
 */
@Schema(description = "管理后台 - OPC 防区配置 Response VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class OpcZoneConfigRespVO extends OpcZoneConfigBaseVO {

    @Schema(description = "配置ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    @Schema(description = "设备名称", example = "1号楼报警主机")
    private String deviceName;

    @Schema(description = "摄像头名称", example = "大门摄像头")
    private String cameraName;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;
}
