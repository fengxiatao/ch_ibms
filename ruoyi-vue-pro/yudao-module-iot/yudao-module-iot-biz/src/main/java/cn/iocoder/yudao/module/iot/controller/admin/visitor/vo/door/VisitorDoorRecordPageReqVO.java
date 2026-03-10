package cn.iocoder.yudao.module.iot.controller.admin.visitor.vo.door;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Schema(description = "管理后台 - 访客开门记录分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
public class VisitorDoorRecordPageReqVO extends PageParam {

    @Schema(description = "访客预约ID（可选）")
    private Long appointmentId;
}
