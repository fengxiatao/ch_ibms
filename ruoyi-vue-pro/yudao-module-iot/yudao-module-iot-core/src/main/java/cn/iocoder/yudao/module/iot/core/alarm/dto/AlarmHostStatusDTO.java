package cn.iocoder.yudao.module.iot.core.alarm.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 报警主机状态 DTO
 * 
 * 用于封装从报警主机查询到的状态信息
 * 
 * @author 芋道源码
 */
@Data
public class AlarmHostStatusDTO {

    /**
     * 请求ID（用于追踪请求响应）
     */
    private String requestId;

    /**
     * 租户ID（多租户上下文）
     */
    private Long tenantId;
    
    /**
     * 主机账号
     */
    private String account;

    /**
     * 系统状态：0-撤防，1-布防，2-居家布防
     */
    private Integer systemStatus;

    /**
     * 分区列表
     */
    private List<PartitionStatus> partitions = new ArrayList<>();

    /**
     * 防区列表
     */
    private List<ZoneStatus> zones = new ArrayList<>();

    /**
     * 原始状态数据（用于调试）
     */
    private String rawData;

    /**
     * 错误码（0=成功，非0=失败）
     */
    private Integer errorCode;

    /**
     * 错误消息
     */
    private String errorMessage;

    /**
     * 分区状态
     */
    @Data
    public static class PartitionStatus {
        /**
         * 分区编号（从1开始）
         */
        private Integer partitionNo;

        /**
         * 布防状态：0-撤防，1-布防，2-居家布防
         */
        private Integer status;
    }

    /**
     * 防区状态
     */
    @Data
    public static class ZoneStatus {
        /**
         * 防区编号（从1开始）
         */
        private Integer zoneNo;

        /**
         * 布防状态：0-撤防，1-布防，2-旁路
         */
        private Integer status;

        /**
         * 报警状态：0-正常，1-报警
         */
        private Integer alarmStatus;
    }
}
