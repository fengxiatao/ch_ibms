package cn.iocoder.yudao.module.iot.controller.admin.building.vo.bac;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - 楼宇自控系统日志 Response VO")
@Data
public class IbmsBacSystemLogRespVO {

    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "日志类型 1-操作日志 2-系统日志 3-通信日志")
    private Integer logType;

    @Schema(description = "日志级别 1-info 2-warn 3-error")
    private Integer logLevel;

    @Schema(description = "模块")
    private String module;

    @Schema(description = "日志内容")
    private String content;

    @Schema(description = "操作人")
    private String operator;

    @Schema(description = "IP地址")
    private String ipAddress;

    @Schema(description = "日志时间")
    private LocalDateTime logTime;

}
