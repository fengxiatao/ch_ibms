package cn.iocoder.yudao.module.iot.controller.admin.security.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * 安防概览 - 摄像头分页请求 VO
 *
 * @author 芋道源码
 */
@Schema(description = "管理后台 - 安防概览摄像头分页请求 VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class SecurityOverviewCameraPageReqVO extends PageParam {

    @Schema(description = "是否包含实时抓图", example = "true")
    private Boolean includeSnapshot = true;

    @Schema(description = "只返回在线设备", example = "false")
    private Boolean onlineOnly = false;
}

