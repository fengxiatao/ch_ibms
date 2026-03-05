package cn.iocoder.yudao.module.iot.controller.admin.parking.vo.system;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Schema(description = "管理后台 - 停车系统配置 保存请求 VO")
@Data
public class ParkingSystemConfigSaveReqVO {

    @Schema(description = "主键，新增为空，编辑必填", example = "1")
    private Long id;

    @Schema(description = "所属停车场 ID", example = "1")
    private Long lotId;

    @Schema(description = "停车场名称", example = "智慧停车场")
    @NotBlank(message = "停车场名称不能为空")
    private String parkingName;

    @Schema(description = "地址")
    private String address;

    @Schema(description = "联系电话")
    private String phone;

    @Schema(description = "总车位数", example = "500")
    @NotNull(message = "总车位数不能为空")
    private Integer totalSpaces;

    @Schema(description = "营业时间，例如 07:00-22:00")
    private String businessHours;

    @Schema(description = "停车场类型")
    private String parkingType;

    @Schema(description = "备注")
    private String remark;
}

