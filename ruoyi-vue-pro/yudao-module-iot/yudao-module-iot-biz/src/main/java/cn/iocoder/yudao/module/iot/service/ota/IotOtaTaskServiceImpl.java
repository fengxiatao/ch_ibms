package cn.iocoder.yudao.module.iot.service.ota;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.iot.controller.admin.ota.vo.task.IotOtaTaskCreateReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.ota.vo.task.IotOtaTaskPageReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.ibms.IbmsDeviceDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.ota.IotOtaFirmwareDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.ota.IotOtaTaskDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.ota.IotOtaTaskRecordDO;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.iot.dal.mysql.ibms.IbmsDeviceMapper;
import cn.iocoder.yudao.module.iot.dal.mysql.ota.IotOtaTaskMapper;
import cn.iocoder.yudao.module.iot.enums.ota.IotOtaTaskDeviceScopeEnum;
import cn.iocoder.yudao.module.iot.enums.ota.IotOtaTaskRecordStatusEnum;
import cn.iocoder.yudao.module.iot.enums.ota.IotOtaTaskStatusEnum;
import cn.iocoder.yudao.module.iot.service.ibms.device.IbmsDeviceRuntimeService;
import cn.iocoder.yudao.module.iot.service.ibms.device.support.OtaDeviceView;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;
import static cn.iocoder.yudao.module.iot.enums.ErrorCodeConstants.*;

/**
 * IoT OTA 升级任务 Service 实现类
 *
 * @author Shelly Chan
 */
@Service
@Validated
@Slf4j
public class IotOtaTaskServiceImpl implements IotOtaTaskService {

    @Resource
    private IotOtaTaskMapper otaTaskMapper;

