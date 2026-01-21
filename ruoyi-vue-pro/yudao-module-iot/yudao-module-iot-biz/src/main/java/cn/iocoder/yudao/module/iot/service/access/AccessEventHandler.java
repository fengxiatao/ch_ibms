package cn.iocoder.yudao.module.iot.service.access;

import cn.iocoder.yudao.module.iot.core.gateway.dto.AccessControlEventMessage;

/**
 * 门禁事件处理器接口
 * 
 * <p>负责处理从 Gateway 模块接收的门禁事件消息，包括：
 * <ul>
 *   <li>关联人员信息 - 通过 personId 或 cardNo 查询人员 (Requirements: 3.2)</li>
 *   <li>保存抓拍图片 - 将 Base64 图片解码并存储 (Requirements: 3.3)</li>
 *   <li>保存事件到数据库 (Requirements: 3.4)</li>
 *   <li>推送事件到前端 (Requirements: 3.5)</li>
 * </ul>
 * </p>
 *
 * @author 芋道源码
 * @see AccessControlEventMessage
 * Requirements: 3.2, 3.3, 3.4, 3.5
 */
public interface AccessEventHandler {

    /**
     * 处理门禁事件
     * 
     * <p>处理流程：
     * <ol>
     *   <li>根据 personId 或 cardNo 关联人员信息 (Requirements: 3.2)</li>
     *   <li>保存抓拍图片到文件系统 (Requirements: 3.3)</li>
     *   <li>保存事件记录到数据库 (Requirements: 3.4)</li>
     *   <li>通过 WebSocket 推送事件到前端 (Requirements: 3.5)</li>
     * </ol>
     * </p>
     *
     * @param event 门禁事件消息
     * Requirements: 3.2, 3.3, 3.4, 3.5
     */
    void handleEvent(AccessControlEventMessage event);

}
