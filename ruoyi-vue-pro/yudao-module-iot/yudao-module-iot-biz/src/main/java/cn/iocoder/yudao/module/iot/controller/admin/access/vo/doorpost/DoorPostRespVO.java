package cn.iocoder.yudao.module.iot.controller.admin.access.vo.doorpost;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class DoorPostRespVO {

    private Long id;
    private String postName;
    private String description;
    private LocalDateTime createTime;

    /**
     * 关联的设备列表
     */
    private List<DoorPostDeviceRespVO> devices;

    @Data
    public static class DoorPostDeviceRespVO {
        private Long deviceId;
        private String deviceName;
        private Integer deviceType;
    }

}


























