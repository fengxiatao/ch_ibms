package cn.iocoder.yudao.module.iot.controller.admin.access.vo.permissiongroup;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 门禁权限组 Response VO
 */
@Schema(description = "管理后台 - 门禁权限组 Response VO")
@Data
public class IotAccessPermissionGroupRespVO {

    @Schema(description = "权限组ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    @Schema(description = "权限组名称", example = "研发部门禁权限")
    private String groupName;

    @Schema(description = "时间模板ID", example = "1")
    private Long timeTemplateId;

    @Schema(description = "时间模板名称", example = "工作日全天")
    private String timeTemplateName;

    @Schema(description = "认证方式（CARD-刷卡，FACE-人脸，FINGERPRINT-指纹，PASSWORD-密码，逗号分隔）", example = "CARD,FACE")
    private String authMode;

    @Schema(description = "认证方式数组（前端使用）", example = "[\"CARD\", \"FACE\"]")
    private List<String> authModes;

    @Schema(description = "描述", example = "研发部门的门禁权限组")
    private String description;

    @Schema(description = "状态（0正常 1停用）", example = "0")
    private Integer status;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "关联的设备数量", example = "5")
    private Integer deviceCount;

    @Schema(description = "关联的人员数量", example = "20")
    private Integer personCount;

    @Schema(description = "关联的设备列表")
    private List<DeviceVO> devices;

    @Schema(description = "关联的人员列表")
    private List<PersonVO> persons;

    @Schema(description = "设备信息")
    @Data
    public static class DeviceVO {
        @Schema(description = "设备ID", example = "110")
        private Long deviceId;

        @Schema(description = "设备名称", example = "大门门禁")
        private String deviceName;

        @Schema(description = "设备IP", example = "192.168.1.100")
        private String deviceIp;

        @Schema(description = "通道ID", example = "1")
        private Long channelId;

        @Schema(description = "通道编号", example = "1")
        private Integer channelNo;

        @Schema(description = "通道名称", example = "1号门")
        private String channelName;
    }

    @Schema(description = "人员信息")
    @Data
    public static class PersonVO {
        @Schema(description = "人员ID", example = "1")
        private Long personId;

        @Schema(description = "人员编号", example = "EMP001")
        private String personCode;

        @Schema(description = "人员姓名", example = "张三")
        private String personName;
    }

}
