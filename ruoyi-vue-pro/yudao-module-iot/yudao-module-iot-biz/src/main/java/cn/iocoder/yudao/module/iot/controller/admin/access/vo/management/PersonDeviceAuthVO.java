package cn.iocoder.yudao.module.iot.controller.admin.access.vo.management;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 人员设备授权记录 VO
 * 
 * 用于授权进度页面展示人员-设备授权记录
 * Requirements: 7.1, 7.2
 *
 * @author 芋道源码
 */
@Schema(description = "管理后台 - 人员设备授权记录 VO")
@Data
public class PersonDeviceAuthVO {

    @Schema(description = "授权记录ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    @Schema(description = "人员ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "100")
    private Long personId;

    @Schema(description = "人员姓名", requiredMode = Schema.RequiredMode.REQUIRED, example = "张三")
    private String personName;

    @Schema(description = "人员编号", example = "EMP001")
    private String personCode;

    @Schema(description = "设备ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "200")
    private Long deviceId;

    @Schema(description = "设备名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "一号门门禁控制器")
    private String deviceName;

    @Schema(description = "通道ID", example = "300")
    private Long channelId;

    @Schema(description = "通道名称", example = "一号门")
    private String channelName;

    @Schema(description = "授权状态: 0-未授权, 1-已授权, 2-授权中, 3-授权失败, 4-待撤销, 5-撤销中", 
            requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer authStatus;

    @Schema(description = "授权状态名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "已授权")
    private String authStatusName;

    @Schema(description = "结果信息（失败时包含错误原因）", example = "设备离线，无法下发")
    private String result;

    @Schema(description = "最后下发时间")
    private LocalDateTime lastDispatchTime;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

    /**
     * 获取授权状态名称
     */
    public static String getAuthStatusName(Integer authStatus) {
        if (authStatus == null) {
            return "未知";
        }
        switch (authStatus) {
            case 0:
                return "未授权";
            case 1:
                return "已授权";
            case 2:
                return "授权中";
            case 3:
                return "授权失败";
            case 4:
                return "待撤销";
            case 5:
                return "撤销中";
            default:
                return "未知";
        }
    }

}
