package cn.iocoder.yudao.module.iot.service.video;

import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.iot.controller.admin.video.vo.CameraPresetSaveReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.video.CameraPresetDO;
import cn.iocoder.yudao.module.iot.dal.mysql.video.CameraPresetMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.iot.enums.ErrorCodeConstants.*;

/**
 * 摄像头预设点 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
@Slf4j
public class CameraPresetServiceImpl implements CameraPresetService {

    @Resource
    private CameraPresetMapper presetMapper;

    @Override
    public Long createPreset(CameraPresetSaveReqVO createReqVO) {
        // 校验预设点编号是否已存在
        if (presetMapper.existsByChannelIdAndPresetNo(createReqVO.getChannelId(), 
                createReqVO.getPresetNo(), null)) {
            throw exception(CAMERA_PRESET_NO_EXISTS);
        }

        // 插入
        CameraPresetDO preset = BeanUtils.toBean(createReqVO, CameraPresetDO.class);
        presetMapper.insert(preset);
        
        log.info("[createPreset] 创建预设点成功: channelId={}, presetNo={}, presetName={}", 
                preset.getChannelId(), preset.getPresetNo(), preset.getPresetName());
        
        return preset.getId();
    }

    @Override
    public void updatePreset(CameraPresetSaveReqVO updateReqVO) {
        // 校验存在
        validatePresetExists(updateReqVO.getId());
        
        // 校验预设点编号是否已被其他记录使用
        if (presetMapper.existsByChannelIdAndPresetNo(updateReqVO.getChannelId(), 
                updateReqVO.getPresetNo(), updateReqVO.getId())) {
            throw exception(CAMERA_PRESET_NO_EXISTS);
        }

        // 更新
        CameraPresetDO updateObj = BeanUtils.toBean(updateReqVO, CameraPresetDO.class);
        presetMapper.updateById(updateObj);
        
        log.info("[updatePreset] 更新预设点成功: id={}, presetNo={}, presetName={}", 
                updateObj.getId(), updateObj.getPresetNo(), updateObj.getPresetName());
    }

    @Override
    public void deletePreset(Long id) {
        // 校验存在
        validatePresetExists(id);
        
        // 删除
        presetMapper.deleteById(id);
        
        log.info("[deletePreset] 删除预设点成功: id={}", id);
    }

    private void validatePresetExists(Long id) {
        if (presetMapper.selectById(id) == null) {
            throw exception(CAMERA_PRESET_NOT_EXISTS);
        }
    }

    @Override
    public CameraPresetDO getPreset(Long id) {
        return presetMapper.selectById(id);
    }

    @Override
    public List<CameraPresetDO> getPresetListByChannelId(Long channelId) {
        return presetMapper.selectListByChannelId(channelId);
    }

}
