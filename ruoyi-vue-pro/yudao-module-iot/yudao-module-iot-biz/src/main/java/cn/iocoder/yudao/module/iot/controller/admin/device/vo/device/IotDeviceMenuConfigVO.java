package cn.iocoder.yudao.module.iot.controller.admin.device.vo.device;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 设备菜单配置 VO
 * 用于表示设备的实际菜单配置（考虑继承）
 *
 * @author ahh
 */
@Schema(description = "管理后台 - 设备菜单配置 VO")
@Data
public class IotDeviceMenuConfigVO {

    @Schema(description = "菜单ID列表（JSON数组）", example = "[1001, 1002, 1003]")
    private String menuIds;
    
    @Schema(description = "主要菜单ID", example = "1001")
    private Long primaryMenuId;
    
    @Schema(description = "配置来源", example = "product", allowableValues = {"product", "device"})
    private String source;
    
    /**
     * 判断是否来自产品配置
     */
    public boolean isFromProduct() {
        return "product".equals(source);
    }
    
    /**
     * 判断是否来自设备配置
     */
    public boolean isFromDevice() {
        return "device".equals(source);
    }
}



