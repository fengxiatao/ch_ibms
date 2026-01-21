package cn.iocoder.yudao.module.iot.controller.admin.camera.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Schema(description = "管理后台 - IoT 摄像头配置分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class IotCameraPageReqVO extends PageParam {

    @Schema(description = "设备ID", example = "1")
    private Long deviceId;

    @Schema(description = "厂商", example = "大华")
    private String manufacturer;

    @Schema(description = "是否支持PTZ", example = "false")
    private Boolean ptzSupport;

}

