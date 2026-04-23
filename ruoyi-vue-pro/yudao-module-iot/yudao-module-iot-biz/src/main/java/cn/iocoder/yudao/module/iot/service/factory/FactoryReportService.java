package cn.iocoder.yudao.module.iot.service.factory;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil;
import cn.iocoder.yudao.framework.tenant.core.context.TenantContextHolder;
import cn.iocoder.yudao.module.iot.controller.admin.factory.vo.report.FactoryReportExportVO;
import cn.iocoder.yudao.module.iot.controller.admin.factory.vo.report.FactoryReportVO;
import jakarta.annotation.Resource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

/**
 * 文件说明：工厂报表中心聚合服务
 *
 * <p>说明：报表中心页面要求严格按原型落地且所有数据均来自真实数据库，
 * 因此当前实现采用 JDBC 直接聚合模板表与生成记录表，避免前端拼凑口径。</p>
 *
 * @author GPT-5.4
 */
@Service
@Validated
public class FactoryReportService {

    /**
     * 默认租户编号
     */
    private static final long DEFAULT_TENANT_ID = 1L;

    /**
     * 时间戳命名格式
     */
    private static final DateTimeFormatter FILE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");

    /**
     * 原型固定分类顺序
     */
    private static final List<String> CATEGORY_ORDER = List.of(
            "日报表",
            "周报表",
            "月报表",
            "设备报表",
            "能耗报表",
            "质量报表"
    );

    @Resource
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    /**
     * 获取报表中心工作台数据
     *
     * @param reqVO 查询参数
     * @return 工作台数据
     */
    public FactoryReportVO.ReportDashboardRespVO getDashboard(FactoryReportVO.ReportDashboardReqVO reqVO) {
        MapSqlParameterSource baseParams = baseParams();
        MapSqlParameterSource queryParams = buildQueryParams(reqVO);
        List<FactoryReportVO.ReportTemplateCardItem> templateCards = queryTemplateCards(queryParams);
        List<FactoryReportVO.ReportRecordRowItem> recentRecords = queryRecentRecords(queryParams);
        LocalDateTime updatedAt = resolveUpdatedAt(templateCards, recentRecords);
        return FactoryReportVO.ReportDashboardRespVO.builder()
                .updatedAt(updatedAt)
                .metrics(buildMetrics(baseParams))
                .categories(buildCategories())
                .templates(templateCards)
                .recentRecords(recentRecords)
                .build();
    }

    /**
     * 生成报表记录
     *
     * @param reqVO 生成参数
     * @return 生成结果
     */
    public FactoryReportVO.ReportGenerateRespVO generateReport(FactoryReportVO.ReportGenerateReqVO reqVO) {
        TemplateRow templateRow = getTemplateById(reqVO.getTemplateId());
        LocalDateTime now = LocalDateTime.now();
        LocalDate bizDate = reqVO.getBizDate() == null ? LocalDate.now() : reqVO.getBizDate();
        String reportName = templateRow.getTemplateName() + "-" + now.format(FILE_TIME_FORMATTER);
        String fileName = reportName + ".xls";
        String operatorName = "system";

        KeyHolder keyHolder = new GeneratedKeyHolder();
        namedParameterJdbcTemplate.update("""
                INSERT INTO iot_factory_report_record
                (tenant_id, template_id, report_name, report_category, status, biz_date, generated_at,
                 file_name, file_url, operator_name, remark, creator, updater)
                VALUES
                (:tenantId, :templateId, :reportName, :reportCategory, :status, :bizDate, :generatedAt,
                 :fileName, '', :operatorName, :remark, :creator, :updater)
                """,
                new MapSqlParameterSource()
                        .addValue("tenantId", getTenantId())
                        .addValue("templateId", templateRow.getId())
                        .addValue("reportName", reportName)
                        .addValue("reportCategory", templateRow.getTemplateCategory())
                        .addValue("status", "SUCCESS")
                        .addValue("bizDate", bizDate)
                        .addValue("generatedAt", now)
                        .addValue("fileName", fileName)
                        .addValue("operatorName", operatorName)
                        .addValue("remark", "手动生成")
                        .addValue("creator", operatorName)
                        .addValue("updater", operatorName),
                keyHolder,
                new String[]{"id"});

        Long recordId = keyHolder.getKey() == null ? null : keyHolder.getKey().longValue();
        if (recordId == null) {
            throw ServiceExceptionUtil.exception0(500, "报表记录创建失败");
        }
        namedParameterJdbcTemplate.update("""
                UPDATE iot_factory_report_record
                SET file_url = :fileUrl, updater = :updater
                WHERE id = :id AND tenant_id = :tenantId
                """,
                new MapSqlParameterSource()
                        .addValue("id", recordId)
                        .addValue("tenantId", getTenantId())
                        .addValue("fileUrl", "/admin-api/iot/factory/report/download?id=" + recordId)
                        .addValue("updater", operatorName));

        return FactoryReportVO.ReportGenerateRespVO.builder()
                .recordId(recordId)
                .status("SUCCESS")
                .reportName(reportName)
                .generatedAt(now)
                .build();
    }

