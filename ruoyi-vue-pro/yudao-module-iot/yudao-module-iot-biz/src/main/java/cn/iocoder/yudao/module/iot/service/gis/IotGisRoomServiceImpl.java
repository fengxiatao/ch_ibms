package cn.iocoder.yudao.module.iot.service.gis;

import cn.iocoder.yudao.module.iot.controller.admin.gis.vo.DeviceVO;
import cn.iocoder.yudao.module.iot.controller.admin.gis.vo.RoomVO;
import cn.iocoder.yudao.module.iot.convert.gis.IotGisConvert;
import cn.iocoder.yudao.module.iot.dal.dataobject.gis.IotDeviceGisDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.gis.RoomDO;
import cn.iocoder.yudao.module.iot.dal.mysql.gis.IotDeviceGisMapper;
import cn.iocoder.yudao.module.iot.dal.mysql.gis.RoomMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * IoT GIS 房间 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Slf4j
public class IotGisRoomServiceImpl implements IotGisRoomService {

    @Autowired
    private RoomMapper roomMapper;

    @Autowired
    private IotDeviceGisMapper deviceMapper;

    @Override
    public RoomVO getRoomDetail(Long roomId) {
        // 1. 查询房间信息
        RoomDO roomDO = roomMapper.selectById(roomId);
        if (roomDO == null) {
            return null;
        }
        
        RoomVO roomVO = IotGisConvert.INSTANCE.convertRoom(roomDO);
        
        // 2. 查询设备列表
        List<IotDeviceGisDO> deviceDOs = deviceMapper.selectListByRoomId(roomId);
        List<DeviceVO> deviceVOs = IotGisConvert.INSTANCE.convertDeviceList(deviceDOs);
        
        roomVO.setDevices(deviceVOs);
        roomVO.setDeviceCount(deviceVOs.size());
        
        return roomVO;
    }

    @Override
    public List<DeviceVO> getRoomDevices(Long roomId) {
        List<IotDeviceGisDO> deviceDOs = deviceMapper.selectListByRoomId(roomId);
        return IotGisConvert.INSTANCE.convertDeviceList(deviceDOs);
    }

}

