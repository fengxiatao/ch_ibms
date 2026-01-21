package cn.iocoder.yudao.module.iot.controller.admin.gis.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 设备识别配置 VO
 * 用于从DXF文件中识别设备时的过滤和映射
 *
 * @author 智慧建筑管理系统
 */
@Schema(description = "管理后台 - DXF设备识别配置")
@Data
public class DeviceRecognitionConfigVO {

    @Schema(description = "设备类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "camera")
    private String deviceType;

    @Schema(description = "匹配类型：block-块名称，layer-图层名称，both-两者之一", requiredMode = Schema.RequiredMode.REQUIRED, example = "both")
    private String matchType;

    @Schema(description = "名称匹配规则（逗号分隔，支持模糊匹配）", requiredMode = Schema.RequiredMode.REQUIRED, example = "camera,摄像头,监控")
    private String namePattern;

    @Schema(description = "是否启用", requiredMode = Schema.RequiredMode.REQUIRED, example = "true")
    private Boolean enabled;
}



















































