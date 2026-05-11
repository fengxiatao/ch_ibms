package cn.iocoder.yudao.module.iot.service.building;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.iot.controller.admin.building.vo.lighting.*;
import cn.iocoder.yudao.module.iot.dal.dataobject.building.*;
import cn.iocoder.yudao.module.iot.dal.mysql.building.*;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Objects;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.iot.enums.ErrorCodeConstants.*;

/**
 * 智能照明 Service 实现类
 *
 * @author 智慧楼宇系统
 */
@Service
@Validated
public class IbmsLightingServiceImpl implements IbmsLightingService {

    @Resource
    private IbmsLightingCircuitMapper circuitMapper;

    @Resource
    private IbmsLightingSceneMapper sceneMapper;

    @Resource
    private IbmsLightingScheduleMapper scheduleMapper;

    @Resource
    private IbmsLightingGatewayMapper gatewayMapper;

    @Resource
    private IbmsLightingControllerMapper controllerMapper;

    @Resource
    private IbmsLightingOperationLogMapper operationLogMapper;

    @Resource
    private IbmsLightingAlarmMapper alarmMapper;

    // ======================= 回路管理 =======================

    @Override
    public PageResult<IbmsLightingCircuitDO> getCircuitPage(IbmsLightingCircuitPageReqVO pageReqVO) {
        return circuitMapper.selectPage(pageReqVO);
    }

    @Override
    public IbmsLightingCircuitDO getCircuit(Long id) {
        return circuitMapper.selectById(id);
    }

    @Override
    public void controlCircuit(Long id, Integer status, String operator) {
        IbmsLightingCircuitDO circuit = circuitMapper.selectById(id);
        if (circuit == null) {
            throw exception(LIGHTING_CIRCUIT_NOT_EXISTS);
        }

        // 记录操作前状态
        Integer beforeStatus = circuit.getStatus();

        // 更新回路状态
        IbmsLightingCircuitDO updateObj = new IbmsLightingCircuitDO();
        updateObj.setId(id);
        updateObj.setStatus(status);
        updateObj.setLastOperateTime(LocalDateTime.now());
        circuitMapper.updateById(updateObj);

        // 记录操作日志
        IbmsLightingOperationLogDO log = new IbmsLightingOperationLogDO();
        log.setOperationType(1); // 1-手动控制
        log.setTargetType(1); // 1-回路
        log.setTargetId(id);
        log.setTargetName(circuit.getCircuitName());
        log.setOperationContent(String.format("状态变更: %s → %s", 
                beforeStatus == 1 ? "开启" : "关闭", status == 1 ? "开启" : "关闭"));
        log.setOperator(operator);
        log.setResult(1); // 成功
        log.setOperateTime(LocalDateTime.now());
        operationLogMapper.insert(log);
    }

    @Override
    public void dimCircuit(Long id, Integer brightness, String operator) {
        IbmsLightingCircuitDO circuit = circuitMapper.selectById(id);
        if (circuit == null) {
            throw exception(LIGHTING_CIRCUIT_NOT_EXISTS);
        }

        // 记录操作前亮度
        Integer beforeBrightness = circuit.getBrightness();

        // 更新回路亮度
        IbmsLightingCircuitDO updateObj = new IbmsLightingCircuitDO();
        updateObj.setId(id);
        updateObj.setBrightness(brightness);
        updateObj.setLastOperateTime(LocalDateTime.now());
        circuitMapper.updateById(updateObj);

        // 记录操作日志
        IbmsLightingOperationLogDO log = new IbmsLightingOperationLogDO();
        log.setOperationType(1); // 1-手动控制
        log.setTargetType(1); // 1-回路
        log.setTargetId(id);
        log.setTargetName(circuit.getCircuitName());
        log.setOperationContent(String.format("调节亮度: %s%% → %s%%", 
                beforeBrightness != null ? beforeBrightness : 0, brightness));
        log.setOperator(operator);
        log.setResult(1); // 成功
        log.setOperateTime(LocalDateTime.now());
        operationLogMapper.insert(log);
    }

    // ======================= 场景管理 =======================

    @Override
    public PageResult<IbmsLightingSceneDO> getScenePage(IbmsLightingScenePageReqVO pageReqVO) {
        return sceneMapper.selectPage(pageReqVO);
    }

    @Override
    public List<IbmsLightingSceneDO> getSceneSimpleList() {
        return sceneMapper.selectSimpleList();
    }

    @Override
    public void executeScene(Long id, String operator) {
        IbmsLightingSceneDO scene = sceneMapper.selectById(id);
        if (scene == null) {
            throw exception(LIGHTING_SCENE_NOT_EXISTS);
        }

        // TODO: 根据场景配置执行回路控制

        // 记录操作日志
        IbmsLightingOperationLogDO log = new IbmsLightingOperationLogDO();
        log.setOperationType(2); // 2-场景执行
        log.setTargetType(2); // 2-场景
        log.setTargetId(id);
        log.setTargetName(scene.getSceneName());
        log.setOperationContent("执行场景: " + scene.getSceneName());
        log.setOperator(operator);
        log.setResult(1); // 成功
        log.setOperateTime(LocalDateTime.now());
        operationLogMapper.insert(log);
    }

    // ======================= 定时任务 =======================

    @Override
    public PageResult<IbmsLightingScheduleDO> getSchedulePage(IbmsLightingSchedulePageReqVO pageReqVO) {
        return scheduleMapper.selectPage(pageReqVO);
    }

