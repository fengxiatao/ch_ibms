package cn.iocoder.yudao.module.iot.controller.admin.visitor.vo.appointment;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "管理后台 - 访客预约 Response VO")
@Data
public class VisitorAppointmentRespVO {

    @Schema(description = "编号", example = "1")
    private Long id;

    @Schema(description = "访客姓名", example = "张三")
    private String name;

    @Schema(description = "联系电话", example = "13800000000")
    private String phone;

    @Schema(description = "访客类型", example = "vip")
    private String type;

    @Schema(description = "所属单位", example = "北京科技有限公司")
    private String company;

    @Schema(description = "被访人", example = "王经理")
    private String host;

    @Schema(description = "被访人部门", example = "技术部")
    private String hostDept;

    @Schema(description = "预约来访时间")
    private LocalDateTime visitTime;

    @Schema(description = "来访事由")
    private String reason;

    @Schema(description = "访问区域")
    private List<String> areas;

    @Schema(description = "身份证号")
    private String idCard;

    @Schema(description = "车牌号")
    private String carNo;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "审批状态")
    private String status;

    @Schema(description = "审批意见")
    private String approvalComment;

    @Schema(description = "审批时间")
    private LocalDateTime approvalTime;

    @Schema(description = "签到时间")
    private LocalDateTime signInTime;

    @Schema(description = "签离时间")
    private LocalDateTime signOutTime;

    @Schema(description = "当前位置")
    private String currentLocation;

    @Schema(description = "评价分数")
    private Double rating;
}

