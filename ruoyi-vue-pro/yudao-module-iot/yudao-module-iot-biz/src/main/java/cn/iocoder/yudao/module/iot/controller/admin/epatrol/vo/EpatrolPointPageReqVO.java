package cn.iocoder.yudao.module.iot.controller.admin.epatrol.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Schema(description = "管理后台 - 电子巡更点分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class EpatrolPointPageReqVO extends PageParam {

    @Schema(description = "点位编号", example = "XGD001")
    private String pointNo;

    @Schema(description = "点位名称", example = "1号楼大厅")
    private String pointName;

    @Schema(description = "点位位置", example = "A区1楼")
    private String pointLocation;

    @Schema(description = "状态", example = "1")
    private Integer status;

}
