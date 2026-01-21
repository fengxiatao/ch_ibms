/**
 * 门禁二代插件包
 * 
 * <p>实现大华门禁二代设备的接入，使用标准 API 进行用户/卡/人脸管理。</p>
 * 
 * <h2>与门禁一代的区别</h2>
 * <ul>
 *     <li>门禁一代：使用 Recordset 操作，通过 recNo 管理卡号/用户</li>
 *     <li>门禁二代：使用标准 API，支持人脸/指纹等生物识别功能</li>
 * </ul>
 * 
 * <h2>主要功能</h2>
 * <ul>
 *     <li>远程开门/关门</li>
 *     <li>用户管理（增删改查）</li>
 *     <li>卡片管理（增删改查）</li>
 *     <li>人脸下发</li>
 *     <li>指纹下发</li>
 *     <li>门禁事件订阅</li>
 * </ul>
 * 
 * <h2>配置前缀</h2>
 * <p>{@code iot.newgateway.plugins.access-gen2}</p>
 * 
 * @author IoT Gateway Team
 * @see cn.iocoder.yudao.module.iot.newgateway.plugins.accessgen2.AccessGen2Plugin
 * @see cn.iocoder.yudao.module.iot.newgateway.plugins.accessgen2.AccessGen2Config
 */
package cn.iocoder.yudao.module.iot.newgateway.plugins.accessgen2;
