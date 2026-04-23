package cn.iocoder.yudao.module.iot.controller.admin.ibms.product.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class IbmsProductPageReqVO extends PageParam {

    @Schema(description = "产品名称，模糊匹配")
    private String productName;

    @Schema(description = "专业分组码 SA/ST/SB/SE/SF")
    private String groupCode;

    @Schema(description = "系统码 VI/AC/BA/...", example = "VI")
    private String systemCode;

    @Schema(description = "型号码 DS/DP/...", example = "DS")
    private String modelCode;

    @Schema(description = "设备类型码 CAM/READER/...", example = "CAM")
    private String deviceTypeCode;

    @Schema(description = "厂商品牌，模糊匹配", example = "海康")
    private String manufacturer;

    @Schema(description = "产品型号，精确匹配", example = "DS-2CD3T26WD-I3")
    private String modelNumber;

    @Schema(description = "创建时间区间")
    private LocalDateTime[] createTime;
}

