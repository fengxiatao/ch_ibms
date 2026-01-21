package cn.iocoder.yudao.module.iot.simulator.core;

import java.util.function.Consumer;

/**
 * 连接接口
 * 表示一个到服务器的连接
 *
 * @author Kiro
 */
public interface Connection {

    /**
     * 获取连接ID
     *
     * @return 连接唯一标识
     */
    String getId();

    /**
     * 检查连接是否活跃
     *
     * @return true表示连接活跃
     */
    boolean isActive();

    /**
     * 发送数据
     *
     * @param data 要发送的字节数组
     */
    void send(byte[] data);

    /**
     * 关闭连接
     */
    void close();

    /**
     * 设置数据接收处理器
     *
     * @param handler 数据处理器
     */
    void setDataHandler(Consumer<byte[]> handler);

    /**
     * 设置连接关闭处理器
     *
     * @param handler 关闭处理器
     */
    void setCloseHandler(Runnable handler);

    /**
     * 设置异常处理器
     *
     * @param handler 异常处理器
     */
    void setExceptionHandler(Consumer<Throwable> handler);
}