    /**
     * 获取报表预览
     *
     * @param reqVO 预览参数
     * @return 预览数据
     */
    public FactoryReportVO.ReportPreviewRespVO getPreview(FactoryReportVO.ReportPreviewReqVO reqVO) {
        RecordDetailRow detailRow = getRecordDetail(reqVO.getId());
        return FactoryReportVO.ReportPreviewRespVO.builder()
                .recordId(detailRow.getId())
                .reportName(detailRow.getReportName())
                .category(detailRow.getReportCategory())
                .bizDate(detailRow.getBizDate())
                .generatedAt(detailRow.getGeneratedAt())
                .status(detailRow.getStatus())
                .operatorName(detailRow.getOperatorName())
                .templateId(detailRow.getTemplateId())
                .templateName(detailRow.getTemplateName())
                .templateDesc(detailRow.getTemplateDesc())
                .build();
    }

    /**
     * 构建下载导出行
     *
     * @param recordId 记录主键
     * @return 导出行
     */
    public List<FactoryReportExportVO.ReportExportRow> getExportRows(Long recordId) {
        RecordDetailRow detailRow = getRecordDetail(recordId);
        FactoryReportExportVO.ReportExportRow row = new FactoryReportExportVO.ReportExportRow();
        row.setReportName(detailRow.getReportName());
        row.setReportCategory(detailRow.getReportCategory());
        row.setTemplateName(detailRow.getTemplateName());
        row.setTemplateDesc(detailRow.getTemplateDesc());
        row.setStatus(detailRow.getStatus());
        row.setBizDate(detailRow.getBizDate());
        row.setGeneratedAt(detailRow.getGeneratedAt());
        row.setOperatorName(detailRow.getOperatorName());
        return List.of(row);
    }

    /**
     * 解析下载文件名
     *
     * @param recordId 记录主键
     * @return 文件名
     */
    public String getExportFileName(Long recordId) {
        RecordDetailRow detailRow = getRecordDetail(recordId);
        return StrUtil.blankToDefault(detailRow.getFileName(), detailRow.getReportName() + ".xls");
    }

    /**
     * 构建顶部统计卡
     *
     * @param params 基础参数
     * @return 统计卡
     */
    private List<FactoryReportVO.ReportMetricItem> buildMetrics(MapSqlParameterSource params) {
        int templateCount = queryInt("""
                SELECT COUNT(1)
                FROM iot_factory_report_template
                WHERE tenant_id = :tenantId
                  AND deleted = b'0'
                  AND status = 'ENABLED'
                """, params);
        int successCount = queryInt("""
                SELECT COUNT(1)
                FROM iot_factory_report_record
                WHERE tenant_id = :tenantId
                  AND deleted = b'0'
                  AND status = 'SUCCESS'
                """, params);
        int todaySuccessCount = queryInt("""
                SELECT COUNT(1)
                FROM iot_factory_report_record
                WHERE tenant_id = :tenantId
                  AND deleted = b'0'
                  AND status = 'SUCCESS'
                  AND DATE(generated_at) = CURRENT_DATE()
                """, params);
        int todayGeneratedTemplateCount = queryInt("""
                SELECT COUNT(DISTINCT template_id)
                FROM iot_factory_report_record
                WHERE tenant_id = :tenantId
                  AND deleted = b'0'
                  AND status = 'SUCCESS'
                  AND DATE(generated_at) = CURRENT_DATE()
                """, params);
        int pendingCount = Math.max(templateCount - todayGeneratedTemplateCount, 0);
        return List.of(
                buildMetric("template-count", "报表总数", String.valueOf(templateCount), "份", "ep:document", "cyan"),
                buildMetric("generated-count", "已生成", String.valueOf(successCount), "份", "ep:circle-check", "emerald"),
                buildMetric("today-generated", "今日生成", String.valueOf(todaySuccessCount), "份", "ep:calendar", "amber"),
                buildMetric("pending-count", "待生成", String.valueOf(pendingCount), "份", "ep:timer", "violet")
        );
    }

