package cn.iocoder.yudao.module.iot.controller.admin.access.vo.doorgroup;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import java.util.List;

@Data
public class DoorGroupCreateReqVO {

    @NotBlank(message = "门组名称不能为空")
    private String groupName;

    private String description;

    /**
     * 门禁设备ID列表
     */
    private List<Long> deviceIds;

}


























