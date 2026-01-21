package cn.iocoder.yudao.module.iot.service.access;

import cn.iocoder.yudao.module.iot.core.gateway.dto.AccessControlDeviceCommand;
import cn.iocoder.yudao.module.iot.core.gateway.dto.AccessControlDeviceResponse;
import cn.iocoder.yudao.module.iot.dal.dataobject.access.*;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.config.AccessDeviceConfig;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.config.GenericDeviceConfig;
import cn.iocoder.yudao.module.iot.dal.mysql.access.IotAccessDeviceSyncRecordMapper;
import cn.iocoder.yudao.module.iot.enums.device.AccessDeviceTypeConstants;
import cn.iocoder.yudao.module.iot.service.access.dto.DeviceSyncCheckResult;
import cn.iocoder.yudao.module.iot.service.access.dto.DeviceSyncCheckResult.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

/**
 * 门禁设备人员同步服务实现
 *
 * @author 长辉信息科技
 */
@Slf4j
@Service
public class IotAccessDeviceSyncServiceImpl implements IotAccessDeviceSyncService {

    @Resource
    private IotAccessDeviceService deviceService;

    @Resource
    private IotAccessPermissionGroupService permissionGroupService;

    @Resource
    private IotAccessPersonService personService;

    @Resource
    private AccessControlMessageBusClient messageBusClient;

    @Resource
    private IotAccessAuthDispatchService dispatchService;

    @Resource
    private IotAccessDeviceSyncRecordMapper syncRecordMapper;

    @Override
    public DeviceSyncCheckResult checkDevice(Long deviceId) {
        long startTime = System.currentTimeMillis();
        log.info("[checkDevice] 开始对账: deviceId={}", deviceId);

        // 1. 获取设备信息
        IotDeviceDO device = deviceService.getAccessDevice(deviceId);
        if (device == null) {
            log.warn("[checkDevice] 设备不存在: deviceId={}", deviceId);
            return DeviceSyncCheckResult.failure(deviceId, null, "设备不存在");
        }

        String deviceName = device.getDeviceName();
        String ip = getDeviceIp(device);

        try {
            // 2. 查询设备上的用户列表
            long queryDeviceStart = System.currentTimeMillis();
            List<DeviceUserInfo> deviceUsers = queryDeviceUsers(deviceId);
            log.info("[checkDevice] 查询设备用户完成: 耗时={}ms, 用户数={}", 
                    System.currentTimeMillis() - queryDeviceStart, deviceUsers.size());

            // 3. 获取系统应授权人员列表
            long querySystemStart = System.currentTimeMillis();
            List<PersonInfo> systemUsers = getSystemUsers(deviceId);
            log.info("[checkDevice] 获取系统人员完成: 耗时={}ms, 人员数={}", 
                    System.currentTimeMillis() - querySystemStart, systemUsers.size());

            // 4. 对比差异
            DeviceSyncCheckResult result = compareAndBuildResult(deviceId, deviceName, ip, systemUsers, deviceUsers);
            
            long totalTime = System.currentTimeMillis() - startTime;
            log.info("[checkDevice] ✅ 对账完成: deviceId={}, deviceName={}, 总耗时={}ms, " +
                    "系统人员={}, 设备用户={}, 已同步={}, 待下发={}, 野生用户={}", 
                    deviceId, deviceName, totalTime,
                    result.getStatistics().getSystemTotal(),
                    result.getStatistics().getDeviceTotal(),
                    result.getStatistics().getSyncedCount(),
                    result.getStatistics().getSystemOnlyCount(),
                    result.getStatistics().getDeviceOnlyCount());
            
            return result;

        } catch (Exception e) {
            log.error("[checkDevice] 对账异常: deviceId={}, 耗时={}ms", 
                    deviceId, System.currentTimeMillis() - startTime, e);
            return DeviceSyncCheckResult.failure(deviceId, deviceName, "对账异常: " + e.getMessage());
        }
    }

    @Override
    public List<DeviceSyncCheckResult> checkDevices(List<Long> deviceIds) {
        return deviceIds.stream()
                .map(this::checkDevice)
                .collect(Collectors.toList());
    }

