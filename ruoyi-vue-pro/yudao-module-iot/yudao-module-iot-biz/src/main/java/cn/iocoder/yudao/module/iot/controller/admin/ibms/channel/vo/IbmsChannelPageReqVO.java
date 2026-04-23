package cn.iocoder.yudao.module.iot.controller.admin.ibms.channel.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class IbmsChannelPageReqVO extends PageParam {

    @Schema(description = "关键字（编码/名称模糊匹配）")
    private String keyword;

    @Schema(description = "业务分类：security/access/alarm/parking/building/environment/lighting/energy")
    private String business;

    @Schema(description = "空间ID")
    private Long spaceId;

    @Schema(description = "设备ID")
    private Long deviceId;

    @Schema(description = "通道类型码（点位类型码），如 VT/DR/DI/...")
    private String typeCode;

    @Schema(description = "系统类型，如 VI/AC/AL/BA/EN...")
    private String systemType;

    @Schema(description = "状态：online/offline/warning/armed")
    private String status;

    @Schema(description = "创建时间区间")
    private LocalDateTime[] createTime;
}

