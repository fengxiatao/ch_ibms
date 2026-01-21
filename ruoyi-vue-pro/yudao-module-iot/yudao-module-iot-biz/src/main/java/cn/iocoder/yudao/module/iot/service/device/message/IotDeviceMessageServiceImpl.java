package cn.iocoder.yudao.module.iot.service.device.message;

import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.date.LocalDateTimeUtils;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.iot.controller.admin.device.vo.message.IotDeviceMessagePageReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.statistics.vo.IotStatisticsDeviceMessageReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.statistics.vo.IotStatisticsDeviceMessageSummaryByDateRespVO;
import cn.iocoder.yudao.module.iot.core.enums.IotDeviceMessageMethodEnum;
import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.core.mq.producer.IotDeviceMessageProducer;
import cn.iocoder.yudao.module.iot.core.util.IotDeviceMessageUtils;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceMessageDO;
import cn.iocoder.yudao.module.iot.dal.tdengine.IotDeviceMessageMapper;
import cn.iocoder.yudao.module.iot.service.device.IotDeviceService;
import cn.iocoder.yudao.module.iot.service.device.property.IotDevicePropertyService;
import cn.iocoder.yudao.module.iot.service.ota.IotOtaTaskRecordService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.common.base.Objects;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.module.iot.enums.ErrorCodeConstants.DEVICE_DOWNSTREAM_FAILED_SERVER_ID_NULL;

/**
 * IoT 设备消息 Service 实现类
 *
 * @author 长辉信息科技有限公司
 */
@Service
@Validated
@Slf4j
public class IotDeviceMessageServiceImpl implements IotDeviceMessageService {

    @Resource
    private IotDeviceService deviceService;
    @Resource
    private IotDevicePropertyService devicePropertyService;
    @Resource
    @Lazy // 延迟加载，避免循环依赖
    private IotOtaTaskRecordService otaTaskRecordService;

    @Resource
    private IotDeviceMessageMapper deviceMessageMapper;

    @Resource
    private IotDeviceMessageProducer deviceMessageProducer;
    
    @Resource
    private cn.iocoder.yudao.module.iot.service.device.handler.DynamicDeviceServiceInvoker deviceServiceInvoker;
    
    @Resource
    private cn.iocoder.yudao.module.iot.service.device.handler.property.DevicePropertyProcessor devicePropertyProcessor;
    
    @Resource
    private cn.iocoder.yudao.module.iot.service.device.handler.event.DeviceEventProcessor deviceEventProcessor;

    @Override
    public void defineDeviceMessageStable() {
        // MySQL版本：表已通过SQL脚本预先创建，此方法仅检查表是否存在
        if (StrUtil.isNotEmpty(deviceMessageMapper.showSTable())) {
            log.info("[defineDeviceMessageStable][MySQL设备消息表已存在]");
            return;
        }
        // MySQL版本：表不存在时仅记录警告，不尝试创建（表应通过SQL脚本创建）
        log.warn("[defineDeviceMessageStable][MySQL设备消息表不存在，请执行SQL脚本: sql/mysql/iot_device_message.sql]");
    }

    @Async
    void createDeviceLogAsync(IotDeviceMessage message) {
        IotDeviceMessageDO messageDO = BeanUtils.toBean(message, IotDeviceMessageDO.class)
                .setUpstream(IotDeviceMessageUtils.isUpstreamMessage(message))
                .setReply(IotDeviceMessageUtils.isReplyMessage(message))
                .setIdentifier(IotDeviceMessageUtils.getIdentifier(message));
        if (message.getParams() != null) {
            messageDO.setParams(JsonUtils.toJsonString(messageDO.getParams()));
        }
        if (messageDO.getData() != null) {
            messageDO.setData(JsonUtils.toJsonString(messageDO.getData()));
        }
        deviceMessageMapper.insert(messageDO);
    }

    @Override
    public IotDeviceMessage sendDeviceMessage(IotDeviceMessage message) {
        IotDeviceDO device = deviceService.validateDeviceExists(message.getDeviceId());
        return sendDeviceMessage(message, device);
    }

    // TODO @长辉开发团队：针对连接网关的设备，是不是 productKey、deviceName 需要调整下；
    @Override
    public IotDeviceMessage sendDeviceMessage(IotDeviceMessage message, IotDeviceDO device) {
        return sendDeviceMessage(message, device, null);
    }

