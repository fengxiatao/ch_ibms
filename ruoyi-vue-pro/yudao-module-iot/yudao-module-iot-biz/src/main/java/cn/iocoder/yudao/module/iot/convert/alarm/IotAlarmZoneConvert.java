package cn.iocoder.yudao.module.iot.convert.alarm;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.iot.controller.admin.alarm.vo.zone.IotAlarmZoneCreateReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.alarm.vo.zone.IotAlarmZoneRespVO;
import cn.iocoder.yudao.module.iot.controller.admin.alarm.vo.zone.IotAlarmZoneUpdateReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.alarm.IotAlarmZoneDO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * 报警主机防区 Convert
 *
 * @author 芋道源码
 */
@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface IotAlarmZoneConvert {

    IotAlarmZoneConvert INSTANCE = Mappers.getMapper(IotAlarmZoneConvert.class);

    IotAlarmZoneDO convert(IotAlarmZoneCreateReqVO bean);

    IotAlarmZoneDO convert(IotAlarmZoneUpdateReqVO bean);

    /**
     * 将 DO 的 status 字段映射到 VO 的 zoneStatusCode 字段
     * armStatus 和 alarmStatus 直接映射
     */
    @Mapping(source = "status", target = "zoneStatusCode")
    IotAlarmZoneRespVO convert(IotAlarmZoneDO bean);

    List<IotAlarmZoneRespVO> convertList(List<IotAlarmZoneDO> list);

    PageResult<IotAlarmZoneRespVO> convertPage(PageResult<IotAlarmZoneDO> page);

}
