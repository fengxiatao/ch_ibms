/**
 * 门禁一代设备插件包
 * 
 * <p>实现大华门禁一代设备的接入，使用 Recordset 操作进行用户/卡管理。</p>
 * 
 * <h2>设备特点</h2>
 * <ul>
 *     <li>使用大华 NetSDK 进行设备登录和通信</li>
 *     <li>使用 Recordset 操作进行卡号/用户管理（通过 recNo）</li>
 *     <li>支持开门、关门、授权下发、授权撤销等命令</li>
 *     <li>支持门禁事件订阅和回调</li>
 * </ul>
 * 
 * <h2>支持的命令</h2>
 * <ul>
 *     <li>OPEN_DOOR - 远程开门</li>
 *     <li>CLOSE_DOOR - 远程关门</li>
 *     <li>DISPATCH_AUTH - 下发授权（通过 Recordset）</li>
 *     <li>REVOKE_AUTH - 撤销授权（通过 Recordset）</li>
 * </ul>
 * 
 * <h2>配置示例</h2>
 * <pre>
 * iot:
 *   newgateway:
 *     plugins:
 *       access-gen1:
 *         enabled: true
 *         keepalive-interval: 60000
 *         reconnect-interval: 30000
 * </pre>
 * 
 * @author IoT Gateway Team
 * @see cn.iocoder.yudao.module.iot.newgateway.plugins.accessgen1.AccessGen1Plugin
 * @see cn.iocoder.yudao.module.iot.newgateway.plugins.accessgen1.AccessGen1Config
 */
package cn.iocoder.yudao.module.iot.newgateway.plugins.accessgen1;
