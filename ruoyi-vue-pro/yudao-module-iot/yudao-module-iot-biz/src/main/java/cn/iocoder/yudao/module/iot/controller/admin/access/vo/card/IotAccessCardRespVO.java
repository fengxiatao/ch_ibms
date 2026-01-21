package cn.iocoder.yudao.module.iot.controller.admin.access.vo.card;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 卡信息响应 VO
 *
 * @author 芋道源码
 */
@Schema(description = "管理后台 - 卡信息响应 VO")
@Data
public class IotAccessCardRespVO {

    @Schema(description = "卡号", example = "1234567890")
    private String cardNo;

    @Schema(description = "人员ID", example = "1")
    private Long personId;

    @Schema(description = "人员姓名", example = "张三")
    private String personName;

    @Schema(description = "有效开始时间", example = "2024-01-01 00:00:00")
    private String validStart;

    @Schema(description = "有效结束时间", example = "2025-12-31 23:59:59")
    private String validEnd;

    @Schema(description = "门权限", example = "0,1,2,3")
    private String doorPermissions;

    @Schema(description = "卡状态", example = "正常")
    private String status;

}
