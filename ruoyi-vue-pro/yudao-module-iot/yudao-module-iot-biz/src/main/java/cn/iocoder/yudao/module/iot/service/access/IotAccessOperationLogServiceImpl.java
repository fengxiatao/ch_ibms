package cn.iocoder.yudao.module.iot.service.access;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import cn.iocoder.yudao.module.iot.controller.admin.access.vo.operationlog.IotAccessOperationLogPageReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.access.IotAccessOperationLogDO;
import cn.iocoder.yudao.module.iot.dal.mysql.access.IotAccessOperationLogMapper;
import cn.iocoder.yudao.module.iot.service.access.dto.DispatchResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import jakarta.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 门禁操作日志 Service 实现类
 * 
 * 支持两类操作日志：
 * 1. 门控操作日志：开门、关门、常开、常闭等
 * 2. 授权操作日志：授权下发、授权撤销等（Requirements: 12.1, 12.2, 12.3）
 *
 * @author 芋道源码
 */
@Slf4j
@Service
@Validated
public class IotAccessOperationLogServiceImpl implements IotAccessOperationLogService {

    @Resource
    private IotAccessOperationLogMapper operationLogMapper;

    @Override
    public Long saveOperationLog(IotAccessOperationLogDO operationLog) {
        if (operationLog.getOperationTime() == null) {
            operationLog.setOperationTime(LocalDateTime.now());
        }
        operationLogMapper.insert(operationLog);
        return operationLog.getId();
    }

    @Override
    public Long logOperation(Long deviceId, Long channelId, String operationType,
                             Long operatorId, String operatorName, Integer result, String errorMessage) {
        IotAccessOperationLogDO operationLog = IotAccessOperationLogDO.builder()
                .deviceId(deviceId)
                .channelId(channelId)
                .operationType(operationType)
                .operationTime(LocalDateTime.now())
                .operatorId(operatorId)
                .operatorName(operatorName)
                .result(result)
                .errorMessage(errorMessage)
                .build();
        operationLogMapper.insert(operationLog);
        return operationLog.getId();
    }

    @Override
    public Long logOperation(Long deviceId, String deviceName, Long channelId, String channelName,
                             String operationType, Long operatorId, String operatorName, 
                             Integer result, String errorMessage) {
        IotAccessOperationLogDO operationLog = IotAccessOperationLogDO.builder()
                .deviceId(deviceId)
                .deviceName(deviceName)
                .channelId(channelId)
                .channelName(channelName)
                .operationType(operationType)
                .operationTime(LocalDateTime.now())
                .operatorId(operatorId)
                .operatorName(operatorName)
                .result(result)
                .errorMessage(errorMessage)
                .build();
        operationLogMapper.insert(operationLog);
        return operationLog.getId();
    }

    @Override
    public IotAccessOperationLogDO getOperationLog(Long id) {
        return operationLogMapper.selectById(id);
    }

    @Override
    public PageResult<IotAccessOperationLogDO> getOperationLogPage(Long deviceId, Long channelId, String operationType,
                                                                    Long operatorId, Integer result,
                                                                    LocalDateTime startTime, LocalDateTime endTime,
                                                                    Integer pageNo, Integer pageSize) {
        return operationLogMapper.selectPage(deviceId, channelId, operationType, operatorId,
                startTime, endTime, pageNo, pageSize);
    }
    
    // ========== 授权操作日志方法实现（Requirements: 12.1, 12.2, 12.3） ==========
    
    @Override
    public Long logAuthDispatch(String operationType, Long authTaskId,
                                Long targetPersonId, String targetPersonCode, String targetPersonName,
                                Long deviceId, String deviceName, Long channelId,
                                Long permissionGroupId, String permissionGroupName,
                                DispatchResult dispatchResult) {
        // 获取当前操作人信息
        Long operatorId = SecurityFrameworkUtils.getLoginUserId();
        String operatorName = SecurityFrameworkUtils.getLoginUserNickname();
        
        // 构建日志记录
        IotAccessOperationLogDO operationLog = IotAccessOperationLogDO.builder()
                .operationType(operationType)
                .operationTime(LocalDateTime.now())
                .operatorId(operatorId)
                .operatorName(operatorName)
                // 设备信息
                .deviceId(deviceId)
                .deviceName(deviceName)
                .channelId(channelId)
                // 目标人员信息
                .targetPersonId(targetPersonId)
                .targetPersonCode(targetPersonCode)
                .targetPersonName(targetPersonName)
                // 权限组信息
                .permissionGroupId(permissionGroupId)
                .permissionGroupName(permissionGroupName)
                // 任务信息
                .authTaskId(authTaskId)
                // 结果信息
                .result(dispatchResult.isSuccess() ? IotAccessOperationLogDO.RESULT_SUCCESS 
                        : IotAccessOperationLogDO.RESULT_FAILURE)
                .errorMessage(dispatchResult.getErrorMessage())
                .sdkErrorCode(dispatchResult.getErrorCode())
                // 凭证信息
                .credentialTypes(buildCredentialTypesString(dispatchResult))
                .successCredentialCount(dispatchResult.getSuccessCredentials() != null 
                        ? dispatchResult.getSuccessCredentials().size() : 0)
                .failedCredentialCount(dispatchResult.getFailedCredentials() != null 
                        ? dispatchResult.getFailedCredentials().size() : 0)
                .build();
        
        // 设置结果描述
        operationLog.setResultDesc(buildResultDesc(dispatchResult));
        
        operationLogMapper.insert(operationLog);
        
        log.info("[logAuthDispatch] 记录授权下发日志: logId={}, operationType={}, personId={}, deviceId={}, result={}",
                operationLog.getId(), operationType, targetPersonId, deviceId, 
                dispatchResult.isSuccess() ? "成功" : "失败");
        
        return operationLog.getId();
    }
    
