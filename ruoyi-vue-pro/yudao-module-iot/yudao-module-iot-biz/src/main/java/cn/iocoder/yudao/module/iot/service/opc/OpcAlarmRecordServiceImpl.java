package cn.iocoder.yudao.module.iot.service.opc;

import cn.iocoder.yudao.module.iot.core.mq.message.opc.OpcAlarmEvent;
import cn.iocoder.yudao.module.iot.dal.dataobject.opc.OpcAlarmRecordDO;
import cn.iocoder.yudao.module.iot.dal.tdengine.OpcAlarmRecordMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * OPC报警记录 Service 实现类
 * 
 * 注意：已从 TDEngine 迁移到 MySQL
 *
 * @author 长辉信息科技有限公司
 */
@Service
@Slf4j
public class OpcAlarmRecordServiceImpl implements OpcAlarmRecordService {

    @Resource
    private OpcAlarmRecordMapper opcAlarmRecordMapper;

    @Override
    public void initOpcAlarmStable() {
        // MySQL版本：表已通过SQL脚本预先创建，此方法仅检查表是否存在
        try {
            if (cn.hutool.core.util.StrUtil.isNotEmpty(opcAlarmRecordMapper.showOpcAlarmStable())) {
                log.info("[initOpcAlarmStable][MySQL OPC报警记录表已存在]");
                return;
            }

            // MySQL版本：表不存在时仅记录警告，不尝试创建
            log.warn("[initOpcAlarmStable][MySQL OPC报警记录表不存在，请执行SQL脚本: sql/mysql/iot_device_message.sql]");

        } catch (Exception e) {
            log.error("[initOpcAlarmStable][检查MySQL OPC报警记录表失败]", e);
            throw new RuntimeException("检查MySQL OPC报警记录表失败", e);
        }
    }

    @Override
    public void saveAlarmRecord(OpcAlarmEvent event) {
        try {
            // 构建DO对象
            OpcAlarmRecordDO record = new OpcAlarmRecordDO();
            record.setAccount(event.getAccount());
            record.setEventCode(event.getEventCode());
            record.setArea(event.getArea());
            record.setPoint(event.getPoint());
            record.setSequence(event.getSequence());
            record.setEventDescription(event.getEventDescription());
            record.setLevel(event.getLevel());
            record.setType(event.getType());
            record.setReceiveTime(event.getReceiveTime());
            record.setRemoteAddress(event.getRemoteAddress());
            record.setRemotePort(event.getRemotePort());
            record.setRawMessage(event.getRawMessage());
            record.setDeviceId(event.getDeviceId());
            record.setDeviceName(event.getDeviceName());
            record.setAreaName(event.getAreaName());
            record.setPointName(event.getPointName());
            record.setLocation(event.getLocation());
            record.setHandled(event.getHandled() != null ? event.getHandled() : false);
            record.setHandleTime(event.getHandleTime());
            record.setHandleBy(event.getHandleBy());
            record.setHandleRemark(event.getHandleRemark());
            record.setCreateTime(LocalDateTime.now());

            // 插入MySQL
            opcAlarmRecordMapper.insertAlarmRecord(record);

            log.debug("[saveAlarmRecord][保存成功] 账号: {}, 事件: {}, 序列: {}",
                    event.getAccount(), event.getEventCode(), event.getSequence());

        } catch (Exception e) {
            log.error("[saveAlarmRecord][保存失败] 事件: {}", event, e);
            throw new RuntimeException("保存OPC报警记录失败", e);
        }
    }
}
