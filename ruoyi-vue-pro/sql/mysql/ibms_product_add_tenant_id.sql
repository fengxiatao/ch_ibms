-- =============================================
-- IBMS 产品相关表补齐 tenant_id（MySQL）
-- 背景：开启多租户后，框架会自动拼接 tenant_id 条件；缺列会报 Unknown column 'tenant_id'
-- 说明：按需执行一次即可（如已存在 tenant_id，请勿重复执行）
-- =============================================

-- 1) ibms_product
ALTER TABLE `ibms_product`
  ADD COLUMN `tenant_id` bigint NOT NULL DEFAULT 1 COMMENT '租户编号' AFTER `id`,
  ADD KEY `idx_tenant_id` (`tenant_id`);

-- 2) ibms_product_point_type
ALTER TABLE `ibms_product_point_type`
  ADD COLUMN `tenant_id` bigint NOT NULL DEFAULT 1 COMMENT '租户编号' AFTER `id`,
  ADD KEY `idx_tenant_id` (`tenant_id`);

-- 3) ibms_product_property
ALTER TABLE `ibms_product_property`
  ADD COLUMN `tenant_id` bigint NOT NULL DEFAULT 1 COMMENT '租户编号' AFTER `id`,
  ADD KEY `idx_tenant_id` (`tenant_id`);

