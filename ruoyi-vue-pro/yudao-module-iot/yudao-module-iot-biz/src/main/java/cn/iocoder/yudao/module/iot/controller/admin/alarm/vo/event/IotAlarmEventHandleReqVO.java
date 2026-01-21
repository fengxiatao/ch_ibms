package cn.iocoder.yudao.module.iot.controller.admin.alarm.vo.event;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 报警事件处理 Request VO
 *
 * @author 长辉信息科技有限公司
 */
@Schema(description = "管理后台 - 报警事件处理 Request VO")
@Data
public class IotAlarmEventHandleReqVO {

    @Schema(description = "事件ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "事件ID不能为空")
    private Long id;

    @Schema(description = "处理备注", example = "已确认并处理")
    private String handleRemark;
}
