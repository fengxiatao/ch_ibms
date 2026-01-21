package cn.iocoder.yudao.module.iot.controller.app.parking.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 微信登录响应 VO
 *
 * @author changhui
 */
@Schema(description = "小程序 - 微信登录响应")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WechatLoginRespVO {

    @Schema(description = "是否已绑定/登录成功", example = "true")
    private Boolean bound;

    @Schema(description = "是否是新用户（自动注册）", example = "false")
    private Boolean isNewUser;

    @Schema(description = "用户ID", example = "1")
    private Long id;

    @Schema(description = "用户名", example = "parking_a1b2c3d4")
    private String username;

    @Schema(description = "昵称", example = "微信用户")
    private String nickname;

    @Schema(description = "微信OpenID", example = "oXXXXXXXXXXX")
    private String openid;

    @Schema(description = "登录Token", example = "xxxxxxxxxxxxxxxx")
    private String token;

    @Schema(description = "消息", example = "登录成功")
    private String message;
}
