package cn.iocoder.yudao.module.iot.convert.gis;

import cn.iocoder.yudao.module.iot.controller.admin.gis.vo.DeviceVO;
import cn.iocoder.yudao.module.iot.controller.admin.gis.vo.FloorVO;
import cn.iocoder.yudao.module.iot.controller.admin.gis.vo.RoomVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.gis.FloorDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.gis.IotDeviceGisDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.gis.RoomDO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * IoT GIS 转换工具
 *
 * @author 芋道源码
 */
@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface IotGisConvert {

    IotGisConvert INSTANCE = Mappers.getMapper(IotGisConvert.class);

    // ==================== Floor ====================

    @Mapping(target = "roomCount", ignore = true)
    @Mapping(target = "deviceCount", ignore = true)
    @Mapping(target = "onlineDeviceCount", ignore = true)
    FloorVO convert(FloorDO bean);

    List<FloorVO> convertFloorList(List<FloorDO> list);

    // ==================== Room ====================

    @Mapping(target = "devices", ignore = true)
    @Mapping(target = "deviceCount", ignore = true)
    RoomVO convertRoom(RoomDO bean);

    List<RoomVO> convertRoomList(List<RoomDO> list);

    // ==================== Device ====================

    DeviceVO convertDevice(IotDeviceGisDO bean);

    List<DeviceVO> convertDeviceList(List<IotDeviceGisDO> list);

}

