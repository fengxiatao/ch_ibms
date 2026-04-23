-- =============================================
-- WP-A3：单一台账对账（只读 SELECT，可重复执行）
-- 输出：仅 IoT / 仅 IBMS / 可人工比对的行数
-- =============================================

SET NAMES utf8mb4;

-- 设备：iot 有效行是否均已落入 ibms（按 id）
SELECT 'device_iot_only_by_id' AS metric,
       COUNT(*) AS cnt
FROM iot_device d
WHERE d.deleted = 0
  AND NOT EXISTS (
    SELECT 1 FROM ibms_device b WHERE b.id = d.id AND b.deleted = 0
  );

-- 产品：数量对比（ibms 无 product_key 列，仅行数）
SELECT 'product_iot_active' AS metric, COUNT(*) AS cnt FROM iot_product WHERE deleted = 0
UNION ALL
SELECT 'product_ibms_active', COUNT(*) FROM ibms_product WHERE deleted = 0;

-- 通道：数量对比
SELECT 'channel_iot_active' AS metric, COUNT(*) AS cnt FROM iot_device_channel WHERE deleted = 0
UNION ALL
SELECT 'channel_ibms_active', COUNT(*) FROM ibms_channel WHERE deleted = 0;

-- A2 对账：按 id 是否已在 IBMS 台账（有效行）
SELECT 'product_iot_only_by_id' AS metric,
       COUNT(*) AS cnt
FROM iot_product p
WHERE p.deleted = 0
  AND NOT EXISTS (
    SELECT 1 FROM ibms_product b WHERE b.id = p.id AND b.deleted = 0
  );

SELECT 'channel_iot_only_by_id' AS metric,
       COUNT(*) AS cnt
FROM iot_device_channel c
WHERE c.deleted = 0
  AND NOT EXISTS (
    SELECT 1 FROM ibms_channel b WHERE b.id = c.id AND b.deleted = 0
  );
