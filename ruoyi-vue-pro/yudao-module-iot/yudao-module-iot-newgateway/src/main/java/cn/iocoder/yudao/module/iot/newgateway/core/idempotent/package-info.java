/**
 * 命令幂等服务包
 * <p>
 * 提供设备命令的幂等保护能力，防止消息重复消费导致命令重复下发。
 * </p>
 *
 * <p>核心组件：</p>
 * <ul>
 *     <li>{@link cn.iocoder.yudao.module.iot.newgateway.core.idempotent.CommandIdempotentService} - 幂等服务</li>
 * </ul>
 *
 * <p>实现策略：</p>
 * <ul>
 *     <li>Redis 分布式锁：适用于多实例部署场景</li>
 *     <li>本地缓存兜底：Redis 不可用时的降级方案</li>
 * </ul>
 *
 * @author IoT Gateway Team
 */
package cn.iocoder.yudao.module.iot.newgateway.core.idempotent;


