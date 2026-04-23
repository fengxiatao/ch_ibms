package cn.iocoder.yudao.module.iot.service.factory;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import cn.iocoder.yudao.framework.tenant.core.context.TenantContextHolder;
import cn.iocoder.yudao.module.iot.controller.admin.factory.vo.collaboration.FactoryCollaborationVO;
import jakarta.annotation.Resource;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 智慧工厂业务协同聚合服务
 *
 * <p>说明：当前四大域工作台以真实数据库表为唯一数据源，
 * 通过 JDBC 聚合查询快速完成最小可用闭环，避免为了本次原型落地
 * 引入大量重复样板 Mapper/Convert 代码。</p>
 *
 * @author GPT-5.4
 */
@Service
@Validated
public class FactoryCollaborationService {

    /**
     * 默认租户编号
     */
    private static final long DEFAULT_TENANT_ID = 1L;

    @Resource
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    /**
     * 查询生产协同工作台数据
     *
     * @param reqVO 查询参数
     * @return 生产协同工作台数据
     */
    public FactoryCollaborationVO.ProductionDashboardRespVO getProductionDashboard(
            FactoryCollaborationVO.ProductionDashboardReqVO reqVO
    ) {
        MapSqlParameterSource params = baseParams();
        params.addValue("keyword", normalizeKeyword(reqVO.getKeyword()));
        params.addValue("likeKeyword", buildLikeKeyword(reqVO.getKeyword()));

        List<FactoryCollaborationVO.ProductionPlanItem> planList = namedParameterJdbcTemplate.query("""
                SELECT id, plan_code, product_name, batch_code, planned_quantity, operator_name, line_name,
                       planned_start_time, status, progress
                FROM iot_factory_collab_production_plan
                WHERE tenant_id = :tenantId
                  AND deleted = b'0'
                  AND (
                      :keyword = ''
                      OR plan_code LIKE :likeKeyword
                      OR product_name LIKE :likeKeyword
                      OR batch_code LIKE :likeKeyword
                      OR line_name LIKE :likeKeyword
                  )
                ORDER BY planned_start_time DESC, id DESC
                LIMIT 20
                """, params, productionPlanRowMapper());

        List<FactoryCollaborationVO.ProductionBatchItem> batchList = namedParameterJdbcTemplate.query("""
                SELECT id, plan_id, batch_code, product_name, current_process, current_location,
                       completed_quantity, yield_rate, status, trace_time
                FROM iot_factory_collab_batch_trace
                WHERE tenant_id = :tenantId
                  AND deleted = b'0'
                  AND (
                      :keyword = ''
                      OR batch_code LIKE :likeKeyword
                      OR product_name LIKE :likeKeyword
                      OR current_process LIKE :likeKeyword
                      OR current_location LIKE :likeKeyword
                  )
                ORDER BY trace_time DESC, id DESC
                LIMIT 20
                """, params, productionBatchRowMapper());

        List<FactoryCollaborationVO.ProductionAlertItem> alertList = namedParameterJdbcTemplate.query("""
                SELECT id, alert_title, level_label, line_name, status, handler_name, happened_at
                FROM iot_factory_collab_production_exception
                WHERE tenant_id = :tenantId
                  AND deleted = b'0'
                  AND (
                      :keyword = ''
                      OR alert_title LIKE :likeKeyword
                      OR line_name LIKE :likeKeyword
                      OR level_label LIKE :likeKeyword
                  )
                ORDER BY happened_at DESC, id DESC
                LIMIT 6
                """, params, productionAlertRowMapper());

        int todayPlanCount = queryInt("""
                SELECT COUNT(1)
                FROM iot_factory_collab_production_plan
                WHERE tenant_id = :tenantId
                  AND deleted = b'0'
                  AND DATE(planned_start_time) = CURDATE()
                """, params);
        BigDecimal averageProgress = queryDecimal("""
                SELECT COALESCE(AVG(progress), 0)
                FROM iot_factory_collab_production_plan
                WHERE tenant_id = :tenantId
                  AND deleted = b'0'
                """, params);
        int pendingPlanCount = queryInt("""
                SELECT COUNT(1)
                FROM iot_factory_collab_production_plan
                WHERE tenant_id = :tenantId
                  AND deleted = b'0'
                  AND status IN ('PENDING', 'NOT_STARTED')
                """, params);
        BigDecimal productionRate = queryDecimal("""
                SELECT COALESCE(AVG(yield_rate), 0)
                FROM iot_factory_collab_batch_trace
                WHERE tenant_id = :tenantId
                  AND deleted = b'0'
                """, params);

        List<FactoryCollaborationVO.MetricCardItem> metrics = List.of(
                buildMetricCard("today-plan", "今日生产批次", String.valueOf(todayPlanCount), "批次", "实时产线任务",
                        "计划跟踪", "ep:document", "cyan"),
                buildMetricCard("completion-rate", "完成率", formatPercent(averageProgress), "",
                        "计划执行进度", "闭环推进", "ep:trend-charts", "emerald"),
                buildMetricCard("pending-plan", "待生产批次", String.valueOf(pendingPlanCount), "批次",
                        "待排产任务", "计划调度", "ep:files", "violet"),
                buildMetricCard("production-rate", "生产效率", formatPercent(productionRate), "",
                        "批次良率视图", "批次追踪", "ep:timer", "amber")
        );

        return FactoryCollaborationVO.ProductionDashboardRespVO.builder()
                .updatedAt(LocalDateTime.now())
                .metrics(metrics)
                .planList(planList)
                .batchList(batchList)
                .alertList(alertList)
                .build();
    }

