package cn.iocoder.yudao.module.iot.controller.admin.ibms.device.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class IbmsDevicePageReqVO extends PageParam {

    @Schema(description = "关键字（名称 / 编码模糊匹配）")
    private String keyword;

    @Schema(description = "专业分组码 SA/ST/SB/SE/SF/GW")
    private String groupCode;

    @Schema(description = "系统码 VI/AC/BA/...", example = "VI")
    private String systemCode;

    @Schema(description = "设备类型码 CAM/NVR/CTR/...", example = "CAM")
    private String deviceTypeCode;

    @Schema(description = "品牌码 HIK/DAH/ZKT/JOH 等")
    private String brand;

    @Schema(description = "接入类型：IP/RS485/韦根/无线/模拟量/开关量")
    private String accessType;

    @Schema(description = "IBMS 产品主键（对齐 ibms_device.ibms_product_id，用于按产品筛选/导出）")
    private Long ibmsProductId;

    @Schema(description = "创建时间区间")
    private LocalDateTime[] createTime;
}

