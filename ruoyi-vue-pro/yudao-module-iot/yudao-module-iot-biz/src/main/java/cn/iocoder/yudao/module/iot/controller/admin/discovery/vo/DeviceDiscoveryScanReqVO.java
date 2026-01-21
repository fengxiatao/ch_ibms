package cn.iocoder.yudao.module.iot.controller.admin.discovery.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * IoT 设备发现 - 扫描请求 Request VO
 *
 * @author 长辉信息科技有限公司
 */
@Schema(description = "管理后台 - IoT 设备发现扫描请求 Request VO")
@Data
public class DeviceDiscoveryScanReqVO {
    
    @Schema(description = "扫描协议列表", example = "[\"onvif\", \"dahua\"]")
    private List<String> protocols;
    
    @Schema(description = "超时时间（秒）", example = "5")
    private Integer timeout;
    
    @Schema(description = "网段模式：auto-自动检测, manual-手动指定", example = "auto")
    private String networkMode;
    
    @Schema(description = "IP起始地址（手动模式）", example = "192.168.1.1")
    private String ipStart;
    
    @Schema(description = "IP结束地址（手动模式）", example = "192.168.1.254")
    private String ipEnd;
    
    @Schema(description = "扫描端口列表", example = "[80, 554, 8899]")
    private List<Integer> ports;
    
    @Schema(description = "并发数", example = "50")
    private Integer concurrency;
    
    @Schema(description = "是否跳过已添加设备", example = "true")
    private Boolean skipAdded;
    
    // ======== 兼容旧版本 ========
    
    @Schema(description = "扫描类型（已废弃，请使用 protocols）", example = "onvif")
    @Deprecated
    private String scanType;
    
    @Schema(description = "IP地址范围（已废弃，请使用 ipStart/ipEnd）", example = "192.168.1.0/24")
    @Deprecated
    private String ipRange;
}

















