-- ============================================================
-- 修复：ibms_channel.space_id 在 Phase 2 被误判"多义保留"未做 ID 重写
-- 现象：t162 端 ibms_channel.space_id 仍指向 t1 旧 ibms_space.id
--       前端按 t162 真 space_id 查 channel → 空，实时预览看不到通道
-- 策略：通过 (code, name) 自然键，将 t162 channel.space_id 从 t1 ID 重写为 t162 ID
--       t1 端不存在的孤儿值（1002/1003/2002/2003）保持原值
-- 实例：127.0.0.1:3306（ch_ibms）
-- ============================================================

USE ch_ibms;

-- 1) 备份现状
DROP TABLE IF EXISTS bak_ibms_channel_space_t162_20260427;
CREATE TABLE bak_ibms_channel_space_t162_20260427 AS
  SELECT id, space_id AS old_space_id FROM ibms_channel WHERE tenant_id=162;

-- 2) 修复前快照
SELECT '=== FIX BEFORE ===' AS msg;
SELECT space_id, COUNT(*) cnt FROM ibms_channel WHERE tenant_id=162 GROUP BY space_id ORDER BY space_id;

-- 3) 按 (code, name) 重写
UPDATE ibms_channel c
JOIN ibms_space src ON src.id = c.space_id AND src.tenant_id = 1
JOIN ibms_space dst ON dst.code = src.code AND dst.name = src.name AND dst.tenant_id = 162
SET c.space_id = dst.id
WHERE c.tenant_id = 162;

SELECT ROW_COUNT() AS rows_updated;

-- 4) 修复后快照
SELECT '=== FIX AFTER ===' AS msg;
SELECT space_id, COUNT(*) cnt FROM ibms_channel WHERE tenant_id=162 GROUP BY space_id ORDER BY space_id;

-- 5) 完整性核对
SELECT '=== INTEGRITY CHECK ===' AS msg;
SELECT
  SUM(CASE WHEN c.space_id IS NULL OR c.space_id=0 THEN 1 ELSE 0 END) AS null_or_zero,
  SUM(CASE WHEN c.space_id<>0 AND c.space_id IS NOT NULL
        AND EXISTS (SELECT 1 FROM ibms_space s WHERE s.id=c.space_id AND s.tenant_id=162) THEN 1 ELSE 0 END) AS pointing_to_t162,
  SUM(CASE WHEN c.space_id<>0 AND c.space_id IS NOT NULL
        AND NOT EXISTS (SELECT 1 FROM ibms_space s WHERE s.id=c.space_id) THEN 1 ELSE 0 END) AS true_orphan,
  COUNT(*) AS total
FROM ibms_channel c WHERE c.tenant_id=162;
