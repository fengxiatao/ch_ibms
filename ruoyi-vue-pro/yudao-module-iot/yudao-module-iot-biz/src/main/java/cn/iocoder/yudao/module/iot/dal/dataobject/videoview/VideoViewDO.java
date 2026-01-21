package cn.iocoder.yudao.module.iot.dal.dataobject.videoview;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * 视频监控 - 实时预览视图 DO
 *
 * @author 芋道源码
 */
@TableName("iot_video_view")
@KeySequence("iot_video_view_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VideoViewDO extends TenantBaseDO {

    /**
     * 视图ID
     */
    @TableId
    private Long id;

    /**
     * 视图名称
     */
    private String name;

    /**
     * 分组ID列表（JSON数组，如：[1,2,3]）
     */
    private String groupIds;

    /**
     * 分屏格式(1/4/6/9/16)
     */
    private Integer gridLayout;

    /**
     * 视图描述
     */
    private String description;

    /**
     * 是否默认视图
     * 0: 否
     * 1: 是
     */
    @TableField(typeHandler = cn.iocoder.yudao.framework.mybatis.core.type.BooleanToIntTypeHandler.class)
    private Boolean isDefault;

    /**
     * 排序
     */
    private Integer sort;

}