    /**
     * 查询批次追溯详情
     *
     * @param reqVO 查询参数
     * @return 批次追溯详情
     */
    public FactoryCollaborationVO.ProductionBatchTraceDetailRespVO getProductionBatchTraceDetail(
            FactoryCollaborationVO.ProductionBatchTraceDetailReqVO reqVO
    ) {
        MapSqlParameterSource params = baseParams();
        params.addValue("batchId", reqVO.getBatchId());

        FactoryCollaborationVO.ProductionBatchTraceSummary summary = namedParameterJdbcTemplate.query("""
                SELECT b.id, b.plan_id, p.plan_code, b.batch_code, b.product_name, p.line_name, p.operator_name,
                       b.current_process, b.current_location, p.planned_quantity, b.completed_quantity, b.yield_rate,
                       b.status, p.planned_start_time, b.trace_time
                FROM iot_factory_collab_batch_trace b
                INNER JOIN iot_factory_collab_production_plan p
                        ON p.id = b.plan_id AND p.deleted = b'0' AND p.tenant_id = b.tenant_id
                WHERE b.tenant_id = :tenantId
                  AND b.id = :batchId
                  AND b.deleted = b'0'
                LIMIT 1
                """, params, rs -> rs.next() ? FactoryCollaborationVO.ProductionBatchTraceSummary.builder()
                .id(rs.getLong("id"))
                .planId(rs.getLong("plan_id"))
                .planCode(rs.getString("plan_code"))
                .batchCode(rs.getString("batch_code"))
                .productName(rs.getString("product_name"))
                .lineName(rs.getString("line_name"))
                .operatorName(rs.getString("operator_name"))
                .currentProcess(rs.getString("current_process"))
                .currentLocation(rs.getString("current_location"))
                .plannedQuantity(rs.getInt("planned_quantity"))
                .completedQuantity(rs.getInt("completed_quantity"))
                .yieldRate(getBigDecimal(rs, "yield_rate"))
                .status(rs.getString("status"))
                .plannedStartTime(rs.getObject("planned_start_time", LocalDateTime.class))
                .updatedAt(rs.getObject("trace_time", LocalDateTime.class))
                .build() : null);
        if (summary == null) {
            return FactoryCollaborationVO.ProductionBatchTraceDetailRespVO.builder()
                    .environmentRecords(List.of())
                    .qualityRecords(List.of())
                    .personnelRecords(List.of())
                    .deviceRecords(List.of())
                    .materialRecords(List.of())
                    .build();
        }

        List<FactoryCollaborationVO.BatchEnvironmentRecordItem> environmentRecords = namedParameterJdbcTemplate.query("""
                SELECT id, record_time, temperature_value, humidity_value, pressure_value, ph_value,
                       clean_level, recorder_name
                FROM iot_factory_collab_batch_environment
                WHERE tenant_id = :tenantId
                  AND batch_trace_id = :batchId
                  AND deleted = b'0'
                ORDER BY record_time DESC, id DESC
                LIMIT 6
                """, params, batchEnvironmentRowMapper());

        List<FactoryCollaborationVO.BatchQualityRecordItem> qualityRecords = namedParameterJdbcTemplate.query("""
                SELECT id, sample_name, inspection_item, standard_value, measured_value,
                       result_status, record_time, inspector_name
                FROM iot_factory_collab_batch_quality
                WHERE tenant_id = :tenantId
                  AND batch_trace_id = :batchId
                  AND deleted = b'0'
                ORDER BY record_time DESC, id DESC
                LIMIT 10
                """, params, batchQualityRowMapper());

        List<FactoryCollaborationVO.BatchPersonnelRecordItem> personnelRecords = namedParameterJdbcTemplate.query("""
                SELECT id, role_name, staff_name, operation_name, workstation_name,
                       record_time, duration_minutes, remark
                FROM iot_factory_collab_batch_personnel
                WHERE tenant_id = :tenantId
                  AND batch_trace_id = :batchId
                  AND deleted = b'0'
                ORDER BY record_time DESC, id DESC
                LIMIT 10
                """, params, batchPersonnelRowMapper());

        List<FactoryCollaborationVO.BatchDeviceRecordItem> deviceRecords = namedParameterJdbcTemplate.query("""
                SELECT id, device_code, device_name, operation_name, running_status,
                       parameter_summary, record_time, operator_name
                FROM iot_factory_collab_batch_device
                WHERE tenant_id = :tenantId
                  AND batch_trace_id = :batchId
                  AND deleted = b'0'
                ORDER BY record_time DESC, id DESC
                LIMIT 10
                """, params, batchDeviceRowMapper());

        List<FactoryCollaborationVO.BatchMaterialRecordItem> materialRecords = namedParameterJdbcTemplate.query("""
                SELECT id, material_code, material_name, material_type, material_batch_no,
                       planned_quantity, actual_quantity, unit, feeder_name, record_time
                FROM iot_factory_collab_batch_material
                WHERE tenant_id = :tenantId
                  AND batch_trace_id = :batchId
                  AND deleted = b'0'
                ORDER BY record_time DESC, id DESC
                LIMIT 10
                """, params, batchMaterialRowMapper());

        return FactoryCollaborationVO.ProductionBatchTraceDetailRespVO.builder()
                .summary(summary)
                .environmentRecords(environmentRecords)
                .qualityRecords(qualityRecords)
                .personnelRecords(personnelRecords)
                .deviceRecords(deviceRecords)
                .materialRecords(materialRecords)
                .build();
    }

    /**
     * 新增生产计划并初始化对应批次记录
     *
     * @param reqVO 生产计划新增请求
     * @return 新增后的计划主键
     */
    @Transactional(rollbackFor = Exception.class)
    public Long createProductionPlan(FactoryCollaborationVO.ProductionPlanCreateReqVO reqVO) {
        MapSqlParameterSource insertParams = auditParams();
        insertParams.addValue("planCode", reqVO.getPlanCode());
        insertParams.addValue("productName", reqVO.getProductName());
        insertParams.addValue("batchCode", reqVO.getBatchCode());
        insertParams.addValue("lineName", reqVO.getLineName());
        insertParams.addValue("plannedQuantity", reqVO.getPlannedQuantity());
        insertParams.addValue("operatorName", reqVO.getOperatorName());
        insertParams.addValue("plannedStartTime", reqVO.getPlannedStartTime());
        insertParams.addValue("plannedEndTime", reqVO.getPlannedStartTime().plusHours(8));
        insertParams.addValue("status", "PENDING");
        insertParams.addValue("progress", 0);
        insertParams.addValue("completedQuantity", 0);
        insertParams.addValue("remark", "由业务协同工作台创建");

        KeyHolder keyHolder = new GeneratedKeyHolder();
        namedParameterJdbcTemplate.update("""
                INSERT INTO iot_factory_collab_production_plan (
                    tenant_id, plan_code, product_name, batch_code, line_name,
                    planned_quantity, completed_quantity, operator_name,
                    planned_start_time, planned_end_time, status, progress, remark,
                    creator, create_time, updater, update_time, deleted
                ) VALUES (
                    :tenantId, :planCode, :productName, :batchCode, :lineName,
                    :plannedQuantity, :completedQuantity, :operatorName,
                    :plannedStartTime, :plannedEndTime, :status, :progress, :remark,
                    :operator, NOW(), :operator, NOW(), b'0'
                )
                """, insertParams, keyHolder, new String[]{"id"});

        Long planId = keyHolder.getKey() != null ? keyHolder.getKey().longValue() : null;
        if (planId == null) {
            throw new IllegalStateException("生产计划创建失败，未返回主键");
        }

        insertParams.addValue("planId", planId);
        insertParams.addValue("currentProcess", "待投产");
        insertParams.addValue("currentLocation", reqVO.getLineName());
        insertParams.addValue("yieldRate", BigDecimal.ZERO);
        insertParams.addValue("traceStatus", "待开始");
        namedParameterJdbcTemplate.update("""
                INSERT INTO iot_factory_collab_batch_trace (
                    tenant_id, plan_id, batch_code, product_name, current_process, current_location,
                    completed_quantity, yield_rate, status, trace_time,
                    creator, create_time, updater, update_time, deleted
                ) VALUES (
                    :tenantId, :planId, :batchCode, :productName, :currentProcess, :currentLocation,
                    0, :yieldRate, :traceStatus, NOW(),
                    :operator, NOW(), :operator, NOW(), b'0'
                )
                """, insertParams);
        return planId;
    }

