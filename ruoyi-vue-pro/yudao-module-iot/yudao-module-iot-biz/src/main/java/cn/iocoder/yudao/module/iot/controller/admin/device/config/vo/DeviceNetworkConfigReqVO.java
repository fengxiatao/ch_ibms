package cn.iocoder.yudao.module.iot.controller.admin.device.config.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * 管理后台 - IoT 设备网络配置 Request VO
 *
 * @author 长辉信息科技有限公司
 */
@Schema(description = "管理后台 - IoT 设备网络配置 Request VO")
@Data
public class DeviceNetworkConfigReqVO {

    @Schema(description = "IP地址", example = "192.168.1.202")
    @Pattern(regexp = "^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$",
            message = "IP地址格式不正确")
    private String ipAddress;

    @Schema(description = "子网掩码", example = "255.255.255.0")
    @Pattern(regexp = "^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$",
            message = "子网掩码格式不正确")
    private String subnetMask;

    @Schema(description = "网关", example = "192.168.1.1")
    @Pattern(regexp = "^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$",
            message = "网关格式不正确")
    private String gateway;

    @Schema(description = "DNS服务器", example = "8.8.8.8")
    @Pattern(regexp = "^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$",
            message = "DNS服务器格式不正确")
    private String dns;

    @Schema(description = "DHCP启用", example = "false")
    private Boolean dhcpEnabled;

    @Schema(description = "HTTP端口", example = "80")
    @Min(value = 1, message = "HTTP端口必须大于0")
    @Max(value = 65535, message = "HTTP端口必须小于65536")
    private Integer httpPort;

    @Schema(description = "RTSP端口", example = "554")
    @Min(value = 1, message = "RTSP端口必须大于0")
    @Max(value = 65535, message = "RTSP端口必须小于65536")
    private Integer rtspPort;

    @Schema(description = "ONVIF端口", example = "8999")
    @Min(value = 1, message = "ONVIF端口必须大于0")
    @Max(value = 65535, message = "ONVIF端口必须小于65536")
    private Integer onvifPort;
}












