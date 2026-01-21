package cn.iocoder.yudao.module.iot.service.alarm;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.iot.controller.admin.alarm.vo.zone.IotAlarmZoneCreateReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.alarm.vo.zone.IotAlarmZonePageReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.alarm.vo.zone.IotAlarmZoneUpdateReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.alarm.IotAlarmZoneDO;

import jakarta.validation.Valid;
import java.util.Collection;
import java.util.List;

/**
 * 报警主机防区 Service 接口
 *
 * @author 芋道源码
 */
public interface IotAlarmZoneService {

    /**
     * 创建防区
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createAlarmZone(@Valid IotAlarmZoneCreateReqVO createReqVO);

    /**
     * 更新防区
     *
     * @param updateReqVO 更新信息
     */
    void updateAlarmZone(@Valid IotAlarmZoneUpdateReqVO updateReqVO);

    /**
     * 删除防区
     *
     * @param id 编号
     */
    void deleteAlarmZone(Long id);

    /**
     * 获得防区
     *
     * @param id 编号
     * @return 防区
     */
    IotAlarmZoneDO getAlarmZone(Long id);

    /**
     * 获得防区列表
     *
     * @param ids 编号
     * @return 防区列表
     */
    List<IotAlarmZoneDO> getAlarmZoneList(Collection<Long> ids);

    /**
     * 获得防区分页
     *
     * @param pageReqVO 分页查询
     * @return 防区分页
     */
    PageResult<IotAlarmZoneDO> getAlarmZonePage(IotAlarmZonePageReqVO pageReqVO);

    /**
     * 根据主机ID获取防区列表
     *
     * @param hostId 主机ID
     * @return 防区列表
     */
    List<IotAlarmZoneDO> getZoneListByHostId(Long hostId);

    /**
     * 根据主机ID和防区编号获取防区
     *
     * @param hostId 主机ID
     * @param zoneNo 防区编号
     * @return 防区
     */
    IotAlarmZoneDO getZoneByHostIdAndZoneNo(Long hostId, Integer zoneNo);

    /**
     * 批量创建或更新防区
     * 用于从报警主机查询状态后同步防区信息
     *
     * @param hostId 主机ID
     * @param zones 防区列表
     */
    void syncZones(Long hostId, List<IotAlarmZoneDO> zones);

    /**
     * 更新防区状态
     *
     * @param hostId 主机ID
     * @param zoneNo 防区编号
     * @param zoneStatus 防区状态
     * @param alarmStatus 报警状态
     */
    void updateZoneStatus(Long hostId, Integer zoneNo, String zoneStatus, Integer alarmStatus);

    /**
     * 记录防区报警
     *
     * @param hostId 主机ID
     * @param zoneNo 防区编号
     */
    void recordZoneAlarm(Long hostId, Integer zoneNo);

    /**
     * 根据设备ID和防区号更新防区状态
     *
     * @param deviceId 设备ID
     * @param areaNo 分区号
     * @param zoneNo 防区号
     * @param status 防区状态
     */
    void updateZoneStatusByDeviceIdAndZoneNo(Long deviceId, String areaNo, String zoneNo, String status);
}