    /**
     * 更新生产计划状态与进度
     *
     * @param reqVO 状态更新请求
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateProductionPlanStatus(FactoryCollaborationVO.ProductionPlanStatusUpdateReqVO reqVO) {
        MapSqlParameterSource params = auditParams();
        params.addValue("id", reqVO.getId());
        params.addValue("status", reqVO.getStatus());
        params.addValue("progress", reqVO.getProgress());
        params.addValue("remark", StrUtil.blankToDefault(reqVO.getRemark(), ""));

        namedParameterJdbcTemplate.update("""
                UPDATE iot_factory_collab_production_plan
                SET status = :status,
                    progress = :progress,
                    completed_quantity = ROUND(planned_quantity * :progress / 100),
                    remark = :remark,
                    updater = :operator,
                    update_time = NOW()
                WHERE tenant_id = :tenantId
                  AND id = :id
                  AND deleted = b'0'
                """, params);

        namedParameterJdbcTemplate.update("""
                UPDATE iot_factory_collab_batch_trace
                SET status = :status,
                    current_process = CASE
                        WHEN :progress >= 100 THEN '完工入库'
                        WHEN :progress >= 50 THEN '灌装中'
                        WHEN :progress > 0 THEN '配料中'
                        ELSE '待投产'
                    END,
                    completed_quantity = (
                        SELECT completed_quantity
                        FROM iot_factory_collab_production_plan
                        WHERE tenant_id = :tenantId AND id = :id AND deleted = b'0'
                    ),
                    yield_rate = CASE
                        WHEN :progress >= 100 THEN 98.20
                        WHEN :progress >= 50 THEN 95.60
                        WHEN :progress > 0 THEN 92.30
                        ELSE 0
                    END,
                    trace_time = NOW(),
                    updater = :operator,
                    update_time = NOW()
                WHERE tenant_id = :tenantId
                  AND plan_id = :id
                  AND deleted = b'0'
                """, params);
    }

    /**
     * 查询能源工作台数据
     *
     * @param reqVO 查询参数
     * @return 能源工作台响应
     */
    public FactoryCollaborationVO.EnergyDashboardRespVO getEnergyDashboard(
            FactoryCollaborationVO.EnergyDashboardReqVO reqVO
    ) {
        LocalDate statDate = reqVO.getStatDate() != null ? reqVO.getStatDate() : LocalDate.now();
        String subTab = StrUtil.blankToDefault(reqVO.getSubTab(), "overview");
        String focusEnergyType = resolveEnergyType(subTab);

        MapSqlParameterSource params = baseParams();
        params.addValue("statDate", statDate);
        params.addValue("focusEnergyType", focusEnergyType);

        BigDecimal electricity = queryDecimal("""
                SELECT COALESCE(SUM(usage_value), 0)
                FROM iot_factory_collab_energy_reading
                WHERE tenant_id = :tenantId AND deleted = b'0'
                  AND energy_type = 'electricity'
                  AND DATE(stat_time) = :statDate
                """, params);
        BigDecimal water = queryDecimal("""
                SELECT COALESCE(SUM(usage_value), 0)
                FROM iot_factory_collab_energy_reading
                WHERE tenant_id = :tenantId AND deleted = b'0'
                  AND energy_type = 'water'
                  AND DATE(stat_time) = :statDate
                """, params);
        BigDecimal gas = queryDecimal("""
                SELECT COALESCE(SUM(usage_value), 0)
                FROM iot_factory_collab_energy_reading
                WHERE tenant_id = :tenantId AND deleted = b'0'
                  AND energy_type = 'gas'
                  AND DATE(stat_time) = :statDate
                """, params);
        BigDecimal yoyRate = queryDecimal("""
                SELECT COALESCE(AVG(yoy_rate), 0)
                FROM iot_factory_collab_energy_reading
                WHERE tenant_id = :tenantId AND deleted = b'0'
                  AND energy_type = :focusEnergyType
                  AND DATE(stat_time) = :statDate
                """, params);

        List<FactoryCollaborationVO.EnergyTrendItem> trendList = namedParameterJdbcTemplate.query("""
                SELECT DATE_FORMAT(stat_time, '%H:%i') AS label,
                       SUM(CASE WHEN energy_type = 'electricity' THEN usage_value ELSE 0 END) AS electricity_value,
                       SUM(CASE WHEN energy_type = 'water' THEN usage_value ELSE 0 END) AS water_value,
                       SUM(CASE WHEN energy_type = 'gas' THEN usage_value ELSE 0 END) AS gas_value,
                       SUM(CASE WHEN energy_type = :focusEnergyType THEN usage_value ELSE 0 END) AS current_value
                FROM iot_factory_collab_energy_reading
                WHERE tenant_id = :tenantId
                  AND deleted = b'0'
                  AND DATE(stat_time) = :statDate
                GROUP BY DATE_FORMAT(stat_time, '%H:%i')
                ORDER BY label ASC
                """, params, energyTrendRowMapper());

        List<FactoryCollaborationVO.EnergyRankingItem> areaRanking = namedParameterJdbcTemplate.query("""
                SELECT area_name, SUM(usage_value) AS total_value
                FROM iot_factory_collab_energy_reading
                WHERE tenant_id = :tenantId
                  AND deleted = b'0'
                  AND energy_type = :focusEnergyType
                  AND DATE(stat_time) = :statDate
                GROUP BY area_name
                ORDER BY total_value DESC, area_name ASC
                LIMIT 5
                """, params, (rs, rowNum) -> FactoryCollaborationVO.EnergyRankingItem.builder()
                .name(rs.getString("area_name"))
                .value(getBigDecimal(rs, "total_value"))
                .unit(resolveEnergyUnit(focusEnergyType))
                .extraText("区域用量")
                .build());

        List<FactoryCollaborationVO.EnergyRankingItem> deviceRanking = namedParameterJdbcTemplate.query("""
                SELECT device_name, SUM(usage_value) AS total_value
                FROM iot_factory_collab_energy_reading
                WHERE tenant_id = :tenantId
                  AND deleted = b'0'
                  AND energy_type = :focusEnergyType
                  AND DATE(stat_time) = :statDate
                GROUP BY device_name
                ORDER BY total_value DESC, device_name ASC
                LIMIT 5
                """, params, (rs, rowNum) -> FactoryCollaborationVO.EnergyRankingItem.builder()
                .name(rs.getString("device_name"))
                .value(getBigDecimal(rs, "total_value"))
                .unit(resolveEnergyUnit(focusEnergyType))
                .extraText("设备用量")
                .build());

        List<FactoryCollaborationVO.EnergySuggestionItem> suggestionList = namedParameterJdbcTemplate.query("""
                SELECT id, title, content, level_label, status
                FROM iot_factory_collab_energy_suggestion
                WHERE tenant_id = :tenantId
                  AND deleted = b'0'
                ORDER BY sort_no ASC, id ASC
                LIMIT 6
                """, params, (rs, rowNum) -> FactoryCollaborationVO.EnergySuggestionItem.builder()
                .id(rs.getLong("id"))
                .title(rs.getString("title"))
                .content(rs.getString("content"))
                .levelLabel(rs.getString("level_label"))
                .status(rs.getString("status"))
                .build());

        List<FactoryCollaborationVO.MetricCardItem> metrics = List.of(
                buildMetricCard("electricity", "今日用电", formatDecimal(electricity), "kWh", "电能监控", "综合概览", "ep:lightning", "amber"),
                buildMetricCard("water", "今日用水", formatDecimal(water), "m³", "水耗分析", "综合概览", "ep:watermelon", "cyan"),
                buildMetricCard("gas", "今日用气", formatDecimal(gas), "m³", "气耗分析", "综合概览", "ep:fire", "violet"),
                buildMetricCard("yoy", "同比节能", formatSignedPercent(yoyRate), "", "真实读数计算", "优化建议", "ep:trend-charts", "emerald")
        );

        return FactoryCollaborationVO.EnergyDashboardRespVO.builder()
                .updatedAt(LocalDateTime.now())
                .subTab(subTab)
                .metrics(metrics)
                .trendList(trendList)
                .areaRanking(areaRanking)
                .deviceRanking(deviceRanking)
                .suggestionList(suggestionList)
                .build();
    }

    /**
     * 更新节能建议处理状态
     *
     * @param reqVO 处理请求
     */
    @Transactional(rollbackFor = Exception.class)
    public void handleEnergySuggestion(FactoryCollaborationVO.EnergySuggestionHandleReqVO reqVO) {
        MapSqlParameterSource params = auditParams();
        params.addValue("id", reqVO.getId());
        params.addValue("status", reqVO.getStatus());
        namedParameterJdbcTemplate.update("""
                UPDATE iot_factory_collab_energy_suggestion
                SET status = :status,
                    updater = :operator,
                    update_time = NOW()
                WHERE tenant_id = :tenantId
                  AND id = :id
                  AND deleted = b'0'
                """, params);
    }

