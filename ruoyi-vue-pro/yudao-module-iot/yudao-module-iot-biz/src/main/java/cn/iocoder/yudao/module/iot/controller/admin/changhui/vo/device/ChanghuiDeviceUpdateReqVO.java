package cn.iocoder.yudao.module.iot.controller.admin.changhui.vo.device;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 长辉设备更新 Request VO
 *
 * @author 长辉信息科技有限公司
 */
@Schema(description = "管理后台 - 长辉设备更新 Request VO")
@Data
public class ChanghuiDeviceUpdateReqVO {

    @Schema(description = "设备ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "设备ID不能为空")
    private Long id;

    @Schema(description = "测站编码", example = "1234567890")
    @Size(max = 20, message = "测站编码长度不能超过20个字符")
    private String stationCode;

    @Schema(description = "设备名称", example = "测控一体化闸门-01")
    @Size(max = 100, message = "设备名称长度不能超过100个字符")
    private String deviceName;

    @Schema(description = "设备类型", example = "1")
    private Integer deviceType;

    @Schema(description = "行政区代码", example = "110000")
    @Size(max = 10, message = "行政区代码长度不能超过10个字符")
    private String provinceCode;

    @Schema(description = "管理处代码", example = "001")
    @Size(max = 10, message = "管理处代码长度不能超过10个字符")
    private String managementCode;

    @Schema(description = "站所代码", example = "001")
    @Size(max = 10, message = "站所代码长度不能超过10个字符")
    private String stationCodePart;

    @Schema(description = "桩号（前）", example = "100")
    @Size(max = 10, message = "桩号（前）长度不能超过10个字符")
    private String pileFront;

    @Schema(description = "桩号（后）", example = "200")
    @Size(max = 10, message = "桩号（后）长度不能超过10个字符")
    private String pileBack;

    @Schema(description = "设备厂家", example = "长辉")
    @Size(max = 10, message = "设备厂家长度不能超过10个字符")
    private String manufacturer;

    @Schema(description = "顺序编号", example = "001")
    @Size(max = 10, message = "顺序编号长度不能超过10个字符")
    private String sequenceNo;

    @Schema(description = "TEA加密密钥", example = "12345678,12345678,12345678,12345678")
    @Size(max = 64, message = "TEA加密密钥长度不能超过64个字符")
    private String teaKey;

    @Schema(description = "设备密码", example = "123456")
    @Size(max = 10, message = "设备密码长度不能超过10个字符")
    private String password;

}
