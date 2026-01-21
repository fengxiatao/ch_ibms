package cn.iocoder.yudao.module.iot.controller.admin.parking.vo.gate;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Schema(description = "管理后台 - 道闸设备新增/修改 Request VO")
@Data
public class ParkingGateSaveReqVO {

    @Schema(description = "道闸ID", example = "1")
    private Long id;

    @Schema(description = "道闸名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "1号入口道闸")
    @NotBlank(message = "道闸名称不能为空")
    private String gateName;

    @Schema(description = "道闸编码", example = "GATE001")
    private String gateCode;

    @Schema(description = "所属车场ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "所属车场不能为空")
    private Long lotId;

    @Schema(description = "所属车道ID", example = "1")
    private Long laneId;

    @Schema(description = "关联的IOT设备ID", example = "1")
    private Long deviceId;

    @Schema(description = "IP地址", example = "192.168.1.100")
    private String ipAddress;

    @Schema(description = "端口号", example = "80")
    private Integer port;

    @Schema(description = "登录用户名", example = "admin")
    private String username;

    @Schema(description = "登录密码", example = "123456")
    private String password;

    @Schema(description = "厂商", example = "海康威视")
    private String manufacturer;

    @Schema(description = "型号", example = "DS-TPR1000")
    private String model;

    @Schema(description = "道闸类型：1-车牌识别一体机，2-普通道闸", example = "1")
    private Integer gateType;

    @Schema(description = "方向：1-入口，2-出口，3-出入口", example = "1")
    private Integer direction;

    @Schema(description = "状态：0-正常，1-停用", example = "0")
    private Integer status;

    @Schema(description = "备注", example = "备注信息")
    private String remark;
}