    /**
     * 查询设备工作台数据
     *
     * @param reqVO 查询参数
     * @return 设备工作台响应
     */
    public FactoryCollaborationVO.DeviceDashboardRespVO getDeviceDashboard(
            FactoryCollaborationVO.DeviceDashboardReqVO reqVO
    ) {
        MapSqlParameterSource params = baseParams();
        params.addValue("keyword", normalizeKeyword(reqVO.getKeyword()));
        params.addValue("likeKeyword", buildLikeKeyword(reqVO.getKeyword()));

        List<FactoryCollaborationVO.DeviceItem> deviceList = namedParameterJdbcTemplate.query("""
                SELECT id, device_code, device_name, category_name, area_name, online_status,
                       running_status, health_status, efficiency_rate
                FROM iot_factory_collab_device
                WHERE tenant_id = :tenantId
                  AND deleted = b'0'
                  AND (
                      :keyword = ''
                      OR device_code LIKE :likeKeyword
                      OR device_name LIKE :likeKeyword
                      OR category_name LIKE :likeKeyword
                      OR area_name LIKE :likeKeyword
                  )
                ORDER BY online_status DESC, id ASC
                LIMIT 50
                """, params, deviceRowMapper());

        Long selectedDeviceId = reqVO.getSelectedDeviceId();
        if (selectedDeviceId == null && !deviceList.isEmpty()) {
            selectedDeviceId = deviceList.get(0).getId();
        }
        params.addValue("selectedDeviceId", selectedDeviceId == null ? -1L : selectedDeviceId);

        FactoryCollaborationVO.DeviceDetail detail = namedParameterJdbcTemplate.query("""
                SELECT d.id, d.device_code, d.device_name, d.category_name, d.model_name, d.area_name,
                       d.online_status, d.running_status, d.health_status, d.efficiency_rate, d.remark,
                       p.owner_name, p.last_execute_date, p.next_execute_date
                FROM iot_factory_collab_device d
                LEFT JOIN iot_factory_collab_maintenance_plan p
                       ON p.device_id = d.id AND p.deleted = b'0' AND p.tenant_id = d.tenant_id
                WHERE d.tenant_id = :tenantId
                  AND d.deleted = b'0'
                  AND d.id = :selectedDeviceId
                ORDER BY p.id DESC
                LIMIT 1
                """, params, rs -> rs.next() ? FactoryCollaborationVO.DeviceDetail.builder()
                .id(rs.getLong("id"))
                .deviceCode(rs.getString("device_code"))
                .deviceName(rs.getString("device_name"))
                .categoryName(rs.getString("category_name"))
                .modelName(rs.getString("model_name"))
                .areaName(rs.getString("area_name"))
                .online(rs.getInt("online_status") == 1)
                .runningStatus(rs.getString("running_status"))
                .healthStatus(rs.getString("health_status"))
                .efficiencyRate(getBigDecimal(rs, "efficiency_rate"))
                .ownerName(rs.getString("owner_name"))
                .lastMaintenanceDate(rs.getObject("last_execute_date", LocalDate.class))
                .nextMaintenanceDate(rs.getObject("next_execute_date", LocalDate.class))
                .remark(rs.getString("remark"))
                .build() : null);

        List<FactoryCollaborationVO.MaintenancePlanItem> maintenancePlanList = namedParameterJdbcTemplate.query("""
                SELECT p.id, p.device_id, p.plan_name, d.device_name, p.cycle_type,
                       p.next_execute_date, p.owner_name, p.status,
                       SUM(CASE WHEN o.status = 'PENDING' THEN 1 ELSE 0 END) AS pending_order_count,
                       MAX(CASE WHEN o.status = 'PENDING' THEN o.id ELSE NULL END) AS latest_order_id
                FROM iot_factory_collab_maintenance_plan p
                INNER JOIN iot_factory_collab_device d
                        ON d.id = p.device_id AND d.deleted = b'0' AND d.tenant_id = p.tenant_id
                LEFT JOIN iot_factory_collab_maintenance_order o
                       ON o.plan_id = p.id AND o.deleted = b'0' AND o.tenant_id = p.tenant_id
                WHERE p.tenant_id = :tenantId
                  AND p.deleted = b'0'
                GROUP BY p.id, p.device_id, p.plan_name, d.device_name, p.cycle_type,
                         p.next_execute_date, p.owner_name, p.status
                ORDER BY p.next_execute_date ASC, p.id ASC
                LIMIT 20
                """, params, (rs, rowNum) -> FactoryCollaborationVO.MaintenancePlanItem.builder()
                .id(rs.getLong("id"))
                .deviceId(rs.getLong("device_id"))
                .planName(rs.getString("plan_name"))
                .deviceName(rs.getString("device_name"))
                .cycleType(rs.getString("cycle_type"))
                .nextExecuteDate(rs.getObject("next_execute_date", LocalDate.class))
                .ownerName(rs.getString("owner_name"))
                .status(rs.getString("status"))
                .pendingOrderCount(rs.getInt("pending_order_count"))
                .latestOrderId(getLong(rs, "latest_order_id"))
                .build());

        int totalDeviceCount = queryInt("""
                SELECT COUNT(1)
                FROM iot_factory_collab_device
                WHERE tenant_id = :tenantId AND deleted = b'0'
                """, params);
        int onlineDeviceCount = queryInt("""
                SELECT COUNT(1)
                FROM iot_factory_collab_device
                WHERE tenant_id = :tenantId AND deleted = b'0' AND online_status = 1
                """, params);
        int faultDeviceCount = queryInt("""
                SELECT COUNT(1)
                FROM iot_factory_collab_device
                WHERE tenant_id = :tenantId AND deleted = b'0' AND health_status = '故障'
                """, params);
        int maintainCount = queryInt("""
                SELECT COUNT(1)
                FROM iot_factory_collab_maintenance_plan
                WHERE tenant_id = :tenantId AND deleted = b'0' AND status = 'IN_PROGRESS'
                """, params);

        List<FactoryCollaborationVO.MetricCardItem> metrics = List.of(
                buildMetricCard("total-device", "设备总数", String.valueOf(totalDeviceCount), "台", "设备台账", "设备列表", "ep:circle-check", "emerald"),
                buildMetricCard("online-device", "在线设备", String.valueOf(onlineDeviceCount), "台", "实时在线", "状态监控", "ep:connection", "cyan"),
                buildMetricCard("fault-device", "故障设备", String.valueOf(faultDeviceCount), "台", "异常待处理", "设备健康", "ep:warning", "violet"),
                buildMetricCard("maintain-device", "维保中", String.valueOf(maintainCount), "项", "维保计划", "维保闭环", "ep:tools", "amber")
        );

        return FactoryCollaborationVO.DeviceDashboardRespVO.builder()
                .updatedAt(LocalDateTime.now())
                .subTab(StrUtil.blankToDefault(reqVO.getSubTab(), "device-list"))
                .metrics(metrics)
                .deviceList(deviceList)
                .maintenancePlanList(maintenancePlanList)
                .detail(detail)
                .build();
    }

