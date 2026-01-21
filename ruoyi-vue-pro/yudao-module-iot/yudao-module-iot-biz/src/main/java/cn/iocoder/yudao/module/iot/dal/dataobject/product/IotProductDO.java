package cn.iocoder.yudao.module.iot.dal.dataobject.product;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * IoT 产品 DO
 *
 * @author ahh
 */
@TableName("iot_product")
@KeySequence("iot_product_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IotProductDO extends TenantBaseDO {

    /**
     * 产品 ID
     */
    @TableId
    private Long id;
    /**
     * 产品名称
     */
    private String name;
    /**
     * 产品标识
     */
    private String productKey;
    /**
     * 关联的菜单ID列表（JSON数组）
     * <p>
     * 示例：[1001, 1002, 1003]
     * 表示该产品关联到菜单ID 1001、1002、1003
     * 关联 system_menu 表的 id 字段
     */
    private String menuIds;
    /**
     * 主要菜单ID
     * <p>
     * 用于首页/默认显示，应该是 menuIds 中的一个
     * 关联 system_menu 表的 id 字段
     */
    private Long primaryMenuId;
    /**
     * 产品图标
     */
    private String icon;
    /**
     * 产品图片
     */
    private String picUrl;
    /**
     * 产品描述
     */
    private String description;

    /**
     * 产品状态
     * <p>
     * 枚举 {@link cn.iocoder.yudao.module.iot.enums.product.IotProductStatusEnum}
     */
    private Integer status;
    /**
     * 设备类型
     * <p>
     * 枚举 {@link cn.iocoder.yudao.module.iot.enums.product.IotProductDeviceTypeEnum}
     */
    private Integer deviceType;
    /**
     * 联网方式
     * <p>
     * 枚举 {@link cn.iocoder.yudao.module.iot.enums.product.IotNetTypeEnum}
     */
    private Integer netType;
    /**
     * 定位方式
     * <p>
     * 枚举 {@link cn.iocoder.yudao.module.iot.enums.product.IotLocationTypeEnum}
     */
    private Integer locationType;
    /**
     * 数据格式（编解码器类型）
     * <p>
     * 字典 {@link cn.iocoder.yudao.module.iot.enums.DictTypeConstants#CODEC_TYPE}
     *
     * 目的：用于 gateway-server 解析消息格式
     */
    private String codecType;

    /**
     * 定时任务配置（JSON格式）
     * <p>
     * 示例：{"offlineCheck":{"enabled":true,"interval":10,"unit":"MINUTE","priority":3}}
     */
    private String jobConfig;

    // ====================  已废弃字段（保留用于数据迁移，后续可删除）  ====================
    // /**
    //  * 产品分类编号（已废弃，使用 menuIds 替代）
    //  * <p>
    //  * 关联 {@link IotProductCategoryDO#getId()}
    //  */
    // private Long categoryId;
    //
    // /**
    //  * 所属子系统代码（已废弃，使用 menuIds 替代）
    //  * <p>
    //  * 例如：security.video（视频监控）、security.access（门禁控制）
    //  * 关联 iot_subsystem 表的 code 字段
    //  */
    // private String subsystemCode;
    //
    // /**
    //  * 支持的子系统（已废弃，使用 menuIds 替代）
    //  * <p>
    //  * 用于跨系统产品，例如AI摄像机可以同时属于视频监控和AI分析
    //  * 示例：["security.video", "security.alarm", "ai.analysis"]
    //  */
    // private String supportedSubsystems;

}