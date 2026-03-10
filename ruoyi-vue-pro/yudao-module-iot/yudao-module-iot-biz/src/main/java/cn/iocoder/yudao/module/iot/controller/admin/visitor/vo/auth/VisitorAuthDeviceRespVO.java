package cn.iocoder.yudao.module.iot.controller.admin.visitor.vo.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - 门禁设备（用于下发权限选择）Response VO")
@Data
public class VisitorAuthDeviceRespVO {

    @Schema(description = "设备/通道ID")
    private Long id;
    @Schema(description = "设备/通道名称")
    private String name;
}