    /**
     * 新增设备
     *
     * @param reqVO 新增请求
     * @return 新增后的设备主键
     */
    @Transactional(rollbackFor = Exception.class)
    public Long createDevice(FactoryCollaborationVO.DeviceCreateReqVO reqVO) {
        MapSqlParameterSource params = auditParams();
        params.addValue("deviceCode", reqVO.getDeviceCode());
        params.addValue("deviceName", reqVO.getDeviceName());
        params.addValue("categoryName", reqVO.getCategoryName());
        params.addValue("modelName", reqVO.getModelName());
        params.addValue("areaName", reqVO.getAreaName());
        params.addValue("onlineStatus", Boolean.TRUE.equals(reqVO.getOnline()) ? 1 : 0);
        params.addValue("runningStatus", StrUtil.blankToDefault(reqVO.getRunningStatus(), "运行中"));
        params.addValue("healthStatus", "正常");
        params.addValue("efficiencyRate", reqVO.getEfficiencyRate() == null ? BigDecimal.valueOf(88) : reqVO.getEfficiencyRate());

        KeyHolder keyHolder = new GeneratedKeyHolder();
        namedParameterJdbcTemplate.update("""
                INSERT INTO iot_factory_collab_device (
                    tenant_id, device_code, device_name, category_name, model_name, area_name,
                    online_status, running_status, health_status, efficiency_rate, remark,
                    creator, create_time, updater, update_time, deleted
                ) VALUES (
                    :tenantId, :deviceCode, :deviceName, :categoryName, :modelName, :areaName,
                    :onlineStatus, :runningStatus, :healthStatus, :efficiencyRate, '由业务协同工作台创建',
                    :operator, NOW(), :operator, NOW(), b'0'
                )
                """, params, keyHolder, new String[]{"id"});
        return keyHolder.getKey() != null ? keyHolder.getKey().longValue() : null;
    }

    /**
     * 新增维保计划并同步生成待执行工单
     *
     * @param reqVO 新增请求
     * @return 维保计划主键
     */
    @Transactional(rollbackFor = Exception.class)
    public Long createMaintenancePlan(FactoryCollaborationVO.MaintenancePlanCreateReqVO reqVO) {
        MapSqlParameterSource params = auditParams();
        params.addValue("deviceId", reqVO.getDeviceId());
        params.addValue("planName", reqVO.getPlanName());
        params.addValue("cycleType", reqVO.getCycleType());
        params.addValue("nextExecuteDate", reqVO.getNextExecuteDate());
        params.addValue("ownerName", reqVO.getOwnerName());
        params.addValue("status", "PLANNED");

        KeyHolder keyHolder = new GeneratedKeyHolder();
        namedParameterJdbcTemplate.update("""
                INSERT INTO iot_factory_collab_maintenance_plan (
                    tenant_id, device_id, plan_name, cycle_type, last_execute_date, next_execute_date,
                    owner_name, status, creator, create_time, updater, update_time, deleted
                ) VALUES (
                    :tenantId, :deviceId, :planName, :cycleType, NULL, :nextExecuteDate,
                    :ownerName, :status, :operator, NOW(), :operator, NOW(), b'0'
                )
                """, params, keyHolder, new String[]{"id"});
        Long planId = keyHolder.getKey() != null ? keyHolder.getKey().longValue() : null;
        if (planId == null) {
            throw new IllegalStateException("维保计划创建失败，未返回主键");
        }

        params.addValue("planId", planId);
        params.addValue("orderCode", buildMaintenanceOrderCode(planId));
        namedParameterJdbcTemplate.update("""
                INSERT INTO iot_factory_collab_maintenance_order (
                    tenant_id, plan_id, device_id, order_code, scheduled_date, completed_date, status, result, remark,
                    creator, create_time, updater, update_time, deleted
                ) VALUES (
                    :tenantId, :planId, :deviceId, :orderCode, :nextExecuteDate, NULL, 'PENDING', '', '',
                    :operator, NOW(), :operator, NOW(), b'0'
                )
                """, params);
        return planId;
    }

    /**
     * 完成维保工单，并滚动下次执行日期
     *
     * @param reqVO 工单完成请求
     */
    @Transactional(rollbackFor = Exception.class)
    public void completeMaintenanceOrder(FactoryCollaborationVO.MaintenanceOrderCompleteReqVO reqVO) {
        MapSqlParameterSource params = auditParams();
        params.addValue("orderId", reqVO.getOrderId());
        params.addValue("result", reqVO.getResult());
        params.addValue("remark", StrUtil.blankToDefault(reqVO.getRemark(), ""));

        Map<String, Object> orderInfo = namedParameterJdbcTemplate.query("""
                SELECT o.plan_id, p.cycle_type, p.next_execute_date
                FROM iot_factory_collab_maintenance_order o
                INNER JOIN iot_factory_collab_maintenance_plan p
                        ON p.id = o.plan_id AND p.deleted = b'0' AND p.tenant_id = o.tenant_id
                WHERE o.tenant_id = :tenantId
                  AND o.id = :orderId
                  AND o.deleted = b'0'
                LIMIT 1
                """, params, rs -> {
            if (!rs.next()) {
                return null;
            }
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("planId", rs.getLong("plan_id"));
            map.put("cycleType", rs.getString("cycle_type"));
            map.put("nextExecuteDate", rs.getObject("next_execute_date", LocalDate.class));
            return map;
        });
        if (orderInfo == null) {
            return;
        }

        LocalDate currentNextExecuteDate = (LocalDate) orderInfo.get("nextExecuteDate");
        String cycleType = Objects.toString(orderInfo.get("cycleType"), "月度");
        LocalDate nextExecuteDate = addCycle(currentNextExecuteDate, cycleType);
        Long planId = Long.parseLong(String.valueOf(orderInfo.get("planId")));

        namedParameterJdbcTemplate.update("""
                UPDATE iot_factory_collab_maintenance_order
                SET status = 'COMPLETED',
                    completed_date = CURDATE(),
                    result = :result,
                    remark = :remark,
                    updater = :operator,
                    update_time = NOW()
                WHERE tenant_id = :tenantId
                  AND id = :orderId
                  AND deleted = b'0'
                """, params);

        params.addValue("planId", planId);
        params.addValue("nextExecuteDate", nextExecuteDate);
        namedParameterJdbcTemplate.update("""
                UPDATE iot_factory_collab_maintenance_plan
                SET last_execute_date = CURDATE(),
                    next_execute_date = :nextExecuteDate,
                    status = 'PLANNED',
                    updater = :operator,
                    update_time = NOW()
                WHERE tenant_id = :tenantId
                  AND id = :planId
                  AND deleted = b'0'
                """, params);
    }

