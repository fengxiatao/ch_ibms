package cn.iocoder.yudao.module.iot.service.changhui.control;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.iot.controller.admin.changhui.vo.control.ChanghuiControlLogPageReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.changhui.vo.control.ChanghuiControlLogRespVO;
import cn.iocoder.yudao.module.iot.convert.changhui.ChanghuiControlLogConvert;
import cn.iocoder.yudao.module.iot.dal.dataobject.changhui.ChanghuiControlLogDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.changhui.ChanghuiDeviceDO;
import cn.iocoder.yudao.module.iot.dal.mysql.changhui.ChanghuiControlLogMapper;
import cn.iocoder.yudao.module.iot.enums.changhui.ChanghuiControlTypeEnum;
import cn.iocoder.yudao.module.iot.enums.device.ChanghuiDeviceTypeConstants;
import cn.iocoder.yudao.module.iot.mq.producer.DeviceCommandPublisher;
import cn.iocoder.yudao.module.iot.service.changhui.device.ChanghuiDeviceService;
import com.alibaba.fastjson.JSON;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.iot.enums.ErrorCodeConstants.CHANGHUI_DEVICE_NOT_FOUND;
import static cn.iocoder.yudao.module.iot.enums.ErrorCodeConstants.CHANGHUI_DEVICE_OFFLINE;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserNickname;

/**
 * 长辉设备远程控制 Service 实现类
 * 
 * <p>所有命令通过 DeviceCommandPublisher 发送到 DEVICE_SERVICE_INVOKE 主题
 * <p>由 newgateway 的 ChanghuiPlugin 处理
 *
 * @author 长辉信息科技有限公司
 */
@Service
@Validated
@Slf4j
public class ChanghuiControlServiceImpl implements ChanghuiControlService {

    /** 控制结果：失败 */
    private static final Integer RESULT_FAILED = 0;
    /** 控制结果：成功 */
    private static final Integer RESULT_SUCCESS = 1;

    @Resource
    private ChanghuiControlLogMapper controlLogMapper;

    @Resource
    private ChanghuiDeviceService deviceService;

