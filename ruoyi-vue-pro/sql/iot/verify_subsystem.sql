-- 验证子系统数据是否已插入
USE ruoyi-vue-pro;

SELECT '=== 子系统数据统计 ===' AS title;
SELECT COUNT(*) AS total_count FROM iot_subsystem;

SELECT '=== 一级子系统（IBMS模块） ===' AS title;
SELECT code AS 代码, name AS 名称, menu_id AS 菜单ID, menu_path AS 菜单路径, enabled AS 启用
FROM iot_subsystem
WHERE level = 1
ORDER BY sort;

SELECT '=== 二级子系统（智慧安防子模块） ===' AS title;
SELECT code AS 代码, name AS 名称, parent_code AS 父代码, menu_id AS 菜单ID, enabled AS 启用
FROM iot_subsystem
WHERE level = 2 AND parent_code = 'security'
ORDER BY sort;




