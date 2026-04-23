package cn.iocoder.yudao.module.iot.service.factory;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import cn.iocoder.yudao.framework.tenant.core.context.TenantContextHolder;
import cn.iocoder.yudao.module.iot.controller.admin.factory.vo.compliance.FactoryComplianceExportVO;
import cn.iocoder.yudao.module.iot.controller.admin.factory.vo.compliance.FactoryComplianceVO;
import jakarta.annotation.Resource;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 文件说明：智慧工厂合规管理聚合服务
 *
 * <p>说明：合规管理页面强调原型克隆与真实数据驱动，
 * 因此使用 JDBC 直接聚合 GMP 合规、环保监测、合规批次追溯三类表，快速形成稳定闭环。</p>
 *
 * @author GPT-5.4
 */
@Service
@Validated
public class FactoryComplianceService {

    /**
     * 默认租户编号
     */
    private static final long DEFAULT_TENANT_ID = 1L;

    /**
     * GMP Tab
     */
    public static final String TAB_GMP = "gmp";

    /**
     * 环保监测 Tab
     */
    public static final String TAB_ENVIRONMENT = "environment";

    /**
     * 合规批次追溯 Tab
     */
    public static final String TAB_BATCH_TRACE = "batch-trace";

    @Resource
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    /**
     * 获取合规管理工作台数据
     *
     * @param reqVO 查询参数
     * @return 工作台数据
     */
    public FactoryComplianceVO.ComplianceDashboardRespVO getComplianceDashboard(
            FactoryComplianceVO.ComplianceDashboardReqVO reqVO
    ) {
        String tab = resolveComplianceTab(reqVO.getTab());
        MapSqlParameterSource params = baseParams();
        params.addValue("keyword", normalizeKeyword(reqVO.getKeyword()));
        params.addValue("likeKeyword", buildLikeKeyword(reqVO.getKeyword()));

        FactoryComplianceVO.ComplianceDashboardRespVO.ComplianceDashboardRespVOBuilder builder =
                FactoryComplianceVO.ComplianceDashboardRespVO.builder()
                        .updatedAt(LocalDateTime.now())
                        .tab(tab);

        if (TAB_ENVIRONMENT.equals(tab)) {
            builder.metrics(buildEnvironmentalMetrics(params))
                    .gmpOverview(emptyGmpOverview())
                    .environmentalOverview(queryEnvironmentalOverview(params))
                    .batchOverview(emptyBatchOverview());
        } else if (TAB_BATCH_TRACE.equals(tab)) {
            builder.metrics(buildBatchMetrics(params))
                    .gmpOverview(emptyGmpOverview())
                    .environmentalOverview(emptyEnvironmentalOverview())
                    .batchOverview(queryBatchOverview(params));
        } else {
            builder.metrics(buildGmpMetrics(params))
                    .gmpOverview(queryGmpOverview(params))
                    .environmentalOverview(emptyEnvironmentalOverview())
                    .batchOverview(emptyBatchOverview());
        }
        return builder.build();
    }

