package cn.iocoder.yudao.module.iot.controller.admin.alarm;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.iot.controller.admin.alarm.vo.host.IotAlarmHostCreateReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.alarm.vo.host.IotAlarmHostPageReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.alarm.vo.host.IotAlarmHostRespVO;
import cn.iocoder.yudao.module.iot.controller.admin.alarm.vo.host.IotAlarmHostStatusRespVO;
import cn.iocoder.yudao.module.iot.controller.admin.alarm.vo.host.IotAlarmHostUpdateReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.alarm.vo.host.IotAlarmHostUpdateNameReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.alarm.vo.host.IotAlarmHostWithDetailsRespVO;
import cn.iocoder.yudao.module.iot.controller.admin.alarm.vo.partition.IotAlarmPartitionRespVO;
import cn.iocoder.yudao.module.iot.controller.admin.alarm.vo.partition.IotAlarmPartitionUpdateNameReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.alarm.vo.zone.IotAlarmZoneRespVO;
import cn.iocoder.yudao.module.iot.controller.admin.alarm.vo.zone.IotAlarmZoneUpdateNameReqVO;
import cn.iocoder.yudao.module.iot.convert.alarm.IotAlarmHostConvert;
import cn.iocoder.yudao.module.iot.dal.dataobject.alarm.IotAlarmHostDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.alarm.IotAlarmZoneDO;
import cn.iocoder.yudao.module.iot.dal.mysql.alarm.IotAlarmZoneMapper;
import cn.iocoder.yudao.module.iot.service.alarm.IotAlarmHostService;
import cn.iocoder.yudao.module.iot.service.alarm.IotAlarmPartitionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

/**
 * 报警主机 Controller
 *
 * @author 长辉信息科技有限公司
 */
@Tag(name = "管理后台 - 报警主机")
@RestController
@RequestMapping("/iot/alarm/host")
@Validated
public class IotAlarmHostController {

    @Resource
    private IotAlarmHostService alarmHostService;

    @Resource
    private IotAlarmPartitionService partitionService;

    @Resource
    private IotAlarmZoneMapper zoneMapper;

