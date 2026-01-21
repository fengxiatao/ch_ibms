package cn.iocoder.yudao.module.iot.controller.admin.changhui.vo.upgrade;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 长辉批量升级结果 VO
 *
 * @author 长辉信息科技有限公司
 */
@Schema(description = "管理后台 - 长辉批量升级结果 VO")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChanghuiBatchUpgradeResultVO {

    @Schema(description = "总数", requiredMode = Schema.RequiredMode.REQUIRED, example = "10")
    private Integer total;

    @Schema(description = "成功创建数", requiredMode = Schema.RequiredMode.REQUIRED, example = "8")
    private Integer successCount;

    @Schema(description = "失败数", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    private Integer failedCount;

    @Schema(description = "创建的任务ID列表")
    private List<Long> taskIds;

    @Schema(description = "失败详情")
    private List<FailedItem> failedItems;

    /**
     * 失败项
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FailedItem {

        @Schema(description = "测站编码", example = "1234567890")
        private String stationCode;

        @Schema(description = "失败原因", example = "设备不存在")
        private String reason;

    }

}
