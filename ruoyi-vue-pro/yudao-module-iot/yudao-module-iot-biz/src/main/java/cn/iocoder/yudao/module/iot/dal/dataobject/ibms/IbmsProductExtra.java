package cn.iocoder.yudao.module.iot.dal.dataobject.ibms;

import lombok.Data;

/**
 * {@code ibms_product.extra} JSON 结构约定（与历史 {@code iot_product} 独有字段对齐）。
 * <p>
 * 所有字段均可选；未设置时序列化可省略。统一通过 {@link cn.iocoder.yudao.module.iot.service.ibms.product.IbmsProductExtraHelper} 读写。
 * </p>
 * <p>
 * JSON 键名（camelCase）：
 * </p>
 * <ul>
 *   <li>{@code productKey} — 对接网关/云平台的 productKey（若与列含义重复，以列为准时可不写入）</li>
 *   <li>{@code menuIds} — 关联菜单 ID 列表的 JSON 数组字符串，如 {@code "[1001,1002]"}</li>
 *   <li>{@code primaryMenuId} — 主菜单 ID</li>
 *   <li>{@code picUrl} — 产品图片 URL</li>
 *   <li>{@code status} — 产品状态（数值，对齐原 IoT 产品状态枚举）</li>
 *   <li>{@code deviceType} — 设备类型（数值）</li>
 *   <li>{@code netType} — 联网方式（数值）</li>
 *   <li>{@code locationType} — 定位方式（数值）</li>
 *   <li>{@code codecType} — 编解码类型（字典/字符串，供网关解析报文）</li>
 *   <li>{@code jobConfig} — 定时任务配置 JSON 字符串</li>
 * </ul>
 */
@Data
public class IbmsProductExtra {

    private String productKey;
    private String menuIds;
    private Long primaryMenuId;
    private String picUrl;
    private Integer status;
    private Integer deviceType;
    private Integer netType;
    private Integer locationType;
    private String codecType;
    private String jobConfig;
}
