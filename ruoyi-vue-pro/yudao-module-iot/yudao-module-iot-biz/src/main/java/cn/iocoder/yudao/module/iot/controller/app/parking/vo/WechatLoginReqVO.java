package cn.iocoder.yudao.module.iot.controller.app.parking.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 微信登录请求 VO
 *
 * @author changhui
 */
@Schema(description = "小程序 - 微信登录请求")
@Data
public class WechatLoginReqVO {

    @Schema(description = "微信授权码", requiredMode = Schema.RequiredMode.REQUIRED, example = "0a1b2c3d4e5f")
    @NotBlank(message = "微信授权码不能为空")
    private String code;
}
