package cn.iocoder.yudao.module.iot.service.patrolplan;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import cn.iocoder.yudao.module.iot.controller.admin.patrolplan.vo.record.PatrolRecordPageReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.patrolplan.IotVideoPatrolRecordDO;
import cn.iocoder.yudao.module.iot.dal.mysql.patrolplan.IotVideoPatrolRecordMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.iot.enums.ErrorCodeConstants.*;

/**
 * 轮巡记录 Service 实现类
 *
 * @author 长辉信息
 */
@Service
@Validated
public class PatrolRecordServiceImpl implements PatrolRecordService {

    @Resource
    private IotVideoPatrolRecordMapper patrolRecordMapper;

    @Override
    public IotVideoPatrolRecordDO getPatrolRecord(Long id) {
        // 自动过滤租户
        return patrolRecordMapper.selectById(id);
    }

    @Override
    public PageResult<IotVideoPatrolRecordDO> getPatrolRecordPage(PatrolRecordPageReqVO pageReqVO) {
        // 自动过滤租户
        return patrolRecordMapper.selectPage(pageReqVO);
    }

    @Override
    public void markAsHandled(Long id, String remark) {
        // 校验存在
        IotVideoPatrolRecordDO record = validatePatrolRecordExists(id);
        
        // 校验是否已处理
        if (Boolean.TRUE.equals(record.getHandled())) {
            throw exception(VIDEO_PATROL_RECORD_ALREADY_HANDLED);
        }
        
        // 更新处理状态
        IotVideoPatrolRecordDO updateObj = IotVideoPatrolRecordDO.builder()
                .id(id)
                .handled(true)
                .handler(SecurityFrameworkUtils.getLoginUserNickname())
                .handleTime(LocalDateTime.now())
                .handleRemark(remark)
                .build();
        
        patrolRecordMapper.updateById(updateObj);
    }

    // ==================== 私有方法 ====================

    /**
     * 校验轮巡记录是否存在
     */
    private IotVideoPatrolRecordDO validatePatrolRecordExists(Long id) {
        IotVideoPatrolRecordDO record = patrolRecordMapper.selectById(id);
        if (record == null) {
            throw exception(VIDEO_PATROL_RECORD_NOT_EXISTS);
        }
        return record;
    }

}
