package cn.iocoder.yudao.module.iot.controller.admin.opc.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * OPC 防区配置分页 Request VO
 *
 * @author 长辉信息科技有限公司
 */
@Schema(description = "管理后台 - OPC 防区配置分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class OpcZoneConfigPageReqVO extends PageParam {

    @Schema(description = "设备ID", example = "1")
    private Long deviceId;

    @Schema(description = "防区号", example = "1")
    private Integer area;

    @Schema(description = "点位号", example = "1")
    private Integer point;

    @Schema(description = "防区名称", example = "大门防区")
    private String zoneName;

    @Schema(description = "是否启用", example = "true")
    private Boolean enabled;
}
