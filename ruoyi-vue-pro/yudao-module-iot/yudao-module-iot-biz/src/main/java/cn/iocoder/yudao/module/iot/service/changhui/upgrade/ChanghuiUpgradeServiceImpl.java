package cn.iocoder.yudao.module.iot.service.changhui.upgrade;

import cn.hutool.core.io.IoUtil;
import cn.hutool.crypto.digest.DigestUtil;
import cn.iocoder.yudao.framework.common.exception.ErrorCode;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.tenant.core.aop.TenantIgnore;
import cn.iocoder.yudao.module.iot.controller.admin.changhui.vo.upgrade.*;
import cn.iocoder.yudao.module.iot.convert.changhui.ChanghuiFirmwareConvert;
import cn.iocoder.yudao.module.iot.convert.changhui.ChanghuiUpgradeTaskConvert;
import cn.iocoder.yudao.module.iot.dal.dataobject.changhui.ChanghuiDeviceDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.changhui.ChanghuiFirmwareDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.changhui.ChanghuiUpgradeTaskDO;
import cn.iocoder.yudao.module.iot.dal.mysql.changhui.ChanghuiFirmwareMapper;
import cn.iocoder.yudao.module.iot.dal.mysql.changhui.ChanghuiUpgradeTaskMapper;
import cn.iocoder.yudao.module.iot.enums.changhui.ChanghuiDeviceTypeEnum;
import cn.iocoder.yudao.module.iot.enums.changhui.ChanghuiUpgradeModeEnum;
import cn.iocoder.yudao.module.iot.enums.changhui.ChanghuiUpgradeStatusEnum;
import cn.iocoder.yudao.module.iot.mq.producer.DeviceCommandPublisher;
import cn.iocoder.yudao.module.iot.service.changhui.device.ChanghuiDeviceService;
import cn.iocoder.yudao.module.infra.api.file.FileApi;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;


/**
 * 长辉设备升级 Service 实现类
 * 
 * <p>管理设备固件和升级任务，支持TCP帧传输和HTTP URL下载两种升级模式
 * <p>基于全渠道量测水设施数据传输规约V2.3（IP9500_OPC协议）
 *
 * @author 长辉信息科技有限公司
 */
@Service
@Validated
@Slf4j
public class ChanghuiUpgradeServiceImpl implements ChanghuiUpgradeService {

    /** 固件不存在错误码 */
    private static final ErrorCode FIRMWARE_NOT_EXISTS = new ErrorCode(1_001_003_001, "固件不存在");
    
    /** 固件版本已存在错误码 */
    private static final ErrorCode FIRMWARE_VERSION_EXISTS = new ErrorCode(1_001_003_002, "该设备类型的固件版本已存在");
    
    /** 固件正在使用中错误码 */
    private static final ErrorCode FIRMWARE_IN_USE = new ErrorCode(1_001_003_003, "固件正在被升级任务使用，无法删除");
    
    /** 升级任务不存在错误码 */
    private static final ErrorCode UPGRADE_TASK_NOT_EXISTS = new ErrorCode(1_001_003_004, "升级任务不存在");
    
    /** 设备正在升级中错误码 */
    private static final ErrorCode DEVICE_UPGRADING = new ErrorCode(1_001_003_005, "设备正在升级中，请等待完成");
    
    /** 升级任务无法取消错误码 */
    private static final ErrorCode UPGRADE_TASK_CANNOT_CANCEL = new ErrorCode(1_001_003_006, "升级任务已完成或已取消，无法取消");
    
    /** 设备不存在错误码 */
    private static final ErrorCode DEVICE_NOT_EXISTS = new ErrorCode(1_001_003_007, "设备不存在");
    
    /** 固件与设备类型不匹配错误码 */
    private static final ErrorCode FIRMWARE_DEVICE_TYPE_MISMATCH = new ErrorCode(1_001_003_008, "固件与设备类型不匹配");
    
    /** 文件上传失败错误码 */
    private static final ErrorCode FILE_UPLOAD_FAILED = new ErrorCode(1_001_003_009, "文件上传失败");

    /** 设备类型常量 */
    private static final String DEVICE_TYPE_CHANGHUI = "CHANGHUI";

    /** 升级任务最大重试次数 */
    private static final int MAX_RETRY_COUNT = 3;

    /** 升级任务超时时间（小时） */
    private static final int UPGRADE_TIMEOUT_HOURS = 24;

    /** 升级触发命令 */
    private static final String COMMAND_UPGRADE_TRIGGER = "UPGRADE_TRIGGER";
    
    /** 升级URL命令 */
    private static final String COMMAND_UPGRADE_URL = "UPGRADE_URL";

    @Resource
    private ChanghuiFirmwareMapper firmwareMapper;

    @Resource
    private ChanghuiUpgradeTaskMapper upgradeTaskMapper;

    @Resource
    private cn.iocoder.yudao.module.iot.dal.mysql.changhui.ChanghuiUpgradeLogMapper upgradeLogMapper;

    @Resource
    private ChanghuiDeviceService deviceService;

    @Resource
    private DeviceCommandPublisher commandPublisher;

    @Resource
    private FileApi fileApi;

    // ==================== 固件管理 ====================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long uploadFirmware(ChanghuiFirmwareUploadReqVO reqVO, MultipartFile file) {
        // 检查版本是否已存在
        ChanghuiFirmwareDO existFirmware = firmwareMapper.selectByVersionAndDeviceType(
                reqVO.getVersion(), reqVO.getDeviceType());
        if (existFirmware != null) {
            throw exception(FIRMWARE_VERSION_EXISTS);
        }