    /**
     * 查询碳资产工作台数据
     *
     * @param reqVO 查询参数
     * @return 碳资产工作台数据
     */
    public FactoryCollaborationVO.CarbonDashboardRespVO getCarbonDashboard(
            FactoryCollaborationVO.CarbonDashboardReqVO reqVO
    ) {
        LocalDate statDate = reqVO.getStatDate() != null ? reqVO.getStatDate() : LocalDate.now();
        MapSqlParameterSource params = baseParams();
        params.addValue("statDate", statDate);
        params.addValue("targetYear", statDate.getYear());

        BigDecimal currentMonthEmission = queryDecimal("""
                SELECT COALESCE(SUM(emission_value), 0)
                FROM iot_factory_collab_carbon_record
                WHERE tenant_id = :tenantId
                  AND deleted = b'0'
                  AND YEAR(stat_date) = YEAR(:statDate)
                  AND MONTH(stat_date) = MONTH(:statDate)
                """, params);
        BigDecimal unitEmission = queryDecimal("""
                SELECT COALESCE(AVG(unit_emission_value), 0)
                FROM iot_factory_collab_carbon_record
                WHERE tenant_id = :tenantId
                  AND deleted = b'0'
                  AND YEAR(stat_date) = YEAR(:statDate)
                  AND MONTH(stat_date) = MONTH(:statDate)
                """, params);
        Map<String, Object> targetInfo = namedParameterJdbcTemplate.query("""
                SELECT annual_target_value, monthly_target_value
                FROM iot_factory_collab_carbon_target
                WHERE tenant_id = :tenantId
                  AND deleted = b'0'
                  AND target_year = :targetYear
                ORDER BY id DESC
                LIMIT 1
                """, params, rs -> {
            if (!rs.next()) {
                return Map.of("annualTarget", BigDecimal.ZERO, "monthlyTarget", BigDecimal.ONE);
            }
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("annualTarget", getBigDecimal(rs, "annual_target_value"));
            result.put("monthlyTarget", getBigDecimal(rs, "monthly_target_value"));
            return result;
        });
        BigDecimal annualTarget = (BigDecimal) targetInfo.get("annualTarget");
        BigDecimal monthlyTarget = (BigDecimal) targetInfo.get("monthlyTarget");
        BigDecimal metricCompletionRate = dividePercent(currentMonthEmission, monthlyTarget);
        BigDecimal annualCompletionRate = dividePercent(currentMonthEmission, annualTarget);
        BigDecimal remainingValue = annualTarget.subtract(currentMonthEmission);

        List<FactoryCollaborationVO.CarbonTrendItem> trendList = namedParameterJdbcTemplate.query("""
                SELECT DATE_FORMAT(stat_date, '%c月') AS label,
                       SUM(emission_value) AS emission_value,
                       MAX(target_value) AS target_value,
                       MIN(stat_date) AS first_date
                FROM iot_factory_collab_carbon_record
                WHERE tenant_id = :tenantId
                  AND deleted = b'0'
                GROUP BY YEAR(stat_date), MONTH(stat_date), DATE_FORMAT(stat_date, '%c月')
                ORDER BY first_date ASC
                LIMIT 12
                """, params, (rs, rowNum) -> FactoryCollaborationVO.CarbonTrendItem.builder()
                .label(rs.getString("label"))
                .emissionValue(getBigDecimal(rs, "emission_value"))
                .targetValue(getBigDecimal(rs, "target_value"))
                .build());

        List<FactoryCollaborationVO.CarbonSourceItem> sourceList = namedParameterJdbcTemplate.query("""
                SELECT MIN(id) AS id, source_name, source_type, SUM(emission_value) AS emission_value
                FROM iot_factory_collab_carbon_record
                WHERE tenant_id = :tenantId
                  AND deleted = b'0'
                  AND YEAR(stat_date) = YEAR(:statDate)
                  AND MONTH(stat_date) = MONTH(:statDate)
                GROUP BY source_name, source_type
                ORDER BY emission_value DESC, source_name ASC
                """, params, rs -> {
            List<FactoryCollaborationVO.CarbonSourceItem> items = new ArrayList<>();
            BigDecimal total = currentMonthEmission.compareTo(BigDecimal.ZERO) == 0 ? BigDecimal.ONE : currentMonthEmission;
            while (rs.next()) {
                BigDecimal sourceValue = getBigDecimal(rs, "emission_value");
                items.add(FactoryCollaborationVO.CarbonSourceItem.builder()
                        .id(rs.getLong("id"))
                        .sourceName(rs.getString("source_name"))
                        .sourceType(rs.getString("source_type"))
                        .emissionValue(sourceValue)
                        .proportion(sourceValue.multiply(BigDecimal.valueOf(100)).divide(total, 2, RoundingMode.HALF_UP))
                        .build());
            }
            return items;
        });

        List<FactoryCollaborationVO.CarbonTradeItem> tradeList = namedParameterJdbcTemplate.query("""
                SELECT id, trade_code, trade_type, quantity, unit_price, amount, balance_after,
                       trade_date, counterparty, status
                FROM iot_factory_collab_carbon_trade
                WHERE tenant_id = :tenantId
                  AND deleted = b'0'
                ORDER BY trade_date DESC, id DESC
                LIMIT 20
                """, params, carbonTradeRowMapper());

        List<FactoryCollaborationVO.MetricCardItem> metrics = List.of(
                buildMetricCard("month-emission", "本月碳排", formatDecimal(currentMonthEmission), "tCO2",
                        "碳排核算", "月度视图", "ep:leaf", "emerald"),
                buildMetricCard("unit-emission", "单位排放", formatDecimal(unitEmission), "tCO2/t",
                        "单位产量碳排", "排放分析", "ep:data-analysis", "cyan"),
                buildMetricCard("annual-target", "年度目标", formatDecimal(annualTarget), "tCO2",
                        "年度目标约束", "目标管理", "ep:aim", "amber"),
                buildMetricCard("completion-rate", "目标完成率", formatDecimal(metricCompletionRate), "%",
                        "按月目标口径", "目标进度", "ep:trend-charts", "violet")
        );

        return FactoryCollaborationVO.CarbonDashboardRespVO.builder()
                .updatedAt(LocalDateTime.now())
                .subTab(StrUtil.blankToDefault(reqVO.getSubTab(), "carbon-overview"))
                .metrics(metrics)
                .trendList(trendList)
                .sourceList(sourceList)
                .tradeList(tradeList)
                .targetCard(FactoryCollaborationVO.CarbonTargetCard.builder()
                        .annualTargetValue(annualTarget)
                        .emittedValue(currentMonthEmission)
                        .remainingValue(remainingValue)
                        .completionRate(annualCompletionRate)
                        .build())
                .build();
    }

    /**
     * 新增碳交易登记
     *
     * @param reqVO 新增请求
     * @return 交易主键
     */
    @Transactional(rollbackFor = Exception.class)
    public Long createCarbonTrade(FactoryCollaborationVO.CarbonTradeCreateReqVO reqVO) {
        MapSqlParameterSource params = auditParams();
        params.addValue("tradeCode", reqVO.getTradeCode());
        params.addValue("tradeType", reqVO.getTradeType());
        params.addValue("quantity", reqVO.getQuantity());
        params.addValue("unitPrice", reqVO.getUnitPrice());
        params.addValue("tradeDate", reqVO.getTradeDate());
        params.addValue("counterparty", reqVO.getCounterparty());

        BigDecimal latestBalance = queryDecimal("""
                SELECT COALESCE(balance_after, 0)
                FROM iot_factory_collab_carbon_trade
                WHERE tenant_id = :tenantId
                  AND deleted = b'0'
                ORDER BY trade_date DESC, id DESC
                LIMIT 1
                """, params);
        BigDecimal delta = "BUY".equalsIgnoreCase(reqVO.getTradeType())
                ? reqVO.getQuantity()
                : reqVO.getQuantity().negate();
        BigDecimal balanceAfter = latestBalance.add(delta);
        BigDecimal amount = reqVO.getQuantity().multiply(reqVO.getUnitPrice()).setScale(2, RoundingMode.HALF_UP);
        params.addValue("amount", amount);
        params.addValue("balanceAfter", balanceAfter);

        KeyHolder keyHolder = new GeneratedKeyHolder();
        namedParameterJdbcTemplate.update("""
                INSERT INTO iot_factory_collab_carbon_trade (
                    tenant_id, trade_code, trade_type, quantity, unit_price, amount, balance_after,
                    trade_date, counterparty, status, creator, create_time, updater, update_time, deleted
                ) VALUES (
                    :tenantId, :tradeCode, :tradeType, :quantity, :unitPrice, :amount, :balanceAfter,
                    :tradeDate, :counterparty, '已登记', :operator, NOW(), :operator, NOW(), b'0'
                )
                """, params, keyHolder, new String[]{"id"});
        return keyHolder.getKey() != null ? keyHolder.getKey().longValue() : null;
    }

