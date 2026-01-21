package cn.iocoder.yudao.module.iot.websocket.message;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 报警主机状态更新消息
 *
 * @author 芋道源码
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlarmHostStatusMessage {

    /**
     * 主机ID
     */
    private Long hostId;

    /**
     * 主机账号
     */
    private String account;

    /**
     * 主机名称
     */
    private String hostName;

    /**
     * 系统状态（0=撤防, 1=外出布防, 2=居家布防）
     */
    private Integer systemStatus;

    /**
     * 在线状态：0-离线, 1-在线
     */
    private Integer onlineStatus;

    /**
     * 报警状态
     */
    private Integer alarmStatus;

    /**
     * 分区列表
     */
    private List<PartitionStatus> partitions;

    /**
     * 防区列表
     */
    private List<ZoneStatus> zones;

    /**
     * 分区状态
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PartitionStatus {
        private Long id;
        private Integer partitionNo;
        private String partitionName;
        private Integer status;
        private Integer alarmStatus;
    }

    /**
     * 防区状态
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ZoneStatus {
        private Long id;
        private Integer zoneNo;
        private String zoneName;
        /**
         * 布防状态枚举：0-撤防，1-布防，2-旁路
         */
        private Integer armStatus;
        /**
         * 报警状态枚举：0-正常，1-报警中，11-17各类报警
         */
        private Integer alarmStatus;
        /**
         * 防区状态码：a-撤防, b-旁路, A-布防无报警, B-布防报警中,
         * C-剪断, D-短路, E-触网, F-松弛, G-拉紧, H-攀爬, I-开路
         */
        private String zoneStatusCode;
    }
}
