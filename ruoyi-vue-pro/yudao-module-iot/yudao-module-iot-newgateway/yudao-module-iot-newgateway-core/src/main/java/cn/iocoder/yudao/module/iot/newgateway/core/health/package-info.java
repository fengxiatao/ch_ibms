/**
 * 健康检查包
 * 
 * <p>提供网关和插件的健康检查功能，通过 Spring Boot Actuator 暴露健康状态。</p>
 * 
 * <h2>端点访问</h2>
 * <ul>
 *     <li>/actuator/health - 标准健康检查端点，包含插件健康状态</li>
 *     <li>/actuator/gateway - 网关详细状态端点</li>
 *     <li>/actuator/gateway/{pluginId} - 指定插件详细状态</li>
 * </ul>
 * 
 * @author IoT Gateway Team
 */
package cn.iocoder.yudao.module.iot.newgateway.core.health;
