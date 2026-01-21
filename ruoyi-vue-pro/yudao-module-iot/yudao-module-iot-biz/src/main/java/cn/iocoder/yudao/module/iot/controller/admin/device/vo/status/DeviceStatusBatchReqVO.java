package cn.iocoder.yudao.module.iot.controller.admin.device.vo.status;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * 批量查询设备状态请求 VO
 * 
 * <p>Requirements: 5.2 - 批量查询的设备数量不超过 100</p>
 *
 * @author 长辉信息科技有限公司
 */
@Schema(description = "管理后台 - 批量查询设备状态 Request VO")
@Data
public class DeviceStatusBatchReqVO {

    @Schema(description = "设备编号列表", requiredMode = Schema.RequiredMode.REQUIRED, example = "[1024, 1025, 1026]")
    @NotEmpty(message = "设备编号列表不能为空")
    @Size(max = 100, message = "一次最多查询100个设备")
    private List<Long> deviceIds;

}
