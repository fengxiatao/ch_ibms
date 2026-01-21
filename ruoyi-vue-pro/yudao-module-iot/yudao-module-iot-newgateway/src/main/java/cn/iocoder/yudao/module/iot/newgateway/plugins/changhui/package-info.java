/**
 * 长辉TCP模拟设备插件
 * 
 * <p>支持长辉自研TCP协议设备的接入，包括：</p>
 * <ul>
 *     <li>心跳保活（AFN=0x3C）</li>
 *     <li>远程升级触发（AFN=0x02）</li>
 *     <li>升级URL下发（AFN=0x10）</li>
 *     <li>升级状态上报（AFN=0x15/0x66/0x67/0x68）</li>
 * </ul>
 * 
 * <h2>协议特点</h2>
 * <ul>
 *     <li>帧头：EF7EEF（3字节）</li>
 *     <li>测站编码：10字节，用于设备标识</li>
 *     <li>被动连接模式：设备主动连接平台</li>
 * </ul>
 * 
 * <h2>配置示例</h2>
 * <pre>
 * iot:
 *   newgateway:
 *     plugins:
 *       changhui:
 *         enabled: true
 *         port: 9700
 *         heartbeat-timeout: 60000
 *         upgrade-timeout: 300000
 * </pre>
 * 
 * @author IoT Gateway Team
 * @see cn.iocoder.yudao.module.iot.newgateway.plugins.changhui.ChanghuiPlugin
 * @see cn.iocoder.yudao.module.iot.newgateway.plugins.changhui.ChanghuiConnectionManager
 * @see cn.iocoder.yudao.module.iot.newgateway.plugins.changhui.ChanghuiConfig
 */
package cn.iocoder.yudao.module.iot.newgateway.plugins.changhui;
