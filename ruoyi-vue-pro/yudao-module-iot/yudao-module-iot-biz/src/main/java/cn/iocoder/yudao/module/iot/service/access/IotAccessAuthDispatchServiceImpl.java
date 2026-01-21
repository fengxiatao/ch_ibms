package cn.iocoder.yudao.module.iot.service.access;

import cn.iocoder.yudao.module.iot.dal.dataobject.access.*;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.config.AccessDeviceConfig;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.config.GenericDeviceConfig;
import cn.iocoder.yudao.module.iot.dal.mysql.access.IotAccessPersonDeviceAuthMapper;
import cn.iocoder.yudao.module.iot.core.gateway.dto.AccessControlDeviceCommand;
import cn.iocoder.yudao.module.iot.core.gateway.dto.AccessControlDeviceResponse;
import cn.iocoder.yudao.module.iot.core.gateway.dto.access.*;
import cn.iocoder.yudao.module.iot.enums.access.CredentialTypeConstants;
import cn.iocoder.yudao.module.iot.enums.device.AccessDeviceTypeConstants;
import cn.iocoder.yudao.module.iot.mq.producer.DeviceCommandPublisher;
import cn.iocoder.yudao.module.iot.service.access.dto.CapabilityCheckResult;
import cn.iocoder.yudao.module.iot.service.access.dto.DispatchResult;
import cn.iocoder.yudao.module.iot.service.access.event.DispatchTaskCreatedEvent;
import cn.iocoder.yudao.module.iot.websocket.AuthTaskProgressPushService;
import cn.iocoder.yudao.framework.tenant.core.util.TenantUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.validation.annotation.Validated;

import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Qualifier;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.iot.enums.ErrorCodeConstants.*;

/**
 * 授权下发服务实现类
 * 
 * <p>通过消息总线与 gateway 模块通信，避免直接依赖 SDK 管理器</p>
 * <p>使用统一的 DeviceCommandPublisher 发送命令到 DEVICE_SERVICE_INVOKE 主题</p>
 * 
 * Requirements: 1.1, 2.1, 2.3, 7.2, 8.1, 8.2, 9.1, 9.2, 9.3, 9.4, 9.5
 *
 * @author Kiro
 */
@Slf4j
@Service
@Validated
public class IotAccessAuthDispatchServiceImpl implements IotAccessAuthDispatchService {

    @Resource
    private IotAccessAuthTaskService authTaskService;
    
    @Resource
    private IotAccessPermissionGroupService permissionGroupService;
    
    @Resource
    private IotAccessPersonService personService;
    
    @Resource
    private IotAccessDeviceService deviceService;
    
    @Resource
    private IotAccessDeviceCapabilityService capabilityService;
    
    @Resource
    private DispatchDataConverter dataConverter;
    
    @Resource
    private IotAccessPersonDeviceAuthMapper personDeviceAuthMapper;
    
    @Resource
    private IotAccessDeviceDispatchQueue dispatchQueue;
    
    // 通过消息总线与gateway通信（替代直接SDK管理器注入）
    @Resource
    private AccessControlMessageBusClient messageBusClient;
    
    /**
     * 统一命令发布器
     * Requirements: 9.1 - 门禁设备命令使用统一接口
     */
    @Resource
    private DeviceCommandPublisher deviceCommandPublisher;
    
    @Resource
    private AuthTaskProgressPushService progressPushService;
    
    @Resource
    private ApplicationEventPublisher eventPublisher;
    
    /**
     * 异步执行器 - 用于在 afterCommit 回调中真正异步提交任务
     * 避免 @Async 自调用问题
     */
    @Resource
    @Qualifier("accessDispatchExecutor")
    private Executor accessDispatchExecutor;
    
