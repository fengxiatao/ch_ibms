package cn.iocoder.yudao.module.iot.controller.admin.access.vo.doorpost;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import java.util.List;

@Data
public class DoorPostCreateReqVO {

    @NotBlank(message = "门岗名称不能为空")
    private String postName;

    private String description;

    /**
     * 关联的设备列表（含设备ID和类型）
     */
    private List<DoorPostDeviceVO> devices;

    @Data
    public static class DoorPostDeviceVO {
        private Long deviceId;
        /**
         * 设备类型：1-门禁通道，2-视频监控
         */
        private Integer deviceType;
    }

}


























