package cn.iocoder.yudao.module.iot.controller.admin.access.vo.person;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 门禁人员批量创建 Response VO
 */
@Schema(description = "管理后台 - 门禁人员批量创建 Response VO")
@Data
public class IotAccessPersonBatchCreateRespVO {

    @Schema(description = "成功创建数量", example = "10")
    private Integer successCount;

    @Schema(description = "失败数量", example = "0")
    private Integer failCount;

    @Schema(description = "创建成功的人员ID列表")
    private List<Long> createdIds;

    @Schema(description = "错误信息列表")
    private List<ErrorItem> errors;

    @Schema(description = "错误信息")
    @Data
    public static class ErrorItem {

        @Schema(description = "人员编号", example = "100001")
        private String personCode;

        @Schema(description = "错误信息", example = "人员编号已存在")
        private String message;

        public ErrorItem() {}

        public ErrorItem(String personCode, String message) {
            this.personCode = personCode;
            this.message = message;
        }
    }
}
