package cn.iocoder.yudao.module.iot.service.visitor.listener;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.module.iot.dal.dataobject.visitor.IotVisitorAuthDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.visitor.IotVisitorAuthDeviceDO;
import cn.iocoder.yudao.module.iot.dal.mysql.visitor.IotVisitorAuthDeviceMapper;
import cn.iocoder.yudao.module.iot.dal.mysql.visitor.IotVisitorAuthMapper;
import cn.iocoder.yudao.module.iot.service.access.IotAccessAuthDispatchService;
import cn.iocoder.yudao.module.iot.service.visitor.event.VisitorAuthDispatchEvent;
import cn.iocoder.yudao.module.iot.service.visitor.event.VisitorAuthRevokeEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import jakarta.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 访客授权事件监听器
 * 负责将访客授权操作与门禁系统集成（松耦合）
 *
 * @author 芋道源码
 */
@Slf4j
@Component
public class VisitorAuthEventListener {

    @Resource
    private IotVisitorAuthMapper authMapper;

    @Resource
    private IotVisitorAuthDeviceMapper authDeviceMapper;

    @Resource
    private IotAccessAuthDispatchService accessAuthDispatchService;

    /**
     * 处理访客权限下发事件
     * 在事务提交后异步执行，与门禁授权下发服务集成
     */
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleDispatchEvent(VisitorAuthDispatchEvent event) {
        log.info("[handleDispatchEvent] 开始处理访客权限下发事件, authId={}, applyId={}",
                event.getAuthId(), event.getApplyId());

        try {
            IotVisitorAuthDO auth = authMapper.selectById(event.getAuthId());
            if (auth == null) {
                log.warn("[handleDispatchEvent] 授权记录不存在, authId={}", event.getAuthId());
                return;
            }

            List<IotVisitorAuthDeviceDO> authDevices = authDeviceMapper.selectListByAuthId(event.getAuthId());
            if (CollUtil.isEmpty(authDevices)) {
                log.warn("[handleDispatchEvent] 没有需要下发的设备, authId={}", event.getAuthId());
                updateAuthStatus(auth, IotVisitorAuthDO.AUTH_STATUS_FAILED, "没有配置授权设备");
                return;
            }

            // 遍历设备，调用门禁授权下发服务
            int successCount = 0;
            int failCount = 0;
            for (IotVisitorAuthDeviceDO authDevice : authDevices) {
                try {
                    // 调用现有门禁授权下发能力（松耦合：通过接口调用）
                    boolean success = dispatchToDevice(auth, authDevice);
                    if (success) {
                        authDevice.setDispatchStatus(IotVisitorAuthDeviceDO.STATUS_SUCCESS);
                        authDevice.setDispatchResult("下发成功");
                        successCount++;
                    } else {
                        authDevice.setDispatchStatus(IotVisitorAuthDeviceDO.STATUS_FAILED);
                        authDevice.setDispatchResult("下发失败");
                        failCount++;
                    }
                } catch (Exception e) {
                    log.error("[handleDispatchEvent] 设备下发异常, deviceId={}", authDevice.getDeviceId(), e);
                    authDevice.setDispatchStatus(IotVisitorAuthDeviceDO.STATUS_FAILED);
                    authDevice.setDispatchResult("下发异常: " + e.getMessage());
                    failCount++;
                }
                authDevice.setDispatchTime(LocalDateTime.now());
                authDeviceMapper.updateById(authDevice);
            }

            // 更新授权状态
            if (failCount == 0) {
                updateAuthStatus(auth, IotVisitorAuthDO.AUTH_STATUS_DISPATCHED, "全部下发成功");
            } else if (successCount == 0) {
                updateAuthStatus(auth, IotVisitorAuthDO.AUTH_STATUS_FAILED, "全部下发失败");
            } else {
                updateAuthStatus(auth, IotVisitorAuthDO.AUTH_STATUS_DISPATCHED,
                        String.format("部分成功: %d成功, %d失败", successCount, failCount));
            }

            log.info("[handleDispatchEvent] 访客权限下发完成, authId={}, successCount={}, failCount={}",
                    event.getAuthId(), successCount, failCount);

        } catch (Exception e) {
            log.error("[handleDispatchEvent] 处理访客权限下发事件异常, authId={}", event.getAuthId(), e);
        }
    }

