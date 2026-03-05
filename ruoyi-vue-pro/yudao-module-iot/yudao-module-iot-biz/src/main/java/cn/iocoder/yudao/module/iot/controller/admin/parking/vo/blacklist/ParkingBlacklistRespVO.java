package cn.iocoder.yudao.module.iot.controller.admin.parking.vo.blacklist;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - 停车场黑名单 Response VO")
@Data
public class ParkingBlacklistRespVO {

    @Schema(description = "ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    @Schema(description = "车牌号", requiredMode = Schema.RequiredMode.REQUIRED, example = "京A12345")
    private String plateNumber;

    @Schema(description = "拉黑原因", requiredMode = Schema.RequiredMode.REQUIRED, example = "恶意逃费")
    private String reason;

    @Schema(description = "黑名单结束时间")
    private LocalDateTime endTime;

    @Schema(description = "适用车场ID", example = "1")
    private Long lotId;

    @Schema(description = "状态：0-生效中，1-已解除", example = "0")
    private Integer status;

    @Schema(description = "备注", example = "备注信息")
    private String remark;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;
}
