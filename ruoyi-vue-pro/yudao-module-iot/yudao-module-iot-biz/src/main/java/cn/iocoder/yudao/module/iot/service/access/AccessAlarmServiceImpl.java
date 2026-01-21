package cn.iocoder.yudao.module.iot.service.access;

import cn.hutool.core.date.LocalDateTimeUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.iot.controller.admin.access.vo.alarm.*;
import cn.iocoder.yudao.module.iot.dal.dataobject.access.AccessAlarmDO;
import cn.iocoder.yudao.module.iot.dal.mysql.access.AccessAlarmMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import jakarta.annotation.Resource;
import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.iot.enums.ErrorCodeConstants.*;

/**
 * 门禁告警 Service 实现类
 *
 * @author 智能化系统
 */
@Service
@Validated
@Slf4j
public class AccessAlarmServiceImpl implements AccessAlarmService {

    @Resource
    private AccessAlarmMapper accessAlarmMapper;

    @Override
    public PageResult<AccessAlarmRespVO> getAccessAlarmPage(AccessAlarmPageReqVO pageReqVO) {
        PageResult<AccessAlarmDO> pageResult = accessAlarmMapper.selectPage(pageReqVO);
        return BeanUtils.toBean(pageResult, AccessAlarmRespVO.class);
    }

    @Override
    public AccessAlarmRespVO getAccessAlarm(Long id) {
        AccessAlarmDO alarm = accessAlarmMapper.selectById(id);
        return BeanUtils.toBean(alarm, AccessAlarmRespVO.class);
    }

    @Override
    public void handleAccessAlarm(AccessAlarmHandleReqVO handleReqVO) {
        // 校验存在
        AccessAlarmDO alarm = accessAlarmMapper.selectById(handleReqVO.getId());
        if (alarm == null) {
            throw exception(ACCESS_ALARM_NOT_EXISTS);
        }

        // 校验是否已处理（状态为2）
        if (alarm.getHandleStatus() != null && alarm.getHandleStatus() == 2) {
            throw exception(ACCESS_ALARM_ALREADY_HANDLED);
        }

        // 更新处理状态
        AccessAlarmDO updateObj = new AccessAlarmDO();
        updateObj.setId(handleReqVO.getId());
        updateObj.setHandleStatus(handleReqVO.getHandleStatus());
        updateObj.setHandleRemark(handleReqVO.getHandleRemark());
        updateObj.setHandleTime(LocalDateTime.now());
        // TODO: 设置处理人（从当前登录用户获取）

        accessAlarmMapper.updateById(updateObj);
    }

    @Override
    public java.util.List<java.util.Map<String, Object>> getAlarmTypeStatistics(
            java.time.LocalDateTime startTime, 
            java.time.LocalDateTime endTime) {
        return accessAlarmMapper.selectAlarmTypeStatistics(startTime, endTime);
    }

}

