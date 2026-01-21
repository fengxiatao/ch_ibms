package cn.iocoder.yudao.module.iot.newgateway.plugins.accessgen1;

import cn.iocoder.yudao.module.iot.newgateway.plugins.accessgen1.dto.AccessGen1CardRecord;
import cn.iocoder.yudao.module.iot.newgateway.plugins.accessgen1.dto.AccessGen1DoorInfo;
import cn.iocoder.yudao.module.iot.newgateway.plugins.accessgen1.dto.AccessGen1LoginResult;
import cn.iocoder.yudao.module.iot.newgateway.plugins.accessgen1.dto.AccessGen1OperationResult;

import java.util.List;

/**
 * 门禁一代设备 SDK 操作接口
 * 
 * <p>定义门禁一代设备的所有 SDK 操作，用于解耦实现和测试。</p>
 * <p>测试时可以 mock 此接口而不需要加载原生库。</p>
 * 
 * @author IoT Gateway Team
 */
public interface AccessGen1SdkOperations {

    /**
     * 检查 SDK 是否已初始化
     */
    boolean isInitialized();

    /**
     * 登录设备
     */
    AccessGen1LoginResult login(String ip, int port, String username, String password);

    /**
     * 登出设备
     */
    boolean logout(long loginHandle);

    /**
     * 远程开门
     */
    AccessGen1OperationResult openDoor(long loginHandle, int channelNo);

    /**
     * 远程关门
     */
    AccessGen1OperationResult closeDoor(long loginHandle, int channelNo);

    /**
     * 查询门通道信息
     */
    AccessGen1OperationResult queryDoorChannels(long loginHandle);

    /**
     * 插入门禁卡记录
     */
    AccessGen1OperationResult insertCard(long loginHandle, AccessGen1CardRecord record);

    /**
     * 更新门禁卡记录
     */
    AccessGen1OperationResult updateCard(long loginHandle, AccessGen1CardRecord record);

    /**
     * 删除门禁卡记录（通过 recNo）
     */
    AccessGen1OperationResult deleteCardByRecNo(long loginHandle, int recNo);

    /**
     * 删除门禁卡记录（通过卡号）
     */
    AccessGen1OperationResult deleteCardByCardNo(long loginHandle, String cardNo);

    /**
     * 清空所有门禁卡记录
     */
    AccessGen1OperationResult clearAllCards(long loginHandle);

    /**
     * 查询所有门禁卡记录
     */
    List<AccessGen1CardRecord> queryAllCards(long loginHandle);

    /**
     * 根据卡号查询门禁卡记录
     */
    AccessGen1CardRecord queryCardByCardNo(long loginHandle, String cardNo);

    /**
     * 根据用户 ID 查询门禁卡记录
     */
    List<AccessGen1CardRecord> queryCardsByUserId(long loginHandle, String userId);

    /**
     * 获取门禁卡记录数量
     */
    int getCardCount(long loginHandle);

    /**
     * 获取最后一次操作的错误码
     */
    int getLastErrorCode();

    /**
     * 获取最后一次操作的错误信息
     */
    String getLastErrorMessage();
}
