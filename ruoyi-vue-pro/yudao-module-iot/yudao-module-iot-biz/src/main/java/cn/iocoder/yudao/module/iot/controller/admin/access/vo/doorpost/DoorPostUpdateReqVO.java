package cn.iocoder.yudao.module.iot.controller.admin.access.vo.doorpost;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import jakarta.validation.constraints.NotNull;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class DoorPostUpdateReqVO extends DoorPostCreateReqVO {

    @NotNull(message = "门岗ID不能为空")
    private Long id;

}


























