package cn.iocoder.yudao.module.iot.controller.admin.alarm.vo.zone;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - 防区 Response VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class IotAlarmZoneRespVO extends IotAlarmZoneBaseVO {

    @Schema(description = "防区ID", example = "1")
    private Long id;

    @Schema(description = "防区状态：ARM-布防, DISARM-撤防, BYPASS-旁路", example = "DISARM")
    private String zoneStatus;
    
    @Schema(description = "防区布防状态枚举：0-撤防, 1-布防, 2-旁路", example = "0")
    private Integer zoneStatusEnum;

    @Schema(description = "在线状态：0-离线, 1-在线", example = "1")
    private Integer onlineStatus;

    @Schema(description = "报警次数", example = "5")
    private Integer alarmCount;

    @Schema(description = "最后报警时间")
    private LocalDateTime lastAlarmTime;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;
    
    // ========== 实时状态字段（从查询状态获取） ==========
    
    @Schema(description = "状态字符：a/b/A/B/C/D/E/F/G/H/I（协议原始状态）", example = "A")
    private String statusChar;
    
    @Schema(description = "状态名称", example = "防区布防")
    private String statusName;
    
    @Schema(description = "布防状态枚举：0-撤防，1-布防，2-旁路", example = "1")
    private Integer armStatus;
    
    @Schema(description = "报警状态枚举：0-正常，1-报警中，11-17各类报警", example = "0")
    private Integer alarmStatus;
    
    /**
     * 防区状态（从 DO 的 status 字段映射而来）
     * a-防区撤防, b-防区旁路, A-防区布防+无报警, B-防区布防+正在报警,
     * C-剪断报警, D-短路报警, E-触网报警, F-松弛报警, G-拉紧报警, H-攀爬报警, I-开路报警
     */
    @Schema(description = "防区状态：a-撤防, b-旁路, A-布防无报警, B-布防报警中, C-剪断, D-短路, E-触网, F-松弛, G-拉紧, H-攀爬, I-开路", example = "A")
    private String zoneStatusCode;
}
