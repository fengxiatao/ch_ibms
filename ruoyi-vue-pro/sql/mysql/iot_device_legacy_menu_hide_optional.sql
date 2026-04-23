-- =============================================
-- 【可选】隐藏芋道原生「IoT 设备管理」菜单（逐步下线 /iot/device 入口）
-- =============================================
-- 执行前请确认：租户已改用 IBMS 设备菜单（如 smart_building / ibms-device），且无人依赖旧「设备管理」路由。
-- 不同环境 system_menu.id 可能不一致，请先 SELECT 再改 visible。
--
-- 示例（仅演示，默认注释）：
-- SELECT id, name, path, component, permission, visible FROM system_menu
--   WHERE deleted = 0 AND (path LIKE '%device%' OR component LIKE '%iot/device%')
--   ORDER BY id;
--
-- 将旧设备「目录/菜单」visible 置为 0（不展示）：
-- UPDATE system_menu SET visible = '0', updater = '1', update_time = NOW()
--   WHERE deleted = 0 AND id IN (4007, 4008);  -- 按你库实际 id 替换
-- =============================================

SELECT 1 AS skip_placeholder;