    @Resource
    private IbmsDeviceMapper ibmsDeviceMapper;
    @Resource
    private IbmsDeviceRuntimeService ibmsDeviceRuntimeService;
    @Resource
    private IotOtaFirmwareService otaFirmwareService;
    @Resource
    @Lazy // 延迟，避免循环依赖报错
    private IotOtaTaskRecordService otaTaskRecordService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createOtaTask(IotOtaTaskCreateReqVO createReqVO) {
        // 1.1 校验固件信息是否存在
        IotOtaFirmwareDO firmware = otaFirmwareService.validateFirmwareExists(createReqVO.getFirmwareId());
        // 1.2 校验同一固件的升级任务名称不重复
        if (otaTaskMapper.selectByFirmwareIdAndName(firmware.getId(), createReqVO.getName()) != null) {
            throw exception(OTA_TASK_CREATE_FAIL_NAME_DUPLICATE);
        }
        // 1.3 校验设备范围信息
        List<OtaDeviceView> devices = validateOtaTaskDeviceScope(createReqVO, firmware.getProductId());

        // 2. 保存升级任务，直接转换
        IotOtaTaskDO task = BeanUtils.toBean(createReqVO, IotOtaTaskDO.class)
                .setStatus(IotOtaTaskStatusEnum.IN_PROGRESS.getStatus())
                .setDeviceTotalCount(devices.size()).setDeviceSuccessCount(0);
        otaTaskMapper.insert(task);

        // 3. 生成设备升级记录
        otaTaskRecordService.createOtaTaskRecordList(devices, firmware.getId(), task.getId());
        return task.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelOtaTask(Long id) {
        // 1.1 校验升级任务是否存在
        IotOtaTaskDO upgradeTask = validateUpgradeTaskExists(id);
        // 1.2 校验升级任务是否可以取消
        if (ObjUtil.notEqual(upgradeTask.getStatus(), IotOtaTaskStatusEnum.IN_PROGRESS.getStatus())) {
            throw exception(OTA_TASK_CANCEL_FAIL_STATUS_END);
        }

        // 2. 更新升级任务状态为已取消
        otaTaskMapper.updateById(IotOtaTaskDO.builder()
                .id(id).status(IotOtaTaskStatusEnum.CANCELED.getStatus())
                .build());

        // 3. 更新升级记录状态为已取消
        otaTaskRecordService.cancelTaskRecordListByTaskId(id);
    }

    @Override
    public IotOtaTaskDO getOtaTask(Long id) {
        return otaTaskMapper.selectById(id);
    }

    @Override
    public PageResult<IotOtaTaskDO> getOtaTaskPage(IotOtaTaskPageReqVO pageReqVO) {
        return otaTaskMapper.selectPage(pageReqVO);
    }

    @Override
    public void updateOtaTaskStatusEnd(Long taskId) {
        int updateCount = otaTaskMapper.updateByIdAndStatus(taskId, IotOtaTaskStatusEnum.IN_PROGRESS.getStatus(),
                new IotOtaTaskDO().setStatus(IotOtaTaskStatusEnum.END.getStatus()));
        if (updateCount == 0) {
            log.warn("[updateOtaTaskStatusEnd][任务({})不存在或状态不是进行中，无法更新]", taskId);
        }
    }

    private List<OtaDeviceView> validateOtaTaskDeviceScope(IotOtaTaskCreateReqVO createReqVO, Long productId) {
        // 情况一：选择设备（收口后：仅使用 IBMS 台账）
        if (Objects.equals(createReqVO.getDeviceScope(), IotOtaTaskDeviceScopeEnum.SELECT.getScope())) {
            List<Long> ids = createReqVO.getDeviceIds();
            if (CollUtil.isEmpty(ids)) {
                throw exception(OTA_TASK_CREATE_FAIL_DEVICE_EMPTY);
            }
            Set<Long> idSet = new LinkedHashSet<>(ids);
            List<IbmsDeviceDO> ibmsList = ibmsDeviceMapper.selectList(
                    new LambdaQueryWrapperX<IbmsDeviceDO>().in(IbmsDeviceDO::getId, idSet));
            if (ibmsList.size() != idSet.size()) {
                // 禁止回退到 iot_device
                throw exception(DEVICE_NOT_EXISTS);
            }
            return validateIbmsDevicesForOtaSelect(ibmsList, productId, createReqVO);
        }
        // 情况二：全部设备（收口后：仅使用 IBMS 台账）
        if (Objects.equals(createReqVO.getDeviceScope(), IotOtaTaskDeviceScopeEnum.ALL.getScope())) {
            List<IbmsDeviceDO> ibmsDevices = ibmsDeviceMapper.selectListByIbmsProductId(productId);
            if (CollUtil.isEmpty(ibmsDevices)) {
                throw exception(OTA_TASK_CREATE_FAIL_DEVICE_EMPTY);
            }
            return validateIbmsDevicesForOtaAll(ibmsDevices, productId, createReqVO);
        }
        throw new IllegalArgumentException("不支持的设备范围：" + createReqVO.getDeviceScope());
    }

    /**
     * 勾选设备：IBMS 台账存在且数量与入参一致时，按 ibms_product_id 与运行态固件校验。
     */
    private List<OtaDeviceView> validateIbmsDevicesForOtaSelect(List<IbmsDeviceDO> ibmsList, Long productId,
                                                                IotOtaTaskCreateReqVO createReqVO) {
        for (IbmsDeviceDO ibms : ibmsList) {
            if (!Objects.equals(ibms.getIbmsProductId(), productId)) {
                throw exception(DEVICE_NOT_EXISTS);
            }
        }
        List<OtaDeviceView> shells = new ArrayList<>(ibmsList.size());
        for (IbmsDeviceDO ibms : ibmsList) {
            OtaDeviceView view = OtaDeviceView.of(ibms,
                    ibmsDeviceRuntimeService.getByDeviceId(ibms.getId()));
            if (view != null) {
                shells.add(view);
            }
        }
        for (OtaDeviceView device : shells) {
            if (Objects.equals(device.getFirmwareId(), createReqVO.getFirmwareId())) {
                throw exception(OTA_TASK_CREATE_FAIL_DEVICE_FIRMWARE_EXISTS, device.getDeviceName());
            }
        }
        List<IotOtaTaskRecordDO> records = otaTaskRecordService.getOtaTaskRecordListByDeviceIdAndStatus(
                convertSet(shells, OtaDeviceView::getId), IotOtaTaskRecordStatusEnum.IN_PROCESS_STATUSES);
        for (OtaDeviceView device : shells) {
            if (CollUtil.contains(records, item -> item.getDeviceId().equals(device.getId()))) {
                throw exception(OTA_TASK_CREATE_FAIL_DEVICE_OTA_IN_PROCESS, device.getDeviceName());
            }
        }
        return shells;
    }

    /**
     * 全部设备：仅 IBMS 台账列表（与固件 productId = ibms_product.id 对齐）。
     */
    private List<OtaDeviceView> validateIbmsDevicesForOtaAll(List<IbmsDeviceDO> ibmsList, Long productId,
                                                             IotOtaTaskCreateReqVO createReqVO) {
        for (IbmsDeviceDO ibms : ibmsList) {
            if (!Objects.equals(ibms.getIbmsProductId(), productId)) {
                throw exception(DEVICE_NOT_EXISTS);
            }
        }
        List<OtaDeviceView> devices = new ArrayList<>(ibmsList.size());
        for (IbmsDeviceDO ibms : ibmsList) {
            OtaDeviceView view = OtaDeviceView.of(ibms,
                    ibmsDeviceRuntimeService.getByDeviceId(ibms.getId()));
            if (view != null) {
                devices.add(view);
            }
        }
        devices.removeIf(d -> Objects.equals(d.getFirmwareId(), createReqVO.getFirmwareId()));
        List<IotOtaTaskRecordDO> records = otaTaskRecordService.getOtaTaskRecordListByDeviceIdAndStatus(
                convertSet(devices, OtaDeviceView::getId), IotOtaTaskRecordStatusEnum.IN_PROCESS_STATUSES);
        devices.removeIf(d -> CollUtil.contains(records, r -> r.getDeviceId().equals(d.getId())));
        if (CollUtil.isEmpty(devices)) {
            throw exception(OTA_TASK_CREATE_FAIL_DEVICE_EMPTY);
        }
        return devices;
    }

    private IotOtaTaskDO validateUpgradeTaskExists(Long id) {
        IotOtaTaskDO upgradeTask = otaTaskMapper.selectById(id);
        if (Objects.isNull(upgradeTask)) {
            throw exception(OTA_TASK_NOT_EXISTS);
        }
        return upgradeTask;
    }

}