    /**
     * 构建顶部指标卡
     *
     * @param key 唯一键
     * @param title 标题
     * @param value 展示值
     * @param unit 单位
     * @param hint 说明
     * @param trend 趋势
     * @param icon 图标
     * @param theme 主题
     * @return 指标卡
     */
    private FactoryCollaborationVO.MetricCardItem buildMetricCard(
            String key,
            String title,
            String value,
            String unit,
            String hint,
            String trend,
            String icon,
            String theme
    ) {
        return FactoryCollaborationVO.MetricCardItem.builder()
                .key(key)
                .title(title)
                .value(value)
                .unit(unit)
                .hint(hint)
                .trend(trend)
                .icon(icon)
                .theme(theme)
                .build();
    }

    /**
     * 构建基础租户参数
     *
     * @return 参数对象
     */
    private MapSqlParameterSource baseParams() {
        return new MapSqlParameterSource().addValue("tenantId", getTenantId());
    }

    /**
     * 构建带审计字段的参数
     *
     * @return 参数对象
     */
    private MapSqlParameterSource auditParams() {
        return baseParams().addValue("operator", getOperator());
    }

    /**
     * 获取当前租户编号
     *
     * @return 租户编号
     */
    private Long getTenantId() {
        return TenantContextHolder.getTenantId() != null ? TenantContextHolder.getTenantId() : DEFAULT_TENANT_ID;
    }

    /**
     * 获取当前操作人
     *
     * @return 操作人
     */
    private String getOperator() {
        return StrUtil.blankToDefault(SecurityFrameworkUtils.getLoginUserNickname(), "system");
    }

    /**
     * 规范化关键字
     *
     * @param keyword 原始关键字
     * @return 规范化结果
     */
    private String normalizeKeyword(String keyword) {
        return StrUtil.blankToDefault(StrUtil.trim(keyword), "");
    }

    /**
     * 构建模糊查询关键字
     *
     * @param keyword 原始关键字
     * @return 模糊查询值
     */
    private String buildLikeKeyword(String keyword) {
        return "%" + normalizeKeyword(keyword) + "%";
    }

    /**
     * 解析能源二级 Tab 对应的能源类型
     *
     * @param subTab 二级 Tab
     * @return 能源类型
     */
    private String resolveEnergyType(String subTab) {
        if ("water".equalsIgnoreCase(subTab)) {
            return "water";
        }
        if ("gas".equalsIgnoreCase(subTab)) {
            return "gas";
        }
        return "electricity";
    }

    /**
     * 解析能源单位
     *
     * @param energyType 能源类型
     * @return 单位
     */
    private String resolveEnergyUnit(String energyType) {
        if ("electricity".equalsIgnoreCase(energyType)) {
            return "kWh";
        }
        return "m³";
    }

    /**
     * 查询整数值
     *
     * @param sql SQL
     * @param params 参数
     * @return 结果
     */
    private int queryInt(String sql, MapSqlParameterSource params) {
        return namedParameterJdbcTemplate.query(sql, params, rs -> rs.next() ? rs.getInt(1) : 0);
    }

    /**
     * 查询十进制值
     *
     * @param sql SQL
     * @param params 参数
     * @return 结果
     */
    private BigDecimal queryDecimal(String sql, MapSqlParameterSource params) {
        return namedParameterJdbcTemplate.query(sql, params, rs -> rs.next() ? rs.getBigDecimal(1) : BigDecimal.ZERO);
    }

    /**
     * 格式化普通数值
     *
     * @param value 数值
     * @return 展示文本
     */
    private String formatDecimal(BigDecimal value) {
        BigDecimal safeValue = value == null ? BigDecimal.ZERO : value;
        return safeValue.stripTrailingZeros().toPlainString();
    }

    /**
     * 格式化百分比
     *
     * @param value 数值
     * @return 展示文本
     */
    private String formatPercent(BigDecimal value) {
        return formatDecimal(value.setScale(1, RoundingMode.HALF_UP)) + "%";
    }

    /**
     * 格式化带符号百分比
     *
     * @param value 数值
     * @return 展示文本
     */
    private String formatSignedPercent(BigDecimal value) {
        BigDecimal safeValue = value == null ? BigDecimal.ZERO : value.setScale(1, RoundingMode.HALF_UP);
        String prefix = safeValue.compareTo(BigDecimal.ZERO) > 0 ? "+" : "";
        return prefix + safeValue.stripTrailingZeros().toPlainString() + "%";
    }

