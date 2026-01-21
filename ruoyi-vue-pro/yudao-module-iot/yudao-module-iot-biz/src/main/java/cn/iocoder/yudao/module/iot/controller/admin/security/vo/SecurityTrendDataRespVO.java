package cn.iocoder.yudao.module.iot.controller.admin.security.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 安防趋势数据响应 VO
 *
 * @author 长辉信息
 */
@Schema(description = "管理后台 - 安防趋势数据响应 VO")
@Data
public class SecurityTrendDataRespVO {

    @Schema(description = "时间标签", requiredMode = Schema.RequiredMode.REQUIRED, example = "[\"10-24\", \"10-25\", \"10-26\"]")
    private List<String> labels;

    @Schema(description = "数据集", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<Dataset> datasets;

    /**
     * 数据集
     */
    @Data
    public static class Dataset {
        @Schema(description = "数据集名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "抓拍总数")
        private String name;

        @Schema(description = "数据值", requiredMode = Schema.RequiredMode.REQUIRED, example = "[150, 180, 165]")
        private List<Integer> data;
    }

}



















