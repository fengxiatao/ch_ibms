package cn.iocoder.yudao.module.iot.service.video;

import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.iot.controller.admin.video.vo.CameraPresetSaveReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.video.CameraPresetDO;
import cn.iocoder.yudao.module.iot.dal.mysql.video.CameraCruisePointMapper;
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

    private static final String LOG_PREFIX = "[CameraPreset]";

    @Resource
    private CameraPresetMapper presetMapper;

    @Resource
    private CameraCruisePointMapper cruisePointMapper;

    @Override
    public Long createPreset(CameraPresetSaveReqVO createReqVO) {
        // 校验预设点编号是否已存在（排除软删除的记录）
        if (presetMapper.existsByChannelIdAndPresetNo(createReqVO.getChannelId(), 
                createReqVO.getPresetNo(), null)) {
            throw exception(CAMERA_PRESET_NO_EXISTS);
        }
        
        // 清理可能存在的软删除记录（防止唯一索引冲突）
        // 由于唯一索引不包含 deleted 字段，如果存在同一 channel_id + preset_no 的软删除记录，
        // 插入时会发生冲突。这里先物理删除这些软删除记录。
        int deletedCount = presetMapper.physicalDeleteSoftDeleted(createReqVO.getChannelId(), createReqVO.getPresetNo());
        if (deletedCount > 0) {
            log.info("{} 清理软删除记录: channelId={}, presetNo={}, count={}", 
                    LOG_PREFIX, createReqVO.getChannelId(), createReqVO.getPresetNo(), deletedCount);
        }

        // 插入数据库
        CameraPresetDO preset = BeanUtils.toBean(createReqVO, CameraPresetDO.class);
        presetMapper.insert(preset);
        
        log.info("{} 创建预设点成功: channelId={}, presetNo={}, presetName={}", 
                LOG_PREFIX, preset.getChannelId(), preset.getPresetNo(), preset.getPresetName());
        
        // 注意：不自动同步到设备。预设点需要先在前端控制摄像头到指定位置，
        // 然后通过 "设置预设点" 操作将当前位置保存到设备。
        // 这里只是在数据库中记录预设点信息。
        
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

        // 更新数据库
        CameraPresetDO updateObj = BeanUtils.toBean(updateReqVO, CameraPresetDO.class);
        presetMapper.updateById(updateObj);
        
        log.info("{} 更新预设点成功: id={}, presetNo={}, presetName={}", 
                LOG_PREFIX, updateObj.getId(), updateObj.getPresetNo(), updateObj.getPresetName());
        
        // 注意：只更新数据库记录，不自动同步到设备。
        // 如果需要更新设备上的预设点位置，用户需要在前端重新"设置预设点"。
    }

    @Override
    public void deletePreset(Long id) {
        // 校验存在
        CameraPresetDO preset = validatePresetExists(id);
        
        // 检查预设点是否被巡航路线使用
        if (cruisePointMapper.existsByPresetId(id)) {
            throw exception(CAMERA_PRESET_USED_BY_CRUISE);
        }
        
        // 物理删除数据库记录（预设点数据不需要保留删除历史）
        // 注意：不自动从设备删除预设点，因为可能会影响其他预设点
        // 用户可以通过前端的 "删除预设点" 功能手动从设备删除
        presetMapper.delete(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<CameraPresetDO>()
                .eq(CameraPresetDO::getId, id));
        
        log.info("{} 删除预设点成功(物理删除): id={}, presetNo={}", LOG_PREFIX, id, preset.getPresetNo());
    }

    private CameraPresetDO validatePresetExists(Long id) {
        CameraPresetDO preset = presetMapper.selectById(id);
        if (preset == null) {
            throw exception(CAMERA_PRESET_NOT_EXISTS);
        }
        return preset;
    }

    @Override
    public CameraPresetDO getPreset(Long id) {
        return presetMapper.selectById(id);
    }

    @Override
    public List<CameraPresetDO> getPresetListByChannelId(Long channelId) {
        return presetMapper.selectListByChannelId(channelId);
    }

    @Override
    public List<CameraPresetDO> getPresetListByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return java.util.Collections.emptyList();
        }
        return presetMapper.selectBatchIds(ids);
    }

}
