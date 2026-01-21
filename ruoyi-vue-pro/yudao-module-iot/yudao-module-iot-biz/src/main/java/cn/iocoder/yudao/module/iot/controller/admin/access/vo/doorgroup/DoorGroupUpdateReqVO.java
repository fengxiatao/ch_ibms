package cn.iocoder.yudao.module.iot.controller.admin.access.vo.doorgroup;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import jakarta.validation.constraints.NotNull;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class DoorGroupUpdateReqVO extends DoorGroupCreateReqVO {

    @NotNull(message = "门组ID不能为空")
    private Long id;

}


























