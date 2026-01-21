package cn.iocoder.yudao.module.iot.service.changhui.upgrade;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.iot.controller.admin.changhui.vo.upgrade.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 长辉设备升级 Service 接口
 * 
 * <p>管理设备固件和升级任务，支持TCP帧传输和HTTP URL下载两种升级模式
 * <p>基于全渠道量测水设施数据传输规约V2.3（IP9500_OPC协议）
 *
 * @author 长辉信息科技有限公司
 */
public interface ChanghuiUpgradeService {

    // ==================== 固件管理 ====================

    /**
     * 上传固件
     *
     * @param reqVO 固件信息
     * @param file  固件文件
     * @return 固件ID
     */
    Long uploadFirmware(ChanghuiFirmwareUploadReqVO reqVO, MultipartFile file);

    /**
     * 获取固件列表
     *
     * @param deviceType 设备类型（可选，为null时返回所有）
     * @return 固件列表
     */
    List<ChanghuiFirmwareRespVO> getFirmwareList(Integer deviceType);

    /**
     * 获取固件详情
     *
     * @param firmwareId 固件ID
     * @return 固件详情
     */
    ChanghuiFirmwareRespVO getFirmware(Long firmwareId);

    /**
     * 删除固件
     *
     * @param firmwareId 固件ID
     */
    void deleteFirmware(Long firmwareId);

    // ==================== 升级任务管理 ====================

    /**
     * 创建升级任务
     *
     * @param reqVO 创建请求
     * @return 任务ID
     */
    Long createUpgradeTask(ChanghuiUpgradeTaskCreateReqVO reqVO);

    /**
     * 批量创建升级任务
     *
     * @param reqVO 批量创建请求
     * @return 批量创建结果
     */
    ChanghuiBatchUpgradeResultVO createBatchUpgradeTasks(ChanghuiBatchUpgradeCreateReqVO reqVO);

    /**
     * 取消升级任务
     *
     * @param taskId 任务ID
     */
    void cancelUpgradeTask(Long taskId);

    /**
     * 获取升级任务详情
     *
     * @param taskId 任务ID
     * @return 任务详情
     */
    ChanghuiUpgradeTaskRespVO getUpgradeTask(Long taskId);

    /**
     * 分页查询升级任务
     *
     * @param reqVO 分页查询参数
     * @return 分页结果
     */
    PageResult<ChanghuiUpgradeTaskRespVO> getUpgradeTaskPage(ChanghuiUpgradeTaskPageReqVO reqVO);

    /**
     * 获取批量升级进度
     *
     * @param taskIds 任务ID列表
     * @return 批量升级进度
     */
    ChanghuiBatchUpgradeProgressVO getBatchUpgradeProgress(List<Long> taskIds);

    // ==================== 升级状态更新（内部使用） ====================

    /**
     * 更新升级进度
     *
     * @param taskId   任务ID
     * @param progress 进度(0-100)
     */
    void updateUpgradeProgress(Long taskId, Integer progress);

    /**
     * 完成升级
     *
     * @param taskId       任务ID
     * @param success      是否成功
     * @param errorMessage 错误信息（失败时）
     */
    void completeUpgrade(Long taskId, boolean success, String errorMessage);
    
    /**
     * 发送升级URL命令（第二步）
     * 
     * <p>在收到设备的 TRIGGER_SUCCESS 响应后调用此方法发送升级URL</p>
     * 
     * @param deviceId 设备ID
     * @param stationCode 测站编码
     * @param firmwareUrl 固件URL
     */
    void sendUpgradeUrlCommand(Long deviceId, String stationCode, String firmwareUrl);
    
    /**
     * 处理升级触发成功事件
     * 
     * <p>收到设备的 TRIGGER_SUCCESS 响应后调用此方法，自动发送 UPGRADE_URL</p>
     * 
     * @param deviceId 设备ID
     * @param stationCode 测站编码
     */
    void handleTriggerSuccess(Long deviceId, String stationCode);
    
    /**
     * 处理升级触发失败事件
     * 
     * @param deviceId 设备ID
     * @param stationCode 测站编码
     * @param errorMessage 错误信息
     */
    void handleTriggerFailed(Long deviceId, String stationCode, String errorMessage);

    /**
     * 根据测站编码更新升级进度
     *
     * @param stationCode 测站编码
     * @param progress    进度(0-100)
     */
    void updateUpgradeProgressByStationCode(String stationCode, Integer progress);

    /**
     * 根据测站编码完成升级
     *
     * @param stationCode  测站编码
     * @param success      是否成功
     * @param errorMessage 错误信息（失败时）
     */
    void completeUpgradeByStationCode(String stationCode, boolean success, String errorMessage);

    // ==================== 设备上线触发升级 ====================

    /**
     * 设备上线时触发待执行的升级任务
     * 
     * <p>当设备上线时调用此方法，检查是否有待执行的升级任务，如果有则重新下发升级命令</p>
     *
     * @param deviceId 设备ID
     */
    void triggerPendingUpgradeOnDeviceOnline(Long deviceId);

    /**
     * 重试升级任务
     * 
     * <p>对已失败或待执行的升级任务进行重试</p>
     *
     * @param taskId 任务ID
     */
    void retryUpgradeTask(Long taskId);

    /**
     * 清理超时的升级任务
     * 
     * <p>将超时的待执行任务标记为失败，由定时任务调用</p>
     *
     * @return 清理的任务数量
     */
    int cleanupTimeoutTasks();

}
