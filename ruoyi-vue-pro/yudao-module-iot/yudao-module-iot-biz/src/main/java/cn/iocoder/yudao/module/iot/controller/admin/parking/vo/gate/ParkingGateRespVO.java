package cn.iocoder.yudao.module.iot.controller.admin.parking.vo.gate;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - 道闸设备 Response VO")
@Data
public class ParkingGateRespVO {

    @Schema(description = "道闸ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    @Schema(description = "道闸名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "1号入口道闸")
    private String gateName;

    @Schema(description = "道闸编码", example = "GATE001")
    private String gateCode;

    @Schema(description = "所属车场ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long lotId;

    @Schema(description = "所属车场名称", example = "地下车库A区")
    private String lotName;

    @Schema(description = "所属车道ID", example = "1")
    private Long laneId;

    @Schema(description = "所属车道名称", example = "1号车道")
    private String laneName;

    @Schema(description = "关联的IOT设备ID", example = "1")
    private Long deviceId;

    @Schema(description = "IP地址", example = "192.168.1.100")
    private String ipAddress;

    @Schema(description = "端口号", example = "80")
    private Integer port;

    @Schema(description = "登录用户名", example = "admin")
    private String username;

    @Schema(description = "厂商", example = "海康威视")
    private String manufacturer;

    @Schema(description = "型号", example = "DS-TPR1000")
    private String model;

    @Schema(description = "道闸类型：1-车牌识别一体机，2-普通道闸", example = "1")
    private Integer gateType;

    @Schema(description = "方向：1-入口，2-出口，3-出入口", example = "1")
    private Integer direction;

    @Schema(description = "在线状态：0-离线，1-在线", example = "1")
    private Integer onlineStatus;

    @Schema(description = "最后心跳时间")
    private LocalDateTime lastHeartbeat;

    @Schema(description = "状态：0-正常，1-停用", example = "0")
    private Integer status;

    @Schema(description = "备注", example = "备注信息")
    private String remark;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;
}
