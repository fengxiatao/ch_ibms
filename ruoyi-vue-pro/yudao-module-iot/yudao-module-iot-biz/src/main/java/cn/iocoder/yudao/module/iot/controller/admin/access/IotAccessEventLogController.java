package cn.iocoder.yudao.module.iot.controller.admin.access;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.iot.controller.admin.access.vo.event.IotAccessEventLogPageReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.access.vo.event.IotAccessEventLogRespVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.access.IotAccessEventLogDO;
import cn.iocoder.yudao.module.iot.service.access.IotAccessEventLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

/**
 * 门禁事件日志 Controller
 */
@Tag(name = "管理后台 - 门禁事件日志")
@RestController
@RequestMapping("/iot/access/event")
@Validated
public class IotAccessEventLogController {

    @Resource
    private IotAccessEventLogService eventLogService;

    @GetMapping("/page")
    @Operation(summary = "获取事件日志分页")
    @PreAuthorize("@ss.hasPermission('iot:access-event:query')")
    public CommonResult<PageResult<IotAccessEventLogRespVO>> getEventLogPage(@Valid IotAccessEventLogPageReqVO pageReqVO) {
        PageResult<IotAccessEventLogDO> pageResult = eventLogService.getEventLogPageEx(
                pageReqVO.getDeviceId(),
                pageReqVO.getChannelId(),
                pageReqVO.getEventType(),  // 直接使用字符串事件类型
                pageReqVO.getEventCategory(),
                pageReqVO.getPersonId(),
                pageReqVO.getVerifyResult(),
                pageReqVO.getStartTime(),
                pageReqVO.getEndTime(),
                pageReqVO.getPageNo(),
                pageReqVO.getPageSize()
        );
        return success(convertPageResult(pageResult));
    }

    @GetMapping("/recent")
    @Operation(summary = "获取最近事件")
    @Parameter(name = "limit", description = "数量限制", example = "10")
    @PreAuthorize("@ss.hasPermission('iot:access-event:query')")
    public CommonResult<List<IotAccessEventLogRespVO>> getRecentEvents(
            @RequestParam(value = "limit", defaultValue = "10") Integer limit) {
        List<IotAccessEventLogDO> events = eventLogService.getRecentEvents(limit);
        return success(convertToVOList(events));
    }

    @GetMapping("/today-statistics")
    @Operation(summary = "获取今日事件统计")
    @PreAuthorize("@ss.hasPermission('iot:access-event:query')")
    public CommonResult<IotAccessEventLogService.EventStatistics> getTodayStatistics() {
        return success(eventLogService.getTodayStatistics());
    }

    @GetMapping("/get")
    @Operation(summary = "获取事件详情")
    @Parameter(name = "id", description = "事件ID", required = true)
    @PreAuthorize("@ss.hasPermission('iot:access-event:query')")
    public CommonResult<IotAccessEventLogRespVO> getEventLog(@RequestParam("id") Long id) {
        IotAccessEventLogDO event = eventLogService.getEventLog(id);
        return success(convertToVO(event));
    }

    // ========== 转换方法 ==========

    private IotAccessEventLogRespVO convertToVO(IotAccessEventLogDO event) {
        if (event == null) {
            return null;
        }
        IotAccessEventLogRespVO vo = new IotAccessEventLogRespVO();
        vo.setId(event.getId());
        vo.setDeviceId(event.getDeviceId());
        vo.setDeviceName(event.getDeviceName());
        vo.setChannelId(event.getChannelId());
        vo.setChannelName(event.getChannelName());
        vo.setEventType(event.getEventType());
        vo.setPersonId(event.getPersonId());
        vo.setPersonName(event.getPersonName());
        vo.setPersonCode(event.getPersonCode());
        vo.setCardNo(event.getCardNo());
        vo.setVerifyMode(event.getVerifyMode());
        vo.setVerifyResult(event.getVerifyResult());
        vo.setVerifyResultDesc(event.getVerifyResultDesc());
        vo.setFailReason(event.getFailReason());
        vo.setCaptureUrl(event.getCaptureUrl() != null ? event.getCaptureUrl() : event.getSnapshotUrl());
        vo.setTemperature(event.getTemperature());
        vo.setMaskStatus(event.getMaskStatus());
        vo.setEventTime(event.getEventTime());
        vo.setCreateTime(event.getCreateTime());
        // 设置事件类型名称
        vo.setEventTypeName(getEventTypeName(event.getEventType()));
        return vo;
    }

    private List<IotAccessEventLogRespVO> convertToVOList(List<IotAccessEventLogDO> events) {
        List<IotAccessEventLogRespVO> result = new ArrayList<>();
        if (events != null) {
            for (IotAccessEventLogDO event : events) {
                result.add(convertToVO(event));
            }
        }
        return result;
    }

    private PageResult<IotAccessEventLogRespVO> convertPageResult(PageResult<IotAccessEventLogDO> pageResult) {
        List<IotAccessEventLogRespVO> list = new ArrayList<>();
        if (pageResult.getList() != null) {
            for (IotAccessEventLogDO event : pageResult.getList()) {
                list.add(convertToVO(event));
            }
        }
        return new PageResult<>(list, pageResult.getTotal());
    }

    private String getEventTypeName(String eventType) {
        if (eventType == null) {
            return "未知";
        }
        // 使用新的事件类型代码映射（与前端 accessEventTypes.ts 保持一致）
        return switch (eventType) {
            // 正常开门事件
            case "CARD_SWIPE" -> "刷卡开门";
            case "FINGERPRINT" -> "指纹开门";
            case "FACE_RECOGNIZE" -> "人脸开门";
            case "PASSWORD" -> "密码开门";
            case "QRCODE" -> "二维码开门";
            case "REMOTE_OPEN" -> "远程开门";
            case "BUTTON_OPEN" -> "按钮开门";
            case "MULTI_PERSON_OPEN" -> "多人组合开门";
            case "ACCESS_STATUS" -> "门禁状态事件";
            case "FINGERPRINT_CAPTURE" -> "指纹采集事件";
            // 报警事件
            case "DOOR_NOT_CLOSED" -> "门未关报警";
            case "BREAK_IN" -> "闯入报警";
            case "REPEAT_ENTER" -> "反复进入报警";
            case "MALICIOUS_OPEN" -> "恶意开门报警";
            case "DURESS" -> "胁迫报警";
            case "TAMPER_ALARM" -> "防拆报警";
            case "LOCAL_ALARM" -> "本地报警";
            // 兼容旧格式
            case "swipe_card" -> "刷卡";
            case "face_recognize" -> "人脸识别";
            case "fingerprint" -> "指纹识别";
            case "password" -> "密码验证";
            case "qrcode" -> "二维码";
            case "alarm" -> "报警";
            case "door_open" -> "开门";
            case "door_close" -> "关门";
            default -> eventType;
        };
    }

}