    private IotDeviceMessage sendDeviceMessage(IotDeviceMessage message, IotDeviceDO device, String serverId) {
        // 1. 补充信息
        appendDeviceMessage(message, device);

        // 2.1 情况一：发送上行消息
        boolean upstream = IotDeviceMessageUtils.isUpstreamMessage(message);
        if (upstream) {
            deviceMessageProducer.sendDeviceMessage(message);
            return message;
        }

        // 2.2 情况二：发送下行消息
        // 如果是下行消息，需要校验 serverId 存在
        // TODO 长辉开发团队：【设计】下行消息需要区分 PUSH 和 PULL 模型
        // 1. PUSH 模型：适用于 MQTT 等长连接协议。通过 serverId 将消息路由到指定网关，实时推送。
        // 2. PULL 模型：适用于 HTTP 等短连接协议。设备无固定 serverId，无法主动推送。
        // 解决方案：
        // 当 serverId 不存在时，将下行消息存入“待拉取消息表”（例如 iot_device_pull_message）。
        // 设备端通过定时轮询一个新增的 API（例如 /iot/message/pull）来拉取属于自己的消息。
        if (StrUtil.isEmpty(serverId)) {
            serverId = devicePropertyService.getDeviceServerId(device.getId());
            if (StrUtil.isEmpty(serverId)) {
                throw exception(DEVICE_DOWNSTREAM_FAILED_SERVER_ID_NULL);
            }
        }
        deviceMessageProducer.sendDeviceMessageToGateway(serverId, message);
        // 特殊：记录消息日志。原因：上行消息，消费时，已经会记录；下行消息，因为消费在 Gateway 端，所以需要在这里记录
        getSelf().createDeviceLogAsync(message);
        return message;
    }

    /**
     * 补充消息的后端字段
     *
     * @param message 消息
     * @param device  设备信息
     */
    private void appendDeviceMessage(IotDeviceMessage message, IotDeviceDO device) {
        message.setId(IotDeviceMessageUtils.generateMessageId()).setReportTime(LocalDateTime.now())
                .setDeviceId(device.getId()).setTenantId(device.getTenantId());
        // 特殊：如果设备没有指定 requestId，则使用 messageId
        if (StrUtil.isEmpty(message.getRequestId())) {
            message.setRequestId(message.getId());
        }
    }

    @Override
    public void handleUpstreamDeviceMessage(IotDeviceMessage message, IotDeviceDO device) {
        // 1. 处理消息
        Object replyData = null;
        ServiceException serviceException = null;
        try {
            replyData = handleUpstreamDeviceMessage0(message, device);
        } catch (ServiceException ex) {
            serviceException = ex;
            log.warn("[handleUpstreamDeviceMessage][message({}) 业务异常]", message, serviceException);
        } catch (Exception ex) {
            log.error("[handleUpstreamDeviceMessage][message({}) 发生异常]", message, ex);
            throw ex;
        }

        // 2. 记录消息
        getSelf().createDeviceLogAsync(message);

        // 3. 回复消息。前提：非 _reply 消息，并且非禁用回复的消息
        if (IotDeviceMessageUtils.isReplyMessage(message)
                || IotDeviceMessageMethodEnum.isReplyDisabled(message.getMethod())
                || StrUtil.isEmpty(message.getServerId())) {
            return;
        }
        try {
            IotDeviceMessage replyMessage = IotDeviceMessage.replyOf(message.getRequestId(), message.getMethod(), replyData,
                    serviceException != null ? serviceException.getCode() : null,
                    serviceException != null ? serviceException.getMessage() : null);
            sendDeviceMessage(replyMessage, device, message.getServerId());
        } catch (Exception ex) {
            log.error("[handleUpstreamDeviceMessage][message({}) 回复消息失败]", message, ex);
        }
    }

