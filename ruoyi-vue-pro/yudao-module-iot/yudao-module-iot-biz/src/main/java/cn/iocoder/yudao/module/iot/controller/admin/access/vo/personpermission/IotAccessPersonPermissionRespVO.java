package cn.iocoder.yudao.module.iot.controller.admin.access.vo.personpermission;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 门禁人员权限响应 VO
 */
@Schema(description = "管理后台 - 门禁人员权限响应 VO")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IotAccessPersonPermissionRespVO {

    @Schema(description = "人员ID", example = "1")
    private Long personId;

    @Schema(description = "人员编号", example = "EMP001")
    private String personCode;

    @Schema(description = "人员姓名", example = "张三")
    private String personName;

    @Schema(description = "关联的权限组列表")
    private List<PermissionGroupInfo> groups;

    @Schema(description = "直接关联的设备列表")
    private List<DeviceInfo> devices;

    @Schema(description = "权限组信息")
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PermissionGroupInfo {
        @Schema(description = "权限组ID", example = "1")
        private Long groupId;

        @Schema(description = "权限组名称", example = "办公区权限组")
        private String groupName;

        @Schema(description = "设备数量", example = "5")
        private Integer deviceCount;
    }

    @Schema(description = "设备信息")
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DeviceInfo {
        @Schema(description = "设备ID", example = "1")
        private Long deviceId;

        @Schema(description = "设备名称", example = "大门门禁")
        private String deviceName;

        @Schema(description = "设备IP", example = "192.168.1.100")
        private String deviceIp;

        @Schema(description = "在线状态", example = "1")
        private Integer onlineStatus;
    }

}
