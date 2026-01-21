package cn.iocoder.yudao.module.iot.controller.admin.access.vo.management;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 门控操作请求 VO
 *
 * @author 芋道源码
 */
@Schema(description = "管理后台 - 门控操作请求 VO")
@Data
public class DoorControlReqVO {

    @Schema(description = "设备ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "110")
    @NotNull(message = "设备ID不能为空")
    private Long deviceId;

    @Schema(description = "通道ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "通道ID不能为空")
    private Long channelId;

    @Schema(description = "通道号", example = "1")
    private Integer channelNo;

    @Schema(description = "操作类型: OPEN_DOOR-开门, CLOSE_DOOR-关门, ALWAYS_OPEN-常开, ALWAYS_CLOSED-常闭, CANCEL_ALWAYS-取消常开常闭", 
            requiredMode = Schema.RequiredMode.REQUIRED, example = "OPEN_DOOR")
    @NotNull(message = "操作类型不能为空")
    private String action;

    /**
     * 操作类型常量
     */
    public interface Action {
        /** 开门 */
        String OPEN_DOOR = "OPEN_DOOR";
        /** 关门 */
        String CLOSE_DOOR = "CLOSE_DOOR";
        /** 常开 */
        String ALWAYS_OPEN = "ALWAYS_OPEN";
        /** 常闭 */
        String ALWAYS_CLOSED = "ALWAYS_CLOSED";
        /** 取消常开常闭 */
        String CANCEL_ALWAYS = "CANCEL_ALWAYS";
    }

}