    /**
     * 获取合规历史记录
     *
     * @param reqVO 查询参数
     * @return 历史记录
     */
    public FactoryComplianceVO.ComplianceHistoryRespVO getComplianceHistory(
            FactoryComplianceVO.ComplianceHistoryReqVO reqVO
    ) {
        String tab = resolveComplianceTab(reqVO.getTab());
        MapSqlParameterSource params = baseParams();
        List<FactoryComplianceVO.ComplianceHistoryItem> historyList;
        if (TAB_ENVIRONMENT.equals(tab)) {
            historyList = namedParameterJdbcTemplate.query("""
                    SELECT id, '环保告警' AS event_type, title, status, handler_name, description, happened_at
                    FROM iot_factory_compliance_env_alert
                    WHERE tenant_id = :tenantId
                      AND deleted = b'0'
                    ORDER BY happened_at DESC, id DESC
                    LIMIT 20
                    """, params, complianceHistoryRowMapper());
        } else if (TAB_BATCH_TRACE.equals(tab)) {
            historyList = namedParameterJdbcTemplate.query("""
                    SELECT id, '批次异常' AS event_type, issue_title AS title, status,
                           owner_name AS handler_name, issue_desc AS description, happened_at
                    FROM iot_factory_compliance_batch_issue
                    WHERE tenant_id = :tenantId
                      AND deleted = b'0'
                    ORDER BY happened_at DESC, id DESC
                    LIMIT 20
                    """, params, complianceHistoryRowMapper());
        } else {
            historyList = namedParameterJdbcTemplate.query("""
                    SELECT id, 'GMP异常' AS event_type, title, status, handler_name, description, happened_at
                    FROM iot_factory_compliance_gmp_exception
                    WHERE tenant_id = :tenantId
                      AND deleted = b'0'
                    ORDER BY happened_at DESC, id DESC
                    LIMIT 20
                    """, params, complianceHistoryRowMapper());
        }
        return FactoryComplianceVO.ComplianceHistoryRespVO.builder()
                .updatedAt(LocalDateTime.now())
                .tab(tab)
                .historyList(historyList)
                .build();
    }

    /**
     * 解析导出场景下的标准 Tab
     *
     * @param tab 原始 Tab
     * @return 标准 Tab
     */
    public String resolveExportTab(String tab) {
        return resolveComplianceTab(tab);
    }

    /**
     * 获取 GMP 导出数据
     *
     * @param reqVO 查询参数
     * @return 导出行
     */
    public List<FactoryComplianceExportVO.GmpReportRow> getGmpExportRows(
            FactoryComplianceVO.ComplianceDashboardReqVO reqVO
    ) {
        MapSqlParameterSource params = buildQueryParams(reqVO);
        return namedParameterJdbcTemplate.query("""
                SELECT p.point_name, i.compliant_count, i.exceed_count, i.status, i.checked_at
                FROM iot_factory_compliance_gmp_inspection i
                INNER JOIN iot_factory_compliance_gmp_point p
                        ON p.id = i.point_id AND p.deleted = b'0' AND p.tenant_id = i.tenant_id
                WHERE i.tenant_id = :tenantId
                  AND i.deleted = b'0'
                  AND (
                      :keyword = ''
                      OR p.point_name LIKE :likeKeyword
                  )
                ORDER BY p.sort_no ASC, i.checked_at DESC
                """, params, (rs, rowNum) -> {
            FactoryComplianceExportVO.GmpReportRow row = new FactoryComplianceExportVO.GmpReportRow();
            row.setPointName(rs.getString("point_name"));
            row.setCompliantCount(rs.getInt("compliant_count"));
            row.setExceedCount(rs.getInt("exceed_count"));
            row.setStatus(rs.getString("status"));
            row.setLastCheckTime(rs.getObject("checked_at", LocalDateTime.class));
            row.setDetailText("GMP合规检查记录");
            return row;
        });
    }

    /**
     * 获取环保监测导出数据
     *
     * @param reqVO 查询参数
     * @return 导出行
     */
    public List<FactoryComplianceExportVO.EnvironmentalReportRow> getEnvironmentalExportRows(
            FactoryComplianceVO.ComplianceDashboardReqVO reqVO
    ) {
        MapSqlParameterSource params = buildQueryParams(reqVO);
        return namedParameterJdbcTemplate.query("""
                SELECT r.region_name, r.current_value, r.standard_value, r.exceed_count, r.status,
                       r.record_time, r.unit
                FROM iot_factory_compliance_env_record r
                WHERE r.tenant_id = :tenantId
                  AND r.deleted = b'0'
                  AND (
                      :keyword = ''
                      OR r.region_name LIKE :likeKeyword
                  )
                ORDER BY r.sort_no ASC, r.record_time DESC
                """, params, (rs, rowNum) -> {
            FactoryComplianceExportVO.EnvironmentalReportRow row = new FactoryComplianceExportVO.EnvironmentalReportRow();
            String unit = StrUtil.blankToDefault(rs.getString("unit"), "");
            row.setPointName(rs.getString("region_name"));
            row.setCurrentValue(formatPlain(getBigDecimal(rs, "current_value")) + unit);
            row.setStandardValue(formatPlain(getBigDecimal(rs, "standard_value")) + unit);
            row.setExceedCount(rs.getInt("exceed_count"));
            row.setStatus(rs.getString("status"));
            row.setLastCheckTime(rs.getObject("record_time", LocalDateTime.class));
            row.setDetailText("环保监测记录");
            return row;
        });
    }

