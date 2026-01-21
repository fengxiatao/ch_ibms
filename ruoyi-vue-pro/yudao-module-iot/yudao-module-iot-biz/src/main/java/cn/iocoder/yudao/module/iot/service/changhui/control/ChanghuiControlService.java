package cn.iocoder.yudao.module.iot.service.changhui.control;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.iot.controller.admin.changhui.vo.control.ChanghuiControlLogPageReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.changhui.vo.control.ChanghuiControlLogRespVO;

/**
 * 长辉设备远程控制 Service 接口
 * 
 * <p>提供设备远程控制功能，包括模式切换、手动控制、自动控制等
 * <p>所有命令通过 DeviceCommandPublisher 发送到 DEVICE_SERVICE_INVOKE 主题
 * <p>基于全渠道量测水设施数据传输规约V2.3（IP9500_OPC协议）
 *
 * @author 长辉信息科技有限公司
 */
public interface ChanghuiControlService {

    /**
     * 模式切换
     * 
     * <p>切换设备工作模式（手动/自动）
     *
     * @param stationCode 测站编码
     * @param mode        目标模式：manual-手动, auto-自动
     * @return 请求ID，用于关联响应
     */
    String switchMode(String stationCode, String mode);

    /**
     * 手动控制
     * 
     * <p>执行手动控制操作（升/降/停）
     *
     * @param stationCode 测站编码
     * @param action      控制动作：rise-升, fall-降, stop-停
     * @return 请求ID，用于关联响应
     */
    String manualControl(String stationCode, String action);

    /**
     * 自动控制
     * 
     * <p>设置自动控制参数，支持流量、开度、水位、水量控制
     *
     * @param stationCode 测站编码
     * @param controlMode 控制模式：flow-流量, opening-开度, level-水位, volume-水量
     * @param targetValue 目标值
     * @return 请求ID，用于关联响应
     */
    String autoControl(String stationCode, String controlMode, Double targetValue);

    /**
     * 获取控制日志分页
     *
     * @param reqVO 分页查询参数
     * @return 控制日志分页
     */
    PageResult<ChanghuiControlLogRespVO> getControlLogs(ChanghuiControlLogPageReqVO reqVO);

    /**
     * 记录控制操作结果
     * 
     * <p>当收到设备控制响应时，更新控制日志的结果
     *
     * @param stationCode  测站编码
     * @param controlType  控制类型
     * @param success      是否成功
     * @param errorMessage 错误信息（失败时）
     */
    void recordControlResult(String stationCode, String controlType, boolean success, String errorMessage);

    /**
     * 删除设备的所有控制日志
     *
     * @param deviceId 设备ID
     */
    void deleteControlLogsByDeviceId(Long deviceId);

    /**
     * 删除设备的所有控制日志（根据测站编码）
     *
     * @param stationCode 测站编码
     */
    void deleteControlLogsByStationCode(String stationCode);

}
