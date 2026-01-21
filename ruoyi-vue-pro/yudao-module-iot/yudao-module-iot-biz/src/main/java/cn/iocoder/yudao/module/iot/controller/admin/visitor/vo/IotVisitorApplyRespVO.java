package cn.iocoder.yudao.module.iot.controller.admin.visitor.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 访客申请 Response VO
 */
@Schema(description = "管理后台 - 访客申请 Response VO")
@Data
public class IotVisitorApplyRespVO {

    @Schema(description = "申请ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    @Schema(description = "申请编号", example = "VIS20240108001")
    private String applyCode;

    // ========== 访客信息 ==========

    @Schema(description = "访客ID", example = "1")
    private Long visitorId;

    @Schema(description = "访客编号", example = "V001")
    private String visitorCode;

    @Schema(description = "访客姓名", example = "张三")
    private String visitorName;

    @Schema(description = "访客电话", example = "13800138000")
    private String visitorPhone;

    @Schema(description = "身份证号", example = "110101199001011234")
    private String idCard;

    @Schema(description = "所属单位", example = "XX科技公司")
    private String company;

    @Schema(description = "人脸照片URL")
    private String faceUrl;

    // ========== 被访人信息 ==========

    @Schema(description = "被访人ID", example = "1")
    private Long visiteeId;

    @Schema(description = "被访人姓名", example = "李四")
    private String visiteeName;

    @Schema(description = "被访人部门ID", example = "1")
    private Long visiteeDeptId;

    @Schema(description = "被访人部门名称", example = "研发部")
    private String visiteeDeptName;

    // ========== 来访信息 ==========

    @Schema(description = "来访事由", example = "商务洽谈")
    private String visitReason;

    @Schema(description = "来访状态：0-待访，1-在访，2-离访，3-已取消", example = "0")
    private Integer visitStatus;

    @Schema(description = "来访状态名称", example = "待访")
    private String visitStatusName;

    @Schema(description = "计划来访时间")
    private LocalDateTime planVisitTime;

    @Schema(description = "计划离开时间")
    private LocalDateTime planLeaveTime;

    @Schema(description = "实际到访时间")
    private LocalDateTime actualVisitTime;

    @Schema(description = "实际离开时间")
    private LocalDateTime actualLeaveTime;

    @Schema(description = "申请时间")
    private LocalDateTime applyTime;

    // ========== 审批信息 ==========

    @Schema(description = "审批状态：0-待审批，1-已通过，2-已拒绝", example = "0")
    private Integer approveStatus;

    @Schema(description = "审批状态名称", example = "待审批")
    private String approveStatusName;

    @Schema(description = "审批时间")
    private LocalDateTime approveTime;

    @Schema(description = "审批人姓名", example = "管理员")
    private String approverName;

    @Schema(description = "审批备注")
    private String approveRemark;

    // ========== 授权信息 ==========

    @Schema(description = "授权状态：0-待下发，1-下发中，2-已下发，3-已回收，4-下发失败", example = "0")
    private Integer authStatus;

    @Schema(description = "授权状态名称", example = "待下发")
    private String authStatusName;

    @Schema(description = "门禁卡号")
    private String cardNo;

    @Schema(description = "时间模板ID")
    private Long timeTemplateId;

    @Schema(description = "时间模板名称")
    private String timeTemplateName;

    @Schema(description = "授权类型：1-按时间段，2-按次数，3-时间+次数", example = "1")
    private Integer authType;

    @Schema(description = "授权类型名称", example = "按时间段")
    private String authTypeName;

    @Schema(description = "授权开始时间")
    private LocalDateTime authStartTime;

    @Schema(description = "授权结束时间")
    private LocalDateTime authEndTime;

    @Schema(description = "最大通行次数")
    private Integer maxAccessCount;

    @Schema(description = "已使用通行次数")
    private Integer usedAccessCount;

    @Schema(description = "每日通行次数限制")
    private Integer dailyAccessLimit;

    @Schema(description = "当日已使用次数")
    private Integer dailyUsedCount;

    @Schema(description = "授权设备列表")
    private List<AuthDeviceVO> authDevices;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    /**
     * 授权设备信息
     */
    @Data
    public static class AuthDeviceVO {
        @Schema(description = "设备ID")
        private Long deviceId;
        @Schema(description = "设备名称")
        private String deviceName;
        @Schema(description = "通道ID")
        private Long channelId;
        @Schema(description = "通道名称")
        private String channelName;
        @Schema(description = "下发状态")
        private Integer dispatchStatus;
        @Schema(description = "下发结果")
        private String dispatchResult;
    }

}