    @Override
    public void updateScheduleEnabled(Long id, Boolean enabled) {
        IbmsLightingScheduleDO schedule = scheduleMapper.selectById(id);
        if (schedule == null) {
            throw exception(LIGHTING_SCHEDULE_NOT_EXISTS);
        }

        IbmsLightingScheduleDO updateObj = new IbmsLightingScheduleDO();
        updateObj.setId(id);
        updateObj.setEnabled(enabled);
        scheduleMapper.updateById(updateObj);
    }

    // ======================= 设备管理 =======================

    @Override
    public PageResult<IbmsLightingGatewayDO> getGatewayPage(IbmsLightingDevicePageReqVO pageReqVO) {
        return gatewayMapper.selectPage(pageReqVO);
    }

    @Override
    public PageResult<IbmsLightingControllerDO> getControllerPage(IbmsLightingDevicePageReqVO pageReqVO) {
        return controllerMapper.selectPage(pageReqVO);
    }

    // ======================= 操作日志 =======================

    @Override
    public PageResult<IbmsLightingOperationLogDO> getOperationLogPage(IbmsLightingOperationLogPageReqVO pageReqVO) {
        return operationLogMapper.selectPage(pageReqVO);
    }

    // ======================= 告警管理 =======================

    @Override
    public PageResult<IbmsLightingAlarmDO> getAlarmPage(IbmsLightingAlarmPageReqVO pageReqVO) {
        return alarmMapper.selectPage(pageReqVO);
    }

    @Override
    public void handleAlarm(Long id, String handler, String handleRemark) {
        IbmsLightingAlarmDO alarm = alarmMapper.selectById(id);
        if (alarm == null) {
            throw exception(LIGHTING_ALARM_NOT_EXISTS);
        }

        IbmsLightingAlarmDO updateObj = new IbmsLightingAlarmDO();
        updateObj.setId(id);
        updateObj.setStatus(2); // 已处理
        updateObj.setHandler(handler);
        updateObj.setHandleRemark(handleRemark);
        updateObj.setHandleTime(LocalDateTime.now());
        alarmMapper.updateById(updateObj);
    }

    // ======================= 统计分析 =======================

    @Override
    public IbmsLightingStatisticsVO getStatistics() {
        IbmsLightingStatisticsVO vo = new IbmsLightingStatisticsVO();

        // 回路统计
        vo.setCircuitTotalCount(circuitMapper.selectCount());
        vo.setCircuitOnCount(circuitMapper.selectCountByStatus(1));
        vo.setCircuitOffCount(circuitMapper.selectCountByStatus(0));
        vo.setCircuitFaultCount(circuitMapper.selectCountByStatus(2));

        // 场景统计
        vo.setSceneTotalCount(sceneMapper.selectCount());

        // 网关统计
        vo.setGatewayTotalCount(gatewayMapper.selectCount());
        vo.setGatewayOnlineCount(gatewayMapper.selectCount(
                new LambdaQueryWrapperX<IbmsLightingGatewayDO>().eq(IbmsLightingGatewayDO::getStatus, 1)));

        // 控制器统计
        vo.setControllerTotalCount(controllerMapper.selectCount());
        vo.setControllerOnlineCount(controllerMapper.selectCount(
                new LambdaQueryWrapperX<IbmsLightingControllerDO>().eq(IbmsLightingControllerDO::getStatus, 1)));

        // 告警统计
        vo.setUnhandledAlarmCount(alarmMapper.selectCountByStatus(0));
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        LocalDateTime todayEnd = LocalDate.now().atTime(LocalTime.MAX);
        vo.setTodayAlarmCount(alarmMapper.selectCount(
                new LambdaQueryWrapperX<IbmsLightingAlarmDO>()
                        .between(IbmsLightingAlarmDO::getAlarmTime, todayStart, todayEnd)));

        // 灯具/功率统计：基于 ibms_lighting_circuit 实测数据
        // - lightTotalCount：所有回路 light_count 之和
        // - totalPower：所有回路 rated_power 之和（kW，DB 字段单位为 W，转 kW）
        // - currentPower：状态=1（开启）的回路 rated_power * brightness/100 之和（kW）
        List<IbmsLightingCircuitDO> circuits = circuitMapper.selectList();
        long lightTotal = circuits.stream()
                .map(IbmsLightingCircuitDO::getLightCount)
                .filter(Objects::nonNull)
                .mapToLong(Integer::longValue)
                .sum();
        vo.setLightTotalCount(lightTotal);

        BigDecimal ratedSumW = circuits.stream()
                .map(IbmsLightingCircuitDO::getRatedPower)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        vo.setTotalPower(ratedSumW.divide(BigDecimal.valueOf(1000), 2, RoundingMode.HALF_UP));

        BigDecimal currentSumW = circuits.stream()
                .filter(c -> Integer.valueOf(1).equals(c.getStatus()))
                .filter(c -> c.getRatedPower() != null)
                .map(c -> {
                    int brightness = c.getBrightness() != null ? c.getBrightness() : 100;
                    return c.getRatedPower().multiply(BigDecimal.valueOf(brightness))
                            .divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        vo.setCurrentPower(currentSumW.divide(BigDecimal.valueOf(1000), 2, RoundingMode.HALF_UP));

        return vo;
    }

}
