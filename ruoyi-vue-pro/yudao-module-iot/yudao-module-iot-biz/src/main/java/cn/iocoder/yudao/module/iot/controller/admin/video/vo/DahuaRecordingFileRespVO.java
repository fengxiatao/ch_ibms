package cn.iocoder.yudao.module.iot.controller.admin.video.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 大华 SDK 录像文件响应 VO
 *
 * @author 长辉信息
 */
@Schema(description = "管理后台 - 大华SDK录像文件响应")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DahuaRecordingFileRespVO {

    @Schema(description = "通道ID", example = "1")
    private Long channelId;

    @Schema(description = "通道号", example = "1")
    private Integer channelNo;

    @Schema(description = "文件名", example = "/20240101/ch01_00_20240101100000_20240101110000.dav")
    private String fileName;

    @Schema(description = "文件大小(字节)", example = "1073741824")
    private Long fileSize;

    @Schema(description = "开始时间", example = "2024-01-01 10:00:00")
    private String startTime;

    @Schema(description = "结束时间", example = "2024-01-01 11:00:00")
    private String endTime;

    @Schema(description = "时长(秒)", example = "3600")
    private Long duration;

    @Schema(description = "录像类型: 0=普通录像, 1=报警录像, 2=移动侦测录像", example = "0")
    private Integer recordType;

    @Schema(description = "磁盘号", example = "0")
    private Integer diskNo;

}
