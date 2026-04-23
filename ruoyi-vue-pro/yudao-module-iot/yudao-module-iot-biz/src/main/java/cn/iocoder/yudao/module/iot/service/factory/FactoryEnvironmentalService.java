package cn.iocoder.yudao.module.iot.service.factory;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.tenant.core.context.TenantContextHolder;
import cn.iocoder.yudao.module.iot.controller.admin.factory.vo.FactoryEnvironmentalOverviewRespVO;
import jakarta.annotation.Resource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 文件说明：工厂环保监测工作台聚合服务
 *
 * <p>说明：独立页环保监测强调“严格原型克隆 + 真实数据库驱动”，
 * 因此当前实现使用 JDBC 直接聚合工厂环保监测独立表，避免与建筑环境、
 * 合规管理中的环保 Tab 发生口径混用。</p>
 *
 * @author GPT-5.4
 */
@Service
@Validated
public class FactoryEnvironmentalService {

    /**
     * 默认租户编号
     */
    private static final long DEFAULT_TENANT_ID = 1L;

    /**
     * 废气限值标线固定比例
     */
    private static final BigDecimal LIMIT_MARKER_PERCENT = new BigDecimal("83.3333");

    @Resource
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    /**
     * 获取环保监测工作台数据
     *
     * @return 工作台响应
     */
    public FactoryEnvironmentalOverviewRespVO getDashboard() {
        MapSqlParameterSource params = baseParams();
        List<PointSnapshot> snapshots = queryLatestSnapshots(params);
        Map<String, PointSnapshot> snapshotMap = snapshots.stream()
                .collect(Collectors.toMap(PointSnapshot::getPointCode, item -> item, (left, right) -> left, LinkedHashMap::new));
        List<FactoryEnvironmentalOverviewRespVO.AlertItem> alerts = queryAlerts(params);

        return FactoryEnvironmentalOverviewRespVO.builder()
                .updatedAt(resolveUpdatedAt(snapshots, alerts))
                .kpiCards(buildKpiCards(snapshotMap))
                .airEmission(buildAirEmissionCard(snapshots))
                .wastewater(buildWastewaterCard(snapshotMap))
                .noise(buildNoiseCard(snapshotMap))
                .alerts(alerts)
                .build();
    }

    /**
     * 查询最新点位快照
     *
     * @param params 查询参数
     * @return 点位快照
     */
    private List<PointSnapshot> queryLatestSnapshots(MapSqlParameterSource params) {
        return namedParameterJdbcTemplate.query("""
                SELECT p.id,
                       p.point_code,
                       p.point_name,
                       p.category,
                       p.location_name,
                       p.unit,
                       p.min_value,
                       p.limit_value,
                       p.display_max_value,
                       p.display_unit_text,
                       p.display_order,
                       r.reading_value,
                       COALESCE(r.status, '正常') AS reading_status,
                       COALESCE(r.exceed_flag, 0) AS exceed_flag,
                       r.recorded_at
                FROM iot_factory_environmental_point p
                LEFT JOIN iot_factory_environmental_reading r
                       ON r.id = (
                           SELECT rr.id
                           FROM iot_factory_environmental_reading rr
                           WHERE rr.point_id = p.id
                             AND rr.tenant_id = p.tenant_id
                             AND rr.deleted = b'0'
                           ORDER BY rr.recorded_at DESC, rr.id DESC
                           LIMIT 1
                       )
                WHERE p.tenant_id = :tenantId
                  AND p.deleted = b'0'
                  AND p.enabled = 1
                ORDER BY p.display_order ASC, p.id ASC
                """, params, (rs, rowNum) -> mapPointSnapshot(rs));
    }

