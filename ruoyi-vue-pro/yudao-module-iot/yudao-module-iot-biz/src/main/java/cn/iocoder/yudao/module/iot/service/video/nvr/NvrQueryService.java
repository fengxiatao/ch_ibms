package cn.iocoder.yudao.module.iot.service.video.nvr;

import cn.iocoder.yudao.module.iot.dal.dataobject.ibms.IbmsDeviceDO;
import cn.iocoder.yudao.module.iot.service.video.nvr.dto.NvrScannedChannelRow;

import java.util.List;

/**
 * NVR 查询服务
 */
public interface NvrQueryService {

    /**
     * 获取 NVR 设备列表（IBMS 台账）
     */
    List<IbmsDeviceDO> getNvrList();

    /**
     * 获取 NVR 的通道（子设备）列表
     * @param nvrId NVR 设备ID
     */
    List<NvrScannedChannelRow> getChannelsByNvrId(Long nvrId);

    /**
     * 刷新并同步 NVR 的通道列表（通过 Gateway 查询后入库）
     * @param nvrId NVR 设备ID
     * @return 最新通道列表
     */
    List<NvrScannedChannelRow> refreshChannelsByNvrId(Long nvrId);
}
