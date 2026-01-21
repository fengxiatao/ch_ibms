package cn.iocoder.yudao.module.iot.controller.admin.channel.vo;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class IotChannelAssignSpatialReqVO {
    @NotEmpty
    private List<Long> channelIds;
    @NotNull
    private Long campusId;
    @NotNull
    private Long buildingId;
    @NotNull
    private Long floorId;
    private Long areaId;
}
