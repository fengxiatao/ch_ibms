package cn.iocoder.yudao.module.iot.service.access;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.iot.controller.admin.access.vo.credential.IotAccessCredentialVerifyReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.access.vo.credential.IotAccessCredentialVerifyRespVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.access.IotAccessPersonCredentialDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.access.IotAccessPersonDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.access.IotAccessPermissionGroupDeviceDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.access.IotAccessPermissionGroupPersonDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.channel.IotDeviceChannelDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceDO;
import cn.iocoder.yudao.module.iot.dal.mysql.access.IotAccessPersonCredentialMapper;
import cn.iocoder.yudao.module.iot.dal.mysql.access.IotAccessPersonMapper;
import cn.iocoder.yudao.module.iot.dal.mysql.access.IotAccessPermissionGroupDeviceMapper;
import cn.iocoder.yudao.module.iot.dal.mysql.access.IotAccessPermissionGroupPersonMapper;
import cn.iocoder.yudao.module.iot.dal.mysql.channel.IotDeviceChannelMapper;
import cn.iocoder.yudao.module.iot.dal.mysql.device.IotDeviceMapper;
import cn.iocoder.yudao.module.iot.service.access.IotAccessEventLogService;
import cn.iocoder.yudao.module.iot.service.access.IotAccessOperationLogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import jakarta.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.iot.enums.ErrorCodeConstants.*;

/**
 * 门禁凭证验证 Service 实现类
 *
 * @author 芋道源码
 */
@Slf4j
@Service
@Validated
public class IotAccessCredentialServiceImpl implements IotAccessCredentialService {

    @Resource
    private IotDeviceChannelMapper channelMapper;

    @Resource
    private IotDeviceMapper deviceMapper;

    @Resource
    private IotAccessPersonCredentialMapper credentialMapper;

    @Resource
    private IotAccessPersonMapper personMapper;

    @Resource
    private IotAccessPermissionGroupPersonMapper groupPersonMapper;

    @Resource
    private IotAccessPermissionGroupDeviceMapper groupDeviceMapper;

    @Resource
    private IotAccessChannelService channelService;

    @Resource
    private IotAccessEventLogService eventLogService;

    @Resource
    private IotAccessOperationLogService operationLogService;

    @Override
    public IotAccessCredentialVerifyRespVO verifyAndOpen(IotAccessCredentialVerifyReqVO reqVO) {
        IotAccessCredentialVerifyRespVO respVO = new IotAccessCredentialVerifyRespVO();
        respVO.setSuccess(false);
        respVO.setCredentialType(reqVO.getCredentialType());
        respVO.setCredentialData(reqVO.getCredentialData());
        respVO.setChannelId(reqVO.getChannelId());
        respVO.setOpenTime(LocalDateTime.now());

        try {
            // 1. 获取通道信息
            IotDeviceChannelDO channel = channelMapper.selectById(reqVO.getChannelId());
            if (channel == null) {
                respVO.setMessage("通道不存在");
                respVO.setFailReason("通道不存在");
                return respVO;
            }
            respVO.setChannelName(channel.getChannelName());

            // 2. 获取设备信息
            IotDeviceDO device = deviceMapper.selectById(channel.getDeviceId());
            if (device == null) {
                respVO.setMessage("设备不存在");
                respVO.setFailReason("设备不存在");
                return respVO;
            }
            respVO.setDeviceId(device.getId());
            respVO.setDeviceName(device.getDeviceName());

            // 3. 根据凭证类型查找凭证
            IotAccessPersonCredentialDO credential = findCredential(reqVO.getCredentialType(), reqVO.getCredentialData());
            if (credential == null) {
                respVO.setMessage("凭证不存在");
                respVO.setFailReason("凭证不存在");
                log.warn("[verifyAndOpen] 凭证不存在: type={}, data={}", reqVO.getCredentialType(), reqVO.getCredentialData());
                return respVO;
            }

            // 4. 获取人员信息
            IotAccessPersonDO person = personMapper.selectById(credential.getPersonId());
            if (person == null) {
                respVO.setMessage("人员不存在");
                respVO.setFailReason("人员不存在");
                return respVO;
            }
            respVO.setPersonId(person.getId());
            respVO.setPersonName(person.getPersonName());
            respVO.setPersonCode(person.getPersonCode());

            // 5. 检查人员状态
            if (person.getStatus() == null || person.getStatus() != 1) {
                respVO.setMessage("人员已禁用");
                respVO.setFailReason("人员已禁用");
                return respVO;
            }

            // 6. 检查人员有效期
            LocalDateTime now = LocalDateTime.now();
            if (person.getValidStart() != null && now.isBefore(person.getValidStart())) {
                respVO.setMessage("人员未到有效期");
                respVO.setFailReason("人员未到有效期");
                return respVO;
            }
            if (person.getValidEnd() != null && now.isAfter(person.getValidEnd())) {
                respVO.setMessage("人员已过有效期");
                respVO.setFailReason("人员已过有效期");
                return respVO;
            }

            // 7. 检查权限（如果需要）
            if (Boolean.TRUE.equals(reqVO.getVerifyPermission())) {
                boolean hasPermission = checkPermission(person.getId(), channel.getDeviceId(), channel.getId());
                if (!hasPermission) {
                    respVO.setMessage("无权限通过此门");
                    respVO.setFailReason("无权限通过此门");
                    log.warn("[verifyAndOpen] 无权限: personId={}, channelId={}", person.getId(), channel.getId());
                    return respVO;
                }
            }

            // 8. 调用开门服务
            try {
                channelService.openDoor(reqVO.getChannelId(), person.getId(), person.getPersonName());
                respVO.setSuccess(true);
                respVO.setMessage("验证成功，开门成功");
                log.info("[verifyAndOpen] 凭证验证开门成功: personId={}, personName={}, channelId={}, channelName={}", 
                        person.getId(), person.getPersonName(), channel.getId(), channel.getChannelName());
            } catch (Exception e) {
                respVO.setMessage("开门失败: " + e.getMessage());
                respVO.setFailReason("开门失败: " + e.getMessage());
                log.error("[verifyAndOpen] 开门失败: personId={}, channelId={}, error={}", 
                        person.getId(), channel.getId(), e.getMessage(), e);
                return respVO;
            }

            // 9. 记录事件日志（成功）
            try {
                eventLogService.recordEvent(
                        device.getId(),
                        device.getDeviceName(),
                        channel.getId(),
                        channel.getChannelName(),
                        "DOOR_OPEN",
                        "凭证验证开门",
                        person.getId(),
                        person.getPersonName(),
                        reqVO.getCredentialType(),
                        reqVO.getCredentialData(),
                        true,
                        null
                );
            } catch (Exception e) {
                log.error("[verifyAndOpen] 记录事件日志失败: {}", e.getMessage(), e);
            }

        } catch (Exception e) {
            respVO.setMessage("系统错误: " + e.getMessage());
            respVO.setFailReason("系统错误: " + e.getMessage());
            log.error("[verifyAndOpen] 系统错误: {}", e.getMessage(), e);
        }

        return respVO;
    }