    /**
     * 处理访客权限回收事件
     */
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleRevokeEvent(VisitorAuthRevokeEvent event) {
        log.info("[handleRevokeEvent] 开始处理访客权限回收事件, authId={}, applyId={}",
                event.getAuthId(), event.getApplyId());

        try {
            IotVisitorAuthDO auth = authMapper.selectById(event.getAuthId());
            if (auth == null) {
                log.warn("[handleRevokeEvent] 授权记录不存在, authId={}", event.getAuthId());
                return;
            }

            List<IotVisitorAuthDeviceDO> authDevices = authDeviceMapper.selectListByAuthId(event.getAuthId());
            if (CollUtil.isEmpty(authDevices)) {
                log.info("[handleRevokeEvent] 没有需要回收的设备, authId={}", event.getAuthId());
                return;
            }

            // 遍历设备，调用门禁授权撤销服务
            for (IotVisitorAuthDeviceDO authDevice : authDevices) {
                try {
                    boolean success = revokeFromDevice(auth, authDevice);
                    authDevice.setRevokeStatus(success ? IotVisitorAuthDeviceDO.STATUS_SUCCESS : IotVisitorAuthDeviceDO.STATUS_FAILED);
                    authDevice.setRevokeResult(success ? "回收成功" : "回收失败");
                } catch (Exception e) {
                    log.error("[handleRevokeEvent] 设备回收异常, deviceId={}", authDevice.getDeviceId(), e);
                    authDevice.setRevokeStatus(IotVisitorAuthDeviceDO.STATUS_FAILED);
                    authDevice.setRevokeResult("回收异常: " + e.getMessage());
                }
                authDevice.setRevokeTime(LocalDateTime.now());
                authDeviceMapper.updateById(authDevice);
            }

            // 更新授权状态为已回收
            updateAuthStatus(auth, IotVisitorAuthDO.AUTH_STATUS_REVOKED, "权限已回收");

            log.info("[handleRevokeEvent] 访客权限回收完成, authId={}", event.getAuthId());

        } catch (Exception e) {
            log.error("[handleRevokeEvent] 处理访客权限回收事件异常, authId={}", event.getAuthId(), e);
        }
    }

    /**
     * 下发权限到设备
     * 通过调用现有门禁授权服务实现（松耦合）
     */
    private boolean dispatchToDevice(IotVisitorAuthDO auth, IotVisitorAuthDeviceDO authDevice) {
        // TODO: 调用 IotAccessAuthDispatchService 的方法
        // 这里需要将访客信息转换为门禁系统可识别的格式
        // 可以创建临时的 person 记录，或者直接调用设备下发接口
        
        log.info("[dispatchToDevice] 下发访客权限到设备, visitorCode={}, deviceId={}, cardNo={}, faceUrl={}",
                auth.getVisitorCode(), authDevice.getDeviceId(), auth.getCardNo(), auth.getFaceUrl());
        
        // 模拟下发成功
        // 实际实现时需要调用: accessAuthDispatchService.dispatchPersonToDevice(...)
        return true;
    }

    /**
     * 从设备回收权限
     * 通过调用现有门禁授权撤销服务实现（松耦合）
     */
    private boolean revokeFromDevice(IotVisitorAuthDO auth, IotVisitorAuthDeviceDO authDevice) {
        // TODO: 调用 IotAccessAuthDispatchService 的撤销方法
        
        log.info("[revokeFromDevice] 从设备回收访客权限, visitorCode={}, deviceId={}",
                auth.getVisitorCode(), authDevice.getDeviceId());
        
        // 模拟回收成功
        // 实际实现时需要调用: accessAuthDispatchService.revokePersonFromDevice(...)
        return true;
    }

    /**
     * 更新授权状态
     */
    private void updateAuthStatus(IotVisitorAuthDO auth, Integer status, String result) {
        auth.setAuthStatus(status);
        if (status == IotVisitorAuthDO.AUTH_STATUS_DISPATCHED || status == IotVisitorAuthDO.AUTH_STATUS_FAILED) {
            auth.setDispatchResult(result);
        } else if (status == IotVisitorAuthDO.AUTH_STATUS_REVOKED) {
            auth.setRevokeResult(result);
        }
        authMapper.updateById(auth);
    }

}