    @Override
    public Long logAuthRevoke(Long authTaskId,
                              Long targetPersonId, String targetPersonCode, String targetPersonName,
                              Long deviceId, String deviceName, Long channelId,
                              DispatchResult dispatchResult) {
        // 获取当前操作人信息
        Long operatorId = SecurityFrameworkUtils.getLoginUserId();
        String operatorName = SecurityFrameworkUtils.getLoginUserNickname();
        
        // 构建日志记录
        IotAccessOperationLogDO operationLog = IotAccessOperationLogDO.builder()
                .operationType(IotAccessOperationLogDO.OP_TYPE_AUTH_REVOKE)
                .operationTime(LocalDateTime.now())
                .operatorId(operatorId)
                .operatorName(operatorName)
                // 设备信息
                .deviceId(deviceId)
                .deviceName(deviceName)
                .channelId(channelId)
                // 目标人员信息
                .targetPersonId(targetPersonId)
                .targetPersonCode(targetPersonCode)
                .targetPersonName(targetPersonName)
                // 任务信息
                .authTaskId(authTaskId)
                // 结果信息
                .result(dispatchResult.isSuccess() ? IotAccessOperationLogDO.RESULT_SUCCESS 
                        : IotAccessOperationLogDO.RESULT_FAILURE)
                .errorMessage(dispatchResult.getErrorMessage())
                .sdkErrorCode(dispatchResult.getErrorCode())
                // 凭证信息
                .credentialTypes(buildCredentialTypesString(dispatchResult))
                .successCredentialCount(dispatchResult.getSuccessCredentials() != null 
                        ? dispatchResult.getSuccessCredentials().size() : 0)
                .failedCredentialCount(dispatchResult.getFailedCredentials() != null 
                        ? dispatchResult.getFailedCredentials().size() : 0)
                .build();
        
        // 设置结果描述
        operationLog.setResultDesc(buildRevokeResultDesc(dispatchResult));
        
        operationLogMapper.insert(operationLog);
        
        log.info("[logAuthRevoke] 记录授权撤销日志: logId={}, personId={}, deviceId={}, result={}",
                operationLog.getId(), targetPersonId, deviceId, 
                dispatchResult.isSuccess() ? "成功" : "失败");
        
        return operationLog.getId();
    }
    
    @Override
    public Long logAuthRetry(Long authTaskId,
                             Long targetPersonId, String targetPersonCode, String targetPersonName,
                             Long deviceId, String deviceName, Long channelId,
                             DispatchResult dispatchResult) {
        // 获取当前操作人信息
        Long operatorId = SecurityFrameworkUtils.getLoginUserId();
        String operatorName = SecurityFrameworkUtils.getLoginUserNickname();
        
        // 构建日志记录
        IotAccessOperationLogDO operationLog = IotAccessOperationLogDO.builder()
                .operationType(IotAccessOperationLogDO.OP_TYPE_AUTH_RETRY)
                .operationTime(LocalDateTime.now())
                .operatorId(operatorId)
                .operatorName(operatorName)
                // 设备信息
                .deviceId(deviceId)
                .deviceName(deviceName)
                .channelId(channelId)
                // 目标人员信息
                .targetPersonId(targetPersonId)
                .targetPersonCode(targetPersonCode)
                .targetPersonName(targetPersonName)
                // 任务信息
                .authTaskId(authTaskId)
                // 结果信息
                .result(dispatchResult.isSuccess() ? IotAccessOperationLogDO.RESULT_SUCCESS 
                        : IotAccessOperationLogDO.RESULT_FAILURE)
                .errorMessage(dispatchResult.getErrorMessage())
                .sdkErrorCode(dispatchResult.getErrorCode())
                // 凭证信息
                .credentialTypes(buildCredentialTypesString(dispatchResult))
                .successCredentialCount(dispatchResult.getSuccessCredentials() != null 
                        ? dispatchResult.getSuccessCredentials().size() : 0)
                .failedCredentialCount(dispatchResult.getFailedCredentials() != null 
                        ? dispatchResult.getFailedCredentials().size() : 0)
                .build();
        
        // 设置结果描述
        operationLog.setResultDesc(buildRetryResultDesc(dispatchResult));
        
        operationLogMapper.insert(operationLog);
        
        log.info("[logAuthRetry] 记录授权重试日志: logId={}, taskId={}, personId={}, deviceId={}, result={}",
                operationLog.getId(), authTaskId, targetPersonId, deviceId, 
                dispatchResult.isSuccess() ? "成功" : "失败");
        
        return operationLog.getId();
    }
    
