package cn.iocoder.yudao.module.iot.controller.admin.visitor;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.iot.controller.admin.visitor.vo.auth.VisitorAuthDeviceRespVO;
import cn.iocoder.yudao.module.iot.controller.admin.visitor.vo.door.VisitorDoorRecordPageReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.visitor.vo.door.VisitorDoorRecordRespVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 新访客管理（开门记录/门禁设备）")
@RestController
@RequestMapping("/iot/visitor")
@Validated
public class VisitorDoorRecordController {

    @GetMapping("/door-record/page")
    @Operation(summary = "开门记录分页（按访客预约ID可选筛选）")
    @PreAuthorize("@ss.hasPermission('security:visitor:query')")
    public CommonResult<PageResult<VisitorDoorRecordRespVO>> pageDoorRecord(@Valid VisitorDoorRecordPageReqVO reqVO) {
        // 暂无开门记录表，返回空分页；后续可对接 access 操作日志或门禁事件
        PageResult<VisitorDoorRecordRespVO> empty = new PageResult<>();
        empty.setList(Collections.emptyList());
        empty.setTotal(0L);
        return success(empty);
    }

    @GetMapping("/auth-devices")
    @Operation(summary = "门禁设备列表（用于下发权限时选择）")
    @PreAuthorize("@ss.hasPermission('security:visitor:query')")
    public CommonResult<List<VisitorAuthDeviceRespVO>> listAuthDevices() {
        // 暂无与访客权限绑定的设备列表接口，返回空列表；后续可对接 access 通道/设备列表
        return success(Collections.emptyList());
    }
}
