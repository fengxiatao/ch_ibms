package cn.iocoder.yudao.module.iot.controller.admin.opc.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * OPC 防区配置 Base VO
 *
 * @author 长辉信息科技有限公司
 */
@Data
public class OpcZoneConfigBaseVO {

    @Schema(description = "设备ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "设备ID不能为空")
    private Long deviceId;

    @Schema(description = "防区号（01-99）", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "防区号不能为空")
    private Integer area;

    @Schema(description = "点位号（001-999）", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "点位号不能为空")
    private Integer point;

    @Schema(description = "防区名称", example = "大门防区")
    private String zoneName;

    @Schema(description = "防区类型", example = "instant1")
    private String zoneType;

    @Schema(description = "位置信息", example = "一楼大厅")
    private String location;

    @Schema(description = "关联摄像头ID", example = "100")
    private Long cameraId;

    @Schema(description = "是否启用", example = "true")
    private Boolean enabled;

    @Schema(description = "备注", example = "重要防区")
    private String remark;
}
