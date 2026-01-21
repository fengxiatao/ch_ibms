package cn.iocoder.yudao.module.iot.controller.admin.access;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.iot.controller.admin.access.vo.permissiongroup.*;
import cn.iocoder.yudao.module.iot.dal.dataobject.access.IotAccessPermissionGroupDO;
import cn.iocoder.yudao.module.iot.service.access.IotAccessPermissionGroupService;
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
 * 门禁权限组 Controller
 */
@Tag(name = "管理后台 - 门禁权限组")
@RestController
@RequestMapping("/iot/access/permission-group")
@Validated
public class IotAccessPermissionGroupController {

    @Resource
    private IotAccessPermissionGroupService permissionGroupService;

    @PostMapping("/create")
    @Operation(summary = "创建权限组")
    @PreAuthorize("@ss.hasPermission('iot:access-permission-group:create')")
    public CommonResult<Long> createPermissionGroup(@Valid @RequestBody IotAccessPermissionGroupCreateReqVO createReqVO) {
        IotAccessPermissionGroupDO group = convertToDO(createReqVO);
        Long groupId = permissionGroupService.createPermissionGroup(group);
        // 关联设备
        if (createReqVO.getDeviceIds() != null && !createReqVO.getDeviceIds().isEmpty()) {
            permissionGroupService.addDevices(groupId, createReqVO.getDeviceIds(), createReqVO.getChannelIds());
        }
        return success(groupId);
    }