    @PostMapping("/create")
    @Operation(summary = "创建报警主机")
    @PreAuthorize("@ss.hasPermission('iot:alarm-host:create')")
    public CommonResult<Long> createAlarmHost(@Valid @RequestBody IotAlarmHostCreateReqVO createReqVO) {
        return success(alarmHostService.createAlarmHost(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新报警主机")
    @PreAuthorize("@ss.hasPermission('iot:alarm-host:update')")
    public CommonResult<Boolean> updateAlarmHost(@Valid @RequestBody IotAlarmHostUpdateReqVO updateReqVO) {
        alarmHostService.updateAlarmHost(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除报警主机")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('iot:alarm-host:delete')")
    public CommonResult<Boolean> deleteAlarmHost(@RequestParam("id") Long id) {
        alarmHostService.deleteAlarmHost(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得报警主机")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('iot:alarm-host:query')")
    public CommonResult<IotAlarmHostRespVO> getAlarmHost(@RequestParam("id") Long id) {
        IotAlarmHostDO alarmHost = alarmHostService.getAlarmHost(id);
        return success(IotAlarmHostConvert.INSTANCE.convert(alarmHost));
    }

    @GetMapping("/page")
    @Operation(summary = "获得报警主机分页")
    @PreAuthorize("@ss.hasPermission('iot:alarm-host:query')")
    public CommonResult<PageResult<IotAlarmHostRespVO>> getAlarmHostPage(@Valid IotAlarmHostPageReqVO pageVO) {
        PageResult<IotAlarmHostDO> pageResult = alarmHostService.getAlarmHostPage(pageVO);
        PageResult<IotAlarmHostRespVO> respPage = IotAlarmHostConvert.INSTANCE.convertPage(pageResult);
        
        // 填充分区和防区数量
        respPage.getList().forEach(host -> {
            // 查询分区数量
            Long partitionCount = Long.valueOf(partitionService.getPartitionCountByHostId(host.getId()));
            host.setPartitionCount(partitionCount != null ? partitionCount.intValue() : 0);
            
            // 查询防区数量
            Long zoneCount = zoneMapper.selectCount(cn.iocoder.yudao.module.iot.dal.dataobject.alarm.IotAlarmZoneDO::getHostId, host.getId());
            host.setZoneCount(zoneCount != null ? zoneCount.intValue() : 0);
        });
        
        return success(respPage);
    }

    @PutMapping("/arm-all")
    @Operation(summary = "全部布防")
    @Parameter(name = "id", description = "主机ID", required = true)
    @PreAuthorize("@ss.hasPermission('iot:alarm-host:arm')")
    public CommonResult<IotAlarmHostWithDetailsRespVO> armAll(@RequestParam("id") Long id) {
        // 返回包含主机、分区、防区的完整状态
        return success(alarmHostService.armAllWithDetails(id));
    }

    @PutMapping("/arm-emergency")
    @Operation(summary = "紧急布防")
    @Parameter(name = "id", description = "主机ID", required = true)
    @PreAuthorize("@ss.hasPermission('iot:alarm-host:arm')")
    public CommonResult<IotAlarmHostWithDetailsRespVO> armEmergency(@RequestParam("id") Long id) {
        // 返回包含主机、分区、防区的完整状态
        return success(alarmHostService.armEmergencyWithDetails(id));
    }

    @PutMapping("/disarm")
    @Operation(summary = "撤防")
    @Parameter(name = "id", description = "主机ID", required = true)
    @PreAuthorize("@ss.hasPermission('iot:alarm-host:arm')")
    public CommonResult<IotAlarmHostWithDetailsRespVO> disarm(@RequestParam("id") Long id) {
        // 返回包含主机、分区、防区的完整状态
        return success(alarmHostService.disarmWithDetails(id));
    }

    @PutMapping("/clear-alarm")
    @Operation(summary = "消警")
    @Parameter(name = "id", description = "主机ID", required = true)
    @PreAuthorize("@ss.hasPermission('iot:alarm-host:clear-alarm')")
    public CommonResult<IotAlarmHostWithDetailsRespVO> clearAlarm(@RequestParam("id") Long id) {
        // 返回包含主机、分区、防区的完整状态
        return success(alarmHostService.clearAlarmWithDetails(id));
    }

    // ==================== 新增：分区和防区查询接口 ====================

    @GetMapping("/{id}/partitions")
    @Operation(summary = "查询主机分区列表")
    @Parameter(name = "id", description = "主机ID", required = true, example = "109")
    @PreAuthorize("@ss.hasPermission('iot:alarm-host:query')")
    public CommonResult<java.util.List<IotAlarmPartitionRespVO>> getPartitionList(@PathVariable("id") Long id) {
        return success(partitionService.getPartitionListByHostId(id));
    }

    @GetMapping("/{id}/zones")
    @Operation(summary = "查询主机防区列表")
    @Parameter(name = "id", description = "主机ID", required = true, example = "109")
    @PreAuthorize("@ss.hasPermission('iot:alarm-host:query')")
    public CommonResult<java.util.List<IotAlarmZoneRespVO>> getZoneList(@PathVariable("id") Long id) {
        java.util.List<IotAlarmZoneDO> zones = zoneMapper.selectList(IotAlarmZoneDO::getHostId, id);
        
        // 如果数据库中没有防区数据，通过 Service 层向 Gateway 请求
        // Service 层会自动同步数据到数据库
        if (zones.isEmpty()) {
            // 触发分区查询，会同时获取防区数据
            partitionService.getPartitionListByHostId(id);
            
            // 重新查询防区列表
            zones = zoneMapper.selectList(IotAlarmZoneDO::getHostId, id);
            
            // 如果仍然没有数据，返回空列表
            if (zones.isEmpty()) {
                return success(java.util.Collections.emptyList());
            }
        }
        
        // 转换数据库中的防区为VO，并添加实时状态信息
        java.util.List<IotAlarmZoneRespVO> vos = zones.stream().map(zone -> {
            IotAlarmZoneRespVO vo = new IotAlarmZoneRespVO();
            vo.setId(zone.getId());
            vo.setHostId(zone.getHostId());
            vo.setZoneNo(zone.getZoneNo());
            vo.setZoneName(zone.getZoneName());
            vo.setZoneType(zone.getZoneType());
            vo.setZoneStatus(zone.getZoneStatus());
            vo.setOnlineStatus(zone.getOnlineStatus());
            vo.setAlarmCount(zone.getAlarmCount());
            vo.setLastAlarmTime(zone.getLastAlarmTime());
            vo.setPartitionId(zone.getPartitionId()); // 设置所属分区ID
            
            // 添加实时状态信息（使用枚举字段）
            vo.setStatusChar(zone.getStatus());
            vo.setArmStatus(zone.getArmStatus());
            vo.setAlarmStatus(zone.getAlarmStatus());
            
            // 设置状态名称：优先使用数据库中的 statusName，如果为空则根据枚举值生成
            if (zone.getStatusName() != null && !zone.getStatusName().isEmpty()) {
                vo.setStatusName(zone.getStatusName());
            } else {
                // 根据枚举值生成状态名称
                vo.setStatusName(generateStatusName(zone.getArmStatus(), zone.getAlarmStatus()));
            }
            
            return vo;
        }).collect(java.util.stream.Collectors.toList());
        return success(vos);
    }

    @PostMapping("/{id}/query-status")
    @Operation(summary = "触发主机状态查询（异步）")
    @Parameter(name = "id", description = "主机ID", required = true, example = "109")
    @PreAuthorize("@ss.hasPermission('iot:alarm-host:query')")
    public CommonResult<Boolean> queryHostStatus(@PathVariable("id") Long id) {
        partitionService.triggerHostStatusQuery(id);
        return success(true);
    }

    /**
     * 快速查询主机状态（指令码0，无参数）
     * 用于快速检测主机在线状态和基本信息
     */
    @PostMapping("/quick-query")
    @Operation(summary = "快速查询主机状态")
    @Parameter(name = "account", description = "主机账号", required = true)
    @PreAuthorize("@ss.hasPermission('iot:alarm-host:query')")
    public CommonResult<Boolean> quickQuery(@RequestParam("account") String account) {
        try {
            alarmHostService.quickQuery(account);
            return success(true);
        } catch (Exception e) {
            return CommonResult.error(500, "快速查询失败: " + e.getMessage());
        }
    }

    /**
     * 触发主机状态查询（异步）- 指令码10，查询分区和防区详细状态
     * 通过查询主机ID来触发状态查询，结果通过消息总线异步返回并更新数据库
     */
    @PostMapping("/trigger-query")
    @Operation(summary = "触发主机状态查询")
    @Parameter(name = "account", description = "主机账号", required = true)
    @PreAuthorize("@ss.hasPermission('iot:alarm-host:query')")
    public CommonResult<Boolean> triggerQuery(@RequestParam("account") String account) {
        try {
            // 1. 根据账号查询主机
            IotAlarmHostDO host = alarmHostService.getAlarmHostByAccount(account);
            if (host == null) {
                return CommonResult.error(404, "主机不存在");
            }
            
            // 2. 调用Service层触发查询（异步，指令码10）
            partitionService.triggerHostStatusQuery(host.getId());
            return success(true);
        } catch (Exception e) {
            return CommonResult.error(500, "触发查询失败: " + e.getMessage());
        }
    }

    /**
     * 获取主机状态（从数据库）
     * 返回最后一次查询并持久化的状态数据
     */
    @GetMapping("/status")
    @Operation(summary = "获取主机状态")
    @Parameter(name = "account", description = "主机账号", required = true)
    @PreAuthorize("@ss.hasPermission('iot:alarm-host:query')")
    public CommonResult<IotAlarmHostStatusRespVO> getHostStatus(@RequestParam("account") String account) {
        // 1. 查询主机
        IotAlarmHostDO host = alarmHostService.getAlarmHostByAccount(account);
        if (host == null) {
            return CommonResult.error(404, "主机不存在");
        }

        // 2. 构造返回数据
        IotAlarmHostStatusRespVO vo = new IotAlarmHostStatusRespVO();
        vo.setHostId(host.getId());
        vo.setAccount(host.getAccount());
        vo.setSystemStatus(host.getSystemStatus());
        vo.setLastQueryTime(host.getLastQueryTime());

        // 3. 查询防区列表
        java.util.List<IotAlarmZoneDO> zones = zoneMapper.selectList(IotAlarmZoneDO::getHostId, host.getId());
        if (!zones.isEmpty()) {
            java.util.List<IotAlarmHostStatusRespVO.ZoneStatusVO> zoneVOs = zones.stream()
                .map(zone -> {
                    IotAlarmHostStatusRespVO.ZoneStatusVO zoneVO = new IotAlarmHostStatusRespVO.ZoneStatusVO();
                    zoneVO.setZoneNo(zone.getZoneNo());
                    zoneVO.setZoneName(zone.getZoneName());
                    zoneVO.setStatus(zone.getStatus());
                    zoneVO.setStatusName(zone.getStatusName());
                    zoneVO.setArmStatus(zone.getArmStatus());
                    zoneVO.setAlarmStatus(zone.getAlarmStatus());
                    zoneVO.setLastAlarmTime(zone.getLastAlarmTime());
                    return zoneVO;
                })
                .collect(java.util.stream.Collectors.toList());
            vo.setZones(zoneVOs);
        }

        return success(vo);
    }

    /**
     * 根据枚举值生成状态名称
     */
    private String generateStatusName(Integer armStatus, Integer alarmStatus) {
        if (armStatus == null) {
            return "未知";
        }
        
        if (armStatus == 0) {
            return "撤防";
        } else if (armStatus == 2) {
            return "旁路";
        } else if (armStatus == 1) {
            // 布防状态，需要根据报警状态细分
            if (alarmStatus == null || alarmStatus == 0) {
                return "布防";
            } else if (alarmStatus == 1) {
                return "报警中";
            } else {
                // 特殊报警类型
                switch (alarmStatus) {
                    case 11: return "剪断报警";
                    case 12: return "短路报警";
                    case 13: return "触网报警";
                    case 14: return "松弛报警";
                    case 15: return "拉紧报警";
                    case 16: return "攀爬报警";
                    case 17: return "开路报警";
                    default: return "报警中";
                }
            }
        }
        
        return "未知";
    }

    @PutMapping("/update-name")
    @Operation(summary = "更新主机名称")
    @PreAuthorize("@ss.hasPermission('iot:alarm-host:update')")
    public CommonResult<Boolean> updateHostName(@RequestBody @Valid IotAlarmHostUpdateNameReqVO reqVO) {
        alarmHostService.updateHostName(reqVO.getId(), reqVO.getHostName());
        return success(true);
    }

    @PutMapping("/partition/update-name")
    @Operation(summary = "更新分区名称")
    @PreAuthorize("@ss.hasPermission('iot:alarm-host:update')")
    public CommonResult<Boolean> updatePartitionName(@RequestBody @Valid IotAlarmPartitionUpdateNameReqVO reqVO) {
        partitionService.updatePartitionName(reqVO.getId(), reqVO.getPartitionName());
        return success(true);
    }

    @PutMapping("/zone/update-name")
    @Operation(summary = "更新防区名称")
    @PreAuthorize("@ss.hasPermission('iot:alarm-host:update')")
    public CommonResult<Boolean> updateZoneName(@RequestBody @Valid IotAlarmZoneUpdateNameReqVO reqVO) {
        alarmHostService.updateZoneName(reqVO.getId(), reqVO.getZoneName());
        return success(true);
    }

    // ==================== 分区操作接口 ====================

    @PutMapping("/partition/arm-all")
    @Operation(summary = "分区外出布防")
    @Parameter(name = "partitionId", description = "分区ID", required = true)
    @PreAuthorize("@ss.hasPermission('iot:alarm-host:arm')")
    public CommonResult<Boolean> armPartitionAll(@RequestParam("partitionId") Long partitionId) {
        partitionService.armPartitionAll(partitionId);
        return success(true);
    }

    @PutMapping("/partition/arm-emergency")
    @Operation(summary = "分区居家布防")
    @Parameter(name = "partitionId", description = "分区ID", required = true)
    @PreAuthorize("@ss.hasPermission('iot:alarm-host:arm')")
    public CommonResult<Boolean> armPartitionEmergency(@RequestParam("partitionId") Long partitionId) {
        partitionService.armPartitionEmergency(partitionId);
        return success(true);
    }

    @PutMapping("/partition/disarm")
    @Operation(summary = "分区撤防")
    @Parameter(name = "partitionId", description = "分区ID", required = true)
    @PreAuthorize("@ss.hasPermission('iot:alarm-host:arm')")
    public CommonResult<Boolean> disarmPartition(@RequestParam("partitionId") Long partitionId) {
        partitionService.disarmPartition(partitionId);
        return success(true);
    }

    @PutMapping("/partition/clear-alarm")
    @Operation(summary = "分区消警")
    @Parameter(name = "partitionId", description = "分区ID", required = true)
    @PreAuthorize("@ss.hasPermission('iot:alarm-host:clear-alarm')")
    public CommonResult<Boolean> clearPartitionAlarm(@RequestParam("partitionId") Long partitionId) {
        partitionService.clearPartitionAlarm(partitionId);
        return success(true);
    }

    // ==================== 防区操作接口 ====================

    @PutMapping("/zone/arm")
    @Operation(summary = "单防区布防")
    @Parameter(name = "hostId", description = "主机ID", required = true)
    @Parameter(name = "zoneNo", description = "防区号", required = true)
    @PreAuthorize("@ss.hasPermission('iot:alarm-host:arm')")
    public CommonResult<IotAlarmHostWithDetailsRespVO> armZone(
            @RequestParam("hostId") Long hostId,
            @RequestParam("zoneNo") Integer zoneNo) {
        return success(alarmHostService.armZoneWithDetails(hostId, zoneNo));
    }

    @PutMapping("/zone/disarm")
    @Operation(summary = "单防区撤防")
    @Parameter(name = "hostId", description = "主机ID", required = true)
    @Parameter(name = "zoneNo", description = "防区号", required = true)
    @PreAuthorize("@ss.hasPermission('iot:alarm-host:arm')")
    public CommonResult<IotAlarmHostWithDetailsRespVO> disarmZone(
            @RequestParam("hostId") Long hostId,
            @RequestParam("zoneNo") Integer zoneNo) {
        return success(alarmHostService.disarmZoneWithDetails(hostId, zoneNo));
    }

    @PutMapping("/zone/bypass")
    @Operation(summary = "防区旁路")
    @Parameter(name = "hostId", description = "主机ID", required = true)
    @Parameter(name = "zoneNo", description = "防区号", required = true)
    @PreAuthorize("@ss.hasPermission('iot:alarm-host:arm')")
    public CommonResult<IotAlarmHostWithDetailsRespVO> bypassZone(
            @RequestParam("hostId") Long hostId,
            @RequestParam("zoneNo") Integer zoneNo) {
        return success(alarmHostService.bypassZoneWithDetails(hostId, zoneNo));
    }

    @PutMapping("/zone/unbypass")
    @Operation(summary = "撤销防区旁路")
    @Parameter(name = "hostId", description = "主机ID", required = true)
    @Parameter(name = "zoneNo", description = "防区号", required = true)
    @PreAuthorize("@ss.hasPermission('iot:alarm-host:arm')")
    public CommonResult<IotAlarmHostWithDetailsRespVO> unbypassZone(
            @RequestParam("hostId") Long hostId,
            @RequestParam("zoneNo") Integer zoneNo) {
        return success(alarmHostService.unbypassZoneWithDetails(hostId, zoneNo));
    }
}