    /**
     * 计算百分比结果
     *
     * @param numerator 分子
     * @param denominator 分母
     * @return 百分比
     */
    private BigDecimal dividePercent(BigDecimal numerator, BigDecimal denominator) {
        if (denominator == null || denominator.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        BigDecimal safeNumerator = numerator == null ? BigDecimal.ZERO : numerator;
        return safeNumerator.multiply(BigDecimal.valueOf(100)).divide(denominator, 1, RoundingMode.HALF_UP);
    }

    /**
     * 计算维保周期的下次执行日期
     *
     * @param currentDate 当前执行日期
     * @param cycleType 周期类型
     * @return 下次执行日期
     */
    private LocalDate addCycle(LocalDate currentDate, String cycleType) {
        LocalDate safeDate = currentDate != null ? currentDate : LocalDate.now();
        if ("周度".equals(cycleType)) {
            return safeDate.plusWeeks(1);
        }
        if ("季度".equals(cycleType)) {
            return safeDate.plusMonths(3);
        }
        return safeDate.plusMonths(1);
    }

    /**
     * 生成维保工单编号
     *
     * @param planId 计划主键
     * @return 工单编号
     */
    private String buildMaintenanceOrderCode(Long planId) {
        return "MO-" + LocalDate.now().toString().replace("-", "") + "-" + planId;
    }

    /**
     * 生产计划行映射器
     *
     * @return 映射器
     */
    private RowMapper<FactoryCollaborationVO.ProductionPlanItem> productionPlanRowMapper() {
        return (rs, rowNum) -> FactoryCollaborationVO.ProductionPlanItem.builder()
                .id(rs.getLong("id"))
                .planCode(rs.getString("plan_code"))
                .productName(rs.getString("product_name"))
                .batchCode(rs.getString("batch_code"))
                .plannedQuantity(rs.getInt("planned_quantity"))
                .operatorName(rs.getString("operator_name"))
                .lineName(rs.getString("line_name"))
                .plannedStartTime(rs.getObject("planned_start_time", LocalDateTime.class))
                .status(rs.getString("status"))
                .progress(rs.getInt("progress"))
                .build();
    }

    /**
     * 批次追踪行映射器
     *
     * @return 映射器
     */
    private RowMapper<FactoryCollaborationVO.ProductionBatchItem> productionBatchRowMapper() {
        return (rs, rowNum) -> FactoryCollaborationVO.ProductionBatchItem.builder()
                .id(rs.getLong("id"))
                .planId(rs.getLong("plan_id"))
                .batchCode(rs.getString("batch_code"))
                .productName(rs.getString("product_name"))
                .currentProcess(rs.getString("current_process"))
                .currentLocation(rs.getString("current_location"))
                .completedQuantity(rs.getInt("completed_quantity"))
                .yieldRate(getBigDecimal(rs, "yield_rate"))
                .status(rs.getString("status"))
                .updatedAt(rs.getObject("trace_time", LocalDateTime.class))
                .build();
    }

    /**
     * 生产告警映射器
     *
     * @return 映射器
     */
    private RowMapper<FactoryCollaborationVO.ProductionAlertItem> productionAlertRowMapper() {
        return (rs, rowNum) -> FactoryCollaborationVO.ProductionAlertItem.builder()
                .id(rs.getLong("id"))
                .alertTitle(rs.getString("alert_title"))
                .levelLabel(rs.getString("level_label"))
                .lineName(rs.getString("line_name"))
                .status(rs.getString("status"))
                .handlerName(rs.getString("handler_name"))
                .happenedAt(rs.getObject("happened_at", LocalDateTime.class))
                .build();
    }

    /**
     * 批次环境记录映射器
     *
     * @return 映射器
     */
    private RowMapper<FactoryCollaborationVO.BatchEnvironmentRecordItem> batchEnvironmentRowMapper() {
        return (rs, rowNum) -> FactoryCollaborationVO.BatchEnvironmentRecordItem.builder()
                .id(rs.getLong("id"))
                .recordTime(rs.getObject("record_time", LocalDateTime.class))
                .temperatureValue(getBigDecimal(rs, "temperature_value"))
                .humidityValue(getBigDecimal(rs, "humidity_value"))
                .pressureValue(getBigDecimal(rs, "pressure_value"))
                .phValue(getBigDecimal(rs, "ph_value"))
                .cleanLevel(rs.getString("clean_level"))
                .recorderName(rs.getString("recorder_name"))
                .build();
    }

    /**
     * 批次质量记录映射器
     *
     * @return 映射器
     */
    private RowMapper<FactoryCollaborationVO.BatchQualityRecordItem> batchQualityRowMapper() {
        return (rs, rowNum) -> FactoryCollaborationVO.BatchQualityRecordItem.builder()
                .id(rs.getLong("id"))
                .sampleName(rs.getString("sample_name"))
                .inspectionItem(rs.getString("inspection_item"))
                .standardValue(rs.getString("standard_value"))
                .measuredValue(rs.getString("measured_value"))
                .resultStatus(rs.getString("result_status"))
                .recordTime(rs.getObject("record_time", LocalDateTime.class))
                .inspectorName(rs.getString("inspector_name"))
                .build();
    }

    /**
     * 批次人员记录映射器
     *
     * @return 映射器
     */
    private RowMapper<FactoryCollaborationVO.BatchPersonnelRecordItem> batchPersonnelRowMapper() {
        return (rs, rowNum) -> FactoryCollaborationVO.BatchPersonnelRecordItem.builder()
                .id(rs.getLong("id"))
                .roleName(rs.getString("role_name"))
                .staffName(rs.getString("staff_name"))
                .operationName(rs.getString("operation_name"))
                .workstationName(rs.getString("workstation_name"))
                .recordTime(rs.getObject("record_time", LocalDateTime.class))
                .durationMinutes(rs.getInt("duration_minutes"))
                .remark(rs.getString("remark"))
                .build();
    }

    /**
     * 批次设备记录映射器
     *
     * @return 映射器
     */
    private RowMapper<FactoryCollaborationVO.BatchDeviceRecordItem> batchDeviceRowMapper() {
        return (rs, rowNum) -> FactoryCollaborationVO.BatchDeviceRecordItem.builder()
                .id(rs.getLong("id"))
                .deviceCode(rs.getString("device_code"))
                .deviceName(rs.getString("device_name"))
                .operationName(rs.getString("operation_name"))
                .runningStatus(rs.getString("running_status"))
                .parameterSummary(rs.getString("parameter_summary"))
                .recordTime(rs.getObject("record_time", LocalDateTime.class))
                .operatorName(rs.getString("operator_name"))
                .build();
    }

    /**
     * 批次原料记录映射器
     *
     * @return 映射器
     */
    private RowMapper<FactoryCollaborationVO.BatchMaterialRecordItem> batchMaterialRowMapper() {
        return (rs, rowNum) -> FactoryCollaborationVO.BatchMaterialRecordItem.builder()
                .id(rs.getLong("id"))
                .materialCode(rs.getString("material_code"))
                .materialName(rs.getString("material_name"))
                .materialType(rs.getString("material_type"))
                .materialBatchNo(rs.getString("material_batch_no"))
                .plannedQuantity(getBigDecimal(rs, "planned_quantity"))
                .actualQuantity(getBigDecimal(rs, "actual_quantity"))
                .unit(rs.getString("unit"))
                .feederName(rs.getString("feeder_name"))
                .recordTime(rs.getObject("record_time", LocalDateTime.class))
                .build();
    }

    /**
     * 能源趋势映射器
     *
     * @return 映射器
     */
    private RowMapper<FactoryCollaborationVO.EnergyTrendItem> energyTrendRowMapper() {
        return (rs, rowNum) -> FactoryCollaborationVO.EnergyTrendItem.builder()
                .label(rs.getString("label"))
                .electricityValue(getBigDecimal(rs, "electricity_value"))
                .waterValue(getBigDecimal(rs, "water_value"))
                .gasValue(getBigDecimal(rs, "gas_value"))
                .currentValue(getBigDecimal(rs, "current_value"))
                .build();
    }

    /**
     * 设备列表映射器
     *
     * @return 映射器
     */
    private RowMapper<FactoryCollaborationVO.DeviceItem> deviceRowMapper() {
        return (rs, rowNum) -> FactoryCollaborationVO.DeviceItem.builder()
                .id(rs.getLong("id"))
                .deviceCode(rs.getString("device_code"))
                .deviceName(rs.getString("device_name"))
                .categoryName(rs.getString("category_name"))
                .areaName(rs.getString("area_name"))
                .online(rs.getInt("online_status") == 1)
                .runningStatus(rs.getString("running_status"))
                .healthStatus(rs.getString("health_status"))
                .efficiencyRate(getBigDecimal(rs, "efficiency_rate"))
                .statusText(rs.getInt("online_status") == 1 ? "在线" : "离线")
                .build();
    }

    /**
     * 碳交易映射器
     *
     * @return 映射器
     */
    private RowMapper<FactoryCollaborationVO.CarbonTradeItem> carbonTradeRowMapper() {
        return (rs, rowNum) -> FactoryCollaborationVO.CarbonTradeItem.builder()
                .id(rs.getLong("id"))
                .tradeCode(rs.getString("trade_code"))
                .tradeType(rs.getString("trade_type"))
                .quantity(getBigDecimal(rs, "quantity"))
                .unitPrice(getBigDecimal(rs, "unit_price"))
                .amount(getBigDecimal(rs, "amount"))
                .balanceAfter(getBigDecimal(rs, "balance_after"))
                .tradeDate(rs.getObject("trade_date", LocalDate.class))
                .counterparty(rs.getString("counterparty"))
                .status(rs.getString("status"))
                .build();
    }

    /**
     * 安全获取 Long 字段
     *
     * @param rs 结果集
     * @param column 字段名
     * @return 值
     * @throws SQLException SQL 异常
     */
    private Long getLong(ResultSet rs, String column) throws SQLException {
        long value = rs.getLong(column);
        return rs.wasNull() ? null : value;
    }

    /**
     * 安全获取 BigDecimal 字段
     *
     * @param rs 结果集
     * @param column 字段名
     * @return 值
     * @throws SQLException SQL 异常
     */
    private BigDecimal getBigDecimal(ResultSet rs, String column) throws SQLException {
        BigDecimal value = rs.getBigDecimal(column);
        return value == null ? BigDecimal.ZERO : value;
    }

}
