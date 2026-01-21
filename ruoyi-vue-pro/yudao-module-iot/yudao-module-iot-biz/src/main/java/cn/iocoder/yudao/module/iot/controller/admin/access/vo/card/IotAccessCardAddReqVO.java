package cn.iocoder.yudao.module.iot.controller.admin.access.vo.card;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 添加卡信息请求 VO
 *
 * @author 芋道源码
 */
@Schema(description = "管理后台 - 添加卡信息请求 VO")
@Data
public class IotAccessCardAddReqVO {

    @Schema(description = "设备ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "110")
    @NotNull(message = "设备ID不能为空")
    private Long deviceId;

    @Schema(description = "卡号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1234567890")
    @NotEmpty(message = "卡号不能为空")
    private String cardNo;

    @Schema(description = "人员ID", example = "1")
    private Long personId;

    @Schema(description = "人员姓名", example = "张三")
    private String personName;

    @Schema(description = "有效开始时间", example = "2024-01-01 00:00:00")
    private String validStart;

    @Schema(description = "有效结束时间", example = "2025-12-31 23:59:59")
    private String validEnd;

    @Schema(description = "门权限(逗号分隔的通道索引)", example = "0,1,2,3")
    private String doorPermissions;

}
