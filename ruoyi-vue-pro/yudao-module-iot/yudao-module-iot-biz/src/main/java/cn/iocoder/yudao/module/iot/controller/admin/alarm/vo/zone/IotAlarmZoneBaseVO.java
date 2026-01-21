package cn.iocoder.yudao.module.iot.controller.admin.alarm.vo.zone;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 防区 Base VO
 *
 * @author 长辉信息科技有限公司
 */
@Data
public class IotAlarmZoneBaseVO {

    @NotNull(message = "所属主机ID不能为空")
    private Long hostId;

    @NotNull(message = "防区编号不能为空")
    private Integer zoneNo;

    @NotBlank(message = "防区名称不能为空")
    private String zoneName;

    private String zoneType;

    private String areaLocation;
    
    private Long partitionId;

    private Boolean isImportant;

    private Boolean is24h;

    private String remark;
}
