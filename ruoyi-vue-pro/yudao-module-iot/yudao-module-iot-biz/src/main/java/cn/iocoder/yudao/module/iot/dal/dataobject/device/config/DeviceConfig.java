package cn.iocoder.yudao.module.iot.dal.dataobject.device.config;

import jakarta.validation.ValidationException;
import java.util.Map;

/**
 * 设备配置接口
 * <p>
 * 用于定义不同设备类型的特有配置。每种设备类型(如门禁设备、长辉设备等)
 * 都应该实现此接口,提供类型安全的配置管理。
 * <p>
 * 配置对象通过 {@link #toMap()} 和 {@link #fromMap(Map)} 方法实现
 * JSON 序列化和反序列化,由 MyBatis TypeHandler 自动处理。
 *
 * @author system
 * @since 2024-12-11
 */
public interface DeviceConfig {

    /**
     * 获取设备类型标识
     * <p>
     * 每种设备类型必须返回唯一的类型标识,用于配置工厂识别和创建对应的配置对象。
     * 类型标识应该使用大写字母,例如: "ACCESS"、"DETONG"、"VIDEO" 等。
     *
     * @return 设备类型标识,不能为 null
     */
    String getDeviceType();

    /**
     * 验证配置的有效性
     * <p>
     * 在配置被保存到数据库之前,应该调用此方法验证配置的完整性和正确性。
     * 验证内容包括但不限于:
     * <ul>
     *   <li>必填字段是否存在</li>
     *   <li>字段格式是否正确(如 IP 地址、端口号等)</li>
     *   <li>字段值是否在有效范围内</li>
     *   <li>字段之间的逻辑关系是否合理</li>
     * </ul>
     *
     * @throws ValidationException 如果配置无效,抛出此异常并包含详细的错误信息
     */
    void validate() throws ValidationException;

    /**
     * 将配置对象转换为 Map,用于 JSON 序列化
     * <p>
     * 此方法由 MyBatis TypeHandler 调用,将配置对象序列化为 JSON 字符串存储到数据库。
     * Map 的 key 应该使用驼峰命名(camelCase),与 JSON 字段名保持一致。
     * <p>
     * 注意事项:
     * <ul>
     *   <li>必须包含 "deviceType" 字段,值为 {@link #getDeviceType()} 的返回值</li>
     *   <li>null 值字段可以不包含在 Map 中,或者显式设置为 null</li>
     *   <li>复杂对象应该转换为可序列化的基本类型或 Map/List</li>
     * </ul>
     *
     * @return 包含配置数据的 Map,不能为 null
     */
    Map<String, Object> toMap();

    /**
     * 从 Map 反序列化配置对象,用于 JSON 反序列化
     * <p>
     * 此方法由 MyBatis TypeHandler 调用,从数据库读取 JSON 字符串并反序列化为配置对象。
     * 实现时应该从 Map 中提取字段值并设置到当前对象的属性中。
     * <p>
     * 注意事项:
     * <ul>
     *   <li>应该处理字段不存在的情况,使用默认值或 null</li>
     *   <li>应该处理类型转换,确保类型安全</li>
     *   <li>可以忽略未知字段,以支持向后兼容</li>
     *   <li>不需要验证配置有效性,验证由 {@link #validate()} 方法负责</li>
     * </ul>
     *
     * @param map 包含配置数据的 Map,不能为 null
     */
    void fromMap(Map<String, Object> map);

    /**
     * 获取设备 IP 地址（可选）
     * <p>
     * 并非所有设备都有 IP 地址（如长辉设备是被动连接，没有 IP）。
     * 默认返回 null，由具体实现类覆盖此方法返回实际的 IP 地址。
     *
     * @return 设备 IP 地址，如果设备没有 IP 地址则返回 null
     */
    default String getIpAddress() {
        return null;
    }

    /**
     * 获取设备端口号（可选）
     * <p>
     * 并非所有设备都有端口号。
     * 默认返回 null，由具体实现类覆盖此方法返回实际的端口号。
     *
     * @return 设备端口号，如果设备没有端口号则返回 null
     */
    default Integer getPort() {
        return null;
    }

}
