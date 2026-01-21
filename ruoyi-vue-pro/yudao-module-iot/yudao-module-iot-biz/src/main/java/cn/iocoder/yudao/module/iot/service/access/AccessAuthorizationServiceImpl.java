package cn.iocoder.yudao.module.iot.service.access;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.iot.controller.admin.access.vo.authorization.*;
import cn.iocoder.yudao.module.iot.dal.dataobject.access.AccessAuthorizationDO;
import cn.iocoder.yudao.module.iot.dal.mysql.access.AccessAuthorizationMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import jakarta.annotation.Resource;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.iot.enums.ErrorCodeConstants.*;

/**
 * 门禁授权 Service 实现类
 *
 * @author 智能化系统
 */
@Service
@Validated
@Slf4j
public class AccessAuthorizationServiceImpl implements AccessAuthorizationService {

    @Resource
    private AccessAuthorizationMapper accessAuthorizationMapper;

    @Override
    public Long createAccessAuthorization(AccessAuthorizationCreateReqVO createReqVO) {
        // 校验授权时间
        validateAuthorizationTime(createReqVO.getStartTime(), createReqVO.getEndTime());

        // 插入
        AccessAuthorizationDO authorization = BeanUtils.toBean(createReqVO, AccessAuthorizationDO.class);
        // ⚠️ 临时处理：authTargetId 映射到 personId（后续可扩展为支持组织等）
        authorization.setPersonId(createReqVO.getAuthTargetId());
        
        accessAuthorizationMapper.insert(authorization);
        
        // 返回
        return authorization.getId();
    }

    @Override
    public void updateAccessAuthorization(AccessAuthorizationUpdateReqVO updateReqVO) {
        // 校验存在
        validateAccessAuthorizationExists(updateReqVO.getId());
        
        // 校验授权时间
        validateAuthorizationTime(updateReqVO.getStartTime(), updateReqVO.getEndTime());

        // 更新
        AccessAuthorizationDO updateObj = BeanUtils.toBean(updateReqVO, AccessAuthorizationDO.class);
        // ⚠️ 临时处理：authTargetId 映射到 personId
        updateObj.setPersonId(updateReqVO.getAuthTargetId());
        
        accessAuthorizationMapper.updateById(updateObj);
    }

    @Override
    public void deleteAccessAuthorization(Long id) {
        // 校验存在
        validateAccessAuthorizationExists(id);
        
        // 删除
        accessAuthorizationMapper.deleteById(id);
    }

    @Override
    public AccessAuthorizationRespVO getAccessAuthorization(Long id) {
        AccessAuthorizationDO authorization = accessAuthorizationMapper.selectById(id);
        if (authorization == null) {
            return null;
        }
        
        AccessAuthorizationRespVO respVO = BeanUtils.toBean(authorization, AccessAuthorizationRespVO.class);
        // ⚠️ 临时处理：personId 映射到 authTargetId
        respVO.setAuthTargetId(authorization.getPersonId());
        
        // TODO: 填充 authTargetName 和 deviceName（需要关联查询）
        
        return respVO;
    }

    @Override
    public PageResult<AccessAuthorizationRespVO> getAccessAuthorizationPage(AccessAuthorizationPageReqVO pageReqVO) {
        PageResult<AccessAuthorizationDO> pageResult = accessAuthorizationMapper.selectPage(pageReqVO);
        
        return BeanUtils.toBean(pageResult, AccessAuthorizationRespVO.class, authorization -> {
            // ⚠️ 临时处理：personId 映射到 authTargetId
            authorization.setAuthTargetId(pageResult.getList().stream()
                    .filter(a -> a.getId().equals(authorization.getId()))
                    .findFirst()
                    .map(AccessAuthorizationDO::getPersonId)
                    .orElse(null));
            
            // TODO: 填充 authTargetName 和 deviceName
        });
    }

    @Override
    public void updateAccessAuthorizationStatus(Long id, Integer status) {
        // 校验存在
        validateAccessAuthorizationExists(id);
        
        // 更新状态
        AccessAuthorizationDO updateObj = new AccessAuthorizationDO();
        updateObj.setId(id);
        updateObj.setAuthStatus(status);
        
        accessAuthorizationMapper.updateById(updateObj);
    }

    // ==================== 私有方法 ====================

    private void validateAccessAuthorizationExists(Long id) {
        if (accessAuthorizationMapper.selectById(id) == null) {
            throw exception(ACCESS_AUTHORIZATION_NOT_EXISTS);
        }
    }

    private void validateAuthorizationTime(java.time.LocalDateTime startTime, java.time.LocalDateTime endTime) {
        if (startTime != null && endTime != null && startTime.isAfter(endTime)) {
            throw exception(ACCESS_AUTHORIZATION_TIME_INVALID);
        }
    }

}


