    /**
     * 构建单个统计卡
     *
     * @param key 键
     * @param title 标题
     * @param value 数值
     * @param unit 单位
     * @param icon 图标
     * @param theme 主题
     * @return 统计卡
     */
    private FactoryReportVO.ReportMetricItem buildMetric(
            String key,
            String title,
            String value,
            String unit,
            String icon,
            String theme
    ) {
        return FactoryReportVO.ReportMetricItem.builder()
                .key(key)
                .title(title)
                .value(value)
                .unit(unit)
                .icon(icon)
                .theme(theme)
                .build();
    }

    /**
     * 构建原型固定分类
     *
     * @return 分类列表
     */
    private List<FactoryReportVO.ReportCategoryOption> buildCategories() {
        List<FactoryReportVO.ReportCategoryOption> fixedOptions = CATEGORY_ORDER.stream()
                .map(item -> FactoryReportVO.ReportCategoryOption.builder().value(item).label(item).build())
                .toList();
        return List.of(FactoryReportVO.ReportCategoryOption.builder().value("").label("全部").build())
                .stream()
                .collect(java.util.stream.Collectors.collectingAndThen(
                        java.util.stream.Collectors.toList(),
                        list -> {
                            list.addAll(fixedOptions);
                            return list;
                        }
                ));
    }

    /**
     * 查询模板卡片
     *
     * @param params 查询参数
     * @return 卡片列表
     */
    private List<FactoryReportVO.ReportTemplateCardItem> queryTemplateCards(MapSqlParameterSource params) {
        return namedParameterJdbcTemplate.query("""
                SELECT t.id,
                       t.template_name,
                       t.template_category,
                       t.template_desc,
                       latest_record.id AS latest_record_id,
                       latest_record.status AS latest_record_status,
                       latest_record.generated_at AS latest_record_time,
                       latest_success.id AS latest_success_record_id,
                       latest_success.generated_at AS latest_success_time
                FROM iot_factory_report_template t
                LEFT JOIN iot_factory_report_record latest_record
                       ON latest_record.id = (
                           SELECT rr.id
                           FROM iot_factory_report_record rr
                           WHERE rr.tenant_id = t.tenant_id
                             AND rr.template_id = t.id
                             AND rr.deleted = b'0'
                           ORDER BY rr.generated_at DESC, rr.id DESC
                           LIMIT 1
                       )
                LEFT JOIN iot_factory_report_record latest_success
                       ON latest_success.id = (
                           SELECT rr.id
                           FROM iot_factory_report_record rr
                           WHERE rr.tenant_id = t.tenant_id
                             AND rr.template_id = t.id
                             AND rr.deleted = b'0'
                             AND rr.status = 'SUCCESS'
                           ORDER BY rr.generated_at DESC, rr.id DESC
                           LIMIT 1
                       )
                WHERE t.tenant_id = :tenantId
                  AND t.deleted = b'0'
                  AND t.status = 'ENABLED'
                  AND (:category = '' OR t.template_category = :category)
                  AND (
                      :keyword = ''
                      OR t.template_name LIKE :likeKeyword
                      OR t.template_desc LIKE :likeKeyword
                  )
                ORDER BY t.sort_no ASC, t.id ASC
                """, params, (rs, rowNum) -> {
            String latestStatus = rs.getString("latest_record_status");
            LocalDateTime latestRecordTime = rs.getObject("latest_record_time", LocalDateTime.class);
            Long latestSuccessRecordId = getLong(rs, "latest_success_record_id");
            return FactoryReportVO.ReportTemplateCardItem.builder()
                    .id(rs.getLong("id"))
                    .templateName(rs.getString("template_name"))
                    .category(rs.getString("template_category"))
                    .description(rs.getString("template_desc"))
                    .status(resolveTemplateStatus(latestStatus, latestRecordTime))
                    .latestSuccessRecordId(latestSuccessRecordId)
                    .lastGeneratedAt(rs.getObject("latest_success_time", LocalDateTime.class))
                    .lastStatus(StrUtil.blankToDefault(latestStatus, "未生成"))
                    .previewAvailable(latestSuccessRecordId != null)
                    .downloadAvailable(latestSuccessRecordId != null)
                    .generateAvailable(Boolean.TRUE)
                    .build();
        });
    }

