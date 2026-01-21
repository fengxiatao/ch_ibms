package cn.iocoder.yudao.module.iot.controller.admin.changhui.vo.upgrade;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 长辉固件 Response VO
 *
 * @author 长辉信息科技有限公司
 */
@Schema(description = "管理后台 - 长辉固件 Response VO")
@Data
public class ChanghuiFirmwareRespVO {

    @Schema(description = "固件ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    @Schema(description = "固件名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "测控一体化闸门固件")
    private String name;

    @Schema(description = "版本号", requiredMode = Schema.RequiredMode.REQUIRED, example = "V1.0.0")
    private String version;

    @Schema(description = "适用设备类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer deviceType;

    @Schema(description = "设备类型名称", example = "测控一体化闸门")
    private String deviceTypeName;

    @Schema(description = "文件路径", example = "/firmware/changhui/v1.0.0.bin")
    private String filePath;

    @Schema(description = "文件大小（字节）", example = "1024000")
    private Long fileSize;

    @Schema(description = "MD5校验值", example = "d41d8cd98f00b204e9800998ecf8427e")
    private String fileMd5;

    @Schema(description = "描述", example = "修复水位采集问题")
    private String description;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

}
