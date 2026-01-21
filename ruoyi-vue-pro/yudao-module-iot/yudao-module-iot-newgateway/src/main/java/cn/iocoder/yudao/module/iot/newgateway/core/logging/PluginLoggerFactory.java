package cn.iocoder.yudao.module.iot.newgateway.core.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 插件日志工厂
 * 
 * <p>提供统一的日志创建和格式化功能，确保所有插件使用一致的日志格式。</p>
 * 
 * <h2>日志格式规范</h2>
 * <ul>
 *     <li>所有插件日志必须使用 [{PluginName}] 前缀</li>
 *     <li>日志级别遵循标准：ERROR > WARN > INFO > DEBUG > TRACE</li>
 *     <li>关键操作（连接、断开、命令执行）使用 INFO 级别</li>
 *     <li>心跳、数据接收等高频操作使用 DEBUG 级别</li>
 *     <li>异常信息使用 ERROR 级别，并包含完整堆栈</li>
 * </ul>
 * 
 * <h2>使用示例</h2>
 * <pre>
 * {@code
 * // 在插件类中定义日志前缀
 * private static final String LOG_PREFIX = PluginLoggerFactory.createLogPrefix("AlarmPlugin");
 * 
 * // 使用日志前缀
 * log.info("{} 设备连接: deviceId={}", LOG_PREFIX, deviceId);
 * }
 * </pre>
 * 
 * @author IoT Gateway Team
 */
public final class PluginLoggerFactory {

    private PluginLoggerFactory() {
        // 工具类，禁止实例化
    }

    /**
     * 创建日志前缀
     * 
     * @param pluginName 插件名称
     * @return 格式化的日志前缀，如 "[AlarmPlugin]"
     */
    public static String createLogPrefix(String pluginName) {
        return "[" + pluginName + "]";
    }

    /**
     * 获取插件日志记录器
     * 
     * @param clazz 插件类
     * @return Logger 实例
     */
    public static Logger getLogger(Class<?> clazz) {
        return LoggerFactory.getLogger(clazz);
    }

    /**
     * 获取插件日志记录器
     * 
     * @param name 日志记录器名称
     * @return Logger 实例
     */
    public static Logger getLogger(String name) {
        return LoggerFactory.getLogger(name);
    }

    /**
     * 格式化设备操作日志消息
     * 
     * @param prefix    日志前缀
     * @param operation 操作名称
     * @param deviceId  设备ID
     * @return 格式化的日志消息
     */
    public static String formatDeviceLog(String prefix, String operation, Long deviceId) {
        return String.format("%s %s: deviceId=%d", prefix, operation, deviceId);
    }

    /**
     * 格式化命令执行日志消息
     * 
     * @param prefix      日志前缀
     * @param deviceId    设备ID
     * @param commandType 命令类型
     * @return 格式化的日志消息
     */
    public static String formatCommandLog(String prefix, Long deviceId, String commandType) {
        return String.format("%s 执行命令: deviceId=%d, commandType=%s", prefix, deviceId, commandType);
    }

    /**
     * 格式化连接日志消息
     * 
     * @param prefix     日志前缀
     * @param deviceId   设备ID
     * @param identifier 设备标识符
     * @return 格式化的日志消息
     */
    public static String formatConnectLog(String prefix, Long deviceId, String identifier) {
        return String.format("%s 设备连接: deviceId=%d, identifier=%s", prefix, deviceId, identifier);
    }

    /**
     * 格式化断开连接日志消息
     * 
     * @param prefix   日志前缀
     * @param deviceId 设备ID
     * @return 格式化的日志消息
     */
    public static String formatDisconnectLog(String prefix, Long deviceId) {
        return String.format("%s 设备断开: deviceId=%d", prefix, deviceId);
    }

    /**
     * 格式化错误日志消息
     * 
     * @param prefix    日志前缀
     * @param operation 操作名称
     * @param deviceId  设备ID
     * @param error     错误信息
     * @return 格式化的日志消息
     */
    public static String formatErrorLog(String prefix, String operation, Long deviceId, String error) {
        return String.format("%s %s失败: deviceId=%d, error=%s", prefix, operation, deviceId, error);
    }
}