    /**
     * 查询最近生成记录
     *
     * @param params 查询参数
     * @return 记录列表
     */
    private List<FactoryReportVO.ReportRecordRowItem> queryRecentRecords(MapSqlParameterSource params) {
        return namedParameterJdbcTemplate.query("""
                SELECT r.id,
                       r.report_name,
                       r.report_category,
                       r.generated_at,
                       r.status,
                       r.operator_name
                FROM iot_factory_report_record r
                INNER JOIN iot_factory_report_template t
                        ON t.id = r.template_id
                       AND t.deleted = b'0'
                       AND t.tenant_id = r.tenant_id
                WHERE r.tenant_id = :tenantId
                  AND r.deleted = b'0'
                  AND (:category = '' OR r.report_category = :category)
                  AND (
                      :keyword = ''
                      OR r.report_name LIKE :likeKeyword
                      OR t.template_name LIKE :likeKeyword
                  )
                ORDER BY r.generated_at DESC, r.id DESC
                LIMIT 8
                """, params, (rs, rowNum) -> FactoryReportVO.ReportRecordRowItem.builder()
                .id(rs.getLong("id"))
                .reportName(rs.getString("report_name"))
                .category(rs.getString("report_category"))
                .generatedAt(rs.getObject("generated_at", LocalDateTime.class))
                .status(rs.getString("status"))
                .operatorName(StrUtil.blankToDefault(rs.getString("operator_name"), "system"))
                .build());
    }

    /**
     * 查询模板详情
     *
     * @param templateId 模板主键
     * @return 模板行
     */
    private TemplateRow getTemplateById(Long templateId) {
        List<TemplateRow> rows = namedParameterJdbcTemplate.query("""
                SELECT id, template_name, template_category, template_desc
                FROM iot_factory_report_template
                WHERE tenant_id = :tenantId
                  AND deleted = b'0'
                  AND status = 'ENABLED'
                  AND id = :id
                LIMIT 1
                """, new MapSqlParameterSource()
                .addValue("tenantId", getTenantId())
                .addValue("id", templateId), (rs, rowNum) -> TemplateRow.builder()
                .id(rs.getLong("id"))
                .templateName(rs.getString("template_name"))
                .templateCategory(rs.getString("template_category"))
                .templateDesc(rs.getString("template_desc"))
                .build());
        if (rows.isEmpty()) {
            throw ServiceExceptionUtil.exception0(404, "报表模板不存在");
        }
        return rows.get(0);
    }

    /**
     * 查询记录详情
     *
     * @param recordId 记录主键
     * @return 记录详情
     */
    private RecordDetailRow getRecordDetail(Long recordId) {
        List<RecordDetailRow> rows = namedParameterJdbcTemplate.query("""
                SELECT r.id,
                       r.template_id,
                       r.report_name,
                       r.report_category,
                       r.status,
                       r.biz_date,
                       r.generated_at,
                       r.file_name,
                       r.file_url,
                       r.operator_name,
                       t.template_name,
                       t.template_desc
                FROM iot_factory_report_record r
                INNER JOIN iot_factory_report_template t
                        ON t.id = r.template_id
                       AND t.deleted = b'0'
                       AND t.tenant_id = r.tenant_id
                WHERE r.tenant_id = :tenantId
                  AND r.deleted = b'0'
                  AND r.id = :id
                LIMIT 1
                """, new MapSqlParameterSource()
                .addValue("tenantId", getTenantId())
                .addValue("id", recordId), (rs, rowNum) -> mapRecordDetail(rs));
        if (rows.isEmpty()) {
            throw ServiceExceptionUtil.exception0(404, "报表记录不存在");
        }
        return rows.get(0);
    }

    /**
     * 构建统一查询参数
     *
     * @param reqVO 请求参数
     * @return 参数
     */
    private MapSqlParameterSource buildQueryParams(FactoryReportVO.ReportDashboardReqVO reqVO) {
        return baseParams()
                .addValue("category", normalize(reqVO.getCategory()))
                .addValue("keyword", normalize(reqVO.getKeyword()))
                .addValue("likeKeyword", "%" + normalize(reqVO.getKeyword()) + "%");
    }

    /**
     * 构建基础参数
     *
     * @return 参数
     */
    private MapSqlParameterSource baseParams() {
        return new MapSqlParameterSource().addValue("tenantId", getTenantId());
    }