    @Resource
    private DeviceCommandPublisher deviceCommandPublisher;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String switchMode(String stationCode, String mode) {
        // 校验设备
        ChanghuiDeviceDO device = validateDeviceOnline(stationCode);

        // 构建控制参数
        Map<String, Object> params = new HashMap<>();
        params.put(ChanghuiDeviceTypeConstants.PARAM_STATION_CODE, stationCode);
        params.put("mode", mode);

        // 记录控制日志
        Map<String, Object> logParams = new HashMap<>();
        logParams.put("mode", mode);
        saveControlLog(device, ChanghuiControlTypeEnum.MODE_SWITCH.getCode(), logParams);

        // 发送命令
        String requestId = deviceCommandPublisher.publishCommand(
                ChanghuiDeviceTypeConstants.CHANGHUI,
                device.getId(),
                ChanghuiDeviceTypeConstants.COMMAND_MODE_SWITCH,
                params
        );

        log.info("[switchMode] 模式切换命令已发送: stationCode={}, mode={}, requestId={}",
                stationCode, mode, requestId);
        return requestId;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String manualControl(String stationCode, String action) {
        // 校验设备
        ChanghuiDeviceDO device = validateDeviceOnline(stationCode);

        // 构建控制参数
        Map<String, Object> params = new HashMap<>();
        params.put(ChanghuiDeviceTypeConstants.PARAM_STATION_CODE, stationCode);
        params.put(ChanghuiDeviceTypeConstants.PARAM_ACTION, action);

        // 记录控制日志
        Map<String, Object> logParams = new HashMap<>();
        logParams.put(ChanghuiDeviceTypeConstants.PARAM_ACTION, action);
        saveControlLog(device, ChanghuiControlTypeEnum.MANUAL_CONTROL.getCode(), logParams);

        // 发送命令
        String requestId = deviceCommandPublisher.publishCommand(
                ChanghuiDeviceTypeConstants.CHANGHUI,
                device.getId(),
                ChanghuiDeviceTypeConstants.COMMAND_MANUAL_CONTROL,
                params
        );

        log.info("[manualControl] 手动控制命令已发送: stationCode={}, action={}, requestId={}",
                stationCode, action, requestId);
        return requestId;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String autoControl(String stationCode, String controlMode, Double targetValue) {
        // 校验设备
        ChanghuiDeviceDO device = validateDeviceOnline(stationCode);

        // 构建控制参数
        Map<String, Object> params = new HashMap<>();
        params.put(ChanghuiDeviceTypeConstants.PARAM_STATION_CODE, stationCode);
        params.put(ChanghuiDeviceTypeConstants.PARAM_CONTROL_MODE, controlMode);
        params.put(ChanghuiDeviceTypeConstants.PARAM_TARGET_VALUE, targetValue);

        // 记录控制日志
        Map<String, Object> logParams = new HashMap<>();
        logParams.put(ChanghuiDeviceTypeConstants.PARAM_CONTROL_MODE, controlMode);
        logParams.put(ChanghuiDeviceTypeConstants.PARAM_TARGET_VALUE, targetValue);
        saveControlLog(device, ChanghuiControlTypeEnum.AUTO_CONTROL.getCode(), logParams);

        // 发送命令
        String requestId = deviceCommandPublisher.publishCommand(
                ChanghuiDeviceTypeConstants.CHANGHUI,
                device.getId(),
                ChanghuiDeviceTypeConstants.COMMAND_AUTO_CONTROL,
                params
        );

        log.info("[autoControl] 自动控制命令已发送: stationCode={}, controlMode={}, targetValue={}, requestId={}",
                stationCode, controlMode, targetValue, requestId);
        return requestId;
    }

    @Override
    public PageResult<ChanghuiControlLogRespVO> getControlLogs(ChanghuiControlLogPageReqVO reqVO) {
        PageResult<ChanghuiControlLogDO> pageResult = controlLogMapper.selectPage(
                reqVO.getStationCode(), reqVO.getDeviceId(), reqVO.getControlType(),
                reqVO.getResult(), reqVO.getStartTime(), reqVO.getEndTime(),
                reqVO.getPageNo(), reqVO.getPageSize());
        PageResult<ChanghuiControlLogRespVO> result = ChanghuiControlLogConvert.INSTANCE.convertPage(pageResult);
        // 填充额外字段
        result.getList().forEach(this::enrichControlLogRespVO);
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void recordControlResult(String stationCode, String controlType, boolean success, String errorMessage) {
        // 查找最近的控制日志
        ChanghuiControlLogDO latestLog = controlLogMapper.selectLatest(stationCode, controlType);
        if (latestLog == null) {
            log.warn("[recordControlResult] 未找到控制日志: stationCode={}, controlType={}", stationCode, controlType);
            return;
        }

        // 更新结果
        ChanghuiControlLogDO updateObj = new ChanghuiControlLogDO();
        updateObj.setId(latestLog.getId());
        updateObj.setResult(success ? RESULT_SUCCESS : RESULT_FAILED);
        if (!success && errorMessage != null) {
            updateObj.setErrorMessage(errorMessage);
        }
        controlLogMapper.updateById(updateObj);

        log.info("[recordControlResult] 控制结果已记录: stationCode={}, controlType={}, success={}, errorMessage={}",
                stationCode, controlType, success, errorMessage);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteControlLogsByDeviceId(Long deviceId) {
        int count = controlLogMapper.deleteByDeviceId(deviceId);
        log.info("[deleteControlLogsByDeviceId] 删除设备控制日志: deviceId={}, count={}", deviceId, count);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteControlLogsByStationCode(String stationCode) {
        int count = controlLogMapper.deleteByStationCode(stationCode);
        log.info("[deleteControlLogsByStationCode] 删除设备控制日志: stationCode={}, count={}", stationCode, count);
    }

    /**
     * 校验设备存在且在线
     *
     * @param stationCode 测站编码
     * @return 设备信息
     */
    private ChanghuiDeviceDO validateDeviceOnline(String stationCode) {
        ChanghuiDeviceDO device = deviceService.getDeviceDOByStationCode(stationCode);
        if (device == null) {
            throw exception(CHANGHUI_DEVICE_NOT_FOUND);
        }
        if (device.getStatus() == null || device.getStatus() != ChanghuiDeviceTypeConstants.STATUS_ONLINE) {
            throw exception(CHANGHUI_DEVICE_OFFLINE);
        }
        return device;
    }

    /**
     * 保存控制日志
     *
     * @param device      设备信息
     * @param controlType 控制类型
     * @param params      控制参数
     */
    private void saveControlLog(ChanghuiDeviceDO device, String controlType, Map<String, Object> params) {
        ChanghuiControlLogDO log = ChanghuiControlLogDO.builder()
                .deviceId(device.getId())
                .stationCode(device.getStationCode())
                .controlType(controlType)
                .controlParams(JSON.toJSONString(params))
                .operator(getLoginUserNickname())
                .operateTime(LocalDateTime.now())
                .build();
        controlLogMapper.insert(log);
    }

    /**
     * 填充控制日志响应VO的额外字段
     */
    private void enrichControlLogRespVO(ChanghuiControlLogRespVO respVO) {
        if (respVO == null) {
            return;
        }
        // 填充控制类型名称
        if (respVO.getControlType() != null) {
            ChanghuiControlTypeEnum controlTypeEnum = ChanghuiControlTypeEnum.getByCode(respVO.getControlType());
            if (controlTypeEnum != null) {
                respVO.setControlTypeName(controlTypeEnum.getDescription());
            } else {
                respVO.setControlTypeName(respVO.getControlType());
            }
        }
        // 填充结果名称
        if (respVO.getResult() != null) {
            respVO.setResultName(RESULT_SUCCESS.equals(respVO.getResult()) ? "成功" : "失败");
        }
    }

}