    /**
     * 查询环保预警
     *
     * @param params 查询参数
     * @return 预警列表
     */
    private List<FactoryEnvironmentalOverviewRespVO.AlertItem> queryAlerts(MapSqlParameterSource params) {
        return namedParameterJdbcTemplate.query("""
                SELECT a.id,
                       a.alert_title,
                       a.alert_level,
                       a.status,
                       a.current_value,
                       a.limit_value,
                       a.happened_at,
                       a.description,
                       p.unit
                FROM iot_factory_environmental_alert a
                LEFT JOIN iot_factory_environmental_point p
                       ON p.id = a.point_id
                      AND p.deleted = b'0'
                      AND p.tenant_id = a.tenant_id
                WHERE a.tenant_id = :tenantId
                  AND a.deleted = b'0'
                ORDER BY a.happened_at DESC, a.id DESC
                LIMIT 6
                """, params, (rs, rowNum) -> FactoryEnvironmentalOverviewRespVO.AlertItem.builder()
                .id(rs.getLong("id"))
                .title(rs.getString("alert_title"))
                .happenedAt(rs.getObject("happened_at", LocalDateTime.class))
                .currentValue(getBigDecimal(rs, "current_value"))
                .currentValueText(formatPlain(getBigDecimal(rs, "current_value")))
                .limitValue(getBigDecimal(rs, "limit_value"))
                .limitValueText(formatPlain(getBigDecimal(rs, "limit_value")))
                .unit(StrUtil.blankToDefault(rs.getString("unit"), ""))
                .level(StrUtil.blankToDefault(rs.getString("alert_level"), "INFO"))
                .tone(resolveAlertTone(rs.getString("status"), rs.getString("alert_level")))
                .description(StrUtil.blankToDefault(rs.getString("description"), ""))
                .build());
    }

    /**
     * 构建顶部 KPI
     *
     * @param snapshotMap 快照映射
     * @return KPI 列表
     */
    private List<FactoryEnvironmentalOverviewRespVO.KpiCardItem> buildKpiCards(Map<String, PointSnapshot> snapshotMap) {
        return List.of(
                buildKpiCard(snapshotMap.get("VOCS_CONCENTRATION"), "vocs", "VOCs浓度", "mdi:weather-windy", "violet"),
                buildKpiCard(snapshotMap.get("WASTEWATER_COD"), "wastewater-cod", "废水COD", "mdi:water-outline", "blue"),
                buildKpiCard(snapshotMap.get("NOISE_LEVEL"), "noise-level", "噪声等级", "mdi:volume-high", "green"),
                buildKpiCard(snapshotMap.get("EXHAUST_FLOW"), "exhaust-flow", "排气流量", "mdi:chart-sankey", "orange")
        );
    }

    /**
     * 构建 KPI 项
     *
     * @param snapshot 点位快照
     * @param key 键
     * @param title 标题
     * @param icon 图标
     * @param theme 主题
     * @return KPI 项
     */
    private FactoryEnvironmentalOverviewRespVO.KpiCardItem buildKpiCard(
            PointSnapshot snapshot,
            String key,
            String title,
            String icon,
            String theme
    ) {
        BigDecimal value = snapshot == null ? BigDecimal.ZERO : snapshot.getReadingValue();
        return FactoryEnvironmentalOverviewRespVO.KpiCardItem.builder()
                .key(key)
                .title(title)
                .value(value)
                .valueText(formatPlain(value))
                .unit(snapshot == null ? "" : snapshot.getUnit())
                .icon(icon)
                .theme(theme)
                .status(snapshot == null ? "正常" : snapshot.getReadingStatus())
                .build();
    }

