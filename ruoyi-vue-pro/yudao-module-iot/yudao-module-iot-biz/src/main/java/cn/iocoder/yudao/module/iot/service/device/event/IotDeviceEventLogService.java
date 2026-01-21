package cn.iocoder.yudao.module.iot.service.device.event;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.iot.controller.admin.device.vo.event.IotDeviceEventLogPageReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceEventLogDO;

public interface IotDeviceEventLogService {
    PageResult<IotDeviceEventLogDO> getEventLogPage(IotDeviceEventLogPageReqVO pageReqVO);
    IotDeviceEventLogDO getEventLog(Long id);
}

