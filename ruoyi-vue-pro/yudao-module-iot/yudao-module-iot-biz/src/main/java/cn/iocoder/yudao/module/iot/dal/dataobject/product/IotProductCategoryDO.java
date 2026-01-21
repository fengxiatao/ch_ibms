package cn.iocoder.yudao.module.iot.dal.dataobject.product;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * IoT 产品分类 DO
 *
 * @author 长辉信息科技有限公司
 */
@TableName("iot_product_category")
@KeySequence("iot_product_category_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IotProductCategoryDO extends BaseDO {

    /**
     * 分类 ID
     */
    @TableId
    private Long id;
    /**
     * 分类名字
     */
    private String name;
    /**
     * 分类排序
     */
    private Integer sort;
    /**
     * 分类状态
     *
     * 枚举 {@link cn.iocoder.yudao.framework.common.enums.CommonStatusEnum}
     */
    private Integer status;
    /**
     * 分类描述
     */
    private String description;
    
    /**
     * 父分类 ID（0 表示顶层分类）
     */
    private Long parentId;
    
    /**
     * 分类层级（1-顶层，2-二级，3-三级）
     */
    private Integer level;
    
    /**
     * 关联的 IBMS 模块编码
     * 例如：building（智慧建筑）、security（智慧安防）、access（智慧通行）、
     *      fire（智慧消防）、energy（智慧能源）
     */
    private String moduleCode;

}