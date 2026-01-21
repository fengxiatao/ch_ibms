package cn.iocoder.yudao.module.iot.service.alarm;

import cn.iocoder.yudao.module.iot.controller.admin.alarm.vo.host.IotAlarmHostStatusRespVO;
import cn.iocoder.yudao.module.iot.controller.admin.alarm.vo.partition.IotAlarmPartitionRespVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.alarm.IotAlarmPartitionDO;

import java.util.List;

/**
 * 报警主机分区 Service 接口
 *
 * @author 芋道源码
 */
public interface IotAlarmPartitionService {

    /**
     * 根据主机ID查询分区列表
     *
     * @param hostId 主机ID
     * @return 分区列表
     */
    List<IotAlarmPartitionRespVO> getPartitionListByHostId(Long hostId);

    /**
     * 触发主机状态查询（异步）
     * 通过IoT平台向设备发送查询指令，结果通过消息总线异步返回
     *
     * @param hostId 主机ID
     */
    void triggerHostStatusQuery(Long hostId);

    /**
     * 同步主机状态到数据库
     *
     * @param hostId 主机ID
     * @param statusDTO 状态信息
     */
    void syncHostStatusToDatabase(Long hostId, IotAlarmHostStatusRespVO statusDTO);

    /**
     * 根据设备ID和分区号更新分区布防状态
     *
     * @param deviceId 设备ID
     * @param areaNo 分区号
     * @param armStatus 布防状态
     */
    void updatePartitionArmStatusByDeviceIdAndAreaNo(Long deviceId, String areaNo, String armStatus);

    /**
     * 获取主机的分区数量
     *
     * @param hostId 主机ID
     * @return 分区数量
     */
    Integer getPartitionCountByHostId(Long hostId);

    /**
     * 更新分区状态
     *
     * @param hostId 主机ID
     * @param partitionNo 分区编号
     * @param status 布防状态
     */
    void updatePartitionStatus(Long hostId, Integer partitionNo, Integer status);

    /**
     * 更新分区名称
     *
     * @param id 分区ID
     * @param partitionName 分区名称
     */
    void updatePartitionName(Long id, String partitionName);

    /**
     * 分区外出布防
     *
     * @param partitionId 分区ID
     */
    void armPartitionAll(Long partitionId);

    /**
     * 分区居家布防
     *
     * @param partitionId 分区ID
     */
    void armPartitionEmergency(Long partitionId);

    /**
     * 分区撤防
     *
     * @param partitionId 分区ID
     */
    void disarmPartition(Long partitionId);

    /**
     * 分区消警
     *
     * @param partitionId 分区ID
     */
    void clearPartitionAlarm(Long partitionId);
}
