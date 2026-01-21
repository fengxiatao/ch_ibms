package cn.iocoder.yudao.module.iot.controller.admin.access.vo.doorgroup;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class DoorGroupRespVO {

    private Long id;
    private String groupName;
    private String description;
    private LocalDateTime createTime;

    /**
     * 关联的门禁设备ID列表
     */
    private List<Long> deviceIds;

}


























