/**
 * 长辉设备服务包
 * 
 * <p>提供长辉TCP模拟设备的业务服务，主要功能包括：</p>
 * <ul>
 *     <li>远程升级触发</li>
 *     <li>升级URL下发</li>
 *     <li>设备状态查询</li>
 * </ul>
 * 
 * <p>所有命令通过统一的 {@link cn.iocoder.yudao.module.iot.mq.producer.DeviceCommandPublisher}
 * 发送到 DEVICE_SERVICE_INVOKE 主题，由 newgateway 的 ChanghuiPlugin 处理。</p>
 * 
 * <h2>设备类型</h2>
 * <p>deviceType = "CHANGHUI"</p>
 * 
 * <h2>支持的命令</h2>
 * <ul>
 *     <li>UPGRADE_TRIGGER - 升级触发</li>
 *     <li>UPGRADE_URL - 升级URL下发</li>
 *     <li>QUERY_STATUS - 状态查询</li>
 * </ul>
 * 
 * @author 长辉信息科技有限公司
 * @see cn.iocoder.yudao.module.iot.enums.device.ChanghuiDeviceTypeConstants
 * @see cn.iocoder.yudao.module.iot.mq.producer.DeviceCommandPublisher
 */
package cn.iocoder.yudao.module.iot.service.changhui;
