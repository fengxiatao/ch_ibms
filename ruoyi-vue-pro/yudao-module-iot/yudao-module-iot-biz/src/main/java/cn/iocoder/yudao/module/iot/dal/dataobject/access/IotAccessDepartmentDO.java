package cn.iocoder.yudao.module.iot.dal.dataobject.access;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.util.List;

/**
 * 门禁组织架构（部门） DO
 *
 * @author 芋道源码
 */
@TableName("iot_access_department")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IotAccessDepartmentDO extends TenantBaseDO {

    /**
     * 部门ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 父部门ID（0表示根节点）
     */
    private Long parentId;

    /**
     * 部门名称
     */
    private String deptName;

    /**
     * 部门编码
     */
    private String deptCode;

    /**
     * 显示顺序
     */
    private Integer sort;

    /**
     * 负责人
     */
    private String leader;

    /**
     * 联系电话
     */
    private String phone;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 状态：0-正常，1-停用
     */
    private Integer status;

    /**
     * 子部门列表（非数据库字段，用于树形结构）
     */
    @TableField(exist = false)
    private List<IotAccessDepartmentDO> children;

    /**
     * 部门人员数量（非数据库字段，用于前端显示）
     */
    @TableField(exist = false)
    private Integer personCount;

}
