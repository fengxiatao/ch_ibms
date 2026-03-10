package cn.iocoder.yudao.module.iot.controller.admin.visitor.vo.door;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - 访客开门记录 Response VO")
@Data
public class VisitorDoorRecordRespVO {

    @Schema(description = "主键")
    private Long id;
    @Schema(description = "开门通道名称")
    private String channelName;
    @Schema(description = "开门方式：face/card")
    private String method;
    @Schema(description = "开门时间")
    private LocalDateTime openTime;
}
