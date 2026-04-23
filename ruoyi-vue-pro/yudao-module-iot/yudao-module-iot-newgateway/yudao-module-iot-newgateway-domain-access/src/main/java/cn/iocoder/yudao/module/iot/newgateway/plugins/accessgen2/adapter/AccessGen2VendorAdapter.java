package cn.iocoder.yudao.module.iot.newgateway.plugins.accessgen2.adapter;

import cn.iocoder.yudao.module.iot.newgateway.plugins.accessgen2.dto.AccessGen2FaceInfo;
import cn.iocoder.yudao.module.iot.newgateway.plugins.accessgen2.dto.AccessGen2FingerprintInfo;
import cn.iocoder.yudao.module.iot.newgateway.plugins.accessgen2.dto.AccessGen2LoginResult;
import cn.iocoder.yudao.module.iot.newgateway.plugins.accessgen2.dto.AccessGen2OperationResult;
import cn.iocoder.yudao.module.iot.newgateway.plugins.accessgen2.dto.AccessGen2UserInfo;
import com.netsdk.lib.NetSDKLib;

import java.util.List;
import java.util.Map;

/**
 * 门禁二代厂商适配器（注册式多厂家，与 IPC {@code IpcVendorAdapter} 同级）。
 */
public interface AccessGen2VendorAdapter {

    String getVendorCode();

    String getVendorName();

    boolean supports(Map<String, Object> deviceInfo);

    default int getPriority() {
        return 100;
    }

    void registerDisconnectListener(DisconnectCallback listener);

    @FunctionalInterface
    interface DisconnectCallback {
        void onDisconnect(long loginHandle, String ip, int port);
    }

    boolean isInitialized();

    AccessGen2LoginResult login(String ip, int port, String username, String password);

    boolean logout(long loginHandle);

    Long subscribeAccessCtlEvent(long loginHandle, long deviceId, NetSDKLib.fAnalyzerDataCallBack callback);

    boolean unsubscribeAccessEvent(long analyzerHandle);

    List<AccessGen2UserInfo> queryUsers(long loginHandle, String userId);

    AccessGen2OperationResult openDoor(long loginHandle, int channelNo);

    AccessGen2OperationResult closeDoor(long loginHandle, int channelNo);

    AccessGen2OperationResult addUser(long loginHandle, AccessGen2UserInfo userInfo);

    AccessGen2OperationResult updateUser(long loginHandle, AccessGen2UserInfo userInfo);

    AccessGen2OperationResult deleteUser(long loginHandle, String userId);

    AccessGen2OperationResult setPassword(long loginHandle, String userId, String password, int channelNo);

    AccessGen2OperationResult addCard(long loginHandle, AccessGen2UserInfo userInfo);

    AccessGen2OperationResult deleteCard(long loginHandle, String cardNo);

    AccessGen2OperationResult clearAllUsers(long loginHandle);

    AccessGen2OperationResult clearAllCards(long loginHandle);

    AccessGen2OperationResult addFace(long loginHandle, AccessGen2FaceInfo faceInfo);

    AccessGen2OperationResult deleteFace(long loginHandle, String userId);

    AccessGen2OperationResult addFingerprint(long loginHandle, AccessGen2FingerprintInfo fingerprintInfo);

    AccessGen2OperationResult deleteFingerprint(long loginHandle, String userId, Integer fingerIndex);

    AccessGen2OperationResult queryDoorChannels(long loginHandle, boolean supportsFace, boolean supportsFingerprint);

    AccessGen2OperationResult setDoorAlwaysOpen(long loginHandle, int channelNo);

    AccessGen2OperationResult setDoorAlwaysClosed(long loginHandle, int channelNo);

    AccessGen2OperationResult cancelDoorAlways(long loginHandle, int channelNo);
}
