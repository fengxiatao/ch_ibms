package cn.iocoder.yudao.module.iot.controller.admin.device.activation.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * IoT 设备激活 - 激活请求 Request VO
 *
 * @author 长辉信息科技有限公司
 */
@Schema(description = "管理后台 - IoT 设备激活请求 Request VO")
@Data
public class DeviceActivationReqVO {
    
    @Schema(description = "产品ID", required = true, example = "1")
    @NotNull(message = "产品ID不能为空")
    private Long productId;
    
    @Schema(description = "设备IP地址", required = true, example = "192.168.1.202")
    @NotBlank(message = "设备IP地址不能为空")
    private String ipAddress;
    
    @Schema(description = "设备用户名", required = true, example = "admin")
    @NotBlank(message = "设备用户名不能为空")
    private String username;
    
    @Schema(description = "设备密码", required = true, example = "admin123")
    @NotBlank(message = "设备密码不能为空")
    private String password;
    
    @Schema(description = "制造商", example = "dahua")
    private String vendor;
    
    @Schema(description = "设备型号", example = "DH-IPC-HFW2431S")
    private String model;
    
    @Schema(description = "序列号", example = "123456")
    private String serialNumber;
    
    @Schema(description = "固件版本", example = "V2.840.0000000.1.R")
    private String firmwareVersion;
    
    @Schema(description = "设备类型", example = "camera")
    private String deviceType;
    
    @Schema(description = "HTTP端口", example = "80")
    private Integer httpPort;
    
    @Schema(description = "RTSP端口", example = "554")
    private Integer rtspPort;
    
    @Schema(description = "ONVIF端口", example = "8999")
    private Integer onvifPort;
}

















