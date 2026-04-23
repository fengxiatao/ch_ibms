package cn.iocoder.yudao.module.iot.dal.dataobject.ibms;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * IBMS 空间（树）DO
 *
 * 对应表：ibms_space
 */
@TableName(value = "ibms_space", autoResultMap = true)
@KeySequence("ibms_space_seq")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class IbmsSpaceDO extends TenantBaseDO {

    /** 空间ID */
    @TableId
    private Long id;

    /** 父空间ID（0 为根） */
    private Long parentId;

    /** 空间编码（组合）：code[-subCode]，如 F01 / F01-LBY */
    private String spaceCode;

    /** 区域码，如 F01/B01/PK/LB/OUT */
    private String code;

    /** 子区域码，如 LBY/FM（可选） */
    private String subCode;

    /** 空间名称 */
    private String name;

    /** 空间类型：floor/area/room */
    private String type;

    /** 排序 */
    private Integer sort;

    /** 扩展 JSON 字符串 */
    private String extra;
}