    /**
     * 根据凭证类型和数据查找凭证
     */
    private IotAccessPersonCredentialDO findCredential(String credentialType, String credentialData) {
        if (StrUtil.isEmpty(credentialType) || StrUtil.isEmpty(credentialData)) {
            return null;
        }

        // 根据凭证类型查询
        List<IotAccessPersonCredentialDO> credentials = credentialMapper.selectList(
                new LambdaQueryWrapperX<IotAccessPersonCredentialDO>()
                        .eq(IotAccessPersonCredentialDO::getCredentialType, credentialType)
        );

        // 遍历匹配凭证数据
        for (IotAccessPersonCredentialDO credential : credentials) {
            if (matchCredential(credentialType, credentialData, credential.getCredentialData())) {
                return credential;
            }
        }

        return null;
    }

    /**
     * 匹配凭证数据
     */
    private boolean matchCredential(String credentialType, String inputData, String storedData) {
        if (StrUtil.isEmpty(inputData) || StrUtil.isEmpty(storedData)) {
            return false;
        }

        switch (credentialType) {
            case "CARD":
                // 卡号直接比较
                return inputData.equals(storedData);
            case "PASSWORD":
                // 密码需要加密比较（这里简化处理，实际应该使用加密算法）
                return inputData.equals(storedData);
            case "FACE":
                // 人脸比对（这里简化处理，实际应该调用人脸识别算法）
                // 可以比较Base64数据或调用SDK进行比对
                return inputData.equals(storedData);
            case "FINGERPRINT":
                // 指纹比对（这里简化处理，实际应该调用指纹识别算法）
                return inputData.equals(storedData);
            default:
                return false;
        }
    }

    /**
     * 检查人员是否有权限通过指定通道
     */
    private boolean checkPermission(Long personId, Long deviceId, Long channelId) {
        // 1. 查询人员所属的权限组
        List<IotAccessPermissionGroupPersonDO> groupPersons = groupPersonMapper.selectList(
                new LambdaQueryWrapperX<IotAccessPermissionGroupPersonDO>()
                        .eq(IotAccessPermissionGroupPersonDO::getPersonId, personId)
        );

        if (groupPersons.isEmpty()) {
            return false;
        }

        // 2. 获取权限组ID列表
        List<Long> groupIds = groupPersons.stream()
                .map(IotAccessPermissionGroupPersonDO::getGroupId)
                .collect(Collectors.toList());

        // 3. 查询权限组关联的设备和通道
        List<IotAccessPermissionGroupDeviceDO> groupDevices = groupDeviceMapper.selectList(
                new LambdaQueryWrapperX<IotAccessPermissionGroupDeviceDO>()
                        .in(IotAccessPermissionGroupDeviceDO::getGroupId, groupIds)
                        .eq(IotAccessPermissionGroupDeviceDO::getDeviceId, deviceId)
        );

        // 4. 检查是否有匹配的通道权限
        for (IotAccessPermissionGroupDeviceDO groupDevice : groupDevices) {
            // 如果channelId为null，表示对整个设备有权限
            if (groupDevice.getChannelId() == null) {
                return true;
            }
            // 如果channelId匹配，表示对该通道有权限
            if (channelId.equals(groupDevice.getChannelId())) {
                return true;
            }
        }

        return false;
    }

}