    /**
     * 获取批次追溯导出数据
     *
     * @param reqVO 查询参数
     * @return 导出行
     */
    public List<FactoryComplianceExportVO.BatchTraceReportRow> getBatchTraceExportRows(
            FactoryComplianceVO.ComplianceDashboardReqVO reqVO
    ) {
        MapSqlParameterSource params = buildQueryParams(reqVO);
        return namedParameterJdbcTemplate.query("""
                SELECT b.batch_code, b.product_name, b.checkpoint_count, b.issue_count, b.status, b.last_check_time
                FROM iot_factory_compliance_batch_trace b
                WHERE b.tenant_id = :tenantId
                  AND b.deleted = b'0'
                  AND (
                      :keyword = ''
                      OR b.batch_code LIKE :likeKeyword
                      OR b.product_name LIKE :likeKeyword
                  )
                ORDER BY b.last_check_time DESC, b.id DESC
                """, params, (rs, rowNum) -> {
            FactoryComplianceExportVO.BatchTraceReportRow row = new FactoryComplianceExportVO.BatchTraceReportRow();
            row.setBatchCode(rs.getString("batch_code"));
            row.setProductName(rs.getString("product_name"));
            row.setCheckpointCount(rs.getInt("checkpoint_count"));
            row.setIssueCount(rs.getInt("issue_count"));
            row.setStatus(rs.getString("status"));
            row.setLastCheckTime(rs.getObject("last_check_time", LocalDateTime.class));
            row.setDetailText("批次合规追溯记录");
            return row;
        });
    }

    /**
     * 查询 GMP 总览
     *
     * @param params 参数
     * @return 总览
     */
    private FactoryComplianceVO.ComplianceGmpOverview queryGmpOverview(MapSqlParameterSource params) {
        List<FactoryComplianceVO.ComplianceSummaryCardItem> regionCards = namedParameterJdbcTemplate.query("""
                SELECT i.id, p.point_name, i.temperature_value, i.humidity_value, i.pressure_value,
                       i.compliance_rate, i.status
                FROM iot_factory_compliance_gmp_inspection i
                INNER JOIN iot_factory_compliance_gmp_point p
                        ON p.id = i.point_id AND p.deleted = b'0' AND p.tenant_id = i.tenant_id
                WHERE i.tenant_id = :tenantId
                  AND i.deleted = b'0'
                ORDER BY p.sort_no ASC, i.checked_at DESC
                LIMIT 3
                """, params, (rs, rowNum) -> FactoryComplianceVO.ComplianceSummaryCardItem.builder()
                .id(rs.getLong("id"))
                .title(rs.getString("point_name"))
                .subtitle("温度: " + formatPlain(getBigDecimal(rs, "temperature_value")) + "°C | 湿度: "
                        + formatPlain(getBigDecimal(rs, "humidity_value")) + "% | 压差: "
                        + formatPlain(getBigDecimal(rs, "pressure_value")) + "Pa")
                .progressRate(getBigDecimal(rs, "compliance_rate"))
                .progressText(formatPlain(getBigDecimal(rs, "compliance_rate")) + "%")
                .tone(resolveTone(rs.getString("status")))
                .build());

        List<FactoryComplianceVO.GmpInspectionRowItem> detailList = namedParameterJdbcTemplate.query("""
                SELECT i.id, p.point_name, i.compliant_count, i.exceed_count, i.status, i.checked_at
                FROM iot_factory_compliance_gmp_inspection i
                INNER JOIN iot_factory_compliance_gmp_point p
                        ON p.id = i.point_id AND p.deleted = b'0' AND p.tenant_id = i.tenant_id
                WHERE i.tenant_id = :tenantId
                  AND i.deleted = b'0'
                  AND (
                      :keyword = ''
                      OR p.point_name LIKE :likeKeyword
                  )
                ORDER BY p.sort_no ASC, i.checked_at DESC
                LIMIT 20
                """, params, (rs, rowNum) -> FactoryComplianceVO.GmpInspectionRowItem.builder()
                .id(rs.getLong("id"))
                .pointName(rs.getString("point_name"))
                .compliantCount(rs.getInt("compliant_count"))
                .exceedCount(rs.getInt("exceed_count"))
                .status(rs.getString("status"))
                .lastCheckTime(rs.getObject("checked_at", LocalDateTime.class))
                .detailText("最新检查记录")
                .build());
        return FactoryComplianceVO.ComplianceGmpOverview.builder()
                .regionCards(regionCards)
                .detailList(detailList)
                .build();
    }