    /**
     * 构建废气排放卡
     *
     * @param snapshots 全量快照
     * @return 废气卡
     */
    private FactoryEnvironmentalOverviewRespVO.AirEmissionCard buildAirEmissionCard(List<PointSnapshot> snapshots) {
        List<FactoryEnvironmentalOverviewRespVO.AirEmissionItem> items = snapshots.stream()
                .filter(item -> StrUtil.equalsIgnoreCase(item.getCategory(), "AIR"))
                .filter(item -> !StrUtil.equalsIgnoreCase(item.getPointCode(), "EXHAUST_FLOW"))
                .sorted(Comparator.comparing(PointSnapshot::getDisplayOrder))
                .map(item -> {
                    BigDecimal limitValue = safeValue(item.getLimitValue());
                    return FactoryEnvironmentalOverviewRespVO.AirEmissionItem.builder()
                            .pointCode(item.getPointCode())
                            .pointName(item.getPointName())
                            .value(item.getReadingValue())
                            .valueText(formatPlain(item.getReadingValue()))
                            .unit(item.getUnit())
                            .limitValue(limitValue)
                            .limitValueText(formatPlain(limitValue))
                            .status(item.getReadingStatus())
                            .tone(resolvePointTone(item))
                            .progressPercent(calculateProgressPercent(item.getReadingValue(), limitValue))
                            .limitMarkerPercent(LIMIT_MARKER_PERCENT)
                            .build();
                })
                .toList();
        boolean anyWarning = items.stream().anyMatch(item -> StrUtil.equals(item.getTone(), "warning"));
        return FactoryEnvironmentalOverviewRespVO.AirEmissionCard.builder()
                .title("废气排放监测")
                .overallStatusText(anyWarning ? "预警关注" : "达标排放")
                .items(items)
                .build();
    }

    /**
     * 构建废水卡
     *
     * @param snapshotMap 快照映射
     * @return 废水卡
     */
    private FactoryEnvironmentalOverviewRespVO.WastewaterCard buildWastewaterCard(Map<String, PointSnapshot> snapshotMap) {
        List<FactoryEnvironmentalOverviewRespVO.WastewaterItem> items = List.of(
                buildWastewaterItem(snapshotMap.get("WASTEWATER_COD"), "emerald"),
                buildWastewaterItem(snapshotMap.get("AMMONIA_NITROGEN"), "emerald"),
                buildWastewaterItem(snapshotMap.get("PH"), "emerald"),
                buildWastewaterItem(snapshotMap.get("WASTEWATER_FLOW"), "blue")
        ).stream().filter(Objects::nonNull).toList();
        boolean anyWarning = items.stream().anyMatch(item -> StrUtil.equals(item.getTone(), "warning"));
        return FactoryEnvironmentalOverviewRespVO.WastewaterCard.builder()
                .title("废水排放监测")
                .overallStatusText(anyWarning ? "预警关注" : "达标排放")
                .items(items)
                .build();
    }

    /**
     * 构建废水明细项
     *
     * @param snapshot 点位快照
     * @param theme 主题
     * @return 废水项
     */
    private FactoryEnvironmentalOverviewRespVO.WastewaterItem buildWastewaterItem(PointSnapshot snapshot, String theme) {
        if (snapshot == null) {
            return null;
        }
        return FactoryEnvironmentalOverviewRespVO.WastewaterItem.builder()
                .pointCode(snapshot.getPointCode())
                .pointName(snapshot.getPointName())
                .value(snapshot.getReadingValue())
                .valueText(formatPlain(snapshot.getReadingValue()))
                .displayUnitText(StrUtil.blankToDefault(snapshot.getDisplayUnitText(), snapshot.getUnit()))
                .status(snapshot.getReadingStatus())
                .tone(resolvePointTone(snapshot))
                .theme(theme)
                .progressPercent(calculateDisplayPercent(snapshot))
                .build();
    }

