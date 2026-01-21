package cn.iocoder.yudao.module.iot.controller.admin.changhui.vo.device;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 长辉设备 Response VO
 *
 * @author 长辉信息科技有限公司
 */
@Schema(description = "管理后台 - 长辉设备 Response VO")
@Data
public class ChanghuiDeviceRespVO {

    @Schema(description = "设备ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    @Schema(description = "测站编码", requiredMode = Schema.RequiredMode.REQUIRED, example = "1234567890")
    private String stationCode;

    @Schema(description = "设备名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "测控一体化闸门-01")
    private String deviceName;

    @Schema(description = "设备类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer deviceType;

    @Schema(description = "设备类型名称", example = "测控一体化闸门")
    private String deviceTypeName;

    @Schema(description = "行政区代码", example = "110000")
    private String provinceCode;

    @Schema(description = "管理处代码", example = "001")
    private String managementCode;

    @Schema(description = "站所代码", example = "001")
    private String stationCodePart;

    @Schema(description = "桩号（前）", example = "100")
    private String pileFront;

    @Schema(description = "桩号（后）", example = "200")
    private String pileBack;

    @Schema(description = "设备厂家", example = "长辉")
    private String manufacturer;

    @Schema(description = "顺序编号", example = "001")
    private String sequenceNo;

    @Schema(description = "状态：0-离线,1-在线", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer status;

    @Schema(description = "状态名称", example = "在线")
    private String statusName;

    @Schema(description = "最后心跳时间", example = "2024-01-01 12:00:00")
    private LocalDateTime lastHeartbeat;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

}
