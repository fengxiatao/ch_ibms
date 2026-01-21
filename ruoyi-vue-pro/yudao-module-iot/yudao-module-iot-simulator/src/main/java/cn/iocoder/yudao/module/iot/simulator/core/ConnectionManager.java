package cn.iocoder.yudao.module.iot.simulator.core;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * 连接管理器接口
 * 管理设备与服务器的TCP/UDP连接
 *
 * @author Kiro
 */
public interface ConnectionManager {

    /**
     * 创建连接
     *
     * @param config 连接配置
     * @return 异步连接结果
     */
    CompletableFuture<Connection> connect(ConnectionConfig config);

    /**
     * 关闭连接
     *
     * @param connectionId 连接ID
     * @return 异步完成结果
     */
    CompletableFuture<Void> disconnect(String connectionId);

    /**
     * 获取连接
     *
     * @param connectionId 连接ID
     * @return 连接对象，不存在返回null
     */
    Connection getConnection(String connectionId);

    /**
     * 获取所有活跃连接
     *
     * @return 活跃连接列表
     */
    List<Connection> getActiveConnections();

    /**
     * 获取连接数量
     *
     * @return 当前连接数
     */
    int getConnectionCount();

    /**
     * 关闭所有连接
     *
     * @return 异步完成结果
     */
    CompletableFuture<Void> closeAll();
}
