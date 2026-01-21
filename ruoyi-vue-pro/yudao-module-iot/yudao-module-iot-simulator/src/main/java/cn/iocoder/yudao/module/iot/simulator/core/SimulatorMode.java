package cn.iocoder.yudao.module.iot.simulator.core;

/**
 * 模拟器行为模式枚举
 *
 * @author Kiro
 */
public enum SimulatorMode {

    /**
     * 成功模式：所有操作都成功响应
     */
    SUCCESS,

    /**
     * 拒绝模式：拒绝升级等操作
     */
    REJECT,

    /**
     * 帧失败模式：在特定帧号失败
     */
    FRAME_FAIL,

    /**
     * 超时模式：延迟响应以模拟慢速设备
     */
    TIMEOUT,

    /**
     * 随机失败模式：随机失败某些操作
     */
    RANDOM_FAIL
}
