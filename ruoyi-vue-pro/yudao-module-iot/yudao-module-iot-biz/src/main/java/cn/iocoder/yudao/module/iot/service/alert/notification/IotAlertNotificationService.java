package cn.iocoder.yudao.module.iot.service.alert.notification;

import cn.iocoder.yudao.module.iot.controller.admin.alert.notification.vo.*;
import cn.iocoder.yudao.framework.common.pojo.PageResult;

/**
 * IoT 告警通知服务接口
 *
 * 提供多渠道告警通知功能：
 * 1. 邮件通知
 * 2. 短信通知
 * 3. WebSocket实时推送
 * 4. 系统内消息
 *
 * @author 长辉信息科技有限公司
 */
public interface IotAlertNotificationService {

    /**
     * 发送告警通知
     *
     * 根据告警规则配置，通过多种渠道发送通知
     *
     * @param alertId 告警ID
     */
    void sendAlertNotification(Long alertId);

    /**
     * 发送邮件通知
     *
     * @param reqVO 邮件通知请求
     */
    void sendEmailNotification(AlertEmailNotificationReqVO reqVO);

    /**
     * 发送短信通知
     *
     * @param reqVO 短信通知请求
     */
    void sendSmsNotification(AlertSmsNotificationReqVO reqVO);

    /**
     * 发送WebSocket推送
     *
     * @param reqVO WebSocket推送请求
     */
    void sendWebSocketPush(AlertWebSocketPushReqVO reqVO);

    /**
     * 获取告警通知规则
     *
     * @param alertConfigId 告警配置ID
     * @return 通知规则
     */
    AlertNotificationRuleRespVO getNotificationRule(Long alertConfigId);

    /**
     * 更新告警通知规则
     *
     * @param reqVO 通知规则请求
     */
    void updateNotificationRule(AlertNotificationRuleReqVO reqVO);

    /**
     * 获取告警通知记录分页
     *
     * @param pageReqVO 分页查询请求
     * @return 通知记录分页
     */
    PageResult<AlertNotificationRecordRespVO> getNotificationRecordPage(
            AlertNotificationRecordPageReqVO pageReqVO);

    /**
     * 重新发送失败的通知
     *
     * @param notificationId 通知记录ID
     */
    void resendFailedNotification(Long notificationId);
}












