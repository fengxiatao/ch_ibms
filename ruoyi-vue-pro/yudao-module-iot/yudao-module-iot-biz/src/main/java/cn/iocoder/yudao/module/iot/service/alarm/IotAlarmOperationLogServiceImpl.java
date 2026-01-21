package cn.iocoder.yudao.module.iot.service.alarm;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.security.core.LoginUser;
import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import cn.iocoder.yudao.module.iot.controller.admin.alarm.vo.operationlog.IotAlarmOperationLogPageReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.alarm.vo.operationlog.IotAlarmOperationLogRespVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.alarm.IotAlarmHostDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.alarm.IotAlarmOperationLogDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.alarm.IotAlarmPartitionDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.alarm.IotAlarmZoneDO;
import cn.iocoder.yudao.module.iot.dal.mysql.alarm.IotAlarmHostMapper;
import cn.iocoder.yudao.module.iot.dal.mysql.alarm.IotAlarmOperationLogMapper;
import cn.iocoder.yudao.module.iot.dal.mysql.alarm.IotAlarmPartitionMapper;
import cn.iocoder.yudao.module.iot.dal.mysql.alarm.IotAlarmZoneMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 报警主机操作记录 Service 实现类
 *
 * @author 长辉信息科技有限公司
 */
@Service
@Validated
@Slf4j
public class IotAlarmOperationLogServiceImpl implements IotAlarmOperationLogService {

    @Resource
    private IotAlarmOperationLogMapper operationLogMapper;

    @Resource
    private IotAlarmHostMapper alarmHostMapper;

    @Resource
    private IotAlarmPartitionMapper alarmPartitionMapper;

    @Resource
    private IotAlarmZoneMapper alarmZoneMapper;

    @Override
    public PageResult<IotAlarmOperationLogRespVO> getOperationLogPage(IotAlarmOperationLogPageReqVO pageReqVO) {
        // 1. 查询操作日志分页
        PageResult<IotAlarmOperationLogDO> pageResult = operationLogMapper.selectPage(pageReqVO);
        if (pageResult.getList().isEmpty()) {
            return PageResult.empty(pageResult.getTotal());
        }

        // 2. 收集所有需要查询的ID
        List<Long> hostIds = pageResult.getList().stream()
                .map(IotAlarmOperationLogDO::getHostId)
                .filter(id -> id != null)
                .distinct()
                .collect(Collectors.toList());

        List<Long> partitionIds = pageResult.getList().stream()
                .map(IotAlarmOperationLogDO::getPartitionId)
                .filter(id -> id != null)
                .distinct()
                .collect(Collectors.toList());

        List<Long> zoneIds = pageResult.getList().stream()
                .map(IotAlarmOperationLogDO::getZoneId)
                .filter(id -> id != null)
                .distinct()
                .collect(Collectors.toList());

        // 3. 批量查询关联数据
        Map<Long, String> hostNameMap = hostIds.isEmpty() ? Map.of() :
                alarmHostMapper.selectBatchIds(hostIds).stream()
                        .collect(Collectors.toMap(IotAlarmHostDO::getId, IotAlarmHostDO::getHostName));

        Map<Long, String> partitionNameMap = partitionIds.isEmpty() ? Map.of() :
                alarmPartitionMapper.selectBatchIds(partitionIds).stream()
                        .collect(Collectors.toMap(IotAlarmPartitionDO::getId, IotAlarmPartitionDO::getPartitionName));

        Map<Long, String> zoneNameMap = zoneIds.isEmpty() ? Map.of() :
                alarmZoneMapper.selectBatchIds(zoneIds).stream()
                        .collect(Collectors.toMap(IotAlarmZoneDO::getId, IotAlarmZoneDO::getZoneName));

        // 4. 转换为VO并填充关联数据
        List<IotAlarmOperationLogRespVO> voList = pageResult.getList().stream()
                .map(log -> {
                    IotAlarmOperationLogRespVO vo = new IotAlarmOperationLogRespVO();
                    vo.setId(log.getId());
                    vo.setHostId(log.getHostId());
                    vo.setHostName(log.getHostId() != null ? hostNameMap.get(log.getHostId()) : null);
                    vo.setPartitionId(log.getPartitionId());
                    vo.setPartitionName(log.getPartitionId() != null ? partitionNameMap.get(log.getPartitionId()) : null);
                    vo.setZoneId(log.getZoneId());
                    vo.setZoneName(log.getZoneId() != null ? zoneNameMap.get(log.getZoneId()) : null);
                    vo.setOperationType(log.getOperationType());
                    vo.setOperationTime(log.getOperationTime());
                    vo.setOperatorId(log.getOperatorId());
                    vo.setOperatorName(log.getOperatorName());
                    vo.setResult(log.getResult());
                    vo.setErrorMessage(log.getErrorMessage());
                    vo.setRequestId(log.getRequestId());
                    vo.setCreateTime(log.getCreateTime());
                    return vo;
                })
                .collect(Collectors.toList());

        return new PageResult<>(voList, pageResult.getTotal());
    }

    @Override
    public void logOperation(Long hostId, Long partitionId, Long zoneId, String operationType,
                            String operatorName, String result, String errorMessage, String requestId) {
        try {
            // 获取当前登录用户信息
            Long userId = SecurityFrameworkUtils.getLoginUserId();
            
            // 如果没有传入操作人姓名，尝试从 LoginUser 获取
            if (operatorName == null && userId != null) {
                LoginUser loginUser = SecurityFrameworkUtils.getLoginUser();
                if (loginUser != null) {
                    // 使用用户ID作为操作人名称（如需昵称可后续扩展）
                    operatorName = "用户" + userId;
                }
            }

            // 如果是防区操作且未指定分区ID，自动设置为1分区
            if (zoneId != null && partitionId == null) {
                partitionId = 1L;
            }

            // 构建操作日志对象
            IotAlarmOperationLogDO operationLog = IotAlarmOperationLogDO.builder()
                    .hostId(hostId)
                    .partitionId(partitionId)
                    .zoneId(zoneId)
                    .operationType(operationType)
                    .operationTime(LocalDateTime.now())
                    .operatorId(userId)
                    .operatorName(operatorName != null ? operatorName : "系统")
                    .result(result != null ? result : "SUCCESS")
                    .errorMessage(errorMessage)
                    .requestId(requestId)
                    .build();

            // 插入数据库
            operationLogMapper.insert(operationLog);

            log.info("[logOperation][记录操作日志] hostId={}, operationType={}, result={}",
                    hostId, operationType, result);
        } catch (Exception e) {
            log.error("[logOperation][记录操作日志失败] hostId={}, operationType={}", 
                    hostId, operationType, e);
        }
    }

}
