package cn.iocoder.yudao.module.iot.controller.app.parking.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 更新昵称请求 VO
 *
 * @author changhui
 */
@Schema(description = "小程序 - 更新昵称请求")
@Data
public class UpdateNicknameReqVO {

    @Schema(description = "昵称", requiredMode = Schema.RequiredMode.REQUIRED, example = "张三")
    @NotBlank(message = "昵称不能为空")
    @Size(max = 64, message = "昵称长度不能超过64个字符")
    private String nickname;
}
