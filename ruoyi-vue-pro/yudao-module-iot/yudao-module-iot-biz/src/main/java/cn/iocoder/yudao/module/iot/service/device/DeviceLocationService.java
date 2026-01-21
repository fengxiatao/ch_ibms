package cn.iocoder.yudao.module.iot.service.device;

import cn.iocoder.yudao.module.iot.controller.admin.device.vo.*;
import jakarta.validation.Valid;

import java.math.BigDecimal;
import java.util.List;

/**
 * 设备位置信息 Service 接口
 *
 * @author IBMS Team
 */
public interface DeviceLocationService {

    /**
     * 创建设备位置信息
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createDeviceLocation(@Valid DeviceLocationSaveReqVO createReqVO);

    /**
     * 更新设备位置信息
     *
     * @param updateReqVO 更新信息
     */
    void updateDeviceLocation(@Valid DeviceLocationSaveReqVO updateReqVO);

    /**
     * 删除设备位置信息
     *
     * @param id 编号
     */
    void deleteDeviceLocation(Long id);

    /**
     * 获得设备位置信息
     *
     * @param id 编号
     * @return 设备位置信息
     */
    DeviceLocationRespVO getDeviceLocation(Long id);

    /**
     * 根据设备ID获取位置信息
     *
     * @param deviceId 设备ID
     * @return 设备位置信息
     */
    DeviceLocationRespVO getDeviceLocationByDeviceId(Long deviceId);

    /**
     * 批量更新设备位置
     *
     * @param batchUpdateReqVO 批量更新信息
     */
    void batchUpdateDeviceLocation(@Valid DeviceLocationBatchUpdateReqVO batchUpdateReqVO);

    /**
     * 获取楼层内的所有设备（包含位置信息）
     *
     * @param floorId 楼层ID
     * @return 设备列表
     */
    List<DeviceWithLocationRespVO> getDevicesInFloor(Long floorId);

    /**
     * 获取区域内的所有设备（包含位置信息）
     *
     * @param areaId 区域ID
     * @return 设备列表
     */
    List<DeviceWithLocationRespVO> getDevicesInArea(Long areaId);

    /**
     * 获取建筑内的所有设备（包含位置信息）
     *
     * @param buildingId 建筑ID
     * @return 设备列表
     */
    List<DeviceWithLocationRespVO> getDevicesInBuilding(Long buildingId);

    /**
     * 自动分配设备到区域
     * 根据设备的本地坐标自动判断所属区域
     *
     * @param deviceId 设备ID
     * @return 是否成功分配
     */
    boolean autoAssignDeviceToArea(Long deviceId);

    /**
     * 批量自动分配设备到区域
     *
     * @param floorId 楼层ID
     * @return 成功分配的设备数量
     */
    int batchAutoAssignDeviceToArea(Long floorId);

    /**
     * 获取设备的全局坐标（经纬度）
     * 根据建筑信息将本地坐标转换为全局坐标
     *
     * @param deviceId 设备ID
     * @return 全局坐标 {longitude, latitude, altitude}
     */
    GlobalCoordinateDTO getDeviceGlobalCoordinate(Long deviceId);

    /**
     * 移动设备到新位置
     *
     * @param deviceId      设备ID
     * @param targetFloorId 目标楼层ID
     * @param targetAreaId  目标区域ID
     * @param localX        新的本地X坐标
     * @param localY        新的本地Y坐标
     * @param localZ        新的本地Z坐标
     */
    void moveDevice(Long deviceId, Long targetFloorId, Long targetAreaId,
                    BigDecimal localX, BigDecimal localY, BigDecimal localZ);

    /**
     * 验证设备位置是否有效
     * 检查是否在楼层范围内、是否在正确的区域内等
     *
     * @param floorId 楼层ID
     * @param localX  本地X坐标
     * @param localY  本地Y坐标
     * @return 验证结果
     */
    ValidationResultDTO validateDeviceLocation(Long floorId, BigDecimal localX, BigDecimal localY);

    /**
     * 全局坐标DTO
     */
    class GlobalCoordinateDTO {
        private BigDecimal longitude;
        private BigDecimal latitude;
        private BigDecimal altitude;

        public GlobalCoordinateDTO(BigDecimal longitude, BigDecimal latitude, BigDecimal altitude) {
            this.longitude = longitude;
            this.latitude = latitude;
            this.altitude = altitude;
        }

        public BigDecimal getLongitude() {
            return longitude;
        }

        public BigDecimal getLatitude() {
            return latitude;
        }

        public BigDecimal getAltitude() {
            return altitude;
        }
    }

    /**
     * 验证结果DTO
     */
    class ValidationResultDTO {
        private boolean valid;
        private String message;
        private Long suggestedAreaId;
        private String suggestedAreaName;

        public ValidationResultDTO(boolean valid, String message) {
            this.valid = valid;
            this.message = message;
        }

        public boolean isValid() {
            return valid;
        }

        public String getMessage() {
            return message;
        }

        public Long getSuggestedAreaId() {
            return suggestedAreaId;
        }

        public void setSuggestedAreaId(Long suggestedAreaId) {
            this.suggestedAreaId = suggestedAreaId;
        }

        public String getSuggestedAreaName() {
            return suggestedAreaName;
        }

        public void setSuggestedAreaName(String suggestedAreaName) {
            this.suggestedAreaName = suggestedAreaName;
        }
    }

}


