    @Override
    public List<DeviceSyncCheckResult> checkByPermissionGroup(Long groupId) {
        // 获取权限组关联的设备
        List<IotAccessPermissionGroupDeviceDO> groupDevices = permissionGroupService.getGroupDevices(groupId);
        List<Long> deviceIds = groupDevices.stream()
                .map(IotAccessPermissionGroupDeviceDO::getDeviceId)
                .distinct()
                .collect(Collectors.toList());

        return checkDevices(deviceIds);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DeviceSyncCheckResult cleanDeviceExtraUsers(Long deviceId) {
        log.info("[cleanDeviceExtraUsers] 开始清理设备多余用户: deviceId={}", deviceId);

        // 1. 先执行对账
        DeviceSyncCheckResult checkResult = checkDevice(deviceId);
        if (!checkResult.isSuccess()) {
            return checkResult;
        }

        // 2. 获取需要清理的用户
        List<DeviceUserInfo> extraUsers = checkResult.getDeviceOnlyUsers();
        if (extraUsers == null || extraUsers.isEmpty()) {
            log.info("[cleanDeviceExtraUsers] 没有需要清理的用户: deviceId={}", deviceId);
            checkResult.setErrorMessage("设备上没有多余用户");
            return checkResult;
        }

        // 3. 逐个删除多余用户
        IotDeviceDO device = deviceService.getAccessDevice(deviceId);
        String ip = getDeviceIp(device);
        Integer port = getDevicePort(device);
        String deviceType = getAccessDeviceType(device);

        int cleanedCount = 0;
        List<String> failedUsers = new ArrayList<>();

        for (DeviceUserInfo user : extraUsers) {
            try {
                boolean success = deleteDeviceUser(deviceId, ip, port, deviceType, user.getUserId());
                if (success) {
                    cleanedCount++;
                    log.info("[cleanDeviceExtraUsers] 删除用户成功: deviceId={}, userId={}", deviceId, user.getUserId());
                } else {
                    failedUsers.add(user.getUserId());
                }
            } catch (Exception e) {
                log.error("[cleanDeviceExtraUsers] 删除用户异常: deviceId={}, userId={}", deviceId, user.getUserId(), e);
                failedUsers.add(user.getUserId());
            }
        }

        // 4. 记录同步结果
        saveSyncRecord(deviceId, device.getDeviceName(), IotAccessDeviceSyncRecordDO.SYNC_TYPE_CLEAN,
                checkResult.getStatistics(), cleanedCount, 0,
                failedUsers.isEmpty() ? null : "部分用户删除失败: " + String.join(",", failedUsers));

        // 5. 返回结果
        checkResult.getStatistics().setDeviceOnlyCount(extraUsers.size() - cleanedCount);
        if (!failedUsers.isEmpty()) {
            checkResult.setErrorMessage("已清理 " + cleanedCount + " 人，失败 " + failedUsers.size() + " 人");
        }

        return checkResult;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DeviceSyncCheckResult repairMissingUsers(Long deviceId) {
        log.info("[repairMissingUsers] 开始补发缺失用户: deviceId={}", deviceId);

        // 1. 先执行对账
        DeviceSyncCheckResult checkResult = checkDevice(deviceId);
        if (!checkResult.isSuccess()) {
            return checkResult;
        }

        // 2. 获取需要补发的用户
        List<PersonInfo> missingUsers = checkResult.getSystemOnlyUsers();
        if (missingUsers == null || missingUsers.isEmpty()) {
            log.info("[repairMissingUsers] 没有需要补发的用户: deviceId={}", deviceId);
            checkResult.setErrorMessage("没有缺失的用户");
            return checkResult;
        }

        // 3. 获取设备关联的通道ID
        IotDeviceDO device = deviceService.getAccessDevice(deviceId);
        Long channelId = getDefaultChannelId(deviceId);

        // 4. 逐个下发缺失用户
        int repairedCount = 0;
        List<String> failedUsers = new ArrayList<>();

        for (PersonInfo person : missingUsers) {
            try {
                var result = dispatchService.dispatchPersonToDevice(person.getPersonId(), deviceId, channelId);
                if (result.isSuccess()) {
                    repairedCount++;
                    log.info("[repairMissingUsers] 下发用户成功: deviceId={}, personId={}", deviceId, person.getPersonId());
                } else {
                    failedUsers.add(person.getPersonCode());
                }
            } catch (Exception e) {
                log.error("[repairMissingUsers] 下发用户异常: deviceId={}, personId={}", deviceId, person.getPersonId(), e);
                failedUsers.add(person.getPersonCode());
            }
        }

        // 5. 记录同步结果
        saveSyncRecord(deviceId, device.getDeviceName(), IotAccessDeviceSyncRecordDO.SYNC_TYPE_REPAIR,
                checkResult.getStatistics(), 0, repairedCount,
                failedUsers.isEmpty() ? null : "部分用户下发失败: " + String.join(",", failedUsers));

        // 6. 返回结果
        checkResult.getStatistics().setSystemOnlyCount(missingUsers.size() - repairedCount);
        if (!failedUsers.isEmpty()) {
            checkResult.setErrorMessage("已补发 " + repairedCount + " 人，失败 " + failedUsers.size() + " 人");
        }

        return checkResult;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DeviceSyncCheckResult fullSync(Long deviceId) {
        log.info("[fullSync] 开始全量同步: deviceId={}", deviceId);

        IotDeviceDO device = deviceService.getAccessDevice(deviceId);
        if (device == null) {
            return DeviceSyncCheckResult.failure(deviceId, null, "设备不存在");
        }

        String deviceName = device.getDeviceName();
        String ip = getDeviceIp(device);
        Integer port = getDevicePort(device);
        String deviceType = getAccessDeviceType(device);

        try {
            // 1. 清空设备上的所有用户
            log.info("[fullSync] 清空设备用户: deviceId={}", deviceId);
            boolean clearSuccess = clearAllDeviceUsers(deviceId, ip, port, deviceType);
            if (!clearSuccess) {
                return DeviceSyncCheckResult.failure(deviceId, deviceName, "清空设备用户失败");
            }

            // 2. 获取系统应授权人员
            List<PersonInfo> systemUsers = getSystemUsers(deviceId);
            if (systemUsers.isEmpty()) {
                log.info("[fullSync] 没有需要同步的用户: deviceId={}", deviceId);
                return DeviceSyncCheckResult.builder()
                        .deviceId(deviceId)
                        .deviceName(deviceName)
                        .deviceIp(ip)
                        .success(true)
                        .systemUsers(systemUsers)
                        .deviceUsers(Collections.emptyList())
                        .statistics(SyncStatistics.builder()
                                .systemTotal(0)
                                .deviceTotal(0)
                                .syncedCount(0)
                                .build())
                        .build();
            }

            // 3. 逐个下发用户
            Long channelId = getDefaultChannelId(deviceId);
            int successCount = 0;
            List<String> failedUsers = new ArrayList<>();

            for (PersonInfo person : systemUsers) {
                try {
                    var result = dispatchService.dispatchPersonToDevice(person.getPersonId(), deviceId, channelId);
                    if (result.isSuccess()) {
                        successCount++;
                    } else {
                        failedUsers.add(person.getPersonCode());
                    }
                } catch (Exception e) {
                    log.error("[fullSync] 下发用户异常: personId={}", person.getPersonId(), e);
                    failedUsers.add(person.getPersonCode());
                }
            }

            // 4. 记录同步结果
            SyncStatistics stats = SyncStatistics.builder()
                    .systemTotal(systemUsers.size())
                    .deviceTotal(successCount)
                    .syncedCount(successCount)
                    .systemOnlyCount(failedUsers.size())
                    .deviceOnlyCount(0)
                    .syncRate(systemUsers.isEmpty() ? 1.0 : (double) successCount / systemUsers.size())
                    .build();

            saveSyncRecord(deviceId, deviceName, IotAccessDeviceSyncRecordDO.SYNC_TYPE_FULL,
                    stats, 0, successCount,
                    failedUsers.isEmpty() ? null : "部分用户下发失败: " + String.join(",", failedUsers));

            return DeviceSyncCheckResult.builder()
                    .deviceId(deviceId)
                    .deviceName(deviceName)
                    .deviceIp(ip)
                    .success(failedUsers.isEmpty())
                    .errorMessage(failedUsers.isEmpty() ? null : "已同步 " + successCount + " 人，失败 " + failedUsers.size() + " 人")
                    .systemUsers(systemUsers)
                    .statistics(stats)
                    .build();

        } catch (Exception e) {
            log.error("[fullSync] 全量同步异常: deviceId={}", deviceId, e);
            return DeviceSyncCheckResult.failure(deviceId, deviceName, "全量同步异常: " + e.getMessage());
        }
    }

    @Override
    public DeviceSyncCheckResult cleanSpecificUsers(Long deviceId, List<String> userIds) {
        log.info("[cleanSpecificUsers] 清理指定用户: deviceId={}, userIds={}", deviceId, userIds);

        IotDeviceDO device = deviceService.getAccessDevice(deviceId);
        if (device == null) {
            return DeviceSyncCheckResult.failure(deviceId, null, "设备不存在");
        }

        String ip = getDeviceIp(device);
        Integer port = getDevicePort(device);
        String deviceType = getAccessDeviceType(device);

        int cleanedCount = 0;
        List<String> failedUsers = new ArrayList<>();

        for (String userId : userIds) {
            try {
                boolean success = deleteDeviceUser(deviceId, ip, port, deviceType, userId);
                if (success) {
                    cleanedCount++;
                } else {
                    failedUsers.add(userId);
                }
            } catch (Exception e) {
                log.error("[cleanSpecificUsers] 删除用户异常: userId={}", userId, e);
                failedUsers.add(userId);
            }
        }

        return DeviceSyncCheckResult.builder()
                .deviceId(deviceId)
                .deviceName(device.getDeviceName())
                .deviceIp(ip)
                .success(failedUsers.isEmpty())
                .errorMessage(failedUsers.isEmpty() ? "已清理 " + cleanedCount + " 人"
                        : "已清理 " + cleanedCount + " 人，失败 " + failedUsers.size() + " 人")
                .statistics(SyncStatistics.builder()
                        .deviceOnlyCount(failedUsers.size())
                        .build())
                .build();
    }

    @Override
    public List<DeviceUserInfo> queryDeviceUsers(Long deviceId) {
        log.info("[queryDeviceUsers] ========== 开始查询设备用户 ==========");
        log.info("[queryDeviceUsers] deviceId={}", deviceId);

        IotDeviceDO device = deviceService.getAccessDevice(deviceId);
        if (device == null) {
            log.warn("[queryDeviceUsers] 设备不存在: deviceId={}", deviceId);
            return Collections.emptyList();
        }

        String ip = getDeviceIp(device);
        Integer port = getDevicePort(device);
        String deviceType = getAccessDeviceType(device);
        
        log.info("[queryDeviceUsers] 设备信息: name={}, ip={}, port={}, deviceType={}", 
                device.getDeviceName(), ip, port, deviceType);

        // 确保设备在线（如果离线则尝试激活，使用正确的 deviceType）
        String username = getDeviceUsername(device);
        String password = getDevicePassword(device);
        if (username != null && password != null) {
            boolean isOnline = messageBusClient.ensureDeviceOnline(deviceId, ip, port, username, password, deviceType);
            if (!isOnline) {
                log.warn("[queryDeviceUsers] 设备不在线且激活失败: deviceId={}, deviceType={}", deviceId, deviceType);
                return Collections.emptyList();
            }
            log.info("[queryDeviceUsers] 设备已在线: deviceId={}, deviceType={}", deviceId, deviceType);
        } else {
            log.warn("[queryDeviceUsers] 设备配置缺少用户名/密码，跳过在线检查: deviceId={}", deviceId);
        }

        try {
            // 构建查询命令参数
            Map<String, Object> params = new HashMap<>();
            params.put("deviceType", deviceType);
            params.put("ipAddress", ip);
            params.put("port", port);

            AccessControlDeviceCommand command = AccessControlDeviceCommand.builder()
                    .deviceId(deviceId)
                    .ipAddress(ip)
                    .port(port)
                    .commandType(AccessControlDeviceCommand.CommandType.QUERY_AUTH)
                    .params(params)
                    .build();

            log.info("[queryDeviceUsers] 发送查询命令: commandType={}, params={}", 
                    command.getCommandType(), params);
            
            // 带重试的查询（解决 RocketMQ 队列分配不均导致的消息丢失问题）
            AccessControlDeviceResponse response = null;
            int maxRetries = 2;        // 减少重试次数，从3次改为2次
            int retryDelayMs = 500;    // 减少重试等待时间，从1秒改为0.5秒
            int queryTimeoutSeconds = 10; // 减少单次超时时间，从15秒改为10秒
            
            for (int attempt = 1; attempt <= maxRetries; attempt++) {
                try {
                    // 每次重试使用新的 requestId
                    if (attempt > 1) {
                        command.setRequestId(java.util.UUID.randomUUID().toString());
                        log.info("[queryDeviceUsers] 第 {} 次重试查询: deviceId={}", attempt, deviceId);
                    }
                    response = messageBusClient.sendAndWait(command, queryTimeoutSeconds);
                    if (response != null && Boolean.TRUE.equals(response.getSuccess())) {
                        break; // 成功，退出重试
                    }
                } catch (TimeoutException te) {
                    log.warn("[queryDeviceUsers] 第 {} 次查询超时: deviceId={}", attempt, deviceId);
                    if (attempt < maxRetries) {
                        try {
                            Thread.sleep(retryDelayMs);
                        } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt();
                            break;
                        }
                    }
                }
            }

            log.info("[queryDeviceUsers] 收到响应: success={}, errorCode={}, errorMessage={}, data={}", 
                    response != null ? response.getSuccess() : "null",
                    response != null ? response.getErrorCode() : "null",
                    response != null ? response.getErrorMessage() : "null",
                    response != null ? response.getData() : "null");

            if (response == null || !Boolean.TRUE.equals(response.getSuccess())) {
                log.warn("[queryDeviceUsers] 查询设备用户失败（重试后仍失败）: deviceId={}, error={}",
                        deviceId, response != null ? response.getErrorMessage() : "响应为空");
                return Collections.emptyList();
            }

            // 解析响应数据
            List<DeviceUserInfo> users = parseDeviceUsersFromResponse(response);
            log.info("[queryDeviceUsers] ✅ 解析完成，获取到 {} 个用户", users.size());
            return users;

        } catch (Exception e) {
            log.error("[queryDeviceUsers] 查询异常: deviceId={}", deviceId, e);
            return Collections.emptyList();
        }
    }

    @Override
    public List<PersonInfo> getSystemUsers(Long deviceId) {
        log.debug("[getSystemUsers] 获取系统应授权人员: deviceId={}", deviceId);

        // 1. 查询设备关联的权限组
        List<IotAccessPermissionGroupDeviceDO> groupDevices = permissionGroupService.getDeviceGroups(deviceId);
        if (groupDevices.isEmpty()) {
            return Collections.emptyList();
        }

        // 2. 汇总所有权限组的人员（去重）
        Set<Long> personIds = new HashSet<>();
        Map<Long, String> personGroupMap = new HashMap<>(); // personId -> groupName

        for (IotAccessPermissionGroupDeviceDO gd : groupDevices) {
            IotAccessPermissionGroupDO group = permissionGroupService.getPermissionGroup(gd.getGroupId());
            if (group == null) continue;

            List<IotAccessPermissionGroupPersonDO> groupPersons = permissionGroupService.getGroupPersons(gd.getGroupId());
            for (IotAccessPermissionGroupPersonDO gp : groupPersons) {
                personIds.add(gp.getPersonId());
                personGroupMap.put(gp.getPersonId(), group.getGroupName());
            }
        }

        // 3. 获取人员详细信息
        List<PersonInfo> result = new ArrayList<>();
        for (Long personId : personIds) {
            IotAccessPersonDO person = personService.getPerson(personId);
            if (person == null) continue;

            // 获取人员凭证
            List<IotAccessPersonCredentialDO> credentials = personService.getPersonCredentials(personId);
            List<String> credentialTypes = credentials.stream()
                    .map(IotAccessPersonCredentialDO::getCredentialType)
                    .distinct()
                    .collect(Collectors.toList());

            String cardNo = credentials.stream()
                    .filter(c -> "CARD".equalsIgnoreCase(c.getCredentialType()))
                    .map(IotAccessPersonCredentialDO::getCredentialData)
                    .filter(Objects::nonNull)  // 过滤掉 null 值，避免 findFirst 抛出 NPE
                    .findFirst()
                    .orElse(null);

            result.add(PersonInfo.builder()
                    .personId(personId)
                    .personCode(person.getPersonCode())
                    .personName(person.getPersonName())
                    .credentialTypes(credentialTypes)
                    .cardNo(cardNo)
                    .groupName(personGroupMap.get(personId))
                    .build());
        }

        return result;
    }

    // ========== 私有方法 ==========

    /**
     * 对比系统人员和设备用户，生成对账结果
     */
    private DeviceSyncCheckResult compareAndBuildResult(Long deviceId, String deviceName, String ip,
                                                         List<PersonInfo> systemUsers, List<DeviceUserInfo> deviceUsers) {
        // 构建设备用户ID集合（用于快速匹配）
        Map<String, DeviceUserInfo> deviceUserMap = deviceUsers.stream()
                .collect(Collectors.toMap(
                        DeviceUserInfo::getUserId,
                        u -> u,
                        (a, b) -> a // 如果重复，保留第一个
                ));

        // 构建系统用户编码集合
        Set<String> systemUserCodes = systemUsers.stream()
                .map(PersonInfo::getPersonCode)
                .collect(Collectors.toSet());

        // 分类
        List<SyncedUser> syncedUsers = new ArrayList<>();
        List<PersonInfo> systemOnlyUsers = new ArrayList<>();
        List<DeviceUserInfo> deviceOnlyUsers = new ArrayList<>();

        // 遍历系统用户，判断是否在设备上
        for (PersonInfo sysUser : systemUsers) {
            DeviceUserInfo devUser = deviceUserMap.get(sysUser.getPersonCode());
            if (devUser != null) {
                // 两边都有，已同步
                syncedUsers.add(SyncedUser.builder()
                        .systemUser(sysUser)
                        .deviceUser(devUser)
                        .consistent(true) // 简化处理，可以后续增加详细对比
                        .build());
            } else {
                // 系统有、设备无
                systemOnlyUsers.add(sysUser);
            }
        }

        // 遍历设备用户，找出设备多余的
        for (DeviceUserInfo devUser : deviceUsers) {
            if (!systemUserCodes.contains(devUser.getUserId())) {
                deviceOnlyUsers.add(devUser);
            }
        }

        // 构建统计
        SyncStatistics stats = SyncStatistics.builder()
                .systemTotal(systemUsers.size())
                .deviceTotal(deviceUsers.size())
                .syncedCount(syncedUsers.size())
                .systemOnlyCount(systemOnlyUsers.size())
                .deviceOnlyCount(deviceOnlyUsers.size())
                .syncRate(systemUsers.isEmpty() ? 1.0 : (double) syncedUsers.size() / systemUsers.size())
                .build();

        return DeviceSyncCheckResult.builder()
                .deviceId(deviceId)
                .deviceName(deviceName)
                .deviceIp(ip)
                .success(true)
                .systemUsers(systemUsers)
                .deviceUsers(deviceUsers)
                .syncedUsers(syncedUsers)
                .systemOnlyUsers(systemOnlyUsers)
                .deviceOnlyUsers(deviceOnlyUsers)
                .statistics(stats)
                .build();
    }

    /**
     * 解析设备返回的用户列表
     */
    @SuppressWarnings("unchecked")
    private List<DeviceUserInfo> parseDeviceUsersFromResponse(AccessControlDeviceResponse response) {
        List<DeviceUserInfo> result = new ArrayList<>();

        Object data = response.getData();
        if (data == null) {
            return result;
        }

        // 假设响应格式为 { records: [...] }
        if (data instanceof Map) {
            Map<String, Object> dataMap = (Map<String, Object>) data;
            Object records = dataMap.get("records");
            if (records instanceof List) {
                List<Map<String, Object>> recordList = (List<Map<String, Object>>) records;
                for (Map<String, Object> record : recordList) {
                    result.add(DeviceUserInfo.builder()
                            .userId(getStringValue(record, "userId", "cardNo"))
                            .userName(getStringValue(record, "userName", "personName"))
                            .cardNo(getStringValue(record, "cardNo"))
                            .recNo(getIntValue(record, "recNo"))
                            .validStart(getStringValue(record, "validStartTime"))
                            .validEnd(getStringValue(record, "validEndTime"))
                            .build());
                }
            }
        }

        return result;
    }

    private String getStringValue(Map<String, Object> map, String... keys) {
        for (String key : keys) {
            Object value = map.get(key);
            if (value != null) {
                return value.toString();
            }
        }
        return null;
    }

    private Integer getIntValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        return null;
    }

    /**
     * 删除设备上的单个用户
     */
    private boolean deleteDeviceUser(Long deviceId, String ip, Integer port, String deviceType, String userId) {
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("deviceType", deviceType);
            params.put("ipAddress", ip);
            params.put("port", port);
            params.put("userId", userId);

            AccessControlDeviceCommand command = AccessControlDeviceCommand.builder()
                    .deviceId(deviceId)
                    .ipAddress(ip)
                    .port(port)
                    .commandType(AccessControlDeviceCommand.CommandType.REVOKE_USER)
                    .userId(userId)
                    .params(params)
                    .build();

            AccessControlDeviceResponse response = messageBusClient.sendAndWait(command, 30);
            return response != null && Boolean.TRUE.equals(response.getSuccess());
        } catch (Exception e) {
            log.error("[deleteDeviceUser] 删除用户异常: userId={}", userId, e);
            return false;
        }
    }

