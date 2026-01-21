package cn.iocoder.yudao.module.iot.controller.admin.changhui.vo.data;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

/**
 * 长辉设备数据查询 Request VO
 *
 * @author 长辉信息科技有限公司
 */
@Schema(description = "管理后台 - 长辉设备数据查询 Request VO")
@Data
public class ChanghuiDataQueryReqVO {

    @Schema(description = "测站编码", requiredMode = Schema.RequiredMode.REQUIRED, example = "1234567890")
    @NotBlank(message = "测站编码不能为空")
    private String stationCode;

    @Schema(description = "指标类型列表", example = "[\"waterLevel\", \"gatePosition\"]")
    private List<String> indicators;

}
