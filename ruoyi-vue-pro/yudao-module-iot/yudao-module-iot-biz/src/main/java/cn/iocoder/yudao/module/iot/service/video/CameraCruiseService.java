package cn.iocoder.yudao.module.iot.service.video;

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
     * 启动巡航
     *
     * @param id 巡航路线ID
     */
    void startCruise(Long id);

    /**
     * 停止巡航
     *
     * @param id 巡航路线ID
     */
    void stopCruise(Long id);

}
