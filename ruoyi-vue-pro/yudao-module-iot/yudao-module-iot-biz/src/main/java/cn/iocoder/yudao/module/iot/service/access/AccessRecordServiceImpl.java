package cn.iocoder.yudao.module.iot.service.access;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.iot.controller.admin.access.vo.record.*;
import cn.iocoder.yudao.module.iot.dal.dataobject.access.AccessRecordDO;
import cn.iocoder.yudao.module.iot.dal.mysql.access.AccessRecordMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import jakarta.annotation.Resource;

/**
 * 门禁记录 Service 实现类
 *
 * @author 智能化系统
 */
@Service
@Validated
@Slf4j
public class AccessRecordServiceImpl implements AccessRecordService {

    @Resource
    private AccessRecordMapper accessRecordMapper;

    @Override
    public PageResult<AccessRecordRespVO> getAccessRecordPage(AccessRecordPageReqVO pageReqVO) {
        PageResult<AccessRecordDO> pageResult = accessRecordMapper.selectPage(pageReqVO);
        return BeanUtils.toBean(pageResult, AccessRecordRespVO.class);
    }

    @Override
    public AccessRecordRespVO getAccessRecord(Long id) {
        AccessRecordDO record = accessRecordMapper.selectById(id);
        return BeanUtils.toBean(record, AccessRecordRespVO.class);
    }

    @Override
    public java.util.List<java.util.Map<String, Object>> getAccessMethodStatistics(
            java.time.LocalDateTime startTime, 
            java.time.LocalDateTime endTime) {
        return accessRecordMapper.selectAccessMethodStatistics(startTime, endTime);
    }

    @Override
    public java.util.List<java.util.Map<String, Object>> getHourlyTrafficStatistics(
            java.time.LocalDate date) {
        // 默认查询今天
        if (date == null) {
            date = java.time.LocalDate.now();
        }
        
        java.time.LocalDateTime startTime = date.atStartOfDay();
        java.time.LocalDateTime endTime = date.plusDays(1).atStartOfDay();
        
        return accessRecordMapper.selectHourlyTrafficStatistics(startTime, endTime);
    }

}