    // TODO @长辉开发团队：可优化：未来逻辑复杂后，可以独立拆除 Processor 处理器
    @SuppressWarnings("SameReturnValue")
    private Object handleUpstreamDeviceMessage0(IotDeviceMessage message, IotDeviceDO device) {
        // 设备上下线
        if (Objects.equal(message.getMethod(), IotDeviceMessageMethodEnum.STATE_UPDATE.getMethod())) {
            String stateStr = IotDeviceMessageUtils.getIdentifier(message);
            assert stateStr != null;
            Assert.notEmpty(stateStr, "设备状态不能为空");
            deviceService.updateDeviceState(device, Integer.valueOf(stateStr));
            // TODO 长辉开发团队：子设备的关联
            return null;
        }

        // ========== 属性上报（使用可扩展处理器） ==========
        if (Objects.equal(message.getMethod(), IotDeviceMessageMethodEnum.PROPERTY_POST.getMethod())) {
            // 使用原有的saveDeviceProperty方法（包含物模型验证）
            devicePropertyService.saveDeviceProperty(device, message);
            return null;
        }

        // ========== 事件上报（使用可扩展处理器） ==========
        if (Objects.equal(message.getMethod(), IotDeviceMessageMethodEnum.EVENT_POST.getMethod())) {
            @SuppressWarnings("unchecked")
            Map<String, Object> params = (Map<String, Object>) message.getParams();
            String eventIdentifier = (String) params.get("identifier");
            return deviceEventProcessor.processEvent(device, eventIdentifier, params, message.getReportTime());
        }

        // ========== 服务调用（使用可扩展处理器） ==========
        if (Objects.equal(message.getMethod(), IotDeviceMessageMethodEnum.SERVICE_INVOKE.getMethod())) {
            return deviceServiceInvoker.handleDeviceService(message, device);
        }

        // OTA 上报升级进度
        if (Objects.equal(message.getMethod(), IotDeviceMessageMethodEnum.OTA_PROGRESS.getMethod())) {
            otaTaskRecordService.updateOtaRecordProgress(device, message);
            return null;
        }

        // TODO @长辉开发团队：这里可以按需，添加别的逻辑；
        return null;
    }

    @Override
    public PageResult<IotDeviceMessageDO> getDeviceMessagePage(IotDeviceMessagePageReqVO pageReqVO) {
        try {
            IPage<IotDeviceMessageDO> page = deviceMessageMapper.selectPage(
                    new Page<>(pageReqVO.getPageNo(), pageReqVO.getPageSize()), pageReqVO);
            return new PageResult<>(page.getRecords(), page.getTotal());
        } catch (Exception exception) {
            if (exception.getMessage().contains("Table does not exist")) {
                return PageResult.empty();
            }
            throw exception;
        }
    }

    @Override
    public List<IotDeviceMessageDO> getDeviceMessageListByRequestIdsAndReply(Long deviceId,
                                                                             List<String> requestIds,
                                                                             Boolean reply) {
        return deviceMessageMapper.selectListByRequestIdsAndReply(deviceId, requestIds, reply);
    }

