/**
 * NVR 录像机插件包
 * 
 * <p>实现大华 NVR 录像机设备的接入，支持以下功能：</p>
 * <ul>
 *     <li>设备登录/登出</li>
 *     <li>PTZ 云台控制</li>
 *     <li>录像回放控制</li>
 *     <li>录像文件查询</li>
 *     <li>通道信息查询</li>
 *     <li>硬盘状态查询</li>
 *     <li>实时抓图</li>
 * </ul>
 * 
 * <h2>主要组件</h2>
 * <ul>
 *     <li>{@link cn.iocoder.yudao.module.iot.newgateway.plugins.nvr.NvrPlugin} - 插件入口</li>
 *     <li>{@link cn.iocoder.yudao.module.iot.newgateway.plugins.nvr.NvrConfig} - 插件配置</li>
 *     <li>{@link cn.iocoder.yudao.module.iot.newgateway.plugins.nvr.NvrConnectionManager} - 连接管理</li>
 *     <li>{@link cn.iocoder.yudao.module.iot.newgateway.plugins.nvr.NvrSdkWrapper} - SDK 封装</li>
 * </ul>
 * 
 * @author IoT Gateway Team
 */
package cn.iocoder.yudao.module.iot.newgateway.plugins.nvr;