        // 上传文件
        String filePath;
        byte[] content;
        try {
            content = IoUtil.readBytes(file.getInputStream());
            // 直接放在根目录下，缩短URL长度以适配设备限制
            filePath = fileApi.createFile(content, file.getOriginalFilename(), null, file.getContentType());
        } catch (IOException e) {
            log.error("[uploadFirmware] 文件上传失败: {}", e.getMessage(), e);
            throw exception(FILE_UPLOAD_FAILED);
        }

        // 计算MD5
        String fileMd5 = DigestUtil.md5Hex(content);

        // 保存固件信息
        ChanghuiFirmwareDO firmware = ChanghuiFirmwareDO.builder()
                .name(reqVO.getName())
                .version(reqVO.getVersion())
                .deviceType(reqVO.getDeviceType())
                .filePath(filePath)
                .fileSize((long) content.length)
                .fileMd5(fileMd5)
                .description(reqVO.getDescription())
                .build();
        firmwareMapper.insert(firmware);

        log.info("[uploadFirmware] 固件上传成功: name={}, version={}, deviceType={}", 
                reqVO.getName(), reqVO.getVersion(), reqVO.getDeviceType());
        return firmware.getId();
    }

    @Override
    public List<ChanghuiFirmwareRespVO> getFirmwareList(Integer deviceType) {
        List<ChanghuiFirmwareDO> firmwareList = firmwareMapper.selectListByDeviceType(deviceType);
        List<ChanghuiFirmwareRespVO> result = ChanghuiFirmwareConvert.INSTANCE.convertList(firmwareList);
        // 填充设备类型名称
        result.forEach(this::enrichFirmwareRespVO);
        return result;
    }

    @Override
    public ChanghuiFirmwareRespVO getFirmware(Long firmwareId) {
        ChanghuiFirmwareDO firmware = firmwareMapper.selectById(firmwareId);
        return enrichFirmwareRespVO(ChanghuiFirmwareConvert.INSTANCE.convert(firmware));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteFirmware(Long firmwareId) {
        // 检查固件是否存在
        ChanghuiFirmwareDO firmware = firmwareMapper.selectById(firmwareId);
        if (firmware == null) {
            throw exception(FIRMWARE_NOT_EXISTS);
        }

        // 检查是否有升级任务在使用
        Long taskCount = upgradeTaskMapper.selectCountByFirmwareId(firmwareId);
        if (taskCount > 0) {
            throw exception(FIRMWARE_IN_USE);
        }

        // 删除固件记录（文件可以保留或异步清理）
        firmwareMapper.deleteById(firmwareId);
        log.info("[deleteFirmware] 固件删除成功: id={}, name={}", firmwareId, firmware.getName());
    }


    // ==================== 升级任务管理 ====================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createUpgradeTask(ChanghuiUpgradeTaskCreateReqVO reqVO) {
        // 获取设备信息
        ChanghuiDeviceDO device = deviceService.getDeviceDOByStationCode(reqVO.getStationCode());
        if (device == null) {
            throw exception(DEVICE_NOT_EXISTS);
        }

        // 获取固件信息
        ChanghuiFirmwareDO firmware = firmwareMapper.selectById(reqVO.getFirmwareId());
        if (firmware == null) {
            throw exception(FIRMWARE_NOT_EXISTS);
        }

        // 检查固件与设备类型是否匹配
        if (!firmware.getDeviceType().equals(device.getDeviceType())) {
            throw exception(FIRMWARE_DEVICE_TYPE_MISMATCH);
        }

        // 检查是否有进行中的升级任务
        ChanghuiUpgradeTaskDO existTask = upgradeTaskMapper.selectInProgressByStationCode(reqVO.getStationCode());
        if (existTask != null) {
            throw exception(DEVICE_UPGRADING);
        }

        // 检查设备是否在线（使用统一的状态判断方法）
        boolean isDeviceOnline = cn.iocoder.yudao.module.iot.core.enums.IotDeviceStateEnum.isOnline(device.getStatus());
        if (!isDeviceOnline) {
            log.warn("[createUpgradeTask] 设备当前离线，升级任务将在设备上线后自动执行: stationCode={}", 
                    reqVO.getStationCode());
        }

        // 创建升级任务
        ChanghuiUpgradeTaskDO task = ChanghuiUpgradeTaskDO.builder()
                .deviceId(device.getId())
                .stationCode(reqVO.getStationCode())
                .firmwareId(reqVO.getFirmwareId())
                .firmwareVersion(firmware.getVersion())
                .upgradeMode(reqVO.getUpgradeMode())
                .firmwareUrl(firmware.getFilePath())
                .status(ChanghuiUpgradeStatusEnum.PENDING.getCode())
                .progress(0)
                .retryCount(0)
                .build();
        upgradeTaskMapper.insert(task);

        log.info("[createUpgradeTask] 升级任务创建成功: taskId={}, stationCode={}, firmwareVersion={}, deviceOnline={}", 
                task.getId(), reqVO.getStationCode(), firmware.getVersion(), isDeviceOnline);

        // 只有设备在线时才立即发送升级命令，离线时等待设备上线后自动触发
        if (isDeviceOnline) {
            sendUpgradeCommand(device.getId(), reqVO.getStationCode(), reqVO.getUpgradeMode(), firmware.getFilePath());
        } else {
            log.info("[createUpgradeTask] 设备离线，升级命令将在设备上线后自动下发: taskId={}, stationCode={}", 
                    task.getId(), reqVO.getStationCode());
        }

        return task.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ChanghuiBatchUpgradeResultVO createBatchUpgradeTasks(ChanghuiBatchUpgradeCreateReqVO reqVO) {
        List<Long> taskIds = new ArrayList<>();
        List<ChanghuiBatchUpgradeResultVO.FailedItem> failedItems = new ArrayList<>();

        // 获取固件信息
        ChanghuiFirmwareDO firmware = firmwareMapper.selectById(reqVO.getFirmwareId());
        if (firmware == null) {
            throw exception(FIRMWARE_NOT_EXISTS);
        }

        for (String stationCode : reqVO.getStationCodes()) {
            try {
                ChanghuiUpgradeTaskCreateReqVO createReqVO = new ChanghuiUpgradeTaskCreateReqVO();
                createReqVO.setStationCode(stationCode);
                createReqVO.setFirmwareId(reqVO.getFirmwareId());
                createReqVO.setUpgradeMode(reqVO.getUpgradeMode());
                
                Long taskId = createUpgradeTask(createReqVO);
                taskIds.add(taskId);
            } catch (Exception e) {
                log.warn("[createBatchUpgradeTasks] 创建升级任务失败: stationCode={}, error={}", 
                        stationCode, e.getMessage());
                failedItems.add(ChanghuiBatchUpgradeResultVO.FailedItem.builder()
                        .stationCode(stationCode)
                        .reason(e.getMessage())
                        .build());
            }
        }

        log.info("[createBatchUpgradeTasks] 批量升级任务创建完成: total={}, success={}, failed={}", 
                reqVO.getStationCodes().size(), taskIds.size(), failedItems.size());

        return ChanghuiBatchUpgradeResultVO.builder()
                .total(reqVO.getStationCodes().size())
                .successCount(taskIds.size())
                .failedCount(failedItems.size())
                .taskIds(taskIds)
                .failedItems(failedItems)
                .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelUpgradeTask(Long taskId) {
        ChanghuiUpgradeTaskDO task = upgradeTaskMapper.selectById(taskId);
        if (task == null) {
            throw exception(UPGRADE_TASK_NOT_EXISTS);
        }

        ChanghuiUpgradeStatusEnum status = ChanghuiUpgradeStatusEnum.getByCode(task.getStatus());
        if (status == null || !status.canCancel()) {
            throw exception(UPGRADE_TASK_CANNOT_CANCEL);
        }

        task.setStatus(ChanghuiUpgradeStatusEnum.CANCELLED.getCode());
        task.setEndTime(LocalDateTime.now());
        upgradeTaskMapper.updateById(task);

        log.info("[cancelUpgradeTask] 升级任务已取消: taskId={}, stationCode={}", taskId, task.getStationCode());
    }

    @Override
    public ChanghuiUpgradeTaskRespVO getUpgradeTask(Long taskId) {
        ChanghuiUpgradeTaskDO task = upgradeTaskMapper.selectById(taskId);
        return enrichUpgradeTaskRespVO(ChanghuiUpgradeTaskConvert.INSTANCE.convert(task));
    }

    @Override
    public PageResult<ChanghuiUpgradeTaskRespVO> getUpgradeTaskPage(ChanghuiUpgradeTaskPageReqVO reqVO) {
        PageResult<ChanghuiUpgradeTaskDO> pageResult = upgradeTaskMapper.selectPage(
                reqVO.getStationCode(), reqVO.getFirmwareId(), reqVO.getStatus(),
                reqVO.getCreateTimeStart(), reqVO.getCreateTimeEnd(),
                reqVO.getPageNo(), reqVO.getPageSize());
        PageResult<ChanghuiUpgradeTaskRespVO> result = ChanghuiUpgradeTaskConvert.INSTANCE.convertPage(pageResult);
        // 填充额外字段
        result.getList().forEach(this::enrichUpgradeTaskRespVO);
        return result;
    }

    @Override
    public ChanghuiBatchUpgradeProgressVO getBatchUpgradeProgress(List<Long> taskIds) {
        if (taskIds == null || taskIds.isEmpty()) {
            return ChanghuiBatchUpgradeProgressVO.builder()
                    .total(0)
                    .pendingCount(0)
                    .inProgressCount(0)
                    .successCount(0)
                    .failedCount(0)
                    .cancelledCount(0)
                    .taskProgresses(Collections.emptyList())
                    .build();
        }

        List<ChanghuiUpgradeTaskDO> tasks = upgradeTaskMapper.selectListByIds(taskIds);
        
        // 统计各状态数量
        Map<Integer, Long> statusCounts = tasks.stream()
                .collect(Collectors.groupingBy(ChanghuiUpgradeTaskDO::getStatus, Collectors.counting()));

        // 构建任务进度列表
        List<ChanghuiBatchUpgradeProgressVO.TaskProgress> taskProgresses = tasks.stream()
                .map(task -> {
                    ChanghuiUpgradeStatusEnum statusEnum = ChanghuiUpgradeStatusEnum.getByCode(task.getStatus());
                    return ChanghuiBatchUpgradeProgressVO.TaskProgress.builder()
                            .taskId(task.getId())
                            .stationCode(task.getStationCode())
                            .status(task.getStatus())
                            .statusName(statusEnum != null ? statusEnum.getDescription() : "未知")
                            .progress(task.getProgress())
                            .errorMessage(task.getErrorMessage())
                            .build();
                })
                .collect(Collectors.toList());

        return ChanghuiBatchUpgradeProgressVO.builder()
                .total(tasks.size())
                .pendingCount(statusCounts.getOrDefault(ChanghuiUpgradeStatusEnum.PENDING.getCode(), 0L).intValue())
                .inProgressCount(statusCounts.getOrDefault(ChanghuiUpgradeStatusEnum.IN_PROGRESS.getCode(), 0L).intValue())
                .successCount(statusCounts.getOrDefault(ChanghuiUpgradeStatusEnum.SUCCESS.getCode(), 0L).intValue())
                .failedCount(statusCounts.getOrDefault(ChanghuiUpgradeStatusEnum.FAILED.getCode(), 0L).intValue())
                .cancelledCount(statusCounts.getOrDefault(ChanghuiUpgradeStatusEnum.CANCELLED.getCode(), 0L).intValue())
                .taskProgresses(taskProgresses)
                .build();
    }


    // ==================== 升级状态更新（内部使用） ====================

    @Override
    @TenantIgnore // RocketMQ 消费者线程无租户上下文
    @Transactional(rollbackFor = Exception.class)
    public void updateUpgradeProgress(Long taskId, Integer progress) {
        ChanghuiUpgradeTaskDO task = upgradeTaskMapper.selectById(taskId);
        if (task == null) {
            log.warn("[updateUpgradeProgress] 升级任务不存在: taskId={}", taskId);
            return;
        }

        // 只有待执行或进行中的任务才能更新进度
        ChanghuiUpgradeStatusEnum status = ChanghuiUpgradeStatusEnum.getByCode(task.getStatus());
        if (status == null || status.isFinal()) {
            log.warn("[updateUpgradeProgress] 升级任务已完成，无法更新进度: taskId={}, status={}", taskId, status);
            return;
        }

        // 进度只能增加，不能减少（除非重置）
        if (progress < task.getProgress()) {
            log.warn("[updateUpgradeProgress] 进度不能减少: taskId={}, currentProgress={}, newProgress={}", 
                    taskId, task.getProgress(), progress);
            return;
        }

        // 更新状态为进行中（如果是待执行）
        if (task.getStatus().equals(ChanghuiUpgradeStatusEnum.PENDING.getCode())) {
            task.setStatus(ChanghuiUpgradeStatusEnum.IN_PROGRESS.getCode());
            task.setStartTime(LocalDateTime.now());
        }

        task.setProgress(progress);
        int updateRows = upgradeTaskMapper.updateById(task);

        if (updateRows > 0) {
            log.info("[updateUpgradeProgress] 升级进度已更新: taskId={}, progress={}, updateRows={}", taskId, progress, updateRows);
        } else {
            log.error("[updateUpgradeProgress] 更新失败(updateRows=0): taskId={}, progress={}, 可能是租户过滤问题", taskId, progress);
        }
    }

    @Override
    @TenantIgnore // RocketMQ 消费者线程无租户上下文
    @Transactional(rollbackFor = Exception.class)
    public void completeUpgrade(Long taskId, boolean success, String errorMessage) {
        ChanghuiUpgradeTaskDO task = upgradeTaskMapper.selectById(taskId);
        if (task == null) {
            log.warn("[completeUpgrade] 升级任务不存在: taskId={}", taskId);
            return;
        }

        // 只有待执行或进行中的任务才能完成
        ChanghuiUpgradeStatusEnum status = ChanghuiUpgradeStatusEnum.getByCode(task.getStatus());
        if (status == null || status.isFinal()) {
            log.warn("[completeUpgrade] 升级任务已完成: taskId={}, status={}", taskId, status);
            return;
        }

        task.setStatus(success ? ChanghuiUpgradeStatusEnum.SUCCESS.getCode() : ChanghuiUpgradeStatusEnum.FAILED.getCode());
        task.setProgress(success ? 100 : task.getProgress());
        task.setEndTime(LocalDateTime.now());
        task.setErrorMessage(errorMessage);
        upgradeTaskMapper.updateById(task);

        log.info("[completeUpgrade] 升级任务完成: taskId={}, success={}, errorMessage={}", 
                taskId, success, errorMessage);
    }

    @Override
    @TenantIgnore // RocketMQ 消费者线程无租户上下文
    @Transactional(rollbackFor = Exception.class)
    public void updateUpgradeProgressByStationCode(String stationCode, Integer progress) {
        log.info("[updateUpgradeProgressByStationCode] 开始更新进度: stationCode={}, progress={}", stationCode, progress);
        ChanghuiUpgradeTaskDO task = upgradeTaskMapper.selectInProgressByStationCode(stationCode);
        if (task != null) {
            log.info("[updateUpgradeProgressByStationCode] 找到任务: taskId={}, currentProgress={}", task.getId(), task.getProgress());
            updateUpgradeProgress(task.getId(), progress);
        } else {
            log.warn("[updateUpgradeProgressByStationCode] 未找到进行中的升级任务: stationCode={}", stationCode);
        }
    }

    @Override
    @TenantIgnore // RocketMQ 消费者线程无租户上下文
    @Transactional(rollbackFor = Exception.class)
    public void completeUpgradeByStationCode(String stationCode, boolean success, String errorMessage) {
        ChanghuiUpgradeTaskDO task = upgradeTaskMapper.selectInProgressByStationCode(stationCode);
        if (task != null) {
            completeUpgrade(task.getId(), success, errorMessage);
        } else {
            log.warn("[completeUpgradeByStationCode] 未找到进行中的升级任务: stationCode={}", stationCode);
        }
    }

    // ==================== 私有方法 ====================

    /**
     * 发送升级触发命令到设备（第一步）
     * 
     * <p>异步升级流程：
     * <ol>
     *   <li>业务侧发送 UPGRADE_TRIGGER 命令</li>
     *   <li>设备响应后，网关侧发布 TRIGGER_SUCCESS/TRIGGER_FAILED 事件</li>
     *   <li>业务侧收到 TRIGGER_SUCCESS 后调用 {@link #sendUpgradeUrlCommand} 发送 UPGRADE_URL</li>
     * </ol>
     * </p>
     */
    private void sendUpgradeCommand(Long deviceId, String stationCode, Integer upgradeMode, String firmwareUrl) {
        Map<String, Object> params = new HashMap<>();
        params.put("stationCode", stationCode);
        params.put("upgradeMode", upgradeMode);
        params.put("firmwareUrl", firmwareUrl);

        // 只发送升级触发命令，等待设备响应后再发送 UPGRADE_URL
        commandPublisher.publishCommand(DEVICE_TYPE_CHANGHUI, deviceId, COMMAND_UPGRADE_TRIGGER, params);

        log.info("[sendUpgradeCommand] 升级触发命令已发送(等待设备响应): deviceId={}, stationCode={}, upgradeMode={}", 
                deviceId, stationCode, upgradeMode);
    }
    
    /**
     * 发送升级URL命令到设备（第二步）
     * 
     * <p>在收到设备的 TRIGGER_SUCCESS 响应后调用此方法</p>
     * 
     * @param deviceId 设备ID
     * @param stationCode 测站编码
     * @param firmwareUrl 固件URL
     */
    @Override
    public void sendUpgradeUrlCommand(Long deviceId, String stationCode, String firmwareUrl) {
        Map<String, Object> params = new HashMap<>();
        params.put("stationCode", stationCode);
        params.put("firmwareUrl", firmwareUrl);
        
        commandPublisher.publishCommand(DEVICE_TYPE_CHANGHUI, deviceId, COMMAND_UPGRADE_URL, params);
        
        log.info("[sendUpgradeUrlCommand] 升级URL命令已发送: deviceId={}, stationCode={}, url={}", 
                deviceId, stationCode, firmwareUrl);
    }
    
    @Override
    @TenantIgnore // RocketMQ 消费者线程无租户上下文
    public void handleTriggerSuccess(Long deviceId, String stationCode) {
        log.info("[handleTriggerSuccess] 收到升级触发成功事件: deviceId={}, stationCode={}", deviceId, stationCode);
        
        // 查找该设备正在进行中的升级任务
        ChanghuiUpgradeTaskDO task = findPendingOrInProgressTask(deviceId, stationCode);
        if (task == null) {
            log.warn("[handleTriggerSuccess] 未找到进行中的升级任务: deviceId={}, stationCode={}", deviceId, stationCode);
            return;
        }
        
        // 检查升级模式，只有 HTTP_URL 模式需要发送 UPGRADE_URL
        if (!ChanghuiUpgradeModeEnum.HTTP_URL.getCode().equals(task.getUpgradeMode())) {
            log.info("[handleTriggerSuccess] 非HTTP URL模式，无需发送URL: taskId={}, upgradeMode={}", 
                    task.getId(), task.getUpgradeMode());
            return;
        }
        
        String firmwareUrl = task.getFirmwareUrl();
        if (firmwareUrl == null || firmwareUrl.isEmpty()) {
            log.error("[handleTriggerSuccess] 升级任务固件URL为空: taskId={}", task.getId());
            completeUpgrade(task.getId(), false, "固件URL为空");
            return;
        }
        
        // 更新任务状态为进行中
        Long taskId = task.getId();
        task.setStatus(ChanghuiUpgradeStatusEnum.IN_PROGRESS.getCode());
        task.setProgress(2); // 触发成功，进度设为2%
        int updateRows = upgradeTaskMapper.updateById(task);
        
        // 验证更新是否成功
        if (updateRows > 0) {
            // 重新查询确认
            ChanghuiUpgradeTaskDO updatedTask = upgradeTaskMapper.selectById(taskId);
            if (updatedTask != null) {
                log.info("[handleTriggerSuccess] 升级任务已更新并验证: taskId={}, status={}, progress={}", 
                        taskId, updatedTask.getStatus(), updatedTask.getProgress());
            } else {
                log.error("[handleTriggerSuccess] 更新后无法查询到任务: taskId={}", taskId);
            }
        } else {
            log.error("[handleTriggerSuccess] 升级任务更新失败(updateRows=0): taskId={}, 可能是租户过滤问题", taskId);
        }
        
        // 发送 UPGRADE_URL 命令
        sendUpgradeUrlCommand(deviceId, stationCode, firmwareUrl);
        
        log.info("[handleTriggerSuccess] 已发送UPGRADE_URL命令: taskId={}, deviceId={}, url={}", 
                task.getId(), deviceId, firmwareUrl);
    }
    
    @Override
    @TenantIgnore // RocketMQ 消费者线程无租户上下文
    public void handleTriggerFailed(Long deviceId, String stationCode, String errorMessage) {
        log.error("[handleTriggerFailed] 收到升级触发失败事件: deviceId={}, stationCode={}, error={}", 
                deviceId, stationCode, errorMessage);
        
        // 查找该设备正在进行中的升级任务
        ChanghuiUpgradeTaskDO task = findPendingOrInProgressTask(deviceId, stationCode);
        if (task == null) {
            log.warn("[handleTriggerFailed] 未找到进行中的升级任务: deviceId={}, stationCode={}", deviceId, stationCode);
            return;
        }
        
        // 标记任务为失败（设备拒绝）
        task.setStatus(ChanghuiUpgradeStatusEnum.REJECTED.getCode());
        task.setErrorMessage(errorMessage);
        task.setEndTime(LocalDateTime.now());
        upgradeTaskMapper.updateById(task);
        
        log.info("[handleTriggerFailed] 升级任务已标记为设备拒绝: taskId={}, deviceId={}", task.getId(), deviceId);
    }
    
    /**
     * 查找待执行或进行中的升级任务
     * 
     * @param deviceId 设备ID
     * @param stationCode 测站编码
     * @return 升级任务，如果不存在返回 null
     */
    private ChanghuiUpgradeTaskDO findPendingOrInProgressTask(Long deviceId, String stationCode) {
        // 优先按 deviceId 查找
        if (deviceId != null) {
            List<ChanghuiUpgradeTaskDO> tasks = upgradeTaskMapper.selectList(
                    new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<ChanghuiUpgradeTaskDO>()
                            .eq(ChanghuiUpgradeTaskDO::getDeviceId, deviceId)
                            .in(ChanghuiUpgradeTaskDO::getStatus, 
                                    ChanghuiUpgradeStatusEnum.PENDING.getCode(),
                                    ChanghuiUpgradeStatusEnum.IN_PROGRESS.getCode())
                            .orderByDesc(ChanghuiUpgradeTaskDO::getCreateTime)
                            .last("LIMIT 1")
            );
            if (!tasks.isEmpty()) {
                return tasks.get(0);
            }
        }
        
        // 按 stationCode 查找
        if (stationCode != null && !stationCode.isEmpty()) {
            List<ChanghuiUpgradeTaskDO> tasks = upgradeTaskMapper.selectList(
                    new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<ChanghuiUpgradeTaskDO>()
                            .eq(ChanghuiUpgradeTaskDO::getStationCode, stationCode)
                            .in(ChanghuiUpgradeTaskDO::getStatus, 
                                    ChanghuiUpgradeStatusEnum.PENDING.getCode(),
                                    ChanghuiUpgradeStatusEnum.IN_PROGRESS.getCode())
                            .orderByDesc(ChanghuiUpgradeTaskDO::getCreateTime)
                            .last("LIMIT 1")
            );
            if (!tasks.isEmpty()) {
                return tasks.get(0);
            }
        }
        
        return null;
    }

    /**
     * 填充固件响应VO的额外字段
     */
    private ChanghuiFirmwareRespVO enrichFirmwareRespVO(ChanghuiFirmwareRespVO respVO) {
        if (respVO == null) {
            return null;
        }
        // 填充设备类型名称
        if (respVO.getDeviceType() != null) {
            ChanghuiDeviceTypeEnum deviceTypeEnum = ChanghuiDeviceTypeEnum.getByCode(respVO.getDeviceType());
            if (deviceTypeEnum != null) {
                respVO.setDeviceTypeName(deviceTypeEnum.getDescription());
            }
        }
        return respVO;
    }

    /**
     * 填充升级任务响应VO的额外字段
     */
    private ChanghuiUpgradeTaskRespVO enrichUpgradeTaskRespVO(ChanghuiUpgradeTaskRespVO respVO) {
        if (respVO == null) {
            return null;
        }
        // 填充升级模式名称
        if (respVO.getUpgradeMode() != null) {
            ChanghuiUpgradeModeEnum modeEnum = ChanghuiUpgradeModeEnum.getByCode(respVO.getUpgradeMode());
            if (modeEnum != null) {
                respVO.setUpgradeModeName(modeEnum.getDescription());
            }
        }
        // 填充状态名称
        if (respVO.getStatus() != null) {
            ChanghuiUpgradeStatusEnum statusEnum = ChanghuiUpgradeStatusEnum.getByCode(respVO.getStatus());
            if (statusEnum != null) {
                respVO.setStatusName(statusEnum.getDescription());
            }
        }
        return respVO;
    }

    // ==================== 设备上线触发升级（4G网络断线重连恢复） ====================

    @Override
    public void triggerPendingUpgradeOnDeviceOnline(Long deviceId) {
        if (deviceId == null) {
            log.warn("[triggerPendingUpgradeOnDeviceOnline] deviceId 为空，忽略");
            return;
        }

        try {
            // 1. 首先检查是否有进行中的升级任务需要恢复（4G网络断线重连场景）
            ChanghuiUpgradeTaskDO inProgressTask = findInProgressTaskByDeviceId(deviceId);
            if (inProgressTask != null) {
                handleInProgressTaskOnReconnect(deviceId, inProgressTask);
                return;
            }

            // 2. 查询该设备待执行的升级任务
            List<ChanghuiUpgradeTaskDO> pendingTasks = upgradeTaskMapper.selectPendingByDeviceId(deviceId);
            
            if (pendingTasks == null || pendingTasks.isEmpty()) {
                log.debug("[triggerPendingUpgradeOnDeviceOnline] 设备无待执行的升级任务: deviceId={}", deviceId);
                return;
            }

            log.info("[triggerPendingUpgradeOnDeviceOnline] 设备上线，发现 {} 个待执行的升级任务: deviceId={}", 
                    pendingTasks.size(), deviceId);

            // 只处理第一个待执行任务（一次只升级一个）
            ChanghuiUpgradeTaskDO task = pendingTasks.get(0);

            // 检查任务是否超时（创建时间超过24小时）
            if (task.getCreateTime() != null && 
                task.getCreateTime().plusHours(UPGRADE_TIMEOUT_HOURS).isBefore(LocalDateTime.now())) {
                log.warn("[triggerPendingUpgradeOnDeviceOnline] 升级任务已超时，标记为失败: taskId={}, createTime={}", 
                        task.getId(), task.getCreateTime());
                completeUpgrade(task.getId(), false, "升级任务超时（超过" + UPGRADE_TIMEOUT_HOURS + "小时）");
                return;
            }

            // 检查是否超过最大重试次数
            if (task.getRetryCount() >= MAX_RETRY_COUNT) {
                log.warn("[triggerPendingUpgradeOnDeviceOnline] 升级任务已超过最大重试次数，标记为失败: taskId={}, retryCount={}", 
                        task.getId(), task.getRetryCount());
                completeUpgrade(task.getId(), false, "超过最大重试次数（" + MAX_RETRY_COUNT + "次）");
                return;
            }
            
            // 获取固件信息
            ChanghuiFirmwareDO firmware = firmwareMapper.selectById(task.getFirmwareId());
            if (firmware == null) {
                log.error("[triggerPendingUpgradeOnDeviceOnline] 固件不存在: firmwareId={}", task.getFirmwareId());
                completeUpgrade(task.getId(), false, "固件不存在");
                return;
            }

            // 增加重试次数并更新
            task.setRetryCount(task.getRetryCount() + 1);
            upgradeTaskMapper.updateById(task);

            // 重新下发升级命令
            sendUpgradeCommand(deviceId, task.getStationCode(), task.getUpgradeMode(), firmware.getFilePath());

            log.info("[triggerPendingUpgradeOnDeviceOnline] 已重新下发升级命令: taskId={}, stationCode={}, retryCount={}/{}", 
                    task.getId(), task.getStationCode(), task.getRetryCount(), MAX_RETRY_COUNT);

        } catch (Exception e) {
            log.error("[triggerPendingUpgradeOnDeviceOnline] 触发待执行升级任务失败: deviceId={}, error={}", 
                    deviceId, e.getMessage(), e);
        }
    }

    /**
     * 查找设备进行中的升级任务
     * 
     * @param deviceId 设备ID
     * @return 进行中的升级任务，如果不存在返回 null
     */
    private ChanghuiUpgradeTaskDO findInProgressTaskByDeviceId(Long deviceId) {
        if (deviceId == null) {
            return null;
        }
        List<ChanghuiUpgradeTaskDO> tasks = upgradeTaskMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<ChanghuiUpgradeTaskDO>()
                        .eq(ChanghuiUpgradeTaskDO::getDeviceId, deviceId)
                        .eq(ChanghuiUpgradeTaskDO::getStatus, ChanghuiUpgradeStatusEnum.IN_PROGRESS.getCode())
                        .orderByDesc(ChanghuiUpgradeTaskDO::getCreateTime)
                        .last("LIMIT 1")
        );
        return tasks.isEmpty() ? null : tasks.get(0);
    }

    /**
     * 处理设备重连时进行中的升级任务（4G网络断线重连恢复）
     * 
     * <p>当设备因4G网络不稳定断线后重连，需要根据任务进度决定如何继续：</p>
     * <ul>
     *   <li>进度 < 5%：可能触发命令丢失，重新发送触发命令</li>
     *   <li>进度 5-50%：可能正在下载固件，等待设备上报状态</li>
     *   <li>进度 > 50%：可能正在烧录/重启，记录重连事件等待结果</li>
     * </ul>
     * 
     * @param deviceId 设备ID
     * @param task 进行中的升级任务
     */
    private void handleInProgressTaskOnReconnect(Long deviceId, ChanghuiUpgradeTaskDO task) {
        log.info("[handleInProgressTaskOnReconnect] 设备重连，发现进行中的升级任务: deviceId={}, taskId={}, progress={}%", 
                deviceId, task.getId(), task.getProgress());

        // 检查任务是否超时
        LocalDateTime startTime = task.getStartTime();
        if (startTime != null && startTime.plusHours(UPGRADE_TIMEOUT_HOURS).isBefore(LocalDateTime.now())) {
            log.warn("[handleInProgressTaskOnReconnect] 升级任务已超时，标记为失败: taskId={}, startTime={}", 
                    task.getId(), startTime);
            completeUpgrade(task.getId(), false, "升级任务超时（网络断线后超过" + UPGRADE_TIMEOUT_HOURS + "小时）");
            return;
        }

        int progress = task.getProgress() != null ? task.getProgress() : 0;

        if (progress < 5) {
            // 进度很低，可能触发命令/URL命令丢失，重新发送
            log.info("[handleInProgressTaskOnReconnect] 任务进度低于5%，重新发送升级命令: taskId={}, progress={}", 
                    task.getId(), progress);
            
            // 增加重试次数
            task.setRetryCount(task.getRetryCount() + 1);
            if (task.getRetryCount() > MAX_RETRY_COUNT) {
                log.warn("[handleInProgressTaskOnReconnect] 重试次数超过限制: taskId={}, retryCount={}", 
                        task.getId(), task.getRetryCount());
                completeUpgrade(task.getId(), false, "网络不稳定，重试次数超过限制");
                return;
            }
            upgradeTaskMapper.updateById(task);
            
            // 获取固件并重新发送命令
            ChanghuiFirmwareDO firmware = firmwareMapper.selectById(task.getFirmwareId());
            if (firmware != null) {
                sendUpgradeCommand(deviceId, task.getStationCode(), task.getUpgradeMode(), firmware.getFilePath());
            }
        } else if (progress < 50) {
            // 可能正在下载固件，记录重连事件，等待设备上报
            log.info("[handleInProgressTaskOnReconnect] 任务进度5-50%，等待设备上报状态: taskId={}, progress={}", 
                    task.getId(), progress);
            // 不做额外操作，等待设备继续上报进度
        } else {
            // 进度较高，可能正在烧录或重启，等待设备上报结果
            log.info("[handleInProgressTaskOnReconnect] 任务进度>50%，设备可能正在升级/重启，等待结果: taskId={}, progress={}", 
                    task.getId(), progress);
            // 不做额外操作，等待设备上报完成/失败
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void retryUpgradeTask(Long taskId) {
        ChanghuiUpgradeTaskDO task = upgradeTaskMapper.selectById(taskId);
        if (task == null) {
            throw exception(UPGRADE_TASK_NOT_EXISTS);
        }

        // 检查任务状态：只有待执行或失败的任务可以重试
        ChanghuiUpgradeStatusEnum status = ChanghuiUpgradeStatusEnum.getByCode(task.getStatus());
        if (status != ChanghuiUpgradeStatusEnum.PENDING && status != ChanghuiUpgradeStatusEnum.FAILED) {
            log.warn("[retryUpgradeTask] 任务状态不允许重试: taskId={}, status={}", taskId, status);
            return;
        }

        // 检查是否超过最大重试次数（手动重试时重置计数）
        // 注意：手动重试会重置重试计数，允许用户强制重试
        
        // 获取固件信息
        ChanghuiFirmwareDO firmware = firmwareMapper.selectById(task.getFirmwareId());
        if (firmware == null) {
            throw exception(FIRMWARE_NOT_EXISTS);
        }

        // 重置状态为待执行，重试计数清零（手动重试）
        task.setStatus(ChanghuiUpgradeStatusEnum.PENDING.getCode());
        task.setProgress(0);
        task.setStartTime(null);
        task.setEndTime(null);
        task.setErrorMessage(null);
        task.setRetryCount(0); // 手动重试时重置计数
        upgradeTaskMapper.updateById(task);

        // 获取设备信息检查是否在线（使用统一的状态判断方法）
        ChanghuiDeviceDO device = deviceService.getDeviceDOByStationCode(task.getStationCode());
        boolean isDeviceOnline = device != null && cn.iocoder.yudao.module.iot.core.enums.IotDeviceStateEnum.isOnline(device.getStatus());

        if (isDeviceOnline) {
            // 设备在线，立即下发升级命令
            sendUpgradeCommand(task.getDeviceId(), task.getStationCode(), task.getUpgradeMode(), firmware.getFilePath());
            log.info("[retryUpgradeTask] 升级任务已重试并下发命令: taskId={}, stationCode={}", 
                    taskId, task.getStationCode());
        } else {
            // 设备离线，等待上线后自动触发
            log.info("[retryUpgradeTask] 升级任务已重试，设备离线等待上线后自动下发: taskId={}, stationCode={}", 
                    taskId, task.getStationCode());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int cleanupTimeoutTasks() {
        // 计算超时时间点
        LocalDateTime timeoutBefore = LocalDateTime.now().minusHours(UPGRADE_TIMEOUT_HOURS);
        
        // 查询超时的待执行任务
        List<ChanghuiUpgradeTaskDO> timeoutTasks = upgradeTaskMapper.selectTimeoutPendingTasks(timeoutBefore);
        
        if (timeoutTasks == null || timeoutTasks.isEmpty()) {
            log.debug("[cleanupTimeoutTasks] 无超时的升级任务");
            return 0;
        }

        log.info("[cleanupTimeoutTasks] 发现 {} 个超时的升级任务，开始清理", timeoutTasks.size());

        int cleanedCount = 0;
        for (ChanghuiUpgradeTaskDO task : timeoutTasks) {
            try {
                task.setStatus(ChanghuiUpgradeStatusEnum.FAILED.getCode());
                task.setEndTime(LocalDateTime.now());
                task.setErrorMessage("升级任务超时（超过" + UPGRADE_TIMEOUT_HOURS + "小时未完成）");
                upgradeTaskMapper.updateById(task);
                cleanedCount++;
                
                log.info("[cleanupTimeoutTasks] 已标记超时任务为失败: taskId={}, stationCode={}, createTime={}", 
                        task.getId(), task.getStationCode(), task.getCreateTime());
            } catch (Exception e) {
                log.error("[cleanupTimeoutTasks] 处理超时任务失败: taskId={}, error={}", 
                        task.getId(), e.getMessage(), e);
            }
        }

        log.info("[cleanupTimeoutTasks] 超时任务清理完成，共清理 {} 个任务", cleanedCount);
        return cleanedCount;
    }

    // ==================== 升级日志记录（4G网络问题排查） ====================

    /**
     * 记录升级日志
     * <p>
     * 用于追踪升级过程中的关键事件，便于排查4G网络不稳定导致的问题
     * </p>
     *
     * @param taskId      任务ID
     * @param deviceId    设备ID
     * @param stationCode 测站编码
     * @param eventType   事件类型
     * @param description 事件描述
     * @param progress    当前进度
     * @param detail      详细信息（可选）
     */
    public void logUpgradeEvent(Long taskId, Long deviceId, String stationCode, 
            String eventType, String description, Integer progress, String detail) {
        try {
            cn.iocoder.yudao.module.iot.dal.dataobject.changhui.ChanghuiUpgradeLogDO logDO = 
                    cn.iocoder.yudao.module.iot.dal.dataobject.changhui.ChanghuiUpgradeLogDO.builder()
                    .taskId(taskId)
                    .deviceId(deviceId)
                    .stationCode(stationCode)
                    .eventType(eventType)
                    .eventDescription(description)
                    .progress(progress)
                    .eventDetail(detail)
                    .eventTime(LocalDateTime.now())
                    .build();
            
            upgradeLogMapper.insert(logDO);
            log.debug("[logUpgradeEvent] 升级日志已记录: taskId={}, eventType={}, progress={}", 
                    taskId, eventType, progress);
        } catch (Exception e) {
            // 日志记录失败不影响主流程
            log.warn("[logUpgradeEvent] 记录升级日志失败: taskId={}, eventType={}, error={}", 
                    taskId, eventType, e.getMessage());
        }
    }

    /**
     * 记录升级日志（简化版）
     */
    public void logUpgradeEvent(Long taskId, String eventType, String description) {
        ChanghuiUpgradeTaskDO task = upgradeTaskMapper.selectById(taskId);
        if (task != null) {
            logUpgradeEvent(taskId, task.getDeviceId(), task.getStationCode(), 
                    eventType, description, task.getProgress(), null);
        }
    }

}
