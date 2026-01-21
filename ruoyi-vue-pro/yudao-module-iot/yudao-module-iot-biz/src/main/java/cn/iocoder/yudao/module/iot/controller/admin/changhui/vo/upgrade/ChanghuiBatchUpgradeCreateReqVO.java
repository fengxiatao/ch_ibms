package cn.iocoder.yudao.module.iot.controller.admin.changhui.vo.upgrade;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 长辉批量升级创建 Request VO
 *
 * @author 长辉信息科技有限公司
 */
@Schema(description = "管理后台 - 长辉批量升级创建 Request VO")
@Data
public class ChanghuiBatchUpgradeCreateReqVO {

    @Schema(description = "测站编码列表", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "测站编码列表不能为空")
    private List<String> stationCodes;

    @Schema(description = "固件ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "固件ID不能为空")
    private Long firmwareId;

    @Schema(description = "升级模式：0=TCP帧传输, 1=HTTP URL下载", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "升级模式不能为空")
    private Integer upgradeMode;

}
