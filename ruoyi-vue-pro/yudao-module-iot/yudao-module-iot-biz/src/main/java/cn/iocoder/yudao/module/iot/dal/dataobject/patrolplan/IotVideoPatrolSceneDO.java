package cn.iocoder.yudao.module.iot.dal.dataobject.patrolplan;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * IoT 轮巡场景 DO
 *
 * @author 长辉信息
 */
@TableName(value = "iot_video_patrol_scene")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IotVideoPatrolSceneDO extends BaseDO {

    /**
     * ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 所属任务ID
     */
    private Long taskId;

    /**
     * 场景名称
     */
    private String sceneName;

    /**
     * 场景顺序
     */
    private Integer sceneOrder;

    /**
     * 停留时长(秒)
     */
    private Integer duration;

    /**
     * 分屏布局
     * 1x1, 2x2, 3x3, 4x4
     */
    private String gridLayout;

    /**
     * 窗格数量
     */
    private Integer gridCount;

    /**
     * 场景描述
     */
    private String description;

    /**
     * 状态
     * 0: 停用
     * 1: 启用
     */
    private Integer status;

}
