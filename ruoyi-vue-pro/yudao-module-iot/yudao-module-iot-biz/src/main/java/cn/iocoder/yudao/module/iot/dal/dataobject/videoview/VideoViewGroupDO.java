package cn.iocoder.yudao.module.iot.dal.dataobject.videoview;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * 视频监控 - 实时预览视图分组 DO
 *
 * @author 芋道源码
 */
@TableName("iot_video_view_group")
@KeySequence("iot_video_view_group_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VideoViewGroupDO extends TenantBaseDO {

    /**
     * 分组ID
     */
    @TableId
    private Long id;

    /**
     * 分组名称
     */
    private String name;

    /**
     * 图标
     */
    private String icon;

    /**
     * 排序
     */
    private Integer sort;

}