    /**
     * 获取租户编号
     *
     * @return 租户编号
     */
    private Long getTenantId() {
        return TenantContextHolder.getTenantId() != null ? TenantContextHolder.getTenantId() : DEFAULT_TENANT_ID;
    }

    /**
     * 规范化字符串
     *
     * @param value 原始值
     * @return 规范化结果
     */
    private String normalize(String value) {
        return StrUtil.blankToDefault(StrUtil.trim(value), "");
    }

    /**
     * 解析模板状态
     *
     * @param latestStatus 最新记录状态
     * @param latestTime 最新记录时间
     * @return 展示状态
     */
    private String resolveTemplateStatus(String latestStatus, LocalDateTime latestTime) {
        if (StrUtil.equalsIgnoreCase(latestStatus, "SUCCESS")
                && latestTime != null
                && LocalDate.now().equals(latestTime.toLocalDate())) {
            return "已生成";
        }
        if (StrUtil.equalsIgnoreCase(latestStatus, "FAILED")
                && latestTime != null
                && LocalDate.now().equals(latestTime.toLocalDate())) {
            return "生成失败";
        }
        return "待生成";
    }

    /**
     * 解析更新时间
     *
     * @param templates 模板卡
     * @param records 记录
     * @return 更新时间
     */
    private LocalDateTime resolveUpdatedAt(
            List<FactoryReportVO.ReportTemplateCardItem> templates,
            List<FactoryReportVO.ReportRecordRowItem> records
    ) {
        LocalDateTime templateTime = templates.stream()
                .map(FactoryReportVO.ReportTemplateCardItem::getLastGeneratedAt)
                .filter(Objects::nonNull)
                .max(LocalDateTime::compareTo)
                .orElse(null);
        LocalDateTime recordTime = records.stream()
                .map(FactoryReportVO.ReportRecordRowItem::getGeneratedAt)
                .filter(Objects::nonNull)
                .max(LocalDateTime::compareTo)
                .orElse(null);
        if (templateTime == null && recordTime == null) {
            return LocalDateTime.now();
        }
        if (templateTime == null) {
            return recordTime;
        }
        if (recordTime == null) {
            return templateTime;
        }
        return templateTime.isAfter(recordTime) ? templateTime : recordTime;
    }

    /**
     * 查询整数
     *
     * @param sql SQL
     * @param params 参数
     * @return 结果
     */
    private int queryInt(String sql, MapSqlParameterSource params) {
        return namedParameterJdbcTemplate.query(sql, params, rs -> rs.next() ? rs.getInt(1) : 0);
    }

    /**
     * 映射记录详情
     *
     * @param rs 结果集
     * @return 详情
     * @throws SQLException SQL 异常
     */
    private RecordDetailRow mapRecordDetail(ResultSet rs) throws SQLException {
        return RecordDetailRow.builder()
                .id(rs.getLong("id"))
                .templateId(rs.getLong("template_id"))
                .reportName(rs.getString("report_name"))
                .reportCategory(rs.getString("report_category"))
                .status(rs.getString("status"))
                .bizDate(rs.getObject("biz_date", LocalDate.class))
                .generatedAt(rs.getObject("generated_at", LocalDateTime.class))
                .fileName(rs.getString("file_name"))
                .fileUrl(rs.getString("file_url"))
                .operatorName(StrUtil.blankToDefault(rs.getString("operator_name"), "system"))
                .templateName(rs.getString("template_name"))
                .templateDesc(rs.getString("template_desc"))
                .build();
    }

    /**
     * 安全获取 Long
     *
     * @param rs 结果集
     * @param column 字段
     * @return Long 值
     * @throws SQLException SQL 异常
     */
    private Long getLong(ResultSet rs, String column) throws SQLException {
        long value = rs.getLong(column);
        return rs.wasNull() ? null : value;
    }

    /**
     * 模板查询行
     */
    @lombok.Data
    @lombok.Builder
    private static class TemplateRow {

        private Long id;
        private String templateName;
        private String templateCategory;
        private String templateDesc;
    }

    /**
     * 记录详情行
     */
    @lombok.Data
    @lombok.Builder
    private static class RecordDetailRow {

        private Long id;
        private Long templateId;
        private String reportName;
        private String reportCategory;
        private String status;
        private LocalDate bizDate;
        private LocalDateTime generatedAt;
        private String fileName;
        private String fileUrl;
        private String operatorName;
        private String templateName;
        private String templateDesc;
    }
}

