package cn.iocoder.yudao.module.iot.newgateway.plugins.accessgen2.adapter;

import cn.iocoder.yudao.module.iot.newgateway.plugins.accessgen2.dto.AccessGen2FaceInfo;
import cn.iocoder.yudao.module.iot.newgateway.plugins.accessgen2.dto.AccessGen2FingerprintInfo;
import cn.iocoder.yudao.module.iot.newgateway.plugins.accessgen2.dto.AccessGen2LoginResult;
import cn.iocoder.yudao.module.iot.newgateway.plugins.accessgen2.dto.AccessGen2OperationResult;
import cn.iocoder.yudao.module.iot.newgateway.plugins.accessgen2.dto.AccessGen2UserInfo;
import com.netsdk.lib.NetSDKLib;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 海康门禁二代占位适配器：注册到工厂，便于按品牌路由；真实 SDK 接入后在此类或子类中实现。
 */
@Slf4j
@Component
@ConditionalOnProperty(prefix = "iot.newgateway.plugins.enabled", name = "access-gen2", havingValue = "true", matchIfMissing = true)
public class HikvisionAccessGen2VendorAdapter implements AccessGen2VendorAdapter {

    public static final String VENDOR_CODE = "HIKVISION";
    public static final String VENDOR_NAME = "海康威视";

    private static final String NOT_IMPLEMENTED =
            "海康门禁二代尚未接入网关 SDK，请使用大华设备或后续版本";

    @Override
    public String getVendorCode() {
        return VENDOR_CODE;
    }

    @Override
    public String getVendorName() {
        return VENDOR_NAME;
    }

    @Override
    public boolean supports(Map<String, Object> deviceInfo) {
        return AccessGen2BrandHints.looksLikeHikvision(deviceInfo);
    }

    @Override
    public int getPriority() {
        return 20;
    }

    @Override
    public void registerDisconnectListener(DisconnectCallback listener) {
        // 无 NetSDK 断线回调，接入海康 SDK 后再转发
    }

    @Override
    public boolean isInitialized() {
        return true;
    }

    @Override
    public AccessGen2LoginResult login(String ip, int port, String username, String password) {
        log.info("[HikvisionAccessGen2] login 被拒绝: {}:{} user={}", ip, port, username);
        return AccessGen2LoginResult.failure(NOT_IMPLEMENTED);
    }

    @Override
    public boolean logout(long loginHandle) {
        return true;
    }

    @Override
    public Long subscribeAccessCtlEvent(long loginHandle, long deviceId, NetSDKLib.fAnalyzerDataCallBack callback) {
        return null;
    }

    @Override
    public boolean unsubscribeAccessEvent(long analyzerHandle) {
        return false;
    }

    @Override
    public List<AccessGen2UserInfo> queryUsers(long loginHandle, String userId) {
        return Collections.emptyList();
    }

    @Override
    public AccessGen2OperationResult openDoor(long loginHandle, int channelNo) {
        return AccessGen2OperationResult.failure(NOT_IMPLEMENTED);
    }

    @Override
    public AccessGen2OperationResult closeDoor(long loginHandle, int channelNo) {
        return AccessGen2OperationResult.failure(NOT_IMPLEMENTED);
    }

    @Override
    public AccessGen2OperationResult addUser(long loginHandle, AccessGen2UserInfo userInfo) {
        return AccessGen2OperationResult.failure(NOT_IMPLEMENTED);
    }

    @Override
    public AccessGen2OperationResult updateUser(long loginHandle, AccessGen2UserInfo userInfo) {
        return AccessGen2OperationResult.failure(NOT_IMPLEMENTED);
    }

    @Override
    public AccessGen2OperationResult deleteUser(long loginHandle, String userId) {
        return AccessGen2OperationResult.failure(NOT_IMPLEMENTED);
    }

    @Override
    public AccessGen2OperationResult setPassword(long loginHandle, String userId, String password, int channelNo) {
        return AccessGen2OperationResult.failure(NOT_IMPLEMENTED);
    }

    @Override
    public AccessGen2OperationResult addCard(long loginHandle, AccessGen2UserInfo userInfo) {
        return AccessGen2OperationResult.failure(NOT_IMPLEMENTED);
    }

    @Override
    public AccessGen2OperationResult deleteCard(long loginHandle, String cardNo) {
        return AccessGen2OperationResult.failure(NOT_IMPLEMENTED);
    }

    @Override
    public AccessGen2OperationResult clearAllUsers(long loginHandle) {
        return AccessGen2OperationResult.failure(NOT_IMPLEMENTED);
    }

    @Override
    public AccessGen2OperationResult clearAllCards(long loginHandle) {
        return AccessGen2OperationResult.failure(NOT_IMPLEMENTED);
    }

    @Override
    public AccessGen2OperationResult addFace(long loginHandle, AccessGen2FaceInfo faceInfo) {
        return AccessGen2OperationResult.failure(NOT_IMPLEMENTED);
    }

    @Override
    public AccessGen2OperationResult deleteFace(long loginHandle, String userId) {
        return AccessGen2OperationResult.failure(NOT_IMPLEMENTED);
    }

    @Override
    public AccessGen2OperationResult addFingerprint(long loginHandle, AccessGen2FingerprintInfo fingerprintInfo) {
        return AccessGen2OperationResult.failure(NOT_IMPLEMENTED);
    }

    @Override
    public AccessGen2OperationResult deleteFingerprint(long loginHandle, String userId, Integer fingerIndex) {
        return AccessGen2OperationResult.failure(NOT_IMPLEMENTED);
    }

    @Override
    public AccessGen2OperationResult queryDoorChannels(long loginHandle, boolean supportsFace, boolean supportsFingerprint) {
        return AccessGen2OperationResult.failure(NOT_IMPLEMENTED);
    }

    @Override
    public AccessGen2OperationResult setDoorAlwaysOpen(long loginHandle, int channelNo) {
        return AccessGen2OperationResult.failure(NOT_IMPLEMENTED);
    }

    @Override
    public AccessGen2OperationResult setDoorAlwaysClosed(long loginHandle, int channelNo) {
        return AccessGen2OperationResult.failure(NOT_IMPLEMENTED);
    }

    @Override
    public AccessGen2OperationResult cancelDoorAlways(long loginHandle, int channelNo) {
        return AccessGen2OperationResult.failure(NOT_IMPLEMENTED);
    }
}
