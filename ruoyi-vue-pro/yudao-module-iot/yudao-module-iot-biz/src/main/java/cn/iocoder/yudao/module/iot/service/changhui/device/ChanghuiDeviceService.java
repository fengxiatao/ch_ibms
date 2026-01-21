package cn.iocoder.yudao.module.iot.service.changhui.device;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.iot.controller.admin.changhui.vo.device.ChanghuiDevicePageReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.changhui.vo.device.ChanghuiDeviceRegisterReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.changhui.vo.device.ChanghuiDeviceRespVO;
import cn.iocoder.yudao.module.iot.controller.admin.changhui.vo.device.ChanghuiDeviceUpdateReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.changhui.ChanghuiDeviceDO;

import java.util.List;

/**
 * 长辉设备 Service 接口
 * 
 * <p>统一管理长辉、德通等使用相同TCP协议的设备
 *
 * @author 长辉信息科技有限公司
 */
public interface ChanghuiDeviceService {

    /**
     * 注册设备
     *
     * @param reqVO 设备注册信息
     * @return 设备ID
     */
    Long registerDevice(ChanghuiDeviceRegisterReqVO reqVO);

    /**
     * 更新设备
     *
     * @param reqVO 设备更新信息
     */
    void updateDevice(ChanghuiDeviceUpdateReqVO reqVO);

    /**
     * 删除设备
     *
     * @param id 设备ID
     */
    void deleteDevice(Long id);

    /**
     * 获取设备
     *
     * @param id 设备ID
     * @return 设备信息
     */
    ChanghuiDeviceRespVO getDevice(Long id);

    /**
     * 根据测站编码获取设备
     *
     * @param stationCode 测站编码
     * @return 设备信息
     */
    ChanghuiDeviceRespVO getDeviceByStationCode(String stationCode);

    /**
     * 获取设备分页
     *
     * @param reqVO 分页查询参数
     * @return 设备分页
     */
    PageResult<ChanghuiDeviceRespVO> getDevicePage(ChanghuiDevicePageReqVO reqVO);

    /**
     * 更新设备状态
     *
     * @param stationCode 测站编码
     * @param status      状态：0-离线,1-在线
     */
    void updateDeviceStatus(String stationCode, Integer status);

    /**
     * 更新设备心跳时间
     *
     * @param stationCode 测站编码
     */
    void updateDeviceHeartbeat(String stationCode);

    /**
     * 获取在线设备列表
     *
     * @return 在线设备列表
     */
    List<ChanghuiDeviceRespVO> getOnlineDevices();

    /**
     * 根据ID获取设备DO（内部使用）
     *
     * @param id 设备ID
     * @return 设备DO
     */
    ChanghuiDeviceDO getDeviceDO(Long id);

    /**
     * 根据测站编码获取设备DO（内部使用）
     *
     * @param stationCode 测站编码
     * @return 设备DO
     */
    ChanghuiDeviceDO getDeviceDOByStationCode(String stationCode);

}
