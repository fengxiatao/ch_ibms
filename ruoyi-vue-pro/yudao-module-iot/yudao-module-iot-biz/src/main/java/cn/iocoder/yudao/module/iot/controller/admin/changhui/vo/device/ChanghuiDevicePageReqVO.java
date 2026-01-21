package cn.iocoder.yudao.module.iot.controller.admin.changhui.vo.device;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * 长辉设备分页查询 Request VO
 *
 * @author 长辉信息科技有限公司
 */
@Schema(description = "管理后台 - 长辉设备分页查询 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ChanghuiDevicePageReqVO extends PageParam {

    @Schema(description = "测站编码", example = "1234567890")
    private String stationCode;

    @Schema(description = "设备名称", example = "测控一体化闸门")
    private String deviceName;

    @Schema(description = "设备类型", example = "1")
    private Integer deviceType;

    @Schema(description = "状态：0-离线,1-在线", example = "1")
    private Integer status;

}
