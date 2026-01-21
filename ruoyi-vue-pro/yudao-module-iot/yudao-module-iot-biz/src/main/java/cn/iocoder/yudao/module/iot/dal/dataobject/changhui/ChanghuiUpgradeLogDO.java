package cn.iocoder.yudao.module.iot.dal.dataobject.changhui;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 长辉设备升级日志 DO
 * 
 * <p>记录固件升级过程中的关键事件，用于：</p>
 * <ul>
 *   <li>问题排查：4G网络不稳定导致的升级失败分析</li>
 *   <li>审计追踪：记录完整的升级过程</li>
 *   <li>性能分析：统计各阶段耗时</li>
 * </ul>
 *
 * @author 长辉信息科技有限公司
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName("changhui_upgrade_log")
public class ChanghuiUpgradeLogDO extends BaseDO {

    /**
     * 日志ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 升级任务ID
     */
    private Long taskId;

    /**
     * 设备ID
     */
    private Long deviceId;

    /**
     * 测站编码
     */
    private String stationCode;

    /**
     * 事件类型
     * 
     * @see EventType
     */
    private String eventType;

    /**
     * 事件描述
     */
    private String eventDescription;

    /**
     * 当前进度（0-100）
     */
    private Integer progress;

    /**
     * 事件详情（JSON格式）
     * 包含错误信息、网络状态等
     */
    private String eventDetail;

    /**
     * 网络信息（JSON格式）
     * 记录网络相关信息用于4G问题排查
     */
    private String networkInfo;

    /**
     * 事件时间
     */
    private LocalDateTime eventTime;

    /**
     * 事件类型常量
     */
    public static class EventType {
        /** 任务创建 */
        public static final String TASK_CREATED = "TASK_CREATED";
        /** 触发命令发送 */
        public static final String TRIGGER_SENT = "TRIGGER_SENT";
        /** 触发命令重试 */
        public static final String TRIGGER_RETRY = "TRIGGER_RETRY";
        /** 触发成功 */
        public static final String TRIGGER_SUCCESS = "TRIGGER_SUCCESS";
        /** 触发失败 */
        public static final String TRIGGER_FAILED = "TRIGGER_FAILED";
        /** URL命令发送 */
        public static final String URL_SENT = "URL_SENT";
        /** URL接收确认 */
        public static final String URL_RECEIVED = "URL_RECEIVED";
        /** URL接收失败 */
        public static final String URL_RECEIVE_FAILED = "URL_RECEIVE_FAILED";
        /** 固件下载中 */
        public static final String DOWNLOADING = "DOWNLOADING";
        /** 下载完成 */
        public static final String DOWNLOAD_COMPLETE = "DOWNLOAD_COMPLETE";
        /** 升级开始 */
        public static final String UPGRADE_START = "UPGRADE_START";
        /** 升级进度更新 */
        public static final String UPGRADE_PROGRESS = "UPGRADE_PROGRESS";
        /** 升级完成 */
        public static final String UPGRADE_COMPLETE = "UPGRADE_COMPLETE";
        /** 升级失败 */
        public static final String UPGRADE_FAILED = "UPGRADE_FAILED";
        /** 设备断线 */
        public static final String DEVICE_DISCONNECTED = "DEVICE_DISCONNECTED";
        /** 设备重连 */
        public static final String DEVICE_RECONNECTED = "DEVICE_RECONNECTED";
        /** 任务超时 */
        public static final String TASK_TIMEOUT = "TASK_TIMEOUT";
        /** 任务取消 */
        public static final String TASK_CANCELLED = "TASK_CANCELLED";
    }
}