    /**
     * 清空设备上所有用户
     */
    private boolean clearAllDeviceUsers(Long deviceId, String ip, Integer port, String deviceType) {
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("deviceType", deviceType);
            params.put("ipAddress", ip);
            params.put("port", port);

            AccessControlDeviceCommand command = AccessControlDeviceCommand.builder()
                    .deviceId(deviceId)
                    .ipAddress(ip)
                    .port(port)
                    .commandType(AccessControlDeviceCommand.CommandType.CLEAR_ALL_AUTH)
                    .params(params)
                    .build();

            AccessControlDeviceResponse response = messageBusClient.sendAndWait(command, 60);
            return response != null && Boolean.TRUE.equals(response.getSuccess());
        } catch (Exception e) {
            log.error("[clearAllDeviceUsers] 清空用户异常: deviceId={}", deviceId, e);
            return false;
        }
    }

    /**
     * 保存同步记录
     */
    private void saveSyncRecord(Long deviceId, String deviceName, int syncType,
                                 SyncStatistics stats, int cleanedCount, int repairedCount, String errorMessage) {
        IotAccessDeviceSyncRecordDO record = IotAccessDeviceSyncRecordDO.builder()
                .deviceId(deviceId)
                .deviceName(deviceName)
                .syncType(syncType)
                .syncStatus(errorMessage == null ? IotAccessDeviceSyncRecordDO.SYNC_STATUS_SUCCESS
                        : IotAccessDeviceSyncRecordDO.SYNC_STATUS_PARTIAL)
                .systemUserCount(stats.getSystemTotal())
                .deviceUserCount(stats.getDeviceTotal())
                .syncedCount(stats.getSyncedCount())
                .systemOnlyCount(stats.getSystemOnlyCount())
                .deviceOnlyCount(stats.getDeviceOnlyCount())
                .cleanedCount(cleanedCount)
                .repairedCount(repairedCount)
                .syncStartTime(LocalDateTime.now())
                .syncEndTime(LocalDateTime.now())
                .errorMessage(errorMessage)
                .build();

        syncRecordMapper.insert(record);
    }

    // ========== 设备信息获取辅助方法 ==========

    private String getDeviceIp(IotDeviceDO device) {
        if (device.getConfig() != null) {
            return device.getConfig().getIpAddress();
        }
        return null;
    }

    private Integer getDevicePort(IotDeviceDO device) {
        if (device.getConfig() instanceof AccessDeviceConfig) {
            AccessDeviceConfig config = (AccessDeviceConfig) device.getConfig();
            return config.getPort() != null ? config.getPort() : 37777;
        }
        return 37777;
    }

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

    private String getDeviceUsername(IotDeviceDO device) {
        if (device.getConfig() instanceof AccessDeviceConfig) {
            AccessDeviceConfig config = (AccessDeviceConfig) device.getConfig();
            return config.getUsername();
        } else if (device.getConfig() instanceof GenericDeviceConfig) {
            GenericDeviceConfig cfg = (GenericDeviceConfig) device.getConfig();
            Object username = cfg.get("username");
            return username != null ? username.toString() : null;
        }
        return null;
    }

    private String getDevicePassword(IotDeviceDO device) {
        if (device.getConfig() instanceof AccessDeviceConfig) {
            AccessDeviceConfig config = (AccessDeviceConfig) device.getConfig();
            return config.getPassword();
        } else if (device.getConfig() instanceof GenericDeviceConfig) {
            GenericDeviceConfig cfg = (GenericDeviceConfig) device.getConfig();
            Object password = cfg.get("password");
            return password != null ? password.toString() : null;
        }
        return null;
    }

    private Long getDefaultChannelId(Long deviceId) {
        // 简化处理，返回null表示使用默认通道
        return null;
    }

}
