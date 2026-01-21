package cn.iocoder.yudao.module.iot.controller.admin.access.vo.permissiongroup;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 门禁权限组分页 Request VO
 */
@Schema(description = "管理后台 - 门禁权限组分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
public class IotAccessPermissionGroupPageReqVO extends PageParam {

    @Schema(description = "权限组名称", example = "研发部")
    private String groupName;

    @Schema(description = "状态（0正常 1停用）", example = "0")
    private Integer status;

}
