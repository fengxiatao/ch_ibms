package cn.iocoder.yudao.module.iot.service.video;

import cn.iocoder.yudao.module.iot.controller.admin.video.vo.CameraPresetSaveReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.video.CameraPresetDO;

import jakarta.validation.Valid;
import java.util.List;

/**
 * 摄像头预设点 Service 接口
 *
 * @author 芋道源码
 */
public interface CameraPresetService {

    /**
     * 创建预设点
     */
    Long createPreset(@Valid CameraPresetSaveReqVO createReqVO);

    /**
     * 更新预设点
     */
    void updatePreset(@Valid CameraPresetSaveReqVO updateReqVO);

    /**
     * 删除预设点
     */
    void deletePreset(Long id);

    /**
     * 获取预设点
     */
    CameraPresetDO getPreset(Long id);

    /**
     * 获取通道的所有预设点
     */
    List<CameraPresetDO> getPresetListByChannelId(Long channelId);

    /**
     * 根据预设点ID列表批量查询预设点
     *
     * @param ids 预设点ID列表
     * @return 预设点列表
     */
    List<CameraPresetDO> getPresetListByIds(List<Long> ids);

}