    @Override
    public Long getDeviceMessageCount(LocalDateTime createTime) {
        try {
            // 如果传入的createTime为null，直接返回0，避免TDengine时间戳格式问题
            if (createTime == null) {
                return deviceMessageMapper.selectCountByCreateTime(null);
            }
            
            // 将LocalDateTime转换为毫秒时间戳，但确保不会超出TDengine支持的范围
            long timestamp = LocalDateTimeUtil.toEpochMilli(createTime);
            
            // TDengine支持的时间戳范围检查（1970-01-01 到 2030-01-01）
            // 时间戳 1893456000000L 对应 2030-01-01 00:00:00
            if (timestamp < 0 || timestamp > 1893456000000L) {
                log.warn("[getDeviceMessageCount][时间戳超出TDengine支持范围: {}，对应时间: {}]", 
                    timestamp, LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.systemDefault()));
                return 0L;
            }
            
            return deviceMessageMapper.selectCountByCreateTime(timestamp);
        } catch (Exception exception) {
            if (exception.getMessage().contains("Table does not exist") || 
                exception.getMessage().contains("doesn't exist")) {
                log.warn("[getDeviceMessageCount][设备消息表不存在，返回默认值0]");
                return 0L;
            }
            log.error("[getDeviceMessageCount][查询设备消息数量失败]", exception);
            return 0L; // 返回默认值，避免前端报错
        }
    }

    @Override
    public List<IotStatisticsDeviceMessageSummaryByDateRespVO> getDeviceMessageSummaryByDate(
            IotStatisticsDeviceMessageReqVO reqVO) {
        try {
            // 将 LocalDate 转换为 LocalDateTime（开始时间为00:00:00，结束时间为23:59:59）
            LocalDateTime startTime = reqVO.getTimes()[0].atStartOfDay();
            LocalDateTime endTime = reqVO.getTimes()[1].atTime(23, 59, 59);
            
            // 1. 按小时统计，获取分项统计数据
            List<Map<String, Object>> countList = deviceMessageMapper.selectDeviceMessageCountGroupByDate(
                    LocalDateTimeUtil.toEpochMilli(startTime),
                    LocalDateTimeUtil.toEpochMilli(endTime));

            // 2. 按照日期间隔，合并数据
            List<LocalDateTime[]> timeRanges = LocalDateTimeUtils.getDateRangeList(startTime, endTime,
                    reqVO.getInterval());
            return convertList(timeRanges, times -> {
                Integer upstreamCount = countList.stream()
                        .filter(vo -> {
                            Timestamp timestamp = parseTimestamp(vo.get("time"));
                            return timestamp != null && LocalDateTimeUtils.isBetween(times[0], times[1], timestamp);
                        })
                        .mapToInt(value -> MapUtil.getInt(value, "upstream_count")).sum();
                Integer downstreamCount = countList.stream()
                        .filter(vo -> {
                            Timestamp timestamp = parseTimestamp(vo.get("time"));
                            return timestamp != null && LocalDateTimeUtils.isBetween(times[0], times[1], timestamp);
                        })
                        .mapToInt(value -> MapUtil.getInt(value, "downstream_count")).sum();
                return new IotStatisticsDeviceMessageSummaryByDateRespVO()
                        .setTime(LocalDateTimeUtils.formatDateRange(times[0], times[1], reqVO.getInterval()))
                        .setUpstreamCount(upstreamCount).setDownstreamCount(downstreamCount);
            });
        } catch (Exception exception) {
            if (exception.getMessage().contains("Table does not exist") || 
                exception.getMessage().contains("doesn't exist")) {
                log.warn("[getDeviceMessageSummaryByDate][设备消息表不存在，返回空列表]");
                return Collections.emptyList();
            }
            log.error("[getDeviceMessageSummaryByDate][查询设备消息统计失败]", exception);
            return Collections.emptyList(); // 返回空列表，避免前端报错
        }
    }

    /**
     * 解析时间戳（兼容 TDengine 返回的 String 类型）
     * 
     * TDengine 可能返回的格式：
     * - yyyy-MM-dd HH:mm:ss.SSS (3位毫秒)
     * - yyyy-MM-dd HH:mm:ss.S   (1位毫秒)
     * - yyyy-MM-dd HH:mm:ss     (无毫秒)
     */
    private Timestamp parseTimestamp(Object timeObj) {
        if (timeObj == null) {
            return null;
        }
        
        // 如果已经是 Timestamp，直接返回
        if (timeObj instanceof Timestamp) {
            return (Timestamp) timeObj;
        }
        
        // 如果是 String，尝试解析
        if (timeObj instanceof String) {
            String timeStr = ((String) timeObj).trim();
            
            try {
                // 使用 Timestamp.valueOf() 直接解析
                // 它能处理: yyyy-MM-dd HH:mm:ss[.fffffffff]
                return Timestamp.valueOf(timeStr);
            } catch (Exception e) {
                // 如果失败，尝试规范化格式后再解析
                try {
                    // 规范化：确保小数点后有9位（纳秒）
                    String normalizedStr = normalizeTimestampString(timeStr);
                    return Timestamp.valueOf(normalizedStr);
                } catch (Exception ex) {
                    log.warn("[parseTimestamp][解析时间戳失败: {}]", timeStr, ex);
                    return null;
                }
            }
        }
        
        log.warn("[parseTimestamp][未知的时间类型: {}]", timeObj.getClass());
        return null;
    }
    
    /**
     * 规范化时间戳字符串格式
     * 将 yyyy-MM-dd HH:mm:ss.S 转换为 yyyy-MM-dd HH:mm:ss.SSSSSSSSS
     */
    private String normalizeTimestampString(String timeStr) {
        // 找到小数点位置
        int dotIndex = timeStr.indexOf('.');
        
        if (dotIndex == -1) {
            // 没有小数点，添加 .0
            return timeStr + ".0";
        }
        
        // 有小数点，检查小数位数
        String beforeDot = timeStr.substring(0, dotIndex);
        String afterDot = timeStr.substring(dotIndex + 1);
        
        // 补齐到9位（纳秒精度）
        while (afterDot.length() < 9) {
            afterDot += "0";
        }
        
        return beforeDot + "." + afterDot;
    }

    private IotDeviceMessageServiceImpl getSelf() {
        return SpringUtil.getBean(getClass());
    }

}
