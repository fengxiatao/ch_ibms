package cn.iocoder.yudao.module.iot.service.rule.data;

import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

/**
 * 数据流转规则统一触发分发器（v22 抽切面）。
 *
 * <p>背景：</p>
 * <ul>
 *   <li>v18~v21 三次迭代后，{@code DeviceEventConsumer} / {@code DeviceServiceResultConsumer}
 *       / {@code DeviceStateChangeConsumer} 三个上行消费者都各自硬编码
 *       {@code try { dataRuleService.executeDataRule(message) } catch (Exception)} 块，错误日志格式不一致。</li>
 *   <li>本类统一封装空指针守卫 + 异常吞噬 + ERROR 日志，三个 consumer 改为
 *       {@code dispatcher.dispatch(message, "Xxx")} 单行调用。</li>
 * </ul>
 *
 * <p>注意：</p>
 * <ul>
 *   <li>本类必须 {@code @Lazy} 持有 {@link IotDataRuleService}，避免与
 *       数据流转规则服务（其内部依赖各类 sink action / cache）形成构造期循环依赖。</li>
 *   <li>异常一律吞噬并 ERROR 输出 — 数据流转规则是旁路消费，不允许阻断主链路（设备状态机/事件存储/前端推送）。</li>
 * </ul>
 *
 * @author 长辉信息科技有限公司
 */
@Component
@Slf4j
public class IotDataRuleDispatcher {

    @Resource
    @Lazy
    private IotDataRuleService dataRuleService;

    /**
     * 分发设备消息到数据流转规则引擎。
     *
     * <p>该方法吞噬所有异常，仅记录 ERROR 日志，<b>不会向调用方抛出</b>。</p>
     *
     * @param message   设备消息（约定不为 {@code null}；{@code null} 时直接忽略不报错）
     * @param sourceTag 来源标识（用于日志定位，例如 {@code "DeviceEventConsumer"}），可为 {@code null}
     */
    public void dispatch(IotDeviceMessage message, String sourceTag) {
        if (message == null) {
            return;
        }
        if (dataRuleService == null) {
            // 理论上 @Lazy 注入不会为 null，这里保留与历史 consumer 中的判空一致行为
            return;
        }
        try {
            dataRuleService.executeDataRule(message);
        } catch (Exception e) {
            log.error("[IotDataRuleDispatcher] 触发数据流转规则失败: source={}, deviceId={}, method={}, requestId={}",
                    sourceTag, message.getDeviceId(), message.getMethod(), message.getRequestId(), e);
        }
    }

}
