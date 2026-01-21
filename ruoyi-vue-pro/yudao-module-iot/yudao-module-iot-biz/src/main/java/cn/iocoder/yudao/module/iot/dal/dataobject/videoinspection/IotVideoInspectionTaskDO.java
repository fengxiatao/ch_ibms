package cn.iocoder.yudao.module.iot.dal.dataobject.videoinspection;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.*;

import java.util.List;

/**
 * 视频巡检任务 DO
 * 
 * @author system
 */
@TableName(value = "iot_video_inspection_task", autoResultMap = true)
@KeySequence("iot_video_inspection_task_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IotVideoInspectionTaskDO extends TenantBaseDO {

    /**
     * 任务ID
     */
    @TableId
    private Long id;

    /**
     * 任务名称
     */
    private String taskName;

    /**
     * 分屏布局（1x1, 2x2, 3x3, 4x4等）
     */
    private String layout;

    /**
     * 场景配置（JSON格式）
     * 使用 JacksonTypeHandler 自动序列化/反序列化
     */
    @com.baomidou.mybatisplus.annotation.TableField(typeHandler = JacksonTypeHandler.class)
    private List<InspectionSceneDO> scenes;

    /**
     * 任务状态（draft-草稿, active-运行中, paused-已暂停）
     */
    private String status;

    /**
     * 场景配置内部类
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InspectionSceneDO {
        /**
         * 格子索引
         */
        private Integer cellIndex;

        /**
         * 通道列表
         */
        private List<InspectionChannelDO> channels;
    }

    /**
     * 通道配置内部类
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InspectionChannelDO {
        /**
         * 设备ID
         */
        private Long deviceId;

        /**
         * 通道ID
         */
        private Long channelId;

        /**
         * 通道名称
         */
        private String channelName;

        /**
         * 播放时长（秒，0表示持续播放）
         */
        private Integer duration;

        /**
         * 设备IP
         */
        private String ipAddress;

        /**
         * 产品ID
         */
        private Long productId;

        /**
         * 配置信息（JSON字符串）
         */
        private String config;

        /**
         * 流地址
         */
        private String streamUrl;

        /**
         * NVR ID
         */
        private Long nvrId;

        /**
         * 通道号
         */
        private Integer channelNo;

        /**
         * 位置信息
         */
        private String location;

        /**
         * 快照地址
         */
        private String snapshot;
    }
}