    /**
     * 构建噪声卡
     *
     * @param snapshotMap 快照映射
     * @return 噪声卡
     */
    private FactoryEnvironmentalOverviewRespVO.NoiseCard buildNoiseCard(Map<String, PointSnapshot> snapshotMap) {
        PointSnapshot day = snapshotMap.get("NOISE_DAY");
        PointSnapshot night = snapshotMap.get("NOISE_NIGHT");
        PointSnapshot limit = snapshotMap.get("NOISE_LIMIT");
        return FactoryEnvironmentalOverviewRespVO.NoiseCard.builder()
                .title("噪声监测")
                .day(buildNoiseGauge(day, "昼间 dB"))
                .night(buildNoiseGauge(night, "夜间 dB"))
                .limit(FactoryEnvironmentalOverviewRespVO.NoiseLimit.builder()
                        .label("限值 dB")
                        .value(limit == null ? BigDecimal.ZERO : limit.getReadingValue())
                        .valueText(formatPlain(limit == null ? BigDecimal.ZERO : limit.getReadingValue()))
                        .unit(limit == null ? "dB" : limit.getUnit())
                        .build())
                .build();
    }

    /**
     * 构建噪声仪表项
     *
     * @param snapshot 点位快照
     * @param label 标签
     * @return 仪表项
     */
    private FactoryEnvironmentalOverviewRespVO.NoiseGauge buildNoiseGauge(PointSnapshot snapshot, String label) {
        BigDecimal value = snapshot == null ? BigDecimal.ZERO : snapshot.getReadingValue();
        return FactoryEnvironmentalOverviewRespVO.NoiseGauge.builder()
                .label(label)
                .value(value)
                .valueText(formatPlain(value))
                .unit(snapshot == null ? "dB" : snapshot.getUnit())
                .tone(snapshot == null ? "normal" : resolvePointTone(snapshot))
                .percent(snapshot == null ? BigDecimal.ZERO : calculateDisplayPercent(snapshot))
                .build();
    }

    /**
     * 解析更新时间
     *
     * @param snapshots 快照
     * @param alerts 预警
     * @return 时间
     */
    private LocalDateTime resolveUpdatedAt(
            List<PointSnapshot> snapshots,
            List<FactoryEnvironmentalOverviewRespVO.AlertItem> alerts
    ) {
        LocalDateTime latestReadingTime = snapshots.stream()
                .map(PointSnapshot::getRecordedAt)
                .filter(Objects::nonNull)
                .max(LocalDateTime::compareTo)
                .orElse(null);
        LocalDateTime latestAlertTime = alerts.stream()
                .map(FactoryEnvironmentalOverviewRespVO.AlertItem::getHappenedAt)
                .filter(Objects::nonNull)
                .max(LocalDateTime::compareTo)
                .orElse(null);
        if (latestReadingTime == null && latestAlertTime == null) {
            return LocalDateTime.now();
        }
        if (latestReadingTime == null) {
            return latestAlertTime;
        }
        if (latestAlertTime == null) {
            return latestReadingTime;
        }
        return latestReadingTime.isAfter(latestAlertTime) ? latestReadingTime : latestAlertTime;
    }

    /**
     * 计算展示百分比
     *
     * @param snapshot 快照
     * @return 百分比
     */
    private BigDecimal calculateDisplayPercent(PointSnapshot snapshot) {
        BigDecimal displayMax = snapshot.getDisplayMaxValue() != null && snapshot.getDisplayMaxValue().compareTo(BigDecimal.ZERO) > 0
                ? snapshot.getDisplayMaxValue()
                : safeValue(snapshot.getLimitValue());
        return calculateProgressPercent(snapshot.getReadingValue(), displayMax);
    }

