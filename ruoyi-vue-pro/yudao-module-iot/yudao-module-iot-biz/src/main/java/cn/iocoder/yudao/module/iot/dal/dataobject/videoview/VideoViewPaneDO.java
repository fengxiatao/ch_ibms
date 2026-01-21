package cn.iocoder.yudao.module.iot.dal.dataobject.videoview;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * 视频监控 - 实时预览视图窗格 DO
 *
 * @author 芋道源码
 */
@TableName("iot_video_view_pane")
@KeySequence("iot_video_view_pane_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VideoViewPaneDO extends TenantBaseDO {

    /**
     * 窗格ID
     */
    @TableId
    private Long id;

    /**
     * 视图ID
     */
    private Long viewId;

    /**
     * 窗格索引(0-15)
     */
    private Integer paneIndex;

    /**
     * 通道ID
     */
    private Long channelId;

    /**
     * 设备ID
     */
    private Long deviceId;

    /**
     * 通道号
     */
    private Integer channelNo;

    /**
     * 通道名称
     */
    private String channelName;

    /**
     * 目标IP
     */
    private String targetIp;

    /**
     * 目标通道号
     */
    private Integer targetChannelNo;

    /**
     * 主码流URL
     */
    private String streamUrlMain;

    /**
     * 子码流URL
     */
    private String streamUrlSub;

    /**
     * 其他配置(JSON格式)
     */
    private String config;

}
