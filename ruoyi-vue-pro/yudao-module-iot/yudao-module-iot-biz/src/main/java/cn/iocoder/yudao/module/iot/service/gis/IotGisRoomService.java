package cn.iocoder.yudao.module.iot.service.gis;

import cn.iocoder.yudao.module.iot.controller.admin.gis.vo.DeviceVO;
import cn.iocoder.yudao.module.iot.controller.admin.gis.vo.RoomVO;

import java.util.List;

/**
 * IoT GIS 房间 Service 接口
 *
 * @author 芋道源码
 */
public interface IotGisRoomService {

    /**
     * 获取房间详情（包含设备列表）
     *
     * @param roomId 房间ID
     * @return 房间详情
     */
    RoomVO getRoomDetail(Long roomId);

    /**
     * 获取房间的设备列表
     *
     * @param roomId 房间ID
     * @return 设备列表
     */
    List<DeviceVO> getRoomDevices(Long roomId);

}












