/**
 * IPC 摄像头插件包
 * 
 * <p>实现独立 IPC 摄像头设备的接入，支持以下功能：</p>
 * <ul>
 *     <li>设备登录/登出（大华 SDK / ONVIF）</li>
 *     <li>实时预览</li>
 *     <li>PTZ 云台控制</li>
 *     <li>抓图</li>
 *     <li>移动侦测事件</li>
 *     <li>OSD 设置</li>
 * </ul>
 * 
 * <p>适用场景：</p>
 * <ul>
 *     <li>独立部署的 IPC（不通过 NVR 管理）</li>
 *     <li>智能摄像头（人脸识别、车牌识别）</li>
 *     <li>需要直接与 IPC 交互获取 AI 分析结果</li>
 * </ul>
 * 
 * @author IoT Gateway Team
 * @since 1.0
 */
package cn.iocoder.yudao.module.iot.newgateway.plugins.ipc;
