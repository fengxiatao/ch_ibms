-- 添加 excluded_menu_ids 列到 system_tenant_package 表
ALTER TABLE system_tenant_package 
  ADD COLUMN excluded_menu_ids varchar(512) DEFAULT NULL 
  COMMENT '排除的菜单编号（归一化后移除这些根菜单下的非按钮菜单）' 
  AFTER menu_ids;
