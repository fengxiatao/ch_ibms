/**
 * 命令执行器包
 * <p>
 * 提供设备命令的异步执行能力，实现线程池隔离，
 * 避免 SDK 类设备的同步阻塞影响 RocketMQ 消费线程。
 * </p>
 *
 * <p>核心组件：</p>
 * <ul>
 *     <li>{@link cn.iocoder.yudao.module.iot.newgateway.core.executor.GatewayExecutorConfig} - 线程池配置</li>
 *     <li>{@link cn.iocoder.yudao.module.iot.newgateway.core.executor.DeviceCommandExecutorService} - 异步执行服务</li>
 * </ul>
 *
 * <p>线程池说明：</p>
 * <ul>
 *     <li>sdkCommandExecutor：专用于 SDK 设备（门禁、NVR），8-16 线程</li>
 *     <li>deviceCommandExecutor：通用设备命令执行，4-8 线程</li>
 * </ul>
 *
 * @author IoT Gateway Team
 */
package cn.iocoder.yudao.module.iot.newgateway.core.executor;
