package cn.iocoder.yudao.module.iot.dal.dataobject.patrolplan;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.*;

import java.util.Map;

/**
 * IoT 轮巡场景通道 DO
 *
 * @author 长辉信息
 */
@TableName(value = "iot_video_patrol_scene_channel", autoResultMap = true)
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IotVideoPatrolSceneChannelDO extends BaseDO {

    /**
     * ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 所属场景ID
     */
    private Long sceneId;

    /**
     * 窗格位置（1开始，对应分屏格子）
     */
    private Integer gridPosition;

    /**
     * 该通道的播放时长（秒）
     */
    private Integer duration;

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
     * 设备IP地址
     */
    private String targetIp;

    /**
     * 目标通道号
     */
    private Integer targetChannelNo;

    /**
     * 主码流地址
     */
    private String streamUrlMain;

    /**
     * 子码流地址
     */
    private String streamUrlSub;

    /**
     * 配置信息（包含用户名、密码、端口等）
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> config;

}
