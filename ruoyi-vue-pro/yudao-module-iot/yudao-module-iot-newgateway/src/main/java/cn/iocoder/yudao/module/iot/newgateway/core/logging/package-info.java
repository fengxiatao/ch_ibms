/**
 * 日志工具包
 * 
 * <p>提供统一的日志格式化和日志工厂功能，确保所有插件使用一致的日志格式。</p>
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
 * @author IoT Gateway Team
 */
package cn.iocoder.yudao.module.iot.newgateway.core.logging;