    @PutMapping("/update")
    @Operation(summary = "更新权限组")
    @PreAuthorize("@ss.hasPermission('iot:access-permission-group:update')")
    public CommonResult<Boolean> updatePermissionGroup(@Valid @RequestBody IotAccessPermissionGroupUpdateReqVO updateReqVO) {
        IotAccessPermissionGroupDO group = convertToDO(updateReqVO);
        group.setId(updateReqVO.getId());
        permissionGroupService.updatePermissionGroup(group);
        // 更新设备关联
        if (updateReqVO.getDeviceIds() != null) {
            permissionGroupService.updateDevices(updateReqVO.getId(), updateReqVO.getDeviceIds(), updateReqVO.getChannelIds());
        }
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除权限组")
    @Parameter(name = "id", description = "权限组ID", required = true)
    @PreAuthorize("@ss.hasPermission('iot:access-permission-group:delete')")
    public CommonResult<Boolean> deletePermissionGroup(@RequestParam("id") Long id) {
        permissionGroupService.deletePermissionGroup(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获取权限组详情")
    @Parameter(name = "id", description = "权限组ID", required = true)
    @PreAuthorize("@ss.hasPermission('iot:access-permission-group:query')")
    public CommonResult<IotAccessPermissionGroupRespVO> getPermissionGroup(@RequestParam("id") Long id) {
        IotAccessPermissionGroupDO group = permissionGroupService.getPermissionGroup(id);
        IotAccessPermissionGroupRespVO vo = convertToVO(group);
        if (vo != null) {
            // 填充设备数量和人员数量
            vo.setDeviceCount(permissionGroupService.getDeviceCount(id));
            vo.setPersonCount(permissionGroupService.getPersonCount(id));
            // 填充设备详情列表（用于编辑回显）
            List<IotAccessPermissionGroupDeviceRespVO> deviceDetails = permissionGroupService.getGroupDevicesWithDetail(id);
            if (deviceDetails != null && !deviceDetails.isEmpty()) {
                List<IotAccessPermissionGroupRespVO.DeviceVO> devices = new ArrayList<>();
                for (IotAccessPermissionGroupDeviceRespVO detail : deviceDetails) {
                    IotAccessPermissionGroupRespVO.DeviceVO deviceVO = new IotAccessPermissionGroupRespVO.DeviceVO();
                    deviceVO.setDeviceId(detail.getDeviceId());
                    deviceVO.setDeviceName(detail.getDeviceName());
                    deviceVO.setDeviceIp(detail.getDeviceIp());
                    deviceVO.setChannelId(detail.getChannelId());
                    deviceVO.setChannelNo(detail.getChannelNo());
                    deviceVO.setChannelName(detail.getChannelName());
                    devices.add(deviceVO);
                }
                vo.setDevices(devices);
            }
        }
        return success(vo);
    }

    @GetMapping("/page")
    @Operation(summary = "获取权限组分页")
    @PreAuthorize("@ss.hasPermission('iot:access-permission-group:query')")
    public CommonResult<PageResult<IotAccessPermissionGroupRespVO>> getPermissionGroupPage(
            @Valid IotAccessPermissionGroupPageReqVO pageReqVO) {
        PageResult<IotAccessPermissionGroupDO> pageResult = permissionGroupService.getPermissionGroupPage(
                pageReqVO.getGroupName(),
                pageReqVO.getStatus(),
                pageReqVO.getPageNo(),
                pageReqVO.getPageSize()
        );
        return success(convertPageResult(pageResult));
    }

    @GetMapping("/list")
    @Operation(summary = "获取权限组列表")
    @PreAuthorize("@ss.hasPermission('iot:access-permission-group:query')")
    public CommonResult<List<IotAccessPermissionGroupRespVO>> getPermissionGroupList() {
        List<IotAccessPermissionGroupDO> list = permissionGroupService.getPermissionGroupList(null, null);
        return success(convertToVOList(list));
    }

    @PostMapping("/add-persons")
    @Operation(summary = "添加人员到权限组（不触发下发）")
    @PreAuthorize("@ss.hasPermission('iot:access-permission-group:update')")
    public CommonResult<Boolean> addPersons(
            @RequestParam("groupId") Long groupId,
            @RequestBody List<Long> personIds) {
        permissionGroupService.addPersons(groupId, personIds);
        return success(true);
    }

    @PostMapping("/add-persons-with-dispatch")
    @Operation(summary = "添加人员到权限组并触发授权下发")
    @PreAuthorize("@ss.hasPermission('iot:access-permission-group:update')")
    public CommonResult<Long> addPersonsWithDispatch(
            @RequestParam("groupId") Long groupId,
            @RequestBody List<Long> personIds) {
        Long taskId = permissionGroupService.addPersonsWithDispatch(groupId, personIds);
        return success(taskId);
    }

    @DeleteMapping("/remove-persons")
    @Operation(summary = "从权限组移除人员（不触发撤销）")
    @PreAuthorize("@ss.hasPermission('iot:access-permission-group:update')")
    public CommonResult<Boolean> removePersons(
            @RequestParam("groupId") Long groupId,
            @RequestBody List<Long> personIds) {
        permissionGroupService.removePersons(groupId, personIds);
        return success(true);
    }

    @DeleteMapping("/remove-persons-with-revoke")
    @Operation(summary = "从权限组移除人员并触发权限撤销")
    @PreAuthorize("@ss.hasPermission('iot:access-permission-group:update')")
    public CommonResult<Long> removePersonsWithRevoke(
            @RequestParam("groupId") Long groupId,
            @RequestBody List<Long> personIds) {
        Long taskId = permissionGroupService.removePersonsWithRevoke(groupId, personIds);
        return success(taskId);
    }

    @GetMapping("/devices")
    @Operation(summary = "获取权限组关联的设备列表")
    @Parameter(name = "groupId", description = "权限组ID", required = true)
    @PreAuthorize("@ss.hasPermission('iot:access-permission-group:query')")
    public CommonResult<List<IotAccessPermissionGroupDeviceRespVO>> getGroupDevices(@RequestParam("groupId") Long groupId) {
        return success(permissionGroupService.getGroupDevicesWithDetail(groupId));
    }

    @GetMapping("/persons")
    @Operation(summary = "获取权限组关联的人员列表")
    @Parameter(name = "groupId", description = "权限组ID", required = true)
    @PreAuthorize("@ss.hasPermission('iot:access-permission-group:query')")
    public CommonResult<List<IotAccessPermissionGroupPersonRespVO>> getGroupPersons(@RequestParam("groupId") Long groupId) {
        return success(permissionGroupService.getGroupPersonsWithDetail(groupId));
    }

    // ========== 转换方法 ==========

    private IotAccessPermissionGroupDO convertToDO(IotAccessPermissionGroupCreateReqVO vo) {
        IotAccessPermissionGroupDO group = new IotAccessPermissionGroupDO();
        group.setGroupName(vo.getGroupName());
        group.setTimeTemplateId(vo.getTimeTemplateId());
        // 处理认证方式：优先使用authModes数组，否则使用authMode字符串
        if (vo.getAuthModes() != null && !vo.getAuthModes().isEmpty()) {
            group.setAuthMode(String.join(",", vo.getAuthModes()));
        } else {
            group.setAuthMode(vo.getAuthMode());
        }
        group.setDescription(vo.getDescription());
        group.setStatus(vo.getStatus() != null ? vo.getStatus() : 0);
        return group;
    }

    private IotAccessPermissionGroupRespVO convertToVO(IotAccessPermissionGroupDO group) {
        if (group == null) {
            return null;
        }
        IotAccessPermissionGroupRespVO vo = new IotAccessPermissionGroupRespVO();
        vo.setId(group.getId());
        vo.setGroupName(group.getGroupName());
        vo.setTimeTemplateId(group.getTimeTemplateId());
        vo.setAuthMode(group.getAuthMode());
        // 将authMode字符串转换为数组供前端使用
        if (group.getAuthMode() != null && !group.getAuthMode().isEmpty()) {
            vo.setAuthModes(java.util.Arrays.asList(group.getAuthMode().split(",")));
        } else {
            vo.setAuthModes(new ArrayList<>());
        }
        vo.setDescription(group.getDescription());
        vo.setStatus(group.getStatus());
        vo.setCreateTime(group.getCreateTime());
        return vo;
    }

    private List<IotAccessPermissionGroupRespVO> convertToVOList(List<IotAccessPermissionGroupDO> list) {
        List<IotAccessPermissionGroupRespVO> result = new ArrayList<>();
        if (list != null) {
            for (IotAccessPermissionGroupDO group : list) {
                result.add(convertToVO(group));
            }
        }
        return result;
    }

    private PageResult<IotAccessPermissionGroupRespVO> convertPageResult(PageResult<IotAccessPermissionGroupDO> pageResult) {
        List<IotAccessPermissionGroupRespVO> list = new ArrayList<>();
        if (pageResult.getList() != null) {
            for (IotAccessPermissionGroupDO group : pageResult.getList()) {
                IotAccessPermissionGroupRespVO vo = convertToVO(group);
                // 填充设备数量和人员数量
                vo.setDeviceCount(permissionGroupService.getDeviceCount(group.getId()));
                vo.setPersonCount(permissionGroupService.getPersonCount(group.getId()));
                list.add(vo);
            }
        }
        return new PageResult<>(list, pageResult.getTotal());
    }

}
