package cn.iocoder.yudao.module.iot.controller.admin.channel.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

/**
 * IoT 设备通道分页查询 Request VO
 *
 * @author IBMS Team
 */
@Schema(description = "管理后台 - IoT 设备通道分页查询 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class IotDeviceChannelPageReqVO extends PageParam {

    @Schema(description = "所属设备ID", example = "100")
    private Long deviceId;

    @Schema(description = "设备类型", example = "NVR")
    private String deviceType;

    @Schema(description = "通道类型", example = "VIDEO")
    private String channelType;

    @Schema(description = "通道名称（模糊匹配）", example = "前台")
    private String channelName;

    @Schema(description = "在线状态（0:离线 1:在线 2:故障 3:未知）", example = "1")
    private Integer onlineStatus;

    @Schema(description = "启用状态（0:禁用 1:启用）", example = "1")
    private Integer enableStatus;

    @Schema(description = "所属建筑ID", example = "1")
    private Long buildingId;

    @Schema(description = "所属楼层ID", example = "1")
    private Long floorId;

    @Schema(description = "所属区域ID", example = "1")
    private Long areaId;

    @Schema(description = "所属空间ID", example = "1")
    private Long spaceId;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}
