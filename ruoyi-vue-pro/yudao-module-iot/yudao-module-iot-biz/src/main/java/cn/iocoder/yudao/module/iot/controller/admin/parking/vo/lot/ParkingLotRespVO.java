package cn.iocoder.yudao.module.iot.controller.admin.parking.vo.lot;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - 停车场/车场 Response VO")
@Data
public class ParkingLotRespVO {

    @Schema(description = "车场ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    @Schema(description = "车场名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "地下车库A区")
    private String lotName;

    @Schema(description = "车场编码", example = "LOT001")
    private String lotCode;

    @Schema(description = "车场类型：1-收费，2-免费", example = "1")
    private Integer lotType;

    @Schema(description = "总车位数", example = "200")
    private Integer totalSpaces;

    @Schema(description = "月租费用", example = "300.00")
    private BigDecimal monthlyFee;

    @Schema(description = "车场地址", example = "XX市XX区XX路")
    private String address;

    @Schema(description = "联系人", example = "张三")
    private String contactPerson;

    @Schema(description = "联系电话", example = "13800138000")
    private String contactPhone;

    @Schema(description = "支付后免费出场时间(分钟)", example = "15")
    private Integer freeExitMinutes;

    @Schema(description = "经度", example = "116.404")
    private BigDecimal longitude;

    @Schema(description = "纬度", example = "39.915")
    private BigDecimal latitude;

    @Schema(description = "状态：0-正常，1-停用", example = "0")
    private Integer status;

    @Schema(description = "备注", example = "备注信息")
    private String remark;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;
}