    /**
     * 查询环保监测总览
     *
     * @param params 参数
     * @return 总览
     */
    private FactoryComplianceVO.ComplianceEnvironmentalOverview queryEnvironmentalOverview(MapSqlParameterSource params) {
        List<FactoryComplianceVO.ComplianceSummaryCardItem> regionCards = namedParameterJdbcTemplate.query("""
                SELECT r.id, r.region_name, r.current_value, r.standard_value, r.unit, r.compliance_rate, r.status
                FROM iot_factory_compliance_env_record r
                WHERE r.tenant_id = :tenantId
                  AND r.deleted = b'0'
                ORDER BY r.sort_no ASC, r.record_time DESC
                LIMIT 3
                """, params, (rs, rowNum) -> FactoryComplianceVO.ComplianceSummaryCardItem.builder()
                .id(rs.getLong("id"))
                .title(rs.getString("region_name"))
                .subtitle("监测值: " + formatPlain(getBigDecimal(rs, "current_value")) + rs.getString("unit")
                        + " | 标准值: " + formatPlain(getBigDecimal(rs, "standard_value")) + rs.getString("unit"))
                .progressRate(getBigDecimal(rs, "compliance_rate"))
                .progressText(formatPlain(getBigDecimal(rs, "compliance_rate")) + "%")
                .tone(resolveTone(rs.getString("status")))
                .build());

        List<FactoryComplianceVO.EnvironmentalMonitorRowItem> detailList = namedParameterJdbcTemplate.query("""
                SELECT r.id, r.region_name, r.current_value, r.standard_value, r.exceed_count, r.status,
                       r.record_time, r.unit
                FROM iot_factory_compliance_env_record r
                WHERE r.tenant_id = :tenantId
                  AND r.deleted = b'0'
                  AND (
                      :keyword = ''
                      OR r.region_name LIKE :likeKeyword
                  )
                ORDER BY r.sort_no ASC, r.record_time DESC
                LIMIT 20
                """, params, (rs, rowNum) -> FactoryComplianceVO.EnvironmentalMonitorRowItem.builder()
                .id(rs.getLong("id"))
                .pointName(rs.getString("region_name"))
                .currentValue(getBigDecimal(rs, "current_value"))
                .standardValue(getBigDecimal(rs, "standard_value"))
                .exceedCount(rs.getInt("exceed_count"))
                .status(rs.getString("status"))
                .lastCheckTime(rs.getObject("record_time", LocalDateTime.class))
                .detailText("环保监测详情")
                .unit(rs.getString("unit"))
                .build());
        return FactoryComplianceVO.ComplianceEnvironmentalOverview.builder()
                .regionCards(regionCards)
                .detailList(detailList)
                .build();
    }

