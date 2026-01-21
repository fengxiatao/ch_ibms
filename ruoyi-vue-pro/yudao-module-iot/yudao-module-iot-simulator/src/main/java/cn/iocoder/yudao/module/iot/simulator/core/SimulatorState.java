package cn.iocoder.yudao.module.iot.simulator.core;

/**
 * 模拟器状态枚举
 *
 * @author Kiro
 */
public enum SimulatorState {
    
    /**
     * 已创建，尚未启动
     */
    CREATED,
    
    /**
     * 正在连接服务器
     */
    CONNECTING,
    
    /**
     * 已连接到服务器
     */
    CONNECTED,
    
    /**
     * 正在运行（已连接且正常工作）
     */
    RUNNING,
    
    /**
     * 已断开连接
     */
    DISCONNECTED,
    
    /**
     * 已停止
     */
    STOPPED,
    
    /**
     * 发生错误
     */
    ERROR,
    
    /**
     * 正在启动
     */
    STARTING,
    
    /**
     * 正在停止
     */
    STOPPING,
    
    /**
     * 正在重连
     */
    RECONNECTING
}
