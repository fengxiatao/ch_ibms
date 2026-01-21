package cn.iocoder.yudao.module.iot.controller.admin.access.vo.authorization;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import jakarta.validation.constraints.NotNull;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class AccessAuthorizationUpdateReqVO extends AccessAuthorizationCreateReqVO {

    @NotNull(message = "授权ID不能为空")
    private Long id;

}


























