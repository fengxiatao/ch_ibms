package cn.iocoder.yudao.module.iot.controller.admin.device.vo.status;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.validation.InEnum;
import cn.iocoder.yudao.module.iot.core.enums.IotDeviceStateEnum;
import cn.iocoder.yudao.module.iot.enums.product.IotProductDeviceTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 分页查询设备状态请求 VO
 * 
 * <p>Requirements: 5.3 - 支持按设备类型、状态、产品ID筛选</p>
 *
 * @author 长辉信息科技有限公司
 */
@Schema(description = "管理后台 - 分页查询设备状态 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
public class DeviceStatusPageReqVO extends PageParam {

    @Schema(description = "设备名称", example = "门禁控制器")
    private String deviceName;

    @Schema(description = "设备类型", example = "1")
    @InEnum(value = IotProductDeviceTypeEnum.class, message = "设备类型必须是 {value}")
    private Integer deviceType;

    @Schema(description = "设备状态", example = "1")
    @InEnum(value = IotDeviceStateEnum.class, message = "设备状态必须是 {value}")
    private Integer state;

    @Schema(description = "产品编号", example = "2048")
    private Long productId;

}