    /**
     * 查询合规批次追溯总览
     *
     * @param params 参数
     * @return 总览
     */
    private FactoryComplianceVO.ComplianceBatchOverview queryBatchOverview(MapSqlParameterSource params) {
        List<FactoryComplianceVO.ComplianceSummaryCardItem> batchCards = namedParameterJdbcTemplate.query("""
                SELECT b.id, b.batch_code, b.product_name, b.compliance_rate, b.status, b.last_check_time
                FROM iot_factory_compliance_batch_trace b
                WHERE b.tenant_id = :tenantId
                  AND b.deleted = b'0'
                ORDER BY b.last_check_time DESC, b.id DESC
                LIMIT 3
                """, params, (rs, rowNum) -> FactoryComplianceVO.ComplianceSummaryCardItem.builder()
                .id(rs.getLong("id"))
                .title(rs.getString("batch_code"))
                .subtitle(rs.getString("product_name") + " | 最近检查: "
                        + formatDateTime(rs.getObject("last_check_time", LocalDateTime.class)))
                .progressRate(getBigDecimal(rs, "compliance_rate"))
                .progressText(formatPlain(getBigDecimal(rs, "compliance_rate")) + "%")
                .tone(resolveTone(rs.getString("status")))
                .build());

        List<FactoryComplianceVO.ComplianceBatchTraceRowItem> detailList = namedParameterJdbcTemplate.query("""
                SELECT b.id, b.batch_code, b.product_name, b.checkpoint_count, b.issue_count, b.status, b.last_check_time
                FROM iot_factory_compliance_batch_trace b
                WHERE b.tenant_id = :tenantId
                  AND b.deleted = b'0'
                  AND (
                      :keyword = ''
                      OR b.batch_code LIKE :likeKeyword
                      OR b.product_name LIKE :likeKeyword
                  )
                ORDER BY b.last_check_time DESC, b.id DESC
                LIMIT 20
                """, params, (rs, rowNum) -> FactoryComplianceVO.ComplianceBatchTraceRowItem.builder()
                .id(rs.getLong("id"))
                .batchCode(rs.getString("batch_code"))
                .productName(rs.getString("product_name"))
                .checkpointCount(rs.getInt("checkpoint_count"))
                .issueCount(rs.getInt("issue_count"))
                .status(rs.getString("status"))
                .lastCheckTime(rs.getObject("last_check_time", LocalDateTime.class))
                .detailText("批次合规追溯")
                .build());
        return FactoryComplianceVO.ComplianceBatchOverview.builder()
                .batchCards(batchCards)
                .detailList(detailList)
                .build();
    }

    /**
     * 构建 GMP 指标卡
     *
     * @param params 参数
     * @return 指标卡
     */
    private List<FactoryComplianceVO.ComplianceMetricItem> buildGmpMetrics(MapSqlParameterSource params) {
        int monitorCount = queryInt("""
                SELECT COUNT(1)
                FROM iot_factory_compliance_gmp_point
                WHERE tenant_id = :tenantId AND deleted = b'0'
                """, params);
        int exceedCount = queryInt("""
                SELECT COUNT(1)
                FROM iot_factory_compliance_gmp_exception
                WHERE tenant_id = :tenantId AND deleted = b'0'
                """, params);
        int recordCount = queryInt("""
                SELECT COUNT(1)
                FROM iot_factory_compliance_gmp_inspection
                WHERE tenant_id = :tenantId AND deleted = b'0'
                """, params);
        BigDecimal complianceRate = queryDecimal("""
                SELECT COALESCE(AVG(compliance_rate), 0)
                FROM iot_factory_compliance_gmp_inspection
                WHERE tenant_id = :tenantId AND deleted = b'0'
                """, params);
        return List.of(
                buildMetric("gmp-rate", "GMP合规率", formatPercent(complianceRate), "", "ep:circle-check", "emerald"),
                buildMetric("gmp-point", "监测点位", String.valueOf(monitorCount), "个", "ep:document", "cyan"),
                buildMetric("gmp-alert", "超标次数", String.valueOf(exceedCount), "次", "ep:warning", "amber"),
                buildMetric("gmp-record", "合规记录", String.valueOf(recordCount), "份", "ep:files", "violet")
        );
    }