    /**
     * 计算进度百分比
     *
     * @param value 当前值
     * @param maxValue 最大值
     * @return 百分比
     */
    private BigDecimal calculateProgressPercent(BigDecimal value, BigDecimal maxValue) {
        if (maxValue == null || maxValue.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        BigDecimal percent = safeValue(value)
                .multiply(BigDecimal.valueOf(100))
                .divide(maxValue, 2, RoundingMode.HALF_UP);
        return clampPercent(percent);
    }

    /**
     * 限制百分比区间
     *
     * @param value 原始值
     * @return 安全值
     */
    private BigDecimal clampPercent(BigDecimal value) {
        if (value.compareTo(BigDecimal.ZERO) < 0) {
            return BigDecimal.ZERO;
        }
        if (value.compareTo(BigDecimal.valueOf(100)) > 0) {
            return BigDecimal.valueOf(100);
        }
        return value;
    }

    /**
     * 解析点位色调
     *
     * @param snapshot 快照
     * @return 语义色
     */
    private String resolvePointTone(PointSnapshot snapshot) {
        if (snapshot == null) {
            return "normal";
        }
        if (snapshot.getExceedFlag() != null && snapshot.getExceedFlag() > 0) {
            return "warning";
        }
        if (StrUtil.containsAnyIgnoreCase(snapshot.getReadingStatus(), "预警", "超标", "异常", "关注")) {
            return "warning";
        }
        return "normal";
    }

    /**
     * 解析预警色调
     *
     * @param status 状态
     * @param level 级别
     * @return 语义色
     */
    private String resolveAlertTone(String status, String level) {
        if (StrUtil.containsAnyIgnoreCase(status, "恢复", "正常")
                || StrUtil.containsAnyIgnoreCase(level, "INFO", "NORMAL", "SUCCESS")) {
            return "success";
        }
        return "warning";
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
     * 获取当前租户
     *
     * @return 租户编号
     */
    private Long getTenantId() {
        return TenantContextHolder.getTenantId() != null ? TenantContextHolder.getTenantId() : DEFAULT_TENANT_ID;
    }

    /**
     * 映射点位快照
     *
     * @param rs 结果集
     * @return 快照
     * @throws SQLException SQL 异常
     */
    private PointSnapshot mapPointSnapshot(ResultSet rs) throws SQLException {
        return PointSnapshot.builder()
                .id(rs.getLong("id"))
                .pointCode(rs.getString("point_code"))
                .pointName(rs.getString("point_name"))
                .category(rs.getString("category"))
                .locationName(rs.getString("location_name"))
                .unit(StrUtil.blankToDefault(rs.getString("unit"), ""))
                .minValue(getBigDecimal(rs, "min_value"))
                .limitValue(getBigDecimal(rs, "limit_value"))
                .displayMaxValue(getBigDecimal(rs, "display_max_value"))
                .displayUnitText(rs.getString("display_unit_text"))
                .displayOrder(rs.getInt("display_order"))
                .readingValue(getBigDecimal(rs, "reading_value"))
                .readingStatus(StrUtil.blankToDefault(rs.getString("reading_status"), "正常"))
                .exceedFlag(rs.getInt("exceed_flag"))
                .recordedAt(rs.getObject("recorded_at", LocalDateTime.class))
                .build();
    }

    /**
     * 安全获取十进制值
     *
     * @param rs 结果集
     * @param column 字段名
     * @return 十进制值
     * @throws SQLException SQL 异常
     */
    private BigDecimal getBigDecimal(ResultSet rs, String column) throws SQLException {
        BigDecimal value = rs.getBigDecimal(column);
        return value == null ? BigDecimal.ZERO : value;
    }

    /**
     * 格式化普通数值
     *
     * @param value 数值
     * @return 文本
     */
    private String formatPlain(BigDecimal value) {
        return safeValue(value).stripTrailingZeros().toPlainString();
    }

    /**
     * 兜底数值
     *
     * @param value 原值
     * @return 安全值
     */
    private BigDecimal safeValue(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    /**
     * 点位最新快照
     */
    @lombok.Data
    @lombok.Builder
    private static class PointSnapshot {

        private Long id;

        private String pointCode;

        private String pointName;

        private String category;

        private String locationName;

        private String unit;

        private BigDecimal minValue;

        private BigDecimal limitValue;

        private BigDecimal displayMaxValue;

        private String displayUnitText;

        private Integer displayOrder;

        private BigDecimal readingValue;

        private String readingStatus;

        private Integer exceedFlag;

        private LocalDateTime recordedAt;
    }
}
