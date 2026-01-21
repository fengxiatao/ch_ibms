package cn.iocoder.yudao.module.iot.controller.admin.access.vo.permissiongroup;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * 门禁权限组创建 Request VO
 */
@Schema(description = "管理后台 - 门禁权限组创建 Request VO")
@Data
public class IotAccessPermissionGroupCreateReqVO {

    @Schema(description = "权限组名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "研发部门禁权限")
    @NotBlank(message = "权限组名称不能为空")
    @Size(max = 100, message = "权限组名称长度不能超过100个字符")
    private String groupName;

    @Schema(description = "时间模板ID", example = "1")
    private Long timeTemplateId;

    @Schema(description = "认证方式（CARD-刷卡，FACE-人脸，FINGERPRINT-指纹，PASSWORD-密码，逗号分隔）", example = "CARD,FACE")
    private String authMode;

    @Schema(description = "认证方式数组（前端使用，会自动转换为authMode）", example = "[\"CARD\", \"FACE\"]")
    private List<String> authModes;

    @Schema(description = "描述", example = "研发部门的门禁权限组")
    @Size(max = 500, message = "描述长度不能超过500个字符")
    private String description;

    @Schema(description = "状态（0正常 1停用）", example = "0")
    private Integer status;

    @Schema(description = "关联的设备ID列表", example = "[110, 111]")
    private List<Long> deviceIds;

    @Schema(description = "关联的通道ID列表", example = "[1, 2, 3]")
    private List<Long> channelIds;

}
