/**
 * 核心模型包
 * <p>
 * 包含 newgateway 特有的数据模型类：
 * <ul>
 *     <li>{@link cn.iocoder.yudao.module.iot.newgateway.core.model.CommandResult} - 命令执行结果</li>
 *     <li>{@link cn.iocoder.yudao.module.iot.newgateway.core.model.HeartbeatData} - 心跳数据</li>
 *     <li>{@link cn.iocoder.yudao.module.iot.newgateway.core.model.DeviceCommand} - 设备命令</li>
 *     <li>{@link cn.iocoder.yudao.module.iot.newgateway.core.model.PluginDescriptor} - 插件描述符</li>
 * </ul>
 * </p>
 * <p>
 * 注意：公共模型类（如 DeviceStateChangeMessage、DeviceInfo 等）应复用 iot-core 模块中的定义，
 * 不在此包中重复定义。
 * </p>
 *
 * @author IoT Gateway Team
 */
package cn.iocoder.yudao.module.iot.newgateway.core.model;