    /**
     * 构建环保监测指标卡
     *
     * @param params 参数
     * @return 指标卡
     */
    private List<FactoryComplianceVO.ComplianceMetricItem> buildEnvironmentalMetrics(MapSqlParameterSource params) {
        int monitorCount = queryInt("""
                SELECT COUNT(1)
                FROM iot_factory_compliance_env_point
                WHERE tenant_id = :tenantId AND deleted = b'0'
                """, params);
        int exceedCount = queryInt("""
                SELECT COUNT(1)
                FROM iot_factory_compliance_env_alert
                WHERE tenant_id = :tenantId AND deleted = b'0'
                """, params);
        int recordCount = queryInt("""
                SELECT COUNT(1)
                FROM iot_factory_compliance_env_record
                WHERE tenant_id = :tenantId AND deleted = b'0'
                """, params);
        BigDecimal complianceRate = queryDecimal("""
                SELECT COALESCE(AVG(compliance_rate), 0)
                FROM iot_factory_compliance_env_record
                WHERE tenant_id = :tenantId AND deleted = b'0'
                """, params);
        return List.of(
                buildMetric("env-rate", "环保达标率", formatPercent(complianceRate), "", "ep:circle-check", "emerald"),
                buildMetric("env-point", "监测点位", String.valueOf(monitorCount), "个", "ep:monitor", "cyan"),
                buildMetric("env-alert", "告警次数", String.valueOf(exceedCount), "次", "ep:warning", "amber"),
                buildMetric("env-record", "监测记录", String.valueOf(recordCount), "条", "ep:files", "violet")
        );
    }

    /**
     * 构建批次追溯指标卡
     *
     * @param params 参数
     * @return 指标卡
     */
    private List<FactoryComplianceVO.ComplianceMetricItem> buildBatchMetrics(MapSqlParameterSource params) {
        int batchCount = queryInt("""
                SELECT COUNT(1)
                FROM iot_factory_compliance_batch_trace
                WHERE tenant_id = :tenantId AND deleted = b'0'
                """, params);
        int issueCount = queryInt("""
                SELECT COUNT(1)
                FROM iot_factory_compliance_batch_issue
                WHERE tenant_id = :tenantId AND deleted = b'0'
                """, params);
        int recordCount = queryInt("""
                SELECT COUNT(1)
                FROM iot_factory_compliance_batch_checkpoint
                WHERE tenant_id = :tenantId AND deleted = b'0'
                """, params);
        BigDecimal completionRate = queryDecimal("""
                SELECT COALESCE(AVG(compliance_rate), 0)
                FROM iot_factory_compliance_batch_trace
                WHERE tenant_id = :tenantId AND deleted = b'0'
                """, params);
        return List.of(
                buildMetric("batch-rate", "追溯完成率", formatPercent(completionRate), "", "ep:circle-check", "emerald"),
                buildMetric("batch-count", "合规批次", String.valueOf(batchCount), "条", "ep:tickets", "cyan"),
                buildMetric("batch-issue", "异常批次", String.valueOf(issueCount), "条", "ep:warning", "amber"),
                buildMetric("batch-record", "追溯记录", String.valueOf(recordCount), "条", "ep:files", "violet")
        );
    }

    /**
     * 空 GMP 总览
     *
     * @return 空对象
     */
    private FactoryComplianceVO.ComplianceGmpOverview emptyGmpOverview() {
        return FactoryComplianceVO.ComplianceGmpOverview.builder()
                .regionCards(List.of())
                .detailList(List.of())
                .build();
    }

