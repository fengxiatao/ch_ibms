/**
 * 监控指标包
 * 
 * <p>提供网关和插件的监控指标功能，通过 Micrometer 暴露到 /actuator/metrics 端点。</p>
 * 
 * <h2>指标类型</h2>
 * <ul>
 *     <li>在线设备数量 (Gauge): gateway.devices.online</li>
 *     <li>消息计数 (Counter): gateway.messages</li>
 *     <li>错误计数 (Counter): gateway.errors</li>
 *     <li>命令执行时间 (Timer): gateway.commands.duration</li>
 * </ul>
 * 
 * <h2>端点访问</h2>
 * <ul>
 *     <li>/actuator/metrics - 标准 Micrometer 指标端点</li>
 *     <li>/actuator/gateway-metrics - 网关自定义指标端点</li>
 *     <li>/actuator/gateway-metrics/{pluginId} - 指定插件指标</li>
 * </ul>
 * 
 * @author IoT Gateway Team
 */
package cn.iocoder.yudao.module.iot.newgateway.core.metrics;
