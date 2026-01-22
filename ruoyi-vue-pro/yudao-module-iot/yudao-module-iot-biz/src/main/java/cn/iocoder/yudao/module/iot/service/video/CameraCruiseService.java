package cn.iocoder.yudao.module.iot.service.video;

import cn.iocoder.yudao.module.iot.controller.admin.video.vo.CameraCruisePointSaveReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.video.vo.CameraCruiseSaveReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.video.CameraCruiseDO;
import jakarta.validation.Valid;

import java.util.List;

/**
 * 摄像头巡航路线 Service 接口
 *
 * @author 芋道源码
 */
public interface CameraCruiseService {

    /**
     * 创建巡航路线
     *
     * @param createReqVO 创建信息
     * @return 巡航路线ID
     */
    Long createCruise(@Valid CameraCruiseSaveReqVO createReqVO);

    /**
     * 更新巡航路线
     *
     * @param updateReqVO 更新信息
     */
    void updateCruise(@Valid CameraCruiseSaveReqVO updateReqVO);

    /**
     * 删除巡航路线
     *
     * @param id 巡航路线ID
     */
    void deleteCruise(Long id);

    /**
     * 获得巡航路线
     *
     * @param id 巡航路线ID
     * @return 巡航路线
     */
    CameraCruiseDO getCruise(Long id);

    /**
     * 获得通道的巡航路线列表
     *
     * @param channelId 通道ID
     * @return 巡航路线列表
     */
    List<CameraCruiseDO> getCruiseListByChannelId(Long channelId);

    /**
     * 启动巡航（软件巡航）
     *
     * @param id 巡航路线ID
     */
    void startCruise(Long id);

    /**
     * 停止巡航（软件巡航）
     *
     * @param id 巡航路线ID
     */
    void stopCruise(Long id);

    /**
     * 同步巡航配置到设备
     * 
     * <p>将本地巡航配置同步到设备的巡航组</p>
     *
     * @param id     巡航路线ID
     * @param tourNo 设备巡航组编号（1-8），默认使用巡航路线ID对8取模+1
     */
    void syncCruiseToDevice(Long id, Integer tourNo);

    /**
     * 启动设备巡航
     * 
     * <p>使用设备原生巡航功能</p>
     *
     * @param id     巡航路线ID
     * @param tourNo 设备巡航组编号（1-8）
     */
    void startDeviceCruise(Long id, Integer tourNo);

    /**
     * 停止设备巡航
     *
     * @param id     巡航路线ID
     * @param tourNo 设备巡航组编号（1-8）
     */
    void stopDeviceCruise(Long id, Integer tourNo);

    // ========== 巡航点管理 ==========

    /**
     * 添加巡航点
     *
     * @param reqVO 巡航点信息
     * @return 巡航点ID
     */
    Long addCruisePoint(@Valid CameraCruisePointSaveReqVO reqVO);

    /**
     * 更新巡航点
     *
     * @param reqVO 巡航点信息
     */
    void updateCruisePoint(@Valid CameraCruisePointSaveReqVO reqVO);

    /**
     * 删除巡航点
     *
     * @param id 巡航点ID
     */
    void deleteCruisePoint(Long id);

}