    /**
     * 空环保总览
     *
     * @return 空对象
     */
    private FactoryComplianceVO.ComplianceEnvironmentalOverview emptyEnvironmentalOverview() {
        return FactoryComplianceVO.ComplianceEnvironmentalOverview.builder()
                .regionCards(List.of())
                .detailList(List.of())
                .build();
    }

    /**
     * 空批次总览
     *
     * @return 空对象
     */
    private FactoryComplianceVO.ComplianceBatchOverview emptyBatchOverview() {
        return FactoryComplianceVO.ComplianceBatchOverview.builder()
                .batchCards(List.of())
                .detailList(List.of())
                .build();
    }

    /**
     * 构建指标卡
     *
     * @param key 键
     * @param title 标题
     * @param value 值
     * @param unit 单位
     * @param icon 图标
     * @param theme 主题
     * @return 指标卡
     */
    private FactoryComplianceVO.ComplianceMetricItem buildMetric(
            String key,
            String title,
            String value,
            String unit,
            String icon,
            String theme
    ) {
        return FactoryComplianceVO.ComplianceMetricItem.builder()
                .key(key)
                .title(title)
                .value(value)
                .unit(unit)
                .icon(icon)
                .theme(theme)
                .build();
    }

    /**
     * 合规历史映射器
     *
     * @return 映射器
     */
    private RowMapper<FactoryComplianceVO.ComplianceHistoryItem> complianceHistoryRowMapper() {
        return (rs, rowNum) -> FactoryComplianceVO.ComplianceHistoryItem.builder()
                .id(rs.getLong("id"))
                .eventType(rs.getString("event_type"))
                .title(rs.getString("title"))
                .status(rs.getString("status"))
                .operatorName(rs.getString("handler_name"))
                .description(rs.getString("description"))
                .happenedAt(rs.getObject("happened_at", LocalDateTime.class))
                .build();
    }

    /**
     * 构建统一查询参数
     *
     * @param reqVO 查询参数
     * @return 参数对象
     */
    private MapSqlParameterSource buildQueryParams(FactoryComplianceVO.ComplianceDashboardReqVO reqVO) {
        MapSqlParameterSource params = baseParams();
        params.addValue("keyword", normalizeKeyword(reqVO.getKeyword()));
        params.addValue("likeKeyword", buildLikeKeyword(reqVO.getKeyword()));
        return params;
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
     * 解析合规 Tab
     *
     * @param tab 原始值
     * @return 标准值
     */
    private String resolveComplianceTab(String tab) {
        if (TAB_ENVIRONMENT.equalsIgnoreCase(tab)) {
            return TAB_ENVIRONMENT;
        }
        if (TAB_BATCH_TRACE.equalsIgnoreCase(tab)) {
            return TAB_BATCH_TRACE;
        }
        return TAB_GMP;
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
     * 格式化百分比
     *
     * @param value 数值
     * @return 文本
     */
    private String formatPercent(BigDecimal value) {
        return formatPlain(value == null ? BigDecimal.ZERO : value.setScale(0, RoundingMode.HALF_UP)) + "%";
    }

    /**
     * 格式化普通数值
     *
     * @param value 数值
     * @return 文本
     */
    private String formatPlain(BigDecimal value) {
        BigDecimal safeValue = value == null ? BigDecimal.ZERO : value;
        return safeValue.stripTrailingZeros().toPlainString();
    }

    /**
     * 格式化时间
     *
     * @param time 时间
     * @return 文本
     */
    private String formatDateTime(LocalDateTime time) {
        return time == null ? "--" : time.toString().replace('T', ' ');
    }

    /**
     * 解析卡片语义色
     *
     * @param status 状态
     * @return 语义色
     */
    private String resolveTone(String status) {
        if (StrUtil.containsAnyIgnoreCase(status, "预警", "注意", "异常")) {
            return "warning";
        }
        return "success";
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
