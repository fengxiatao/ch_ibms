-- ============================================================
-- 修复：iot_video_view / iot_video_view_pane / iot_video_view_group 的 creator/updater
-- 现象：长辉IBMS（tenant_id=162）admin 进入实时预览，分组里看不到任何视图
-- 根因：VideoViewMapper.selectListByCreator() 按 creator=当前登录用户ID 过滤；
--       同步过来的 t162 视图 creator 还是 t1 旧用户ID（1=t1 admin、144=t1 feng），
--       而 t162 admin id=143，按 creator=143 查 → 0 条
-- 策略：把 t162 端三张视图表的 creator/updater 全部改为 t162 admin (id=143)
-- 实例：127.0.0.1:3306（ch_ibms）
-- ============================================================

USE ch_ibms;

-- 备份当前 creator/updater
DROP TABLE IF EXISTS bak_iot_video_view_creator_t162_20260427;
CREATE TABLE bak_iot_video_view_creator_t162_20260427 AS
  SELECT id, creator, updater FROM iot_video_view WHERE tenant_id=162;

DROP TABLE IF EXISTS bak_iot_video_view_pane_creator_t162_20260427;
CREATE TABLE bak_iot_video_view_pane_creator_t162_20260427 AS
  SELECT id, creator, updater FROM iot_video_view_pane WHERE tenant_id=162;

DROP TABLE IF EXISTS bak_iot_video_view_group_creator_t162_20260427;
CREATE TABLE bak_iot_video_view_group_creator_t162_20260427 AS
  SELECT id, creator, updater FROM iot_video_view_group WHERE tenant_id=162;

-- 修复前
SELECT '=== BEFORE iot_video_view ===' AS m;
SELECT creator, COUNT(*) FROM iot_video_view WHERE tenant_id=162 GROUP BY creator;

-- 改 creator/updater = 143（t162 admin）
UPDATE iot_video_view       SET creator='143', updater='143' WHERE tenant_id=162;
UPDATE iot_video_view_pane  SET creator='143', updater='143' WHERE tenant_id=162;
UPDATE iot_video_view_group SET creator='143', updater='143' WHERE tenant_id=162;

-- 修复后
SELECT '=== AFTER iot_video_view ===' AS m;
SELECT creator, COUNT(*) FROM iot_video_view WHERE tenant_id=162 GROUP BY creator;
SELECT '=== AFTER iot_video_view_pane ===' AS m;
SELECT creator, COUNT(*) FROM iot_video_view_pane WHERE tenant_id=162 GROUP BY creator;
SELECT '=== AFTER iot_video_view_group ===' AS m;
SELECT creator, COUNT(*) FROM iot_video_view_group WHERE tenant_id=162 GROUP BY creator;