    // 正在执行的任务集合（用于取消任务）
    private final Set<Long> runningTasks = ConcurrentHashMap.newKeySet();

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW, timeout = 5, rollbackFor = Exception.class)
    public Long dispatchPermissionGroup(Long groupId) {
        // 1. 校验权限组存在
        IotAccessPermissionGroupDO group = permissionGroupService.getPermissionGroup(groupId);
        if (group == null) {
            throw exception(ACCESS_PERMISSION_GROUP_NOT_EXISTS);
        }
        
        // 2. 获取权限组关联的设备和人员
        List<IotAccessPermissionGroupDeviceDO> groupDevices = permissionGroupService.getGroupDevices(groupId);
        List<IotAccessPermissionGroupPersonDO> groupPersons = permissionGroupService.getGroupPersons(groupId);
        
        if (groupDevices.isEmpty()) {
            log.warn("[dispatchPermissionGroup] 权限组没有关联设备: groupId={}", groupId);
            throw exception(ACCESS_PERMISSION_GROUP_NOT_EXISTS);
        }
        
        if (groupPersons.isEmpty()) {
            log.warn("[dispatchPermissionGroup] 权限组没有关联人员: groupId={}", groupId);
            throw exception(ACCESS_PERMISSION_GROUP_HAS_PERSON);
        }
        
        // 3. 创建授权任务（短事务）
        Long taskId = authTaskService.createTask(IotAccessAuthTaskDO.TASK_TYPE_GROUP_DISPATCH, groupId, null);
        
        // 4. 创建任务明细（人员 × 设备）
        List<IotAccessAuthTaskDetailDO> details = createTaskDetails(
                groupPersons.stream().map(IotAccessPermissionGroupPersonDO::getPersonId).collect(Collectors.toList()),
                groupDevices
        );
        authTaskService.addTaskDetails(taskId, details);
        
        // 5. 注册事务提交后回调，确保任务在事务提交后执行
        final Long finalTaskId = taskId;
        final Long finalGroupId = groupId;
        final int personCount = groupPersons.size();
        final int deviceCount = groupDevices.size();
        
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    log.info("[dispatchPermissionGroup] 事务已提交，提交异步任务: taskId={}", finalTaskId);
                    // 使用执行器真正异步提交任务，避免阻塞 afterCommit 回调
                    accessDispatchExecutor.execute(() -> {
                        try {
                            executeTaskAsyncInternal(finalTaskId);
                        } catch (Exception e) {
                            log.error("[dispatchPermissionGroup] 异步任务执行异常: taskId={}", finalTaskId, e);
                        }
                    });
                }
            });
            log.info("[dispatchPermissionGroup] 创建权限组下发任务(事务同步): taskId={}, groupId={}, personCount={}, deviceCount={}",
                    taskId, finalGroupId, personCount, deviceCount);
        } else {
            // 如果没有活动的事务同步，直接发布事件
            log.warn("[dispatchPermissionGroup] 无活动事务同步，使用事件模式: taskId={}", taskId);
            List<Long> detailIds = details.stream()
                    .map(IotAccessAuthTaskDetailDO::getId)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
            eventPublisher.publishEvent(DispatchTaskCreatedEvent.forPermissionGroup(this, taskId, groupId, detailIds));
            log.info("[dispatchPermissionGroup] 创建权限组下发任务(事件模式): taskId={}, groupId={}, personCount={}, deviceCount={}",
                    taskId, finalGroupId, personCount, deviceCount);
        }
        
        return taskId;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW, timeout = 5, rollbackFor = Exception.class)
    public Long dispatchPerson(Long personId) {
        // 1. 校验人员存在
        IotAccessPersonDO person = personService.getPerson(personId);
        if (person == null) {
            throw exception(ACCESS_PERSON_NOT_EXISTS);
        }
        
        // 2. 获取人员关联的所有权限组
        List<IotAccessPermissionGroupPersonDO> personGroups = permissionGroupService.getPersonGroups(personId);
        if (personGroups.isEmpty()) {
            log.warn("[dispatchPerson] 人员没有关联权限组: personId={}", personId);
            throw exception(ACCESS_PERMISSION_GROUP_NOT_EXISTS);
        }
        
        // 3. 汇总所有需要下发的设备（去重）
        Set<DeviceChannelKey> deviceSet = new HashSet<>();
        for (IotAccessPermissionGroupPersonDO pg : personGroups) {
            List<IotAccessPermissionGroupDeviceDO> groupDevices = 
                    permissionGroupService.getGroupDevices(pg.getGroupId());
            for (IotAccessPermissionGroupDeviceDO gd : groupDevices) {
                deviceSet.add(new DeviceChannelKey(gd.getDeviceId(), gd.getChannelId()));
            }
        }
        
        if (deviceSet.isEmpty()) {
            log.warn("[dispatchPerson] 人员关联的权限组没有设备: personId={}", personId);
            throw exception(ACCESS_PERMISSION_GROUP_NOT_EXISTS);
        }
        
        // 4. 创建授权任务（短事务）
        Long taskId = authTaskService.createTask(IotAccessAuthTaskDO.TASK_TYPE_PERSON_DISPATCH, null, personId);
        
        // 5. 创建任务明细
        List<IotAccessAuthTaskDetailDO> details = new ArrayList<>();
        for (DeviceChannelKey key : deviceSet) {
            IotDeviceDO device = deviceService.getAccessDevice(key.deviceId);
            details.add(IotAccessAuthTaskDetailDO.builder()
                    .personId(personId)
                    .personCode(person.getPersonCode())
                    .personName(person.getPersonName())
                    .deviceId(key.deviceId)
                    .deviceName(device != null ? device.getDeviceName() : null)
                    .channelId(key.channelId)
                    .build());
        }
        authTaskService.addTaskDetails(taskId, details);
        
        // 6. 注册事务提交后回调，确保任务在事务提交后执行
        final Long finalTaskId = taskId;
        final Long finalPersonId = personId;
        final int deviceCount = deviceSet.size();
        
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    log.info("[dispatchPerson] 事务已提交，提交异步任务: taskId={}", finalTaskId);
                    // 使用执行器真正异步提交任务，避免阻塞 afterCommit 回调
                    accessDispatchExecutor.execute(() -> {
                        try {
                            executeTaskAsyncInternal(finalTaskId);
                        } catch (Exception e) {
                            log.error("[dispatchPerson] 异步任务执行异常: taskId={}", finalTaskId, e);
                        }
                    });
                }
            });
            log.info("[dispatchPerson] 创建单人下发任务(事务同步): taskId={}, personId={}, deviceCount={}",
                    taskId, finalPersonId, deviceCount);
        } else {
            log.warn("[dispatchPerson] 无活动事务同步，使用事件模式: taskId={}", taskId);
            List<Long> detailIds = details.stream()
                    .map(IotAccessAuthTaskDetailDO::getId)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
            eventPublisher.publishEvent(DispatchTaskCreatedEvent.forPerson(this, taskId, personId, detailIds));
            log.info("[dispatchPerson] 创建单人下发任务(事件模式): taskId={}, personId={}, deviceCount={}",
                    taskId, finalPersonId, deviceCount);
        }
        
        return taskId;
    }

    @Override
    public DispatchResult dispatchPersonToDevice(Long personId, Long deviceId, Long channelId) {
        log.info("[dispatchPersonToDevice] 开始下发: personId={}, deviceId={}, channelId={}", 
                personId, deviceId, channelId);
        
        try {
            // 1. 获取人员信息和凭证
            IotAccessPersonDO person = personService.getPerson(personId);
            if (person == null) {
                return DispatchResult.failure(-1, "人员不存在");
            }
            
            List<IotAccessPersonCredentialDO> credentials = personService.getPersonCredentials(personId);
            
            // 2. 获取设备信息
            IotDeviceDO device = deviceService.getAccessDevice(deviceId);
            if (device == null) {
                return DispatchResult.failure(-1, "设备不存在");
            }
            
            // 3. 获取设备连接信息
            String ip = getDeviceIp(device);
            Integer port = getDevicePort(device);
            String username = getDeviceUsername(device);
            String password = getDevicePassword(device);
            String deviceType = getAccessDeviceType(device);
            
            // 4. 检查设备是否在线，如果不在线则尝试自动激活 (Requirements: 4.1, 4.2)
            // 注意：必须在设备能力预检查之前执行，因为预检查也需要设备在线
            if (!messageBusClient.isDeviceOnline(deviceId, ip, port, deviceType)) {
                log.info("[dispatchPersonToDevice] 设备未在线，尝试自动激活: deviceId={}, ip={}", deviceId, ip);
                
                // 检查是否有足够的连接信息
                if (username == null || username.isEmpty() || password == null || password.isEmpty()) {
                    log.warn("[dispatchPersonToDevice] 设备配置不完整，无法自动激活: deviceId={}", deviceId);
                    return DispatchResult.failure(-1, "设备未连接且配置不完整，无法自动激活");
                }
                
                // 尝试自动激活设备
                cn.iocoder.yudao.module.iot.core.gateway.dto.access.ActivationResult activationResult = 
                        messageBusClient.activateDevice(deviceId, ip, port, username, password);
                
                if (!activationResult.isSuccess()) {
                    log.warn("[dispatchPersonToDevice] 设备自动激活失败: deviceId={}, error={}", 
                            deviceId, activationResult.getErrorMessage());
                    return DispatchResult.failure(-1, "设备激活失败: " + activationResult.getErrorMessage());
                }
                
                log.info("[dispatchPersonToDevice] 设备自动激活成功: deviceId={}, 耗时={}ms", 
                        deviceId, activationResult.getActivationTimeMs());

                // 激活成功后立即刷新能力并写回 iot_device.config（供后续按能力精准下发）
                try {
                    Long tenantId = device.getTenantId() != null ? device.getTenantId() : 1L;
                    TenantUtils.execute(tenantId, () -> capabilityService.refreshCapability(deviceId));
                } catch (Exception e) {
                    log.warn("[dispatchPersonToDevice] 激活后刷新能力失败（不影响主流程）: deviceId={}, error={}",
                            deviceId, e.getMessage());
                }
            }
            
            // 5. 设备能力预检查（Requirements: 16.1, 16.2）
            // 注意：必须在设备激活之后执行，否则会因为设备不在线而失败
            CapabilityCheckResult checkResult = capabilityService.preCheckCapability(deviceId, credentials);
            if (!checkResult.isCanProceed()) {
                String errorMsg = checkResult.getErrorSummary();
                log.warn("[dispatchPersonToDevice] 设备能力预检查失败: deviceId={}, error={}", deviceId, errorMsg);
                return DispatchResult.failure(-1, errorMsg != null ? errorMsg : "设备能力检查失败");
            }
            
            // 记录警告信息
            if (!checkResult.getWarnings().isEmpty()) {
                log.info("[dispatchPersonToDevice] 设备能力检查警告: deviceId={}, warnings={}", 
                        deviceId, checkResult.getWarnings());
            }
            
            // 6. 获取设备能力（用于后续凭证下发判断）
            IotAccessDeviceCapabilityDO capability = capabilityService.getCapability(deviceId);
            
            // 7. 构造SDK数据结构
            int[] doors = {0}; // 默认门0，实际应根据通道配置
            NetAccessUserInfo userInfo = dataConverter.convertToUserInfo(person, credentials, null, doors);
            
            // 8. 下发用户信息
            List<String> successCredentials = new ArrayList<>();
            List<DispatchResult.CredentialError> failedCredentials = new ArrayList<>();
            
            // 8.1 通过消息总线下发用户基本信息
            AccessControlDeviceResponse userResponse = sendDispatchUserCommand(personId, deviceId, channelId, ip, port, userInfo);
            if (userResponse == null || !Boolean.TRUE.equals(userResponse.getSuccess())) {
                String errorMsg = userResponse != null ? userResponse.getErrorMessage() : "发送命令超时";
                // 安全获取 errorCode，避免 NPE
                int errorCode = (userResponse != null && userResponse.getErrorCode() != null) 
                        ? userResponse.getErrorCode() : -1;
                return DispatchResult.failure(errorCode, "用户信息下发失败: " + errorMsg);
            }
            
            // 8.2 下发各类凭证（跳过不支持的凭证类型，Requirements: 3.6, 16.2）
            Set<String> unsupportedTypes = new HashSet<>(checkResult.getUnsupportedCredentialTypes());
            List<String> skippedCredentials = new ArrayList<>(); // 设备不支持的凭证类型（不算失败）
            
            for (IotAccessPersonCredentialDO credential : credentials) {
                String credentialType = credential.getCredentialType();
                
                // 检查是否为不支持的凭证类型，跳过但不算失败（只记录警告）
                if (unsupportedTypes.contains(credentialType)) {
                    log.warn("[dispatchPersonToDevice] 跳过不支持的凭证类型（设备能力限制，不算失败）: type={}, deviceId={}", 
                            credentialType, deviceId);
                    skippedCredentials.add(credentialType.toUpperCase());
                    continue;
                }
                
                try {
                    boolean success = dispatchCredentialViaMessageBus(deviceId, ip, port, person, credential, capability);
                    if (success) {
                        // 统一使用大写存储，便于后续比较
                        successCredentials.add(credentialType.toUpperCase());
                    } else {
                        failedCredentials.add(DispatchResult.CredentialError.builder()
                                .credentialType(credentialType)
                                .errorCode(-1)
                                .errorMessage("凭证下发失败")
                                .build());
                    }
                } catch (Exception e) {
                    log.error("[dispatchPersonToDevice] 凭证下发异常: type={}", credentialType, e);
                    failedCredentials.add(DispatchResult.CredentialError.builder()
                            .credentialType(credentialType)
                            .errorCode(-1)
                            .errorMessage(e.getMessage())
                            .build());
                }
            }
            
            // 记录跳过的凭证类型（便于排查）
            if (!skippedCredentials.isEmpty()) {
                log.info("[dispatchPersonToDevice] 以下凭证类型因设备不支持而跳过（不影响任务状态）: {}", skippedCredentials);
            }
            
            // 9. 计算凭证哈希并更新人员设备授权状态（Requirements: 15.3, 15.4）
            // 注意：设备不支持的凭证类型不算失败，只有实际下发失败才算失败
            String credentialHash = credentialHashCalculator.calculateHash(credentials);
            String statusMsg = failedCredentials.isEmpty() 
                    ? (skippedCredentials.isEmpty() ? "下发成功" : "下发成功（部分凭证类型设备不支持）")
                    : "部分凭证下发失败";
            updatePersonDeviceAuthWithHash(personId, deviceId, channelId, 
                    failedCredentials.isEmpty() ? IotAccessPersonDeviceAuthDO.AUTH_STATUS_AUTHORIZED 
                            : IotAccessPersonDeviceAuthDO.AUTH_STATUS_FAILED,
                    statusMsg,
                    credentialHash);
            
            // 10. 更新设备使用量
            if (!successCredentials.isEmpty()) {
                int faceCount = successCredentials.contains(CredentialTypeConstants.FACE) ? 1 : 0;
                int cardCount = (int) successCredentials.stream()
                        .filter(CredentialTypeConstants.CARD::equals).count();
                int fpCount = (int) successCredentials.stream()
                        .filter(CredentialTypeConstants.FINGERPRINT::equals).count();
                capabilityService.incrementUsage(deviceId, 1, cardCount, faceCount, fpCount);
            }
            
            return DispatchResult.partial(successCredentials, failedCredentials);
            
        } catch (Exception e) {
            log.error("[dispatchPersonToDevice] 下发异常: personId={}, deviceId={}", personId, deviceId, e);
            return DispatchResult.failure(-1, "下发异常: " + e.getMessage());
        }
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long revokePerson(Long personId) {
        // 1. 校验人员存在
        IotAccessPersonDO person = personService.getPerson(personId);
        if (person == null) {
            throw exception(ACCESS_PERSON_NOT_EXISTS);
        }
        
        // 2. 获取人员已授权的设备列表
        List<IotAccessPersonDeviceAuthDO> authList = personDeviceAuthMapper.selectListByPersonId(personId);
        List<IotAccessPersonDeviceAuthDO> authorizedList = authList.stream()
                .filter(a -> a.getAuthStatus() == IotAccessPersonDeviceAuthDO.AUTH_STATUS_AUTHORIZED)
                .collect(Collectors.toList());
        
        if (authorizedList.isEmpty()) {
            log.warn("[revokePerson] 人员没有已授权的设备: personId={}", personId);
            // 仍然创建任务，但没有明细
            return authTaskService.createTask(IotAccessAuthTaskDO.TASK_TYPE_REVOKE, null, personId);
        }
        
        // 3. 创建撤销任务
        Long taskId = authTaskService.createTask(IotAccessAuthTaskDO.TASK_TYPE_REVOKE, null, personId);
        
        // 4. 创建任务明细
        List<IotAccessAuthTaskDetailDO> details = new ArrayList<>();
        for (IotAccessPersonDeviceAuthDO auth : authorizedList) {
            IotDeviceDO device = deviceService.getAccessDevice(auth.getDeviceId());
            details.add(IotAccessAuthTaskDetailDO.builder()
                    .personId(personId)
                    .personCode(person.getPersonCode())
                    .personName(person.getPersonName())
                    .deviceId(auth.getDeviceId())
                    .deviceName(device != null ? device.getDeviceName() : null)
                    .channelId(auth.getChannelId())
                    .build());
        }
        authTaskService.addTaskDetails(taskId, details);
        
        // 5. 异步执行撤销任务
        executeRevokeTaskAsync(taskId);
        
        log.info("[revokePerson] 创建撤销任务: taskId={}, personId={}, deviceCount={}",
                taskId, personId, authorizedList.size());
        
        return taskId;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long revokePersons(List<Long> personIds) {
        if (personIds == null || personIds.isEmpty()) {
            throw exception(ACCESS_PERSON_NOT_EXISTS);
        }
        
        // 1. 获取所有人员已授权的设备列表
        List<IotAccessPersonDeviceAuthDO> allAuthList = personDeviceAuthMapper.selectListByPersonIdsAndAuthStatus(
                personIds, IotAccessPersonDeviceAuthDO.AUTH_STATUS_AUTHORIZED);
        
        if (allAuthList.isEmpty()) {
            log.warn("[revokePersons] 人员没有已授权的设备: personIds={}", personIds);
            // 仍然创建任务，但没有明细
            return authTaskService.createTask(IotAccessAuthTaskDO.TASK_TYPE_REVOKE, null, null);
        }
        
        // 2. 创建撤销任务
        Long taskId = authTaskService.createTask(IotAccessAuthTaskDO.TASK_TYPE_REVOKE, null, null);
        
        // 3. 创建任务明细
        List<IotAccessAuthTaskDetailDO> details = new ArrayList<>();
        for (IotAccessPersonDeviceAuthDO auth : allAuthList) {
            IotAccessPersonDO person = personService.getPerson(auth.getPersonId());
            if (person == null) {
                continue;
            }
            IotDeviceDO device = deviceService.getAccessDevice(auth.getDeviceId());
            details.add(IotAccessAuthTaskDetailDO.builder()
                    .personId(auth.getPersonId())
                    .personCode(person.getPersonCode())
                    .personName(person.getPersonName())
                    .deviceId(auth.getDeviceId())
                    .deviceName(device != null ? device.getDeviceName() : null)
                    .channelId(auth.getChannelId())
                    .build());
        }
        
        if (!details.isEmpty()) {
            authTaskService.addTaskDetails(taskId, details);
        }
        
        // 4. 异步执行撤销任务
        executeRevokeTaskAsync(taskId);
        
        log.info("[revokePersons] 创建批量撤销任务: taskId={}, personCount={}, detailCount={}",
                taskId, personIds.size(), details.size());
        
        return taskId;
    }

    @Override
    public DispatchResult revokePersonFromDevice(Long personId, Long deviceId, Long channelId) {
        log.info("[revokePersonFromDevice] 开始撤销: personId={}, deviceId={}, channelId={}", 
                personId, deviceId, channelId);
        
        try {
            // 1. 获取人员信息
            IotAccessPersonDO person = personService.getPerson(personId);
            if (person == null) {
                return DispatchResult.failure(-1, "人员不存在");
            }
            
            // 2. 获取设备信息
            IotDeviceDO device = deviceService.getAccessDevice(deviceId);
            if (device == null) {
                return DispatchResult.failure(-1, "设备不存在");
            }
            
            // 3. 获取设备连接信息并检查在线状态
            String ip = getDeviceIp(device);
            Integer port = getDevicePort(device);
            String username = getDeviceUsername(device);
            String password = getDevicePassword(device);
            String deviceType = getAccessDeviceType(device);
            
            if (!messageBusClient.isDeviceOnline(deviceId, ip, port, deviceType)) {
                // 尝试自动激活设备 (Requirements: 4.1, 4.2)
                if (username != null && !username.isEmpty() && password != null && !password.isEmpty()) {
                    log.info("[revokePersonFromDevice] 设备未在线，尝试自动激活: deviceId={}", deviceId);
                    cn.iocoder.yudao.module.iot.core.gateway.dto.access.ActivationResult activationResult = 
                            messageBusClient.activateDevice(deviceId, ip, port, username, password);
                    
                    if (!activationResult.isSuccess()) {
                        // 激活失败，标记待撤销
                        log.warn("[revokePersonFromDevice] 设备激活失败，标记待撤销: deviceId={}", deviceId);
                        markPendingRevoke(personId, deviceId, channelId);
                        return DispatchResult.failure(-2, "设备激活失败，已标记待撤销: " + activationResult.getErrorMessage());
                    }
                    log.info("[revokePersonFromDevice] 设备自动激活成功: deviceId={}", deviceId);

                    // 激活成功后立即刷新能力并写回 iot_device.config（供后续按能力精准撤销）
                    try {
                        Long tenantId = device.getTenantId() != null ? device.getTenantId() : 1L;
                        TenantUtils.execute(tenantId, () -> capabilityService.refreshCapability(deviceId));
                    } catch (Exception e) {
                        log.warn("[revokePersonFromDevice] 激活后刷新能力失败（不影响主流程）: deviceId={}, error={}",
                                deviceId, e.getMessage());
                    }
                } else {
                    // 配置不完整，标记待撤销
                    markPendingRevoke(personId, deviceId, channelId);
                    return DispatchResult.failure(-2, "设备未连接，已标记待撤销");
                }
            }
            
            // 4. 更新授权状态为撤销中
            updatePersonDeviceAuth(personId, deviceId, channelId,
                    IotAccessPersonDeviceAuthDO.AUTH_STATUS_REVOKING, null);
            
            // 5. 获取人员实际拥有的凭证类型（修复：根据人员凭证而非设备能力判断需要撤销的凭证）
            List<IotAccessPersonCredentialDO> personCredentials = personService.getPersonCredentials(personId);
            Set<String> personCredentialTypes = personCredentials.stream()
                    .map(c -> CredentialTypeConstants.normalize(c.getCredentialType()))
                    .filter(t -> t != null)
                    .collect(Collectors.toSet());
            
            log.info("[revokePersonFromDevice] 人员凭证类型: personId={}, credentialTypes={}", 
                    personId, personCredentialTypes);
            
            // 6. 级联删除人员实际拥有的凭证（Requirements: 8.3）
            List<String> successCredentials = new ArrayList<>();
            List<DispatchResult.CredentialError> failedCredentials = new ArrayList<>();
            String userId = person.getPersonCode();
            
            // 6.1 删除人脸（仅当人员有人脸凭证时）
            if (personCredentialTypes.contains(CredentialTypeConstants.FACE)) {
                revokeCredentialViaMessageBus(deviceId, ip, port, userId, 
                        AccessControlDeviceCommand.CommandType.REVOKE_FACE,
                        CredentialTypeConstants.FACE, successCredentials, failedCredentials);
            }
            
            // 6.2 删除卡片（仅当人员有卡片凭证时）
            if (personCredentialTypes.contains(CredentialTypeConstants.CARD)) {
                revokeCredentialViaMessageBus(deviceId, ip, port, userId,
                        AccessControlDeviceCommand.CommandType.REVOKE_CARD,
                        CredentialTypeConstants.CARD, successCredentials, failedCredentials);
            }
            
            // 6.3 删除指纹（仅当人员有指纹凭证时）
            if (personCredentialTypes.contains(CredentialTypeConstants.FINGERPRINT)) {
                revokeCredentialViaMessageBus(deviceId, ip, port, userId,
                        AccessControlDeviceCommand.CommandType.REVOKE_FINGERPRINT,
                        CredentialTypeConstants.FINGERPRINT, successCredentials, failedCredentials);
            }
            
            // 6.4 删除用户信息（最后删除，因为凭证关联用户）
            AccessControlDeviceResponse userResponse = sendRevokeUserCommand(deviceId, ip, port, userId);
            if (userResponse == null || !Boolean.TRUE.equals(userResponse.getSuccess())) {
                // 安全获取 errorCode，避免 NPE
                int errorCode = (userResponse != null && userResponse.getErrorCode() != null) 
                        ? userResponse.getErrorCode() : -1;
                if (errorCode != 4) { // 非"用户不存在"错误
                    String errorMsg = userResponse != null ? userResponse.getErrorMessage() : "发送命令超时";
                    return DispatchResult.failure(errorCode, "删除用户失败: " + errorMsg);
                }
            }
            
            // 7. 更新授权状态
            updatePersonDeviceAuth(personId, deviceId, channelId,
                    IotAccessPersonDeviceAuthDO.AUTH_STATUS_UNAUTHORIZED, "已撤销");
            
            // 8. 减少设备使用量
            int faceCount = successCredentials.contains(CredentialTypeConstants.FACE) ? 1 : 0;
            int cardCount = (int) successCredentials.stream()
                    .filter(CredentialTypeConstants.CARD::equals).count();
            int fpCount = (int) successCredentials.stream()
                    .filter(CredentialTypeConstants.FINGERPRINT::equals).count();
            capabilityService.decrementUsage(deviceId, 1, cardCount, faceCount, fpCount);
            
            log.info("[revokePersonFromDevice] 撤销成功: personId={}, deviceId={}, successCredentials={}, failedCredentials={}", 
                    personId, deviceId, successCredentials, 
                    failedCredentials.stream().map(e -> e.getCredentialType() + ":" + e.getErrorMessage()).toList());
            
            // 撤销核心是删除用户（REVOKE_USER），只要用户删除成功，即使部分凭证删除失败也视为成功
            // 因为用户删除后，设备上该用户的所有凭证都会失效
            if (!failedCredentials.isEmpty()) {
                log.warn("[revokePersonFromDevice] 部分凭证撤销失败，但用户已删除: personId={}, deviceId={}, failedTypes={}", 
                        personId, deviceId, failedCredentials.stream().map(DispatchResult.CredentialError::getCredentialType).toList());
            }
            // 返回成功结果，包含所有尝试撤销的凭证类型
            List<String> allCredentialTypes = new ArrayList<>(successCredentials);
            failedCredentials.forEach(e -> allCredentialTypes.add(e.getCredentialType()));
            return DispatchResult.success(allCredentialTypes);
            
        } catch (Exception e) {
            log.error("[revokePersonFromDevice] 撤销异常: personId={}, deviceId={}", personId, deviceId, e);
            return DispatchResult.failure(-1, "撤销异常: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long revokePersonFromGroup(Long personId, Long groupId) {
        // 1. 校验人员存在
        IotAccessPersonDO person = personService.getPerson(personId);
        if (person == null) {
            throw exception(ACCESS_PERSON_NOT_EXISTS);
        }
        
        // 2. 获取权限组关联的设备
        List<IotAccessPermissionGroupDeviceDO> groupDevices = permissionGroupService.getGroupDevices(groupId);
        if (groupDevices.isEmpty()) {
            log.warn("[revokePersonFromGroup] 权限组没有关联设备: groupId={}", groupId);
            return authTaskService.createTask(IotAccessAuthTaskDO.TASK_TYPE_REVOKE, groupId, personId);
        }
        
        // 3. 检查人员在这些设备上是否还有其他权限组的授权
        // 如果有其他权限组也授权了同一设备，则不撤销
        List<IotAccessPermissionGroupPersonDO> personGroups = permissionGroupService.getPersonGroups(personId);
        Set<Long> otherGroupDeviceIds = new HashSet<>();
        for (IotAccessPermissionGroupPersonDO pg : personGroups) {
            if (!pg.getGroupId().equals(groupId)) {
                List<IotAccessPermissionGroupDeviceDO> otherDevices = 
                        permissionGroupService.getGroupDevices(pg.getGroupId());
                for (IotAccessPermissionGroupDeviceDO od : otherDevices) {
                    otherGroupDeviceIds.add(od.getDeviceId());
                }
            }
        }
        
        // 4. 筛选出需要撤销的设备（不在其他权限组中的设备）
        List<IotAccessPermissionGroupDeviceDO> devicesToRevoke = groupDevices.stream()
                .filter(gd -> !otherGroupDeviceIds.contains(gd.getDeviceId()))
                .collect(Collectors.toList());
        
        if (devicesToRevoke.isEmpty()) {
            log.info("[revokePersonFromGroup] 人员在其他权限组仍有授权，无需撤销: personId={}, groupId={}", 
                    personId, groupId);
            return authTaskService.createTask(IotAccessAuthTaskDO.TASK_TYPE_REVOKE, groupId, personId);
        }
        
        // 5. 创建撤销任务
        Long taskId = authTaskService.createTask(IotAccessAuthTaskDO.TASK_TYPE_REVOKE, groupId, personId);
        
        // 6. 创建任务明细
        List<IotAccessAuthTaskDetailDO> details = new ArrayList<>();
        for (IotAccessPermissionGroupDeviceDO gd : devicesToRevoke) {
            IotDeviceDO device = deviceService.getAccessDevice(gd.getDeviceId());
            details.add(IotAccessAuthTaskDetailDO.builder()
                    .personId(personId)
                    .personCode(person.getPersonCode())
                    .personName(person.getPersonName())
                    .deviceId(gd.getDeviceId())
                    .deviceName(device != null ? device.getDeviceName() : null)
                    .channelId(gd.getChannelId())
                    .build());
        }
        authTaskService.addTaskDetails(taskId, details);
        
        // 7. 异步执行撤销任务
        executeRevokeTaskAsync(taskId);
        
        log.info("[revokePersonFromGroup] 创建权限组撤销任务: taskId={}, personId={}, groupId={}, deviceCount={}",
                taskId, personId, groupId, devicesToRevoke.size());
        
        return taskId;
    }

    @Override
    public void markPendingRevoke(Long personId, Long deviceId, Long channelId) {
        log.info("[markPendingRevoke] 标记待撤销: personId={}, deviceId={}, channelId={}", 
                personId, deviceId, channelId);
        
        updatePersonDeviceAuth(personId, deviceId, channelId,
                IotAccessPersonDeviceAuthDO.AUTH_STATUS_PENDING_REVOKE, "设备离线，待撤销");
    }

    @Override
    @Async("accessDispatchExecutor")
    public void executePendingRevokes(Long deviceId) {
        log.info("[executePendingRevokes] 执行待撤销任务: deviceId={}", deviceId);
        
        // 1. 查询该设备上所有待撤销的人员
        List<IotAccessPersonDeviceAuthDO> pendingList = personDeviceAuthMapper
                .selectListByDeviceIdAndAuthStatus(deviceId, IotAccessPersonDeviceAuthDO.AUTH_STATUS_PENDING_REVOKE);
        
        if (pendingList.isEmpty()) {
            log.info("[executePendingRevokes] 没有待撤销的人员: deviceId={}", deviceId);
            return;
        }
        
        // 2. 逐个执行撤销
        for (IotAccessPersonDeviceAuthDO auth : pendingList) {
            try {
                DispatchResult result = revokePersonFromDevice(
                        auth.getPersonId(), auth.getDeviceId(), auth.getChannelId());
                
                if (result.isSuccess()) {
                    log.info("[executePendingRevokes] 撤销成功: personId={}, deviceId={}", 
                            auth.getPersonId(), deviceId);
                } else {
                    log.warn("[executePendingRevokes] 撤销失败: personId={}, deviceId={}, error={}", 
                            auth.getPersonId(), deviceId, result.getErrorMessage());
                }
            } catch (Exception e) {
                log.error("[executePendingRevokes] 撤销异常: personId={}, deviceId={}", 
                        auth.getPersonId(), deviceId, e);
            }
        }
    }

    @Override
    public Long retryFailedDetails(Long taskId) {
        // 1. 获取原任务信息，判断任务类型
        IotAccessAuthTaskDO originalTask = authTaskService.getTask(taskId);
        if (originalTask == null) {
            throw exception(ACCESS_AUTH_TASK_NOT_EXISTS);
        }
        
        // 2. 创建重试任务
        Long newTaskId = authTaskService.retryTask(taskId);
        
        // 3. 根据任务类型触发异步执行
        String taskType = originalTask.getTaskType();
        log.info("[retryFailedDetails] 创建重试任务并触发执行: originalTaskId={}, newTaskId={}, taskType={}", 
                taskId, newTaskId, taskType);
        
        if (IotAccessAuthTaskDO.TASK_TYPE_REVOKE.equals(taskType)) {
            // 撤销任务
            executeRevokeTaskAsync(newTaskId);
        } else {
            // 下发任务（包括 person_dispatch, group_dispatch 等）
            executeTaskAsync(newTaskId);
        }
        
        return newTaskId;
    }

    @Override
    @Async("accessDispatchExecutor")
    public void executeTaskAsync(Long taskId) {
        if (runningTasks.contains(taskId)) {
            log.warn("[executeTaskAsync] 任务已在执行中: taskId={}", taskId);
            return;
        }
        
        runningTasks.add(taskId);
        try {
            // 1. 开始任务
            authTaskService.startTask(taskId);
            
            // 1.1 推送任务开始通知 (Requirements: 6.2)
            IotAccessAuthTaskDO task = authTaskService.getTask(taskId);
            progressPushService.pushStarted(task);
            
            // 2. 获取任务明细
            List<IotAccessAuthTaskDetailDO> details = authTaskService.getTaskDetails(taskId);
            
            // 3. 按设备分组，确保同一设备串行处理
            Map<Long, List<IotAccessAuthTaskDetailDO>> deviceDetailsMap = details.stream()
                    .collect(Collectors.groupingBy(IotAccessAuthTaskDetailDO::getDeviceId));
            
            // 4. 提交到设备队列执行
            for (Map.Entry<Long, List<IotAccessAuthTaskDetailDO>> entry : deviceDetailsMap.entrySet()) {
                Long deviceId = entry.getKey();
                List<IotAccessAuthTaskDetailDO> deviceDetails = entry.getValue();
                
                dispatchQueue.submit(deviceId, () -> {
                    for (IotAccessAuthTaskDetailDO detail : deviceDetails) {
                        if (!runningTasks.contains(taskId)) {
                            log.info("[executeTaskAsync] 任务已取消: taskId={}", taskId);
                            break;
                        }
                        executeDetail(taskId, detail);
                    }
                });
            }
            
            // 5. 等待所有设备队列完成（简化实现，实际应使用CountDownLatch或CompletableFuture）
            // 这里通过轮询检查任务状态
            waitForTaskCompletion(taskId, details.size());
            
        } catch (Exception e) {
            log.error("[executeTaskAsync] 任务执行异常: taskId={}", taskId, e);
            authTaskService.completeTask(taskId, "任务执行异常: " + e.getMessage());
        } finally {
            runningTasks.remove(taskId);
        }
    }

    @Override
    public boolean cancelTask(Long taskId) {
        if (!runningTasks.contains(taskId)) {
            return false;
        }
        runningTasks.remove(taskId);
        log.info("[cancelTask] 任务已标记取消: taskId={}", taskId);
        return true;
    }

    @Override
    public void executeDispatchAsync(Long taskId) {
        // 此方法由事件监听器调用，直接委托给 executeTaskAsync
        // 事件监听器已经确保在事务提交后、异步线程中执行
        log.info("[executeDispatchAsync] 开始执行下发任务: taskId={}", taskId);
        executeTaskAsyncInternal(taskId);
    }

    @Override
    public void executeRevokeDispatchAsync(Long taskId) {
        // 此方法由事件监听器调用，执行撤销任务
        // 事件监听器已经确保在事务提交后、异步线程中执行
        log.info("[executeRevokeDispatchAsync] 开始执行撤销任务: taskId={}", taskId);
        executeRevokeTaskAsyncInternal(taskId);
    }

    /**
     * 内部异步执行方法（不带 @Async 注解，由调用方控制异步）
     */
    private void executeTaskAsyncInternal(Long taskId) {
        if (runningTasks.contains(taskId)) {
            log.warn("[executeTaskAsyncInternal] 任务已在执行中: taskId={}", taskId);
            return;
        }
        
        runningTasks.add(taskId);
        try {
            // 1. 开始任务
            authTaskService.startTask(taskId);
            
            // 1.1 推送任务开始通知
            IotAccessAuthTaskDO task = authTaskService.getTask(taskId);
            progressPushService.pushStarted(task);
            
            // 2. 获取任务明细
            List<IotAccessAuthTaskDetailDO> details = authTaskService.getTaskDetails(taskId);
            
            // 3. 按设备分组，确保同一设备串行处理
            Map<Long, List<IotAccessAuthTaskDetailDO>> deviceDetailsMap = details.stream()
                    .collect(Collectors.groupingBy(IotAccessAuthTaskDetailDO::getDeviceId));
            
            // 4. 提交到设备队列执行
            for (Map.Entry<Long, List<IotAccessAuthTaskDetailDO>> entry : deviceDetailsMap.entrySet()) {
                Long deviceId = entry.getKey();
                List<IotAccessAuthTaskDetailDO> deviceDetails = entry.getValue();
                
                dispatchQueue.submit(deviceId, () -> {
                    for (IotAccessAuthTaskDetailDO detail : deviceDetails) {
                        if (!runningTasks.contains(taskId)) {
                            log.info("[executeTaskAsyncInternal] 任务已取消: taskId={}", taskId);
                            break;
                        }
                        executeDetail(taskId, detail);
                    }
                });
            }
            
            // 5. 等待所有设备队列完成
            waitForTaskCompletion(taskId, details.size());
            
        } catch (Exception e) {
            log.error("[executeTaskAsyncInternal] 任务执行异常: taskId={}", taskId, e);
            authTaskService.completeTask(taskId, "任务执行异常: " + e.getMessage());
        } finally {
            runningTasks.remove(taskId);
        }
    }

    // ========== 私有方法 ==========

    /**
     * 创建任务明细
     */
    private List<IotAccessAuthTaskDetailDO> createTaskDetails(List<Long> personIds, 
            List<IotAccessPermissionGroupDeviceDO> groupDevices) {
        List<IotAccessAuthTaskDetailDO> details = new ArrayList<>();
        
        for (Long personId : personIds) {
            IotAccessPersonDO person = personService.getPerson(personId);
            if (person == null) {
                continue;
            }
            
            for (IotAccessPermissionGroupDeviceDO gd : groupDevices) {
                IotDeviceDO device = deviceService.getAccessDevice(gd.getDeviceId());
                details.add(IotAccessAuthTaskDetailDO.builder()
                        .personId(personId)
                        .personCode(person.getPersonCode())
                        .personName(person.getPersonName())
                        .deviceId(gd.getDeviceId())
                        .deviceName(device != null ? device.getDeviceName() : null)
                        .channelId(gd.getChannelId())
                        .build());
            }
        }
        
        return details;
    }

    /**
     * 执行单个任务明细
     */
    private void executeDetail(Long taskId, IotAccessAuthTaskDetailDO detail) {
        try {
            // 更新授权状态为授权中
            updatePersonDeviceAuth(detail.getPersonId(), detail.getDeviceId(), detail.getChannelId(),
                    IotAccessPersonDeviceAuthDO.AUTH_STATUS_AUTHORIZING, null);
            
            // 执行下发
            DispatchResult result = dispatchPersonToDevice(
                    detail.getPersonId(), detail.getDeviceId(), detail.getChannelId());
            
            // 更新明细状态
            if (result.isSuccess()) {
                authTaskService.updateTaskDetailStatus(detail.getId(), 
                        IotAccessAuthTaskDetailDO.STATUS_SUCCESS, null, result.getCredentialTypesString());
                authTaskService.updateTaskStatus(taskId, null, 1, 0);
                
                // 推送进度更新 (Requirements: 6.2)
                IotAccessAuthTaskDO task = authTaskService.getTask(taskId);
                progressPushService.pushProgress(task, detail.getPersonName(), detail.getDeviceName());
            } else {
                authTaskService.updateTaskDetailStatus(detail.getId(),
                        IotAccessAuthTaskDetailDO.STATUS_FAILED, result.getErrorMessage(), 
                        result.getCredentialTypesString());
                authTaskService.updateTaskStatus(taskId, null, 0, 1);
                
                // 推送进度更新（带错误信息）(Requirements: 6.2)
                IotAccessAuthTaskDO task = authTaskService.getTask(taskId);
                progressPushService.pushProgressWithError(task, detail.getPersonName(), 
                        detail.getDeviceName(), result.getErrorMessage());
            }
            
        } catch (Exception e) {
            log.error("[executeDetail] 明细执行异常: detailId={}", detail.getId(), e);
            authTaskService.updateTaskDetailStatus(detail.getId(),
                    IotAccessAuthTaskDetailDO.STATUS_FAILED, "执行异常: " + e.getMessage(), null);
            authTaskService.updateTaskStatus(taskId, null, 0, 1);
            
            // 推送进度更新（带错误信息）(Requirements: 6.2)
            IotAccessAuthTaskDO task = authTaskService.getTask(taskId);
            progressPushService.pushProgressWithError(task, detail.getPersonName(), 
                    detail.getDeviceName(), e.getMessage());
        }
    }

    /**
     * 异步执行撤销任务
     */
    @Async("accessDispatchExecutor")
    public void executeRevokeTaskAsync(Long taskId) {
        executeRevokeTaskAsyncInternal(taskId);
    }

    /**
     * 内部撤销任务执行方法（不带 @Async 注解，由调用方控制异步）
     */
    private void executeRevokeTaskAsyncInternal(Long taskId) {
        if (runningTasks.contains(taskId)) {
            return;
        }
        
        runningTasks.add(taskId);
        try {
            authTaskService.startTask(taskId);
            
            // 推送任务开始通知 (Requirements: 6.2)
            IotAccessAuthTaskDO task = authTaskService.getTask(taskId);
            progressPushService.pushStarted(task);
            
            List<IotAccessAuthTaskDetailDO> details = authTaskService.getTaskDetails(taskId);
            
            for (IotAccessAuthTaskDetailDO detail : details) {
                if (!runningTasks.contains(taskId)) {
                    break;
                }
                executeRevokeDetail(taskId, detail);
            }
            
            authTaskService.completeTask(taskId, null);
            
            // 推送任务完成通知 (Requirements: 6.2)
            task = authTaskService.getTask(taskId);
            progressPushService.pushCompleted(task);
            
        } catch (Exception e) {
            log.error("[executeRevokeTaskAsyncInternal] 撤销任务执行异常: taskId={}", taskId, e);
            authTaskService.completeTask(taskId, "撤销任务执行异常: " + e.getMessage());
            
            // 推送任务完成通知（异常）(Requirements: 6.2)
            IotAccessAuthTaskDO task = authTaskService.getTask(taskId);
            progressPushService.pushCompleted(task);
        } finally {
            runningTasks.remove(taskId);
        }
    }

    /**
     * 执行单个撤销明细
     * 使用 revokePersonFromDevice 方法实现级联删除所有凭证
     * Requirements: 8.1, 8.2, 8.3
     */
    private void executeRevokeDetail(Long taskId, IotAccessAuthTaskDetailDO detail) {
        try {
            IotAccessPersonDO person = personService.getPerson(detail.getPersonId());
            if (person == null) {
                // 人员已删除，视为撤销成功
                authTaskService.updateTaskDetailStatus(detail.getId(),
                        IotAccessAuthTaskDetailDO.STATUS_SUCCESS, "人员已删除", null);
                authTaskService.updateTaskStatus(taskId, null, 1, 0);
                
                // 清理授权状态记录
                updatePersonDeviceAuth(detail.getPersonId(), detail.getDeviceId(), detail.getChannelId(),
                        IotAccessPersonDeviceAuthDO.AUTH_STATUS_UNAUTHORIZED, "人员已删除");
                
                // 推送进度更新 (Requirements: 6.2)
                IotAccessAuthTaskDO task = authTaskService.getTask(taskId);
                progressPushService.pushProgress(task, detail.getPersonName(), detail.getDeviceName());
                return;
            }
            
            // 使用 revokePersonFromDevice 执行级联删除
            DispatchResult result = revokePersonFromDevice(
                    detail.getPersonId(), detail.getDeviceId(), detail.getChannelId());
            if (result == null) {
                authTaskService.updateTaskDetailStatus(detail.getId(),
                        IotAccessAuthTaskDetailDO.STATUS_FAILED, "撤销结果为空", null);
                authTaskService.updateTaskStatus(taskId, null, 0, 1);
                IotAccessAuthTaskDO task = authTaskService.getTask(taskId);
                progressPushService.pushProgressWithError(task, detail.getPersonName(),
                        detail.getDeviceName(), "撤销结果为空");
                return;
            }
            Integer resultErrorCode = result.getErrorCode();
            
            if (result.isSuccess()) {
                authTaskService.updateTaskDetailStatus(detail.getId(),
                        IotAccessAuthTaskDetailDO.STATUS_SUCCESS, null, result.getCredentialTypesString());
                authTaskService.updateTaskStatus(taskId, null, 1, 0);
                
                // 推送进度更新 (Requirements: 6.2)
                IotAccessAuthTaskDO task = authTaskService.getTask(taskId);
                progressPushService.pushProgress(task, detail.getPersonName(), detail.getDeviceName());
            } else if (Integer.valueOf(-2).equals(resultErrorCode)) {
                // 设备离线，已标记待撤销
                authTaskService.updateTaskDetailStatus(detail.getId(),
                        IotAccessAuthTaskDetailDO.STATUS_FAILED, result.getErrorMessage(), null);
                authTaskService.updateTaskStatus(taskId, null, 0, 1);
                
                // 推送进度更新（带错误信息）(Requirements: 6.2)
                IotAccessAuthTaskDO task = authTaskService.getTask(taskId);
                progressPushService.pushProgressWithError(task, detail.getPersonName(), 
                        detail.getDeviceName(), result.getErrorMessage());
            } else {
                authTaskService.updateTaskDetailStatus(detail.getId(),
                        IotAccessAuthTaskDetailDO.STATUS_FAILED, result.getErrorMessage(), 
                        result.getCredentialTypesString());
                authTaskService.updateTaskStatus(taskId, null, 0, 1);
                
                // 推送进度更新（带错误信息）(Requirements: 6.2)
                IotAccessAuthTaskDO task = authTaskService.getTask(taskId);
                progressPushService.pushProgressWithError(task, detail.getPersonName(), 
                        detail.getDeviceName(), result.getErrorMessage());
            }
            
        } catch (Exception e) {
            log.error("[executeRevokeDetail] 撤销明细执行异常: detailId={}", detail.getId(), e);
            authTaskService.updateTaskDetailStatus(detail.getId(),
                    IotAccessAuthTaskDetailDO.STATUS_FAILED, "执行异常: " + e.getMessage(), null);
            authTaskService.updateTaskStatus(taskId, null, 0, 1);
            
            // 推送进度更新（带错误信息）(Requirements: 6.2)
            IotAccessAuthTaskDO task = authTaskService.getTask(taskId);
            progressPushService.pushProgressWithError(task, detail.getPersonName(), 
                    detail.getDeviceName(), e.getMessage());
        }
    }

    /**
     * 通过消息总线下发单个凭证
     * 
     * 注意：凭证类型比较使用 CredentialTypeConstants 的大小写不敏感方法，
     * 以兼容数据库中可能存在的不同大小写格式
     */
    private boolean dispatchCredentialViaMessageBus(Long deviceId, String ip, Integer port,
            IotAccessPersonDO person, IotAccessPersonCredentialDO credential, 
            IotAccessDeviceCapabilityDO capability) {
        String type = credential.getCredentialType();
        if (type == null) {
            log.warn("[dispatchCredential] 凭证类型为空: credentialId={}", credential.getId());
            return false;
        }
        
        // 使用 CredentialTypeConstants 的大小写不敏感比较方法
        if (CredentialTypeConstants.isFace(type)) {
            if (capability != null && capability.getSupFaceService() != 1) {
                log.warn("[dispatchCredential] 设备不支持人脸: personId={}", person.getId());
                return false;
            }
            NetAccessFaceInfo faceInfo = dataConverter.convertToFaceInfo(credential, person);
            if (faceInfo == null || faceInfo.getFaceData() == null || faceInfo.getFaceData().length == 0) {
                log.warn("[dispatchCredential] 人脸数据为空或加载失败: personId={}, credentialId={}", 
                        person.getId(), credential.getId());
                return false;
            }
            log.info("[dispatchCredential] 开始下发人脸: personId={}, deviceId={}, faceDataLen={}", 
                    person.getId(), deviceId, faceInfo.getFaceDataLen());
            return sendDispatchFaceCommand(deviceId, ip, port, faceInfo);
        }
        
        if (CredentialTypeConstants.isCard(type)) {
            if (capability != null && capability.getSupCardService() != 1) {
                // 一代设备不支持独立卡片服务，卡片信息已包含在用户信息中
                return true;
            }
            NetAccessCardInfo cardInfo = dataConverter.convertToCardInfo(credential, person, null, new int[]{0});
            return sendDispatchCardCommand(deviceId, ip, port, cardInfo);
        }
        
        if (CredentialTypeConstants.isFingerprint(type)) {
            if (capability != null && capability.getSupFingerprintService() != 1) {
                log.warn("[dispatchCredential] 设备不支持指纹: personId={}", person.getId());
                return false;
            }
            NetAccessFingerprintInfo fpInfo = dataConverter.convertToFingerprintInfo(credential, person);
            return sendDispatchFingerprintCommand(deviceId, ip, port, fpInfo);
        }
        
        if (CredentialTypeConstants.isPassword(type)) {
            // 密码已包含在用户信息中，无需单独下发
            return true;
        }
        
        log.warn("[dispatchCredential] 未知凭证类型: type={}, 期望格式: FACE, CARD, PASSWORD, FINGERPRINT (大小写不敏感)", type);
        return false;
    }
    
    // ========== 消息总线命令发送辅助方法 ==========
    
    /**
     * 发送下发用户命令
     * 
     * <p>通过消息总线客户端发送命令到 DEVICE_SERVICE_INVOKE 主题</p>
     * <p>Requirements: 9.1, 9.3 - 门禁设备命令使用统一接口</p>
     * 
     * @param personId 人员ID（用于响应关联）
     * @param deviceId 设备ID
     * @param channelId 通道ID（可选）
     * @param ip 设备IP
     * @param port 设备端口
     * @param userInfo 用户信息
     * @return 响应结果
     */
    private AccessControlDeviceResponse sendDispatchUserCommand(Long personId, Long deviceId, Long channelId,
            String ip, Integer port, NetAccessUserInfo userInfo) {
        try {
            // 获取设备信息以确定设备类型和登录凭据
            IotDeviceDO device = deviceService.getAccessDevice(deviceId);
            String deviceType = device != null ? getAccessDeviceType(device) : AccessDeviceTypeConstants.ACCESS_GEN1;
            String username = device != null ? getDeviceUsername(device) : "admin";
            String password = device != null ? getDevicePassword(device) : "";
            
            // 构建命令参数，确保包含 deviceType 和登录凭据以支持按需连接
            Map<String, Object> params = new HashMap<>();
            params.put("deviceType", deviceType);  // 关键：传递设备类型
            params.put("personId", personId);
            params.put("channelId", channelId);
            params.put("ipAddress", ip);
            params.put("port", port);
            params.put("username", username);      // 关键：传递用户名以支持按需连接
            params.put("password", password);      // 关键：传递密码以支持按需连接
            params.put("userInfo", userInfo);
            
            // 使用消息总线客户端发送命令（不再使用 deviceCommandPublisher 避免双重发送）
            AccessControlDeviceCommand command = AccessControlDeviceCommand.builder()
                    .deviceId(deviceId)
                    .personId(personId)
                    .channelId(channelId)
                    .ipAddress(ip)
                    .port(port)
                    .username(username)            // 关键：设置用户名
                    .password(password)            // 关键：设置密码
                    .commandType(AccessControlDeviceCommand.CommandType.DISPATCH_USER)
                    .userInfo(userInfo)
                    .params(params)  // 包含 deviceType 和登录凭据
                    .build();
            
            log.info("[sendDispatchUserCommand] 发送命令: deviceType={}, deviceId={}, personId={}", 
                    deviceType, deviceId, personId);
            
            return messageBusClient.sendAndWait(command, 30);
        } catch (TimeoutException e) {
            log.error("[sendDispatchUserCommand] 发送命令超时: deviceId={}, personId={}", deviceId, personId);
            return null;
        }
    }
    
    /**
     * 发送下发人脸命令
     * 
     * <p>通过消息总线客户端发送命令到 DEVICE_SERVICE_INVOKE 主题</p>
     * <p>Requirements: 9.1, 9.3 - 门禁设备命令使用统一接口</p>
     */
    private boolean sendDispatchFaceCommand(Long deviceId, String ip, Integer port, NetAccessFaceInfo faceInfo) {
        try {
            // 获取设备信息以确定设备类型和登录凭据
            IotDeviceDO device = deviceService.getAccessDevice(deviceId);
            String deviceType = device != null ? getAccessDeviceType(device) : AccessDeviceTypeConstants.ACCESS_GEN2;
            String username = device != null ? getDeviceUsername(device) : "admin";
            String password = device != null ? getDevicePassword(device) : "";
            
            // 构建命令参数，确保包含 deviceType 和登录凭据以支持按需连接
            Map<String, Object> params = new HashMap<>();
            params.put("deviceType", deviceType);  // 关键：传递设备类型
            params.put("ipAddress", ip);
            params.put("port", port);
            params.put("username", username);      // 关键：传递用户名以支持按需连接
            params.put("password", password);      // 关键：传递密码以支持按需连接
            params.put("faceInfo", faceInfo);
            
            // 使用消息总线客户端发送命令
            AccessControlDeviceCommand command = AccessControlDeviceCommand.builder()
                    .deviceId(deviceId)
                    .ipAddress(ip)
                    .port(port)
                    .username(username)            // 关键：设置用户名
                    .password(password)            // 关键：设置密码
                    .commandType(AccessControlDeviceCommand.CommandType.DISPATCH_FACE)
                    .faceInfo(faceInfo)
                    .params(params)  // 包含 deviceType 和登录凭据
                    .build();
            
            log.info("[sendDispatchFaceCommand] 发送命令: deviceType={}, deviceId={}", deviceType, deviceId);
            
            AccessControlDeviceResponse response = messageBusClient.sendAndWait(command, 30);
            return response != null && Boolean.TRUE.equals(response.getSuccess());
        } catch (TimeoutException e) {
            log.error("[sendDispatchFaceCommand] 发送命令超时: deviceId={}", deviceId);
            return false;
        }
    }
    
    /**
     * 发送下发卡片命令
     * 
     * <p>通过消息总线客户端发送命令到 DEVICE_SERVICE_INVOKE 主题</p>
     * <p>Requirements: 9.1, 9.3 - 门禁设备命令使用统一接口</p>
     */
    private boolean sendDispatchCardCommand(Long deviceId, String ip, Integer port, NetAccessCardInfo cardInfo) {
        try {
            // 获取设备信息以确定设备类型和登录凭据
            IotDeviceDO device = deviceService.getAccessDevice(deviceId);
            String deviceType = device != null ? getAccessDeviceType(device) : AccessDeviceTypeConstants.ACCESS_GEN1;
            String username = device != null ? getDeviceUsername(device) : "admin";
            String password = device != null ? getDevicePassword(device) : "";
            
            // 构建命令参数，确保包含 deviceType 和登录凭据以支持按需连接
            Map<String, Object> params = new HashMap<>();
            params.put("deviceType", deviceType);  // 关键：传递设备类型
            params.put("ipAddress", ip);
            params.put("port", port);
            params.put("username", username);      // 关键：传递用户名以支持按需连接
            params.put("password", password);      // 关键：传递密码以支持按需连接
            params.put("cardInfo", cardInfo);
            
            // 使用消息总线客户端发送命令
            AccessControlDeviceCommand command = AccessControlDeviceCommand.builder()
                    .deviceId(deviceId)
                    .ipAddress(ip)
                    .port(port)
                    .username(username)            // 关键：设置用户名
                    .password(password)            // 关键：设置密码
                    .commandType(AccessControlDeviceCommand.CommandType.DISPATCH_CARD)
                    .cardInfo(cardInfo)
                    .params(params)  // 包含 deviceType 和登录凭据
                    .build();
            
            log.info("[sendDispatchCardCommand] 发送命令: deviceType={}, deviceId={}", deviceType, deviceId);
            
            AccessControlDeviceResponse response = messageBusClient.sendAndWait(command, 30);
            return response != null && Boolean.TRUE.equals(response.getSuccess());
        } catch (TimeoutException e) {
            log.error("[sendDispatchCardCommand] 发送命令超时: deviceId={}", deviceId);
            return false;
        }
    }
    
    /**
     * 发送下发指纹命令
     * 
     * <p>通过消息总线客户端发送命令到 DEVICE_SERVICE_INVOKE 主题</p>
     * <p>Requirements: 9.1, 9.3 - 门禁设备命令使用统一接口</p>
     */
    private boolean sendDispatchFingerprintCommand(Long deviceId, String ip, Integer port, 
            NetAccessFingerprintInfo fpInfo) {
        try {
            // 获取设备信息以确定设备类型
            IotDeviceDO device = deviceService.getAccessDevice(deviceId);
            String deviceType = device != null ? getAccessDeviceType(device) : AccessDeviceTypeConstants.ACCESS_GEN2;
            
            // 构建命令参数，确保包含 deviceType
            Map<String, Object> params = new HashMap<>();
            params.put("deviceType", deviceType);  // 关键：传递设备类型
            params.put("ipAddress", ip);
            params.put("port", port);
            params.put("fingerprintInfo", fpInfo);
            
            // 使用消息总线客户端发送命令
            AccessControlDeviceCommand command = AccessControlDeviceCommand.builder()
                    .deviceId(deviceId)
                    .ipAddress(ip)
                    .port(port)
                    .commandType(AccessControlDeviceCommand.CommandType.DISPATCH_FINGERPRINT)
                    .fingerprintInfo(fpInfo)
                    .params(params)  // 包含 deviceType
                    .build();
            
            log.info("[sendDispatchFingerprintCommand] 发送命令: deviceType={}, deviceId={}", deviceType, deviceId);
            
            AccessControlDeviceResponse response = messageBusClient.sendAndWait(command, 30);
            return response != null && Boolean.TRUE.equals(response.getSuccess());
        } catch (TimeoutException e) {
            log.error("[sendDispatchFingerprintCommand] 发送命令超时: deviceId={}", deviceId);
            return false;
        }
    }
    
    /**
     * 发送撤销用户命令
     * 
     * <p>通过消息总线客户端发送命令到 DEVICE_SERVICE_INVOKE 主题</p>
     * <p>Requirements: 9.1, 9.3 - 门禁设备命令使用统一接口</p>
     */
    private AccessControlDeviceResponse sendRevokeUserCommand(Long deviceId, String ip, Integer port, String userId) {
        try {
            // 获取设备信息以确定设备类型和登录凭据
            IotDeviceDO device = deviceService.getAccessDevice(deviceId);
            String deviceType = device != null ? getAccessDeviceType(device) : AccessDeviceTypeConstants.ACCESS_GEN1;
            String username = device != null ? getDeviceUsername(device) : "admin";
            String password = device != null ? getDevicePassword(device) : "";
            
            // 构建命令参数，确保包含 deviceType 和登录凭据以支持按需连接
            // 注意：一代门禁撤销时，会用 userId 作为 cardNo（与下发逻辑保持一致）
            Map<String, Object> params = new HashMap<>();
            params.put("deviceType", deviceType);  // 关键：传递设备类型
            params.put("ipAddress", ip);
            params.put("port", port);
            params.put("username", username);      // 关键：传递用户名以支持按需连接
            params.put("password", password);      // 关键：传递密码以支持按需连接
            params.put("userId", userId);
            params.put("cardNo", userId);          // 关键：一代门禁下发时用userId作为cardNo，撤销时也要传递
            
            // 使用消息总线客户端发送命令
            AccessControlDeviceCommand command = AccessControlDeviceCommand.builder()
                    .deviceId(deviceId)
                    .ipAddress(ip)
                    .port(port)
                    .username(username)            // 关键：设置用户名
                    .password(password)            // 关键：设置密码
                    .commandType(AccessControlDeviceCommand.CommandType.REVOKE_USER)
                    .userId(userId)
                    .cardNo(userId)                // 关键：用userId作为cardNo
                    .params(params)  // 包含 deviceType 和登录凭据
                    .build();
            
            log.info("[sendRevokeUserCommand] 发送命令: deviceType={}, deviceId={}, userId={}", 
                    deviceType, deviceId, userId);
            
            return messageBusClient.sendAndWait(command, 30);
        } catch (TimeoutException e) {
            log.error("[sendRevokeUserCommand] 发送命令超时: deviceId={}", deviceId);
            return null;
        }
    }
    
    /**
     * 通过消息总线撤销凭证
     * 
     * @param deviceId 设备ID
     * @param ip 设备IP
     * @param port 设备端口
     * @param userId 用户ID
     * @param commandType 命令类型
     * @param credentialType 凭证类型
     * @param successCredentials 成功凭证列表
     * @param failedCredentials 失败凭证列表
     */
    private void revokeCredentialViaMessageBus(Long deviceId, String ip, Integer port, String userId,
            String commandType, String credentialType, 
            List<String> successCredentials, List<DispatchResult.CredentialError> failedCredentials) {
        try {
            // 获取设备信息以确定设备类型和登录凭据
            IotDeviceDO device = deviceService.getAccessDevice(deviceId);
            String deviceType = device != null ? getAccessDeviceType(device) : AccessDeviceTypeConstants.ACCESS_GEN1;
            String username = device != null ? getDeviceUsername(device) : "admin";
            String password = device != null ? getDevicePassword(device) : "";
            
            // 构建命令参数，确保包含 deviceType 和登录凭据以支持按需连接
            Map<String, Object> params = new HashMap<>();
            params.put("deviceType", deviceType);  // 关键：传递设备类型
            params.put("ipAddress", ip);
            params.put("port", port);
            params.put("username", username);      // 关键：传递用户名以支持按需连接
            params.put("password", password);      // 关键：传递密码以支持按需连接
            params.put("userId", userId);
            
            AccessControlDeviceCommand command = AccessControlDeviceCommand.builder()
                    .deviceId(deviceId)
                    .ipAddress(ip)
                    .port(port)
                    .username(username)            // 关键：设置用户名
                    .password(password)            // 关键：设置密码
                    .commandType(commandType)
                    .userId(userId)
                    .params(params)  // 包含 deviceType 和登录凭据
                    .build();
            
            log.debug("[revokeCredentialViaMessageBus] 发送撤销命令: deviceType={}, deviceId={}, commandType={}", 
                    deviceType, deviceId, commandType);
            
            AccessControlDeviceResponse response = messageBusClient.sendAndWait(command, 30);
            
            if (response != null && Boolean.TRUE.equals(response.getSuccess())) {
                successCredentials.add(credentialType);
            } else {
                // 安全获取 errorCode，避免 NPE
                int errorCode = (response != null && response.getErrorCode() != null) 
                        ? response.getErrorCode() : -1;
                if (errorCode == 4) { // 用户不存在也视为成功
                    successCredentials.add(credentialType);
                } else {
                    String errorMsg = response != null ? response.getErrorMessage() : "发送命令超时";
                    failedCredentials.add(DispatchResult.CredentialError.builder()
                            .credentialType(credentialType)
                            .errorCode(errorCode)
                            .errorMessage(errorMsg)
                            .build());
                }
            }
        } catch (TimeoutException e) {
            log.warn("[revokeCredentialViaMessageBus] 撤销{}超时: deviceId={}", credentialType, deviceId);
            failedCredentials.add(DispatchResult.CredentialError.builder()
                    .credentialType(credentialType)
                    .errorCode(-1)
                    .errorMessage("发送命令超时")
                    .build());
        }
    }
    
    /**
     * 获取设备IP地址
     */
    private String getDeviceIp(IotDeviceDO device) {
        // 从config中获取IP地址
        if (device.getConfig() != null) {
            String ipAddress = device.getConfig().getIpAddress();
            if (ipAddress != null && !ipAddress.isEmpty()) {
                return ipAddress;
            }
        }
        return null;
    }
    
    /**
     * 获取设备端口
     */
    private Integer getDevicePort(IotDeviceDO device) {
        if (device.getConfig() instanceof AccessDeviceConfig) {
            AccessDeviceConfig config = (AccessDeviceConfig) device.getConfig();
            return config.getPort() != null ? config.getPort() : 37777;
        }
        return 37777; // 大华设备默认端口
    }

    /**
     * 获取设备用户名
     * Requirements: 4.1 - 用于设备自动激活
     */
    private String getDeviceUsername(IotDeviceDO device) {
        if (device.getConfig() instanceof AccessDeviceConfig) {
            AccessDeviceConfig config = (AccessDeviceConfig) device.getConfig();
            return config.getUsername();
        } else if (device.getConfig() instanceof GenericDeviceConfig) {
            // 兼容 GenericDeviceConfig（deviceType=GENERIC 的情况）
            GenericDeviceConfig config = (GenericDeviceConfig) device.getConfig();
            Object usernameObj = config.get("username");
            if (usernameObj instanceof String) {
                return (String) usernameObj;
            }
        }
        return null;
    }

    /**
     * 获取设备密码
     * Requirements: 4.1 - 用于设备自动激活
     */
    private String getDevicePassword(IotDeviceDO device) {
        if (device.getConfig() instanceof AccessDeviceConfig) {
            AccessDeviceConfig config = (AccessDeviceConfig) device.getConfig();
            return config.getPassword();
        } else if (device.getConfig() instanceof GenericDeviceConfig) {
            // 兼容 GenericDeviceConfig（deviceType=GENERIC 的情况）
            GenericDeviceConfig config = (GenericDeviceConfig) device.getConfig();
            Object passwordObj = config.get("password");
            if (passwordObj instanceof String) {
                return (String) passwordObj;
            }
        }
        return null;
    }

    /**
     * 获取设备类型（ACCESS_GEN1 或 ACCESS_GEN2）
     * Requirements: 9.2 - 根据 supportVideo 判断设备类型
     * 
     * @param device 设备对象
     * @return 设备类型字符串
     */
    private String getAccessDeviceType(IotDeviceDO device) {
        Boolean supportVideo = null;
        String configDeviceType = null;

        if (device.getConfig() instanceof AccessDeviceConfig) {
            AccessDeviceConfig config = (AccessDeviceConfig) device.getConfig();
            supportVideo = config.getSupportVideo();
        } else if (device.getConfig() instanceof GenericDeviceConfig) {
            GenericDeviceConfig cfg = (GenericDeviceConfig) device.getConfig();
            Object dt = cfg.get("deviceType");
            Object sv = cfg.get("supportVideo");
            configDeviceType = dt != null ? dt.toString() : null;
            supportVideo = (sv instanceof Boolean) ? (Boolean) sv : null;
        }

        return AccessDeviceTypeConstants.resolveDeviceType(configDeviceType, supportVideo);
    }

    /**
     * 从 iot_device.config 中读取门禁能力快照
     * <p>优先读取 config.accessCapabilities（AccessDeviceConfig）或 config["accessCapabilities"]（GenericDeviceConfig）</p>
     */
    @SuppressWarnings("unchecked")
    private Boolean getAccessCapabilityFlag(IotDeviceDO device, String key) {
        if (device == null || device.getConfig() == null || key == null) {
            return null;
        }
        Object capsObj = null;
        if (device.getConfig() instanceof AccessDeviceConfig) {
            capsObj = ((AccessDeviceConfig) device.getConfig()).getAccessCapabilities();
        } else if (device.getConfig() instanceof GenericDeviceConfig) {
            capsObj = ((GenericDeviceConfig) device.getConfig()).get("accessCapabilities");
        }
        if (!(capsObj instanceof Map)) {
            return null;
        }
        Object v = ((Map<String, Object>) capsObj).get(key);
        if (v instanceof Boolean) {
            return (Boolean) v;
        }
        if (v != null) {
            return Boolean.parseBoolean(String.valueOf(v));
        }
        return null;
    }

    /**
     * 更新人员设备授权状态
     */
    private void updatePersonDeviceAuth(Long personId, Long deviceId, Long channelId,
            Integer authStatus, String result) {
        IotAccessPersonDeviceAuthDO auth = personDeviceAuthMapper.selectByPersonIdAndDeviceId(
                personId, deviceId, channelId);
        
        if (auth == null) {
            auth = IotAccessPersonDeviceAuthDO.builder()
                    .personId(personId)
                    .deviceId(deviceId)
                    .channelId(channelId)
                    .authStatus(authStatus)
                    .lastDispatchTime(LocalDateTime.now())
                    .lastDispatchResult(result)
                    .build();
            personDeviceAuthMapper.insert(auth);
        } else {
            auth.setAuthStatus(authStatus);
            auth.setLastDispatchTime(LocalDateTime.now());
            auth.setLastDispatchResult(result);
            personDeviceAuthMapper.updateById(auth);
        }
    }

    /**
     * 等待任务完成
     */
    private void waitForTaskCompletion(Long taskId, int totalCount) {
        int maxWaitSeconds = 300; // 最多等待5分钟
        int waitedSeconds = 0;
        
        while (waitedSeconds < maxWaitSeconds) {
            try {
                Thread.sleep(1000);
                waitedSeconds++;
                
                // 检查已处理的明细数量
                long successCount = authTaskService.countTaskDetailsByStatus(taskId, 
                        IotAccessAuthTaskDetailDO.STATUS_SUCCESS);
                long failCount = authTaskService.countTaskDetailsByStatus(taskId,
                        IotAccessAuthTaskDetailDO.STATUS_FAILED);
                
                if (successCount + failCount >= totalCount) {
                    // 所有明细已处理完成
                    authTaskService.completeTask(taskId, null);
                    
                    // 推送任务完成通知 (Requirements: 6.2)
                    IotAccessAuthTaskDO task = authTaskService.getTask(taskId);
                    progressPushService.pushCompleted(task);
                    return;
                }
                
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        
        // 超时，标记任务完成
        authTaskService.completeTask(taskId, "任务执行超时");
        
        // 推送任务完成通知（超时）(Requirements: 6.2)
        IotAccessAuthTaskDO task = authTaskService.getTask(taskId);
        progressPushService.pushCompleted(task);
    }

    /**
     * 设备通道键（用于去重）
     */
    private static class DeviceChannelKey {
        final Long deviceId;
        final Long channelId;
        
        DeviceChannelKey(Long deviceId, Long channelId) {
            this.deviceId = deviceId;
            this.channelId = channelId;
        }
        
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            DeviceChannelKey that = (DeviceChannelKey) o;
            return Objects.equals(deviceId, that.deviceId) && Objects.equals(channelId, that.channelId);
        }
        
        @Override
        public int hashCode() {
            return Objects.hash(deviceId, channelId);
        }
    }

    // ========== 增量授权支持实现 (Requirements: 15.1, 15.2, 15.3, 15.4) ==========

    @Resource
    private CredentialHashCalculator credentialHashCalculator;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long dispatchPermissionGroupIncremental(Long groupId) {
        // 1. 校验权限组存在
        IotAccessPermissionGroupDO group = permissionGroupService.getPermissionGroup(groupId);
        if (group == null) {
            throw exception(ACCESS_PERMISSION_GROUP_NOT_EXISTS);
        }
        
        // 2. 获取权限组关联的设备和人员
        List<IotAccessPermissionGroupDeviceDO> groupDevices = permissionGroupService.getGroupDevices(groupId);
        List<IotAccessPermissionGroupPersonDO> groupPersons = permissionGroupService.getGroupPersons(groupId);
        
        if (groupDevices.isEmpty() || groupPersons.isEmpty()) {
            log.warn("[dispatchPermissionGroupIncremental] 权限组没有关联设备或人员: groupId={}", groupId);
            throw exception(ACCESS_PERMISSION_GROUP_NOT_EXISTS);
        }
        
        // 3. 筛选需要下发的人员（新增或凭证变更）
        List<Long> personsToDispatch = new ArrayList<>();
        for (IotAccessPermissionGroupPersonDO gp : groupPersons) {
            Long personId = gp.getPersonId();
            boolean needDispatch = false;
            
            for (IotAccessPermissionGroupDeviceDO gd : groupDevices) {
                if (hasCredentialChanged(personId, gd.getDeviceId(), gd.getChannelId())) {
                    needDispatch = true;
                    break;
                }
            }
            
            if (needDispatch) {
                personsToDispatch.add(personId);
            }
        }
        
        if (personsToDispatch.isEmpty()) {
            log.info("[dispatchPermissionGroupIncremental] 无需增量下发: groupId={}", groupId);
            return null;
        }
        
        // 4. 创建授权任务
        Long taskId = authTaskService.createTask(IotAccessAuthTaskDO.TASK_TYPE_GROUP_DISPATCH, groupId, null);
        
        // 5. 创建任务明细（只包含需要下发的人员）
        List<IotAccessAuthTaskDetailDO> details = createTaskDetails(personsToDispatch, groupDevices);
        authTaskService.addTaskDetails(taskId, details);
        
        // 6. 异步执行任务
        executeTaskAsync(taskId);
        
        log.info("[dispatchPermissionGroupIncremental] 创建增量下发任务: taskId={}, groupId={}, personCount={}/{}",
                taskId, groupId, personsToDispatch.size(), groupPersons.size());
        
        return taskId;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long dispatchPersonIncremental(Long personId) {
        // 1. 校验人员存在
        IotAccessPersonDO person = personService.getPerson(personId);
        if (person == null) {
            throw exception(ACCESS_PERSON_NOT_EXISTS);
        }
        
        // 2. 获取人员关联的所有权限组
        List<IotAccessPermissionGroupPersonDO> personGroups = permissionGroupService.getPersonGroups(personId);
        if (personGroups.isEmpty()) {
            log.warn("[dispatchPersonIncremental] 人员没有关联权限组: personId={}", personId);
            return null;
        }
        
        // 3. 汇总所有需要下发的设备（去重）并检测变更
        Set<DeviceChannelKey> devicesToDispatch = new HashSet<>();
        for (IotAccessPermissionGroupPersonDO pg : personGroups) {
            List<IotAccessPermissionGroupDeviceDO> groupDevices = 
                    permissionGroupService.getGroupDevices(pg.getGroupId());
            for (IotAccessPermissionGroupDeviceDO gd : groupDevices) {
                // 只添加有变更的设备
                if (hasCredentialChanged(personId, gd.getDeviceId(), gd.getChannelId())) {
                    devicesToDispatch.add(new DeviceChannelKey(gd.getDeviceId(), gd.getChannelId()));
                }
            }
        }
        
        if (devicesToDispatch.isEmpty()) {
            log.info("[dispatchPersonIncremental] 无需增量下发: personId={}", personId);
            return null;
        }
        
        // 4. 创建授权任务
        Long taskId = authTaskService.createTask(IotAccessAuthTaskDO.TASK_TYPE_PERSON_DISPATCH, null, personId);
        
        // 5. 创建任务明细
        List<IotAccessAuthTaskDetailDO> details = new ArrayList<>();
        for (DeviceChannelKey key : devicesToDispatch) {
            IotDeviceDO device = deviceService.getAccessDevice(key.deviceId);
            details.add(IotAccessAuthTaskDetailDO.builder()
                    .personId(personId)
                    .personCode(person.getPersonCode())
                    .personName(person.getPersonName())
                    .deviceId(key.deviceId)
                    .deviceName(device != null ? device.getDeviceName() : null)
                    .channelId(key.channelId)
                    .build());
        }
        authTaskService.addTaskDetails(taskId, details);
        
        // 6. 异步执行任务
        executeTaskAsync(taskId);
        
        log.info("[dispatchPersonIncremental] 创建增量下发任务: taskId={}, personId={}, deviceCount={}",
                taskId, personId, devicesToDispatch.size());
        
        return taskId;
    }

    @Override
    public boolean hasCredentialChanged(Long personId, Long deviceId, Long channelId) {
        // 1. 获取人员当前凭证
        List<IotAccessPersonCredentialDO> credentials = personService.getPersonCredentials(personId);
        
        // 2. 获取上次下发的哈希值
        IotAccessPersonDeviceAuthDO auth = personDeviceAuthMapper.selectByPersonIdAndDeviceId(
                personId, deviceId, channelId);
        
        if (auth == null) {
            // 没有授权记录，视为需要下发
            return credentials != null && !credentials.isEmpty();
        }
        
        // 3. 比较哈希值
        return credentialHashCalculator.hasChanged(credentials, auth.getCredentialHash());
    }

    @Override
    public String updateCredentialHash(Long personId, Long deviceId, Long channelId) {
        // 1. 获取人员当前凭证
        List<IotAccessPersonCredentialDO> credentials = personService.getPersonCredentials(personId);
        
        // 2. 计算哈希值
        String hash = credentialHashCalculator.calculateHash(credentials);
        
        // 3. 更新授权记录中的哈希值
        IotAccessPersonDeviceAuthDO auth = personDeviceAuthMapper.selectByPersonIdAndDeviceId(
                personId, deviceId, channelId);
        
        if (auth != null) {
            auth.setCredentialHash(hash);
            personDeviceAuthMapper.updateById(auth);
        }
        
        return hash;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long dispatchGroupToNewDevice(Long groupId, Long deviceId, Long channelId) {
        // 1. 校验权限组存在
        IotAccessPermissionGroupDO group = permissionGroupService.getPermissionGroup(groupId);
        if (group == null) {
            throw exception(ACCESS_PERMISSION_GROUP_NOT_EXISTS);
        }
        
        // 2. 获取权限组关联的人员
        List<IotAccessPermissionGroupPersonDO> groupPersons = permissionGroupService.getGroupPersons(groupId);
        if (groupPersons.isEmpty()) {
            log.warn("[dispatchGroupToNewDevice] 权限组没有关联人员: groupId={}", groupId);
            return null;
        }
        
        // 3. 获取设备信息
        IotDeviceDO device = deviceService.getAccessDevice(deviceId);
        if (device == null) {
            throw exception(ACCESS_DEVICE_NOT_EXISTS);
        }
        
        // 4. 创建授权任务
        Long taskId = authTaskService.createTask(IotAccessAuthTaskDO.TASK_TYPE_GROUP_DISPATCH, groupId, null);
        
        // 5. 创建任务明细（所有人员到新设备）
        List<IotAccessAuthTaskDetailDO> details = new ArrayList<>();
        for (IotAccessPermissionGroupPersonDO gp : groupPersons) {
            IotAccessPersonDO person = personService.getPerson(gp.getPersonId());
            if (person == null) {
                continue;
            }
            details.add(IotAccessAuthTaskDetailDO.builder()
                    .personId(gp.getPersonId())
                    .personCode(person.getPersonCode())
                    .personName(person.getPersonName())
                    .deviceId(deviceId)
                    .deviceName(device.getDeviceName())
                    .channelId(channelId)
                    .build());
        }
        
        if (details.isEmpty()) {
            log.warn("[dispatchGroupToNewDevice] 没有有效的人员: groupId={}", groupId);
            return null;
        }
        
        authTaskService.addTaskDetails(taskId, details);
        
        // 6. 异步执行任务
        executeTaskAsync(taskId);
        
        log.info("[dispatchGroupToNewDevice] 创建新设备下发任务: taskId={}, groupId={}, deviceId={}, personCount={}",
                taskId, groupId, deviceId, details.size());
        
        return taskId;
    }

    /**
     * 更新人员设备授权状态（带凭证哈希）
     */
    private void updatePersonDeviceAuthWithHash(Long personId, Long deviceId, Long channelId,
            Integer authStatus, String result, String credentialHash) {
        IotAccessPersonDeviceAuthDO auth = personDeviceAuthMapper.selectByPersonIdAndDeviceId(
                personId, deviceId, channelId);
        
        if (auth == null) {
            auth = IotAccessPersonDeviceAuthDO.builder()
                    .personId(personId)
                    .deviceId(deviceId)
                    .channelId(channelId)
                    .authStatus(authStatus)
                    .lastDispatchTime(LocalDateTime.now())
                    .lastDispatchResult(result)
                    .credentialHash(credentialHash)
                    .build();
            personDeviceAuthMapper.insert(auth);
        } else {
            auth.setAuthStatus(authStatus);
            auth.setLastDispatchTime(LocalDateTime.now());
            auth.setLastDispatchResult(result);
            if (credentialHash != null) {
                auth.setCredentialHash(credentialHash);
            }
            personDeviceAuthMapper.updateById(auth);
        }
    }

    // ========== 权限组人员变更触发下发实现 (Requirements: 1.1, 1.2, 2.1, 2.2, 2.4) ==========

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long dispatchPermissionGroupPersons(Long groupId, List<Long> personIds) {
        if (personIds == null || personIds.isEmpty()) {
            log.warn("[dispatchPermissionGroupPersons] 人员列表为空: groupId={}", groupId);
            throw exception(ACCESS_PERSON_NOT_EXISTS);
        }
        
        // 1. 校验权限组存在
        IotAccessPermissionGroupDO group = permissionGroupService.getPermissionGroup(groupId);
        if (group == null) {
            throw exception(ACCESS_PERMISSION_GROUP_NOT_EXISTS);
        }
        
        // 2. 获取权限组关联的设备列表
        List<IotAccessPermissionGroupDeviceDO> groupDevices = permissionGroupService.getGroupDevices(groupId);
        if (groupDevices.isEmpty()) {
            log.warn("[dispatchPermissionGroupPersons] 权限组没有关联设备: groupId={}", groupId);
            throw exception(ACCESS_PERMISSION_GROUP_NOT_EXISTS);
        }
        
        // 3. 创建授权任务
        Long taskId = authTaskService.createTask(IotAccessAuthTaskDO.TASK_TYPE_GROUP_DISPATCH, groupId, null);
        
        // 4. 创建任务明细（指定人员 × 权限组设备）
        List<IotAccessAuthTaskDetailDO> details = createTaskDetails(personIds, groupDevices);
        if (details.isEmpty()) {
            log.warn("[dispatchPermissionGroupPersons] 没有有效的人员: groupId={}, personIds={}", groupId, personIds);
            return taskId;
        }
        authTaskService.addTaskDetails(taskId, details);
        
        // 5. 注册事务提交后回调，确保任务在事务提交后执行
        // Requirements: 6.1, 6.2 - 数据库操作和下发操作解耦
        final Long finalTaskId = taskId;
        final Long finalGroupId = groupId;
        final int personCount = personIds.size();
        final int deviceCount = groupDevices.size();
        
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    log.info("[dispatchPermissionGroupPersons] 事务已提交，提交异步任务: taskId={}", finalTaskId);
                    // 使用执行器真正异步提交任务
                    accessDispatchExecutor.execute(() -> {
                        try {
                            executeTaskAsyncInternal(finalTaskId);
                        } catch (Exception e) {
                            log.error("[dispatchPermissionGroupPersons] 异步任务执行异常: taskId={}", finalTaskId, e);
                        }
                    });
                }
            });
            log.info("[dispatchPermissionGroupPersons] 创建权限组人员下发任务(事务同步): taskId={}, groupId={}, personCount={}, deviceCount={}",
                    taskId, finalGroupId, personCount, deviceCount);
        } else {
            List<Long> detailIds = details.stream()
                    .map(IotAccessAuthTaskDetailDO::getId)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
            eventPublisher.publishEvent(DispatchTaskCreatedEvent.forPermissionGroup(this, taskId, groupId, detailIds));
            log.info("[dispatchPermissionGroupPersons] 创建权限组人员下发任务(事件模式): taskId={}, groupId={}, personCount={}, deviceCount={}",
                    taskId, finalGroupId, personCount, deviceCount);
        }
        
        return taskId;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long revokePermissionGroupPersons(Long groupId, List<Long> personIds) {
        if (personIds == null || personIds.isEmpty()) {
            log.warn("[revokePermissionGroupPersons] 人员列表为空: groupId={}", groupId);
            throw exception(ACCESS_PERSON_NOT_EXISTS);
        }
        
        // 1. 校验权限组存在
        IotAccessPermissionGroupDO group = permissionGroupService.getPermissionGroup(groupId);
        if (group == null) {
            throw exception(ACCESS_PERMISSION_GROUP_NOT_EXISTS);
        }
        
        // 2. 获取权限组关联的设备列表
        List<IotAccessPermissionGroupDeviceDO> groupDevices = permissionGroupService.getGroupDevices(groupId);
        if (groupDevices.isEmpty()) {
            log.warn("[revokePermissionGroupPersons] 权限组没有关联设备: groupId={}", groupId);
            return authTaskService.createTask(IotAccessAuthTaskDO.TASK_TYPE_REVOKE, groupId, null);
        }
        
        // 3. 创建撤销任务
        Long taskId = authTaskService.createTask(IotAccessAuthTaskDO.TASK_TYPE_REVOKE, groupId, null);
        
        // 4. 为每个人员检查是否需要撤销（考虑其他权限组的重叠）
        List<IotAccessAuthTaskDetailDO> details = new ArrayList<>();
        
        for (Long personId : personIds) {
            IotAccessPersonDO person = personService.getPerson(personId);
            if (person == null) {
                continue;
            }
            
            // 获取人员在其他权限组中关联的设备（排除当前权限组）
            Set<Long> otherGroupDeviceIds = getOtherGroupDeviceIds(personId, groupId);
            
            // 筛选出需要撤销的设备（不在其他权限组中的设备）
            for (IotAccessPermissionGroupDeviceDO gd : groupDevices) {
                // Requirements: 2.4 - 如果人员在其他权限组仍有该设备权限，则保留
                if (otherGroupDeviceIds.contains(gd.getDeviceId())) {
                    log.info("[revokePermissionGroupPersons] 人员在其他权限组仍有设备权限，跳过撤销: personId={}, deviceId={}", 
                            personId, gd.getDeviceId());
                    continue;
                }
                
                IotDeviceDO device = deviceService.getAccessDevice(gd.getDeviceId());
                details.add(IotAccessAuthTaskDetailDO.builder()
                        .personId(personId)
                        .personCode(person.getPersonCode())
                        .personName(person.getPersonName())
                        .deviceId(gd.getDeviceId())
                        .deviceName(device != null ? device.getDeviceName() : null)
                        .channelId(gd.getChannelId())
                        .build());
            }
        }
        
        if (details.isEmpty()) {
            log.info("[revokePermissionGroupPersons] 所有人员在其他权限组仍有授权，无需撤销: groupId={}", groupId);
            return taskId;
        }
        
        authTaskService.addTaskDetails(taskId, details);
        
        // 5. 注册事务提交后回调，确保任务在事务提交后执行
        // Requirements: 6.1, 6.3 - 数据库操作和撤销操作解耦
        final Long finalTaskId = taskId;
        final Long finalGroupId = groupId;
        final int personCount = personIds.size();
        final int detailCount = details.size();
        
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    log.info("[revokePermissionGroupPersons] 事务已提交，提交异步任务: taskId={}", finalTaskId);
                    // 使用执行器真正异步提交撤销任务
                    accessDispatchExecutor.execute(() -> {
                        try {
                            executeRevokeTaskAsyncInternal(finalTaskId);
                        } catch (Exception e) {
                            log.error("[revokePermissionGroupPersons] 异步任务执行异常: taskId={}", finalTaskId, e);
                        }
                    });
                }
            });
            log.info("[revokePermissionGroupPersons] 创建权限组人员撤销任务(事务同步): taskId={}, groupId={}, personCount={}, detailCount={}",
                    taskId, finalGroupId, personCount, detailCount);
        } else {
            List<Long> detailIds = details.stream()
                    .map(IotAccessAuthTaskDetailDO::getId)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
            eventPublisher.publishEvent(DispatchTaskCreatedEvent.forPermissionGroupRevoke(this, taskId, groupId, detailIds));
            log.info("[revokePermissionGroupPersons] 创建权限组人员撤销任务(事件模式): taskId={}, groupId={}, personCount={}, detailCount={}",
                    taskId, finalGroupId, personCount, detailCount);
        }
        
        return taskId;
    }

    /**
     * 获取人员在其他权限组中关联的设备ID集合
     * 用于判断撤销时是否需要保留设备权限
     * 
     * Requirements: 2.4
     * 
     * @param personId 人员ID
     * @param excludeGroupId 排除的权限组ID
     * @return 其他权限组关联的设备ID集合
     */
    private Set<Long> getOtherGroupDeviceIds(Long personId, Long excludeGroupId) {
        Set<Long> deviceIds = new HashSet<>();
        
        // 获取人员所属的所有权限组
        List<IotAccessPermissionGroupPersonDO> personGroups = permissionGroupService.getPersonGroups(personId);
        
        for (IotAccessPermissionGroupPersonDO pg : personGroups) {
            // 排除当前权限组
            if (pg.getGroupId().equals(excludeGroupId)) {
                continue;
            }
            
            // 获取该权限组关联的设备
            List<IotAccessPermissionGroupDeviceDO> groupDevices = 
                    permissionGroupService.getGroupDevices(pg.getGroupId());
            for (IotAccessPermissionGroupDeviceDO gd : groupDevices) {
                deviceIds.add(gd.getDeviceId());
            }
        }
        
        return deviceIds;
    }
}
