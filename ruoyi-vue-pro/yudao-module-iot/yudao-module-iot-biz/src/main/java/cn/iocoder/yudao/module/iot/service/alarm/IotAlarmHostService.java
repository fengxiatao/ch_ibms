package cn.iocoder.yudao.module.iot.service.alarm;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.iot.controller.admin.alarm.vo.host.IotAlarmHostCreateReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.alarm.vo.host.IotAlarmHostPageReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.alarm.vo.host.IotAlarmHostUpdateReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.alarm.vo.host.IotAlarmHostWithDetailsRespVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.alarm.IotAlarmHostDO;

import jakarta.validation.Valid;

/**
 * 报警主机 Service 接口
 *
 * @author 长辉信息科技有限公司
 */
public interface IotAlarmHostService {

    /**
     * 创建报警主机
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createAlarmHost(@Valid IotAlarmHostCreateReqVO createReqVO);

    /**
     * 更新报警主机
     *
     * @param updateReqVO 更新信息
     */
    void updateAlarmHost(@Valid IotAlarmHostUpdateReqVO updateReqVO);

    /**
     * 删除报警主机
     *
     * @param id 编号
     */
    void deleteAlarmHost(Long id);

    /**
     * 获得报警主机
     *
     * @param id 编号
     * @return 报警主机
     */
    IotAlarmHostDO getAlarmHost(Long id);

    /**
     * 获得报警主机分页
     *
     * @param pageReqVO 分页查询
     * @return 报警主机分页
     */
    PageResult<IotAlarmHostDO> getAlarmHostPage(IotAlarmHostPageReqVO pageReqVO);

    /**
     * 快速查询主机状态（指令码0，无参数）
     * 用于快速检测主机在线状态和基本信息
     *
     * @param account 主机账号
     */
    void quickQuery(String account);

    /**
     * 全部布防
     *
     * @param id 主机ID
     * @return 返回最新的主机状态
     */
    IotAlarmHostDO armAll(Long id);

    /**
     * 紧急布防
     *
     * @param id 主机ID
     * @return 返回最新的主机状态
     */
    IotAlarmHostDO armEmergency(Long id);

    /**
     * 撤防
     *
     * @param id 主机ID
     * @return 返回最新的主机状态
     */
    IotAlarmHostDO disarm(Long id);

    /**
     * 消警
     *
     * @param id 主机ID
     * @return 返回最新的主机状态
     */
    IotAlarmHostDO clearAlarm(Long id);
    
    /**
     * 全部布防（返回完整状态：主机+分区+防区）
     * 
     * @param id 主机ID
     * @return 包含主机、分区、防区的完整状态
     */
    IotAlarmHostWithDetailsRespVO armAllWithDetails(Long id);
    
    /**
     * 紧急布防（返回完整状态：主机+分区+防区）
     * 
     * @param id 主机ID
     * @return 包含主机、分区、防区的完整状态
     */
    IotAlarmHostWithDetailsRespVO armEmergencyWithDetails(Long id);
    
    /**
     * 撤防（返回完整状态：主机+分区+防区）
     * 
     * @param id 主机ID
     * @return 包含主机、分区、防区的完整状态
     */
    IotAlarmHostWithDetailsRespVO disarmWithDetails(Long id);
    
    /**
     * 消警（返回完整状态：主机+分区+防区）
     * 
     * @param id 主机ID
     * @return 包含主机、分区、防区的完整状态
     */
    IotAlarmHostWithDetailsRespVO clearAlarmWithDetails(Long id);
    
    /**
     * 根据主机账号获取报警主机
     *
     * @param account 主机账号
     * @return 报警主机
     */
    IotAlarmHostDO getAlarmHostByAccount(String account);
    
    /**
     * 根据设备ID获取报警主机
     *
     * @param deviceId 设备ID
     * @return 报警主机
     */
    IotAlarmHostDO getAlarmHostByDeviceId(Long deviceId);
    
    /**
     * 更新主机状态（从Gateway查询结果）
     *
     * @param account 主机账号
     * @param systemStatus 系统状态
     * @param zones 防区状态列表
     */
    void updateHostStatus(String account, Integer systemStatus, java.util.List<cn.iocoder.yudao.module.iot.core.alarm.dto.AlarmHostStatusDTO.ZoneStatus> zones);
    
    /**
     * 更新主机状态（包含分区，从Gateway查询结果）
     *
     * @param account 主机账号
     * @param systemStatus 系统状态
     * @param partitions 分区状态列表
     * @param zones 防区状态列表
     */
    void updateHostStatusWithPartitions(String account, Integer systemStatus, 
                                       java.util.List<cn.iocoder.yudao.module.iot.core.alarm.dto.AlarmHostStatusDTO.PartitionStatus> partitions,
                                       java.util.List<cn.iocoder.yudao.module.iot.core.alarm.dto.AlarmHostStatusDTO.ZoneStatus> zones);
    
    /**
     * 根据设备ID更新主机布防状态
     *
     * @param deviceId 设备ID
     * @param armStatus 布防状态
     */
    void updateHostArmStatusByDeviceId(Long deviceId, String armStatus);

    /**
     * 更新主机名称
     *
     * @param id 主机ID
     * @param hostName 主机名称
     */
    void updateHostName(Long id, String hostName);

    /**
     * 更新防区名称
     *
     * @param id 防区ID
     * @param zoneName 防区名称
     */
    void updateZoneName(Long id, String zoneName);
    
    /**
     * 处理Gateway返回的控制命令响应
     *
     * @param response 控制命令响应
     */
    void handleControlResponse(cn.iocoder.yudao.module.iot.core.mq.message.alarm.AlarmHostControlResponse response);
    
    /**
     * 处理Gateway返回的状态更新通知
     *
     * @param statusDTO 状态更新DTO
     */
    void handleStatusUpdate(cn.iocoder.yudao.module.iot.core.alarm.dto.AlarmHostStatusDTO statusDTO);
    
    // ==================== 防区操作方法 ====================
    
    /**
     * 单防区布防（返回完整状态：主机+分区+防区）
     *
     * @param hostId 主机ID
     * @param zoneNo 防区号
     * @return 包含主机、分区、防区的完整状态
     */
    IotAlarmHostWithDetailsRespVO armZoneWithDetails(Long hostId, Integer zoneNo);
    
    /**
     * 单防区撤防（返回完整状态：主机+分区+防区）
     *
     * @param hostId 主机ID
     * @param zoneNo 防区号
     * @return 包含主机、分区、防区的完整状态
     */
    IotAlarmHostWithDetailsRespVO disarmZoneWithDetails(Long hostId, Integer zoneNo);
    
    /**
     * 防区旁路（返回完整状态：主机+分区+防区）
     *
     * @param hostId 主机ID
     * @param zoneNo 防区号
     * @return 包含主机、分区、防区的完整状态
     */
    IotAlarmHostWithDetailsRespVO bypassZoneWithDetails(Long hostId, Integer zoneNo);
    
    /**
     * 撤销防区旁路（返回完整状态：主机+分区+防区）
     *
     * @param hostId 主机ID
     * @param zoneNo 防区号
     * @return 包含主机、分区、防区的完整状态
     */
    IotAlarmHostWithDetailsRespVO unbypassZoneWithDetails(Long hostId, Integer zoneNo);
}
