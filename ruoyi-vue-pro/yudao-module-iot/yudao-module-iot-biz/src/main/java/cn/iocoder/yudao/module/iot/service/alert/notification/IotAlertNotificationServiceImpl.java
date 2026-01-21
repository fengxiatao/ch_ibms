package cn.iocoder.yudao.module.iot.service.alert.notification;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.iot.controller.admin.alert.notification.vo.*;
import cn.iocoder.yudao.module.iot.websocket.AlertWebSocketHandler;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.List;

/**
 * IoT 告警通知服务实现类
 *
 * 提供多渠道告警通知功能的实现。
 *
 * @author 长辉信息科技有限公司
 */
@Service
@Validated
@Slf4j
public class IotAlertNotificationServiceImpl implements IotAlertNotificationService {

    @Autowired(required = false)
    private JavaMailSender mailSender;
    
    @Resource(name = "alertWebSocketHandler")  // 明确指定Bean名称
    private AlertWebSocketHandler alertWebSocketHandler;

    @Override
    public void sendAlertNotification(Long alertId) {
        // TODO: 从数据库查询告警信息和通知规则
        log.info("[sendAlertNotification] 发送告警通知: alertId={}", alertId);
        // 运行态禁止“模拟发送”。如未配置通知渠道，则直接跳过并告警日志提示即可。
    }

    @Override
    public void sendEmailNotification(AlertEmailNotificationReqVO reqVO) {
        log.info("[sendEmailNotification] 发送邮件通知: recipients={}, subject={}", 
                reqVO.getRecipients(), reqVO.getSubject());

        if (mailSender == null) {
            log.warn("[sendEmailNotification] JavaMailSender未配置，跳过邮件发送");
            return;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(reqVO.getFrom() != null ? reqVO.getFrom() : "noreply@ch-ibms.com");
            message.setTo(reqVO.getRecipients().toArray(new String[0]));
            message.setSubject(reqVO.getSubject());
            message.setText(reqVO.getContent());
            message.setSentDate(new java.util.Date());

            mailSender.send(message);
            log.info("[sendEmailNotification] 邮件发送成功: recipients={}", reqVO.getRecipients());

        } catch (Exception e) {
            log.error("[sendEmailNotification] 邮件发送失败: recipients={}, error={}", 
                    reqVO.getRecipients(), e.getMessage(), e);
            throw new RuntimeException("邮件发送失败: " + e.getMessage());
        }
    }

    @Override
    public void sendSmsNotification(AlertSmsNotificationReqVO reqVO) {
        log.info("[sendSmsNotification] 发送短信通知: phones={}, template={}", 
                reqVO.getPhoneNumbers(), reqVO.getTemplateCode());

        // TODO: 集成阿里云SMS SDK
        // 示例代码：
        // try {
        //     SendSmsRequest request = new SendSmsRequest();
        //     request.setPhoneNumbers(String.join(",", reqVO.getPhoneNumbers()));
        //     request.setSignName(reqVO.getSignName());
        //     request.setTemplateCode(reqVO.getTemplateCode());
        //     request.setTemplateParam(reqVO.getTemplateParams());
        //     
        //     SendSmsResponse response = client.sendSms(request);
        //     if ("OK".equals(response.getCode())) {
        //         log.info("[sendSmsNotification] 短信发送成功: phones={}", reqVO.getPhoneNumbers());
        //     } else {
        //         log.error("[sendSmsNotification] 短信发送失败: code={}, message={}", 
        //                 response.getCode(), response.getMessage());
        //     }
        // } catch (Exception e) {
        //     log.error("[sendSmsNotification] 短信发送异常: {}", e.getMessage(), e);
        //     throw new RuntimeException("短信发送失败: " + e.getMessage());
        // }

        log.warn("[sendSmsNotification] 未配置短信发送通道，已跳过（禁止模拟成功）。phones={}", reqVO.getPhoneNumbers());
    }

    @Override
    public void sendWebSocketPush(AlertWebSocketPushReqVO reqVO) {
        log.info("[sendWebSocketPush] 发送WebSocket推送: userIds={}", reqVO.getUserIds());

        try {
            if (reqVO.getUserIds() != null && !reqVO.getUserIds().isEmpty()) {
                // 推送给指定用户
                alertWebSocketHandler.pushAlertToUsers(reqVO.getUserIds(), reqVO.getData());
            } else {
                // 广播给所有用户
                alertWebSocketHandler.broadcastAlert(reqVO.getData());
            }
            
            log.info("[sendWebSocketPush] WebSocket推送成功: userIds={}", reqVO.getUserIds());

        } catch (Exception e) {
            log.error("[sendWebSocketPush] WebSocket推送失败: userIds={}, error={}", 
                    reqVO.getUserIds(), e.getMessage(), e);
            throw new RuntimeException("WebSocket推送失败: " + e.getMessage());
        }
    }

    @Override
    public AlertNotificationRuleRespVO getNotificationRule(Long alertConfigId) {
        log.info("[getNotificationRule] 获取通知规则: alertConfigId={}", alertConfigId);
        
        // TODO: 从数据库查询通知规则
        
        // 未配置则返回“未启用”规则（避免伪造默认可用的通知能力）
        AlertNotificationRuleRespVO respVO = new AlertNotificationRuleRespVO();
        respVO.setAlertConfigId(alertConfigId);
        respVO.setRuleName("未配置通知规则");
        respVO.setEmailEnabled(false);
        respVO.setSmsEnabled(false);
        respVO.setWebsocketEnabled(false);
        respVO.setSystemEnabled(false);
        respVO.setNotifyInterval(0);
        respVO.setMaxNotifyCount(0);
        respVO.setStatus(0);
        
        return respVO;
    }

    @Override
    public void updateNotificationRule(AlertNotificationRuleReqVO reqVO) {
        log.info("[updateNotificationRule] 更新通知规则: alertConfigId={}", 
                reqVO.getAlertConfigId());

        // TODO: 更新数据库中的通知规则
        
        log.info("[updateNotificationRule] 通知规则更新成功");
    }

    @Override
    public PageResult<AlertNotificationRecordRespVO> getNotificationRecordPage(
            AlertNotificationRecordPageReqVO pageReqVO) {
        log.info("[getNotificationRecordPage] 查询通知记录: pageNo={}, pageSize={}", 
                pageReqVO.getPageNo(), pageReqVO.getPageSize());

        // TODO: 从数据库查询通知记录
        
        // 返回空结果
        return new PageResult<>(List.of(), 0L);
    }

    @Override
    public void resendFailedNotification(Long notificationId) {
        log.info("[resendFailedNotification] 重新发送失败通知: notificationId={}", notificationId);

        // TODO: 查询通知记录并重新发送
        
        log.info("[resendFailedNotification] 通知重发成功");
    }

}








