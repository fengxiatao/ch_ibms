/**
 * 报警主机插件包
 * 
 * <p>实现报警主机设备的接入，支持 PS600 OPC 协议。</p>
 * 
 * <h2>主要组件</h2>
 * <ul>
 *     <li>{@link cn.iocoder.yudao.module.iot.newgateway.plugins.alarm.AlarmPlugin} - 插件入口，实现 PassiveDeviceHandler</li>
 *     <li>{@link cn.iocoder.yudao.module.iot.newgateway.plugins.alarm.AlarmConnectionManager} - 连接管理器</li>
 *     <li>{@link cn.iocoder.yudao.module.iot.newgateway.plugins.alarm.AlarmConfig} - 插件配置</li>
 * </ul>
 * 
 * <h2>支持的命令</h2>
 * <ul>
 *     <li>ARM_ALL - 全局布防</li>
 *     <li>ARM_STAY - 留守布防</li>
 *     <li>DISARM - 撤防</li>
 *     <li>ARM_PARTITION - 分区布防</li>
 *     <li>DISARM_PARTITION - 分区撤防</li>
 *     <li>BYPASS_ZONE - 旁路防区</li>
 *     <li>UNBYPASS_ZONE - 取消旁路</li>
 *     <li>QUERY_STATUS - 状态查询</li>
 * </ul>
 * 
 * <h2>配置示例</h2>
 * <pre>
 * iot:
 *   newgateway:
 *     plugins:
 *       alarm:
 *         enabled: true
 *         port: 9500
 *         heartbeat-timeout: 60000
 *         default-password: "1234"
 * </pre>
 * 
 * @author IoT Gateway Team
 */
package cn.iocoder.yudao.module.iot.newgateway.plugins.alarm;
