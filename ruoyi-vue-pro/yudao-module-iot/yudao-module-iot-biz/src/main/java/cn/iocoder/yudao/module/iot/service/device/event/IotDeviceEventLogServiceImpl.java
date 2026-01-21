package cn.iocoder.yudao.module.iot.service.device.event;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.iot.controller.admin.device.vo.event.IotDeviceEventLogPageReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceEventLogDO;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class IotDeviceEventLogServiceImpl implements IotDeviceEventLogService {
    @Override
    public PageResult<IotDeviceEventLogDO> getEventLogPage(IotDeviceEventLogPageReqVO pageReqVO) {
        return new PageResult<>(Collections.emptyList(), 0L);
    }

    @Override
    public IotDeviceEventLogDO getEventLog(Long id) {
        return null;
    }
}