    @Override
    public PageResult<IotAccessOperationLogDO> getAuthOperationLogPage(IotAccessOperationLogPageReqVO pageReqVO) {
        return operationLogMapper.selectAuthLogPage(pageReqVO);
    }
    
    @Override
    public List<IotAccessOperationLogDO> getAuthLogsByPerson(Long personId, LocalDateTime startTime, LocalDateTime endTime) {
        return operationLogMapper.selectAuthLogsByPerson(personId, startTime, endTime);
    }
    
    @Override
    public List<IotAccessOperationLogDO> getAuthLogsByDevice(Long deviceId, LocalDateTime startTime, LocalDateTime endTime) {
        return operationLogMapper.selectAuthLogsByDevice(deviceId, startTime, endTime);
    }
    
    @Override
    public List<IotAccessOperationLogDO> getAuthLogsByTaskId(Long authTaskId) {
        return operationLogMapper.selectAuthLogsByTaskId(authTaskId);
    }
    
    // ========== 私有辅助方法 ==========
    
    /**
     * 构建凭证类型字符串
     */
    private String buildCredentialTypesString(DispatchResult result) {
        if (result == null) {
            return null;
        }
        
        StringBuilder sb = new StringBuilder();
        
        // 添加成功的凭证类型
        if (result.getSuccessCredentials() != null && !result.getSuccessCredentials().isEmpty()) {
            sb.append(String.join(",", result.getSuccessCredentials()));
        }
        
        // 添加失败的凭证类型
        if (result.getFailedCredentials() != null && !result.getFailedCredentials().isEmpty()) {
            if (sb.length() > 0) {
                sb.append(",");
            }
            sb.append(result.getFailedCredentials().stream()
                    .map(DispatchResult.CredentialError::getCredentialType)
                    .collect(Collectors.joining(",")));
        }
        
        return sb.length() > 0 ? sb.toString() : null;
    }
    
    /**
     * 构建下发结果描述
     */
    private String buildResultDesc(DispatchResult result) {
        if (result == null) {
            return "未知结果";
        }
        
        if (result.isSuccess()) {
            int successCount = result.getSuccessCredentials() != null ? result.getSuccessCredentials().size() : 0;
            return String.format("下发成功，共%d种凭证", successCount);
        } else if (result.isPartialSuccess()) {
            int successCount = result.getSuccessCredentials() != null ? result.getSuccessCredentials().size() : 0;
            int failCount = result.getFailedCredentials() != null ? result.getFailedCredentials().size() : 0;
            return String.format("部分成功，成功%d种，失败%d种", successCount, failCount);
        } else {
            return "下发失败：" + (result.getErrorMessage() != null ? result.getErrorMessage() : "未知错误");
        }
    }
    
    /**
     * 构建撤销结果描述
     */
    private String buildRevokeResultDesc(DispatchResult result) {
        if (result == null) {
            return "未知结果";
        }
        
        if (result.isSuccess()) {
            return "撤销成功";
        } else if (result.isPartialSuccess()) {
            int successCount = result.getSuccessCredentials() != null ? result.getSuccessCredentials().size() : 0;
            int failCount = result.getFailedCredentials() != null ? result.getFailedCredentials().size() : 0;
            return String.format("部分撤销，成功%d种，失败%d种", successCount, failCount);
        } else {
            return "撤销失败：" + (result.getErrorMessage() != null ? result.getErrorMessage() : "未知错误");
        }
    }
    
    /**
     * 构建重试结果描述
     */
    private String buildRetryResultDesc(DispatchResult result) {
        if (result == null) {
            return "未知结果";
        }
        
        if (result.isSuccess()) {
            return "重试成功";
        } else if (result.isPartialSuccess()) {
            int successCount = result.getSuccessCredentials() != null ? result.getSuccessCredentials().size() : 0;
            int failCount = result.getFailedCredentials() != null ? result.getFailedCredentials().size() : 0;
            return String.format("部分成功，成功%d种，失败%d种", successCount, failCount);
        } else {
            return "重试失败：" + (result.getErrorMessage() != null ? result.getErrorMessage() : "未知错误");
        }
    }

}
