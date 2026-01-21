package cn.iocoder.yudao.module.iot.service.access;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.iot.controller.admin.access.vo.dispatch.*;
import cn.iocoder.yudao.module.iot.dal.dataobject.access.AccessDispatchDO;
import cn.iocoder.yudao.module.iot.dal.mysql.access.AccessDispatchMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import jakarta.annotation.Resource;

/**
 * 门禁下发记录 Service 实现类
 *
 * @author 智能化系统
 */
@Service
@Validated
@Slf4j
public class AccessDispatchServiceImpl implements AccessDispatchService {

    @Resource
    private AccessDispatchMapper accessDispatchMapper;

    @Override
    public PageResult<AccessDispatchRespVO> getAccessDispatchPage(AccessDispatchPageReqVO pageReqVO) {
        PageResult<AccessDispatchDO> pageResult = accessDispatchMapper.selectPage(pageReqVO);
        return BeanUtils.toBean(pageResult, AccessDispatchRespVO.class);
    }

    @Override
    public AccessDispatchRespVO getAccessDispatch(Long id) {
        AccessDispatchDO dispatch = accessDispatchMapper.selectById(id);
        return BeanUtils.toBean(dispatch, AccessDispatchRespVO.class);
    }

}


























