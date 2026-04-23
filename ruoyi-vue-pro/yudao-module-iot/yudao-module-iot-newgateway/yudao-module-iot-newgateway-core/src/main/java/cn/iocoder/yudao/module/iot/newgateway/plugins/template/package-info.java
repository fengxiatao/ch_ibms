/**
 * 插件模板包
 * 
 * <p>提供设备插件的参考实现模板，开发新设备插件时可以复制此模板并修改。</p>
 * 
 * <p>模板包含以下组件：</p>
 * <ul>
 *   <li>{@link cn.iocoder.yudao.module.iot.newgateway.plugins.template.TemplatePlugin} - 插件入口类</li>
 *   <li>{@link cn.iocoder.yudao.module.iot.newgateway.plugins.template.TemplateConnectionManager} - 连接管理器</li>
 *   <li>{@link cn.iocoder.yudao.module.iot.newgateway.plugins.template.TemplateConfig} - 插件配置类</li>
 * </ul>
 * 
 * <p>使用步骤：</p>
 * <ol>
 *   <li>复制 template 目录到 plugins/{your_device_type}/</li>
 *   <li>重命名所有类，将 Template 替换为你的设备类型名称</li>
 *   <li>修改 @DevicePlugin 注解中的属性</li>
 *   <li>实现设备特定的协议解析和命令处理逻辑</li>
 *   <li>在 application.yaml 中配置插件参数</li>
 * </ol>
 * 
 * @author IoT Gateway Team
 * @see cn.iocoder.yudao.module.iot.newgateway.core.annotation.DevicePlugin
 * @see cn.iocoder.yudao.module.iot.newgateway.core.handler.PassiveDeviceHandler
 */
package cn.iocoder.yudao.module.iot.newgateway.plugins.template;
