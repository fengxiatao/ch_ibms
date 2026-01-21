-- =============================================
-- 报警主机产品类型初始化脚本（幂等）
-- 适用数据库：MySQL
-- 数据库名：ch_ibms
-- 作用：创建“报警主机”产品分类与产品（如不存在），用于设备创建设置默认产品
-- =============================================

USE ch_ibms;

-- 1) 确保顶层分类【智能安防】存在
INSERT INTO iot_product_category (`name`, `parent_id`, `icon`, `description`, `sort`, `tenant_id`)
SELECT '智能安防', 0, 'security', '智能安防设备', 1, 1
FROM DUAL
WHERE NOT EXISTS (
  SELECT 1 FROM iot_product_category 
  WHERE `name` = '智能安防' AND `parent_id` = 0 AND `deleted` = 0
);

-- 记录顶层分类ID
SET @root_category_id := (
  SELECT id FROM iot_product_category 
  WHERE `name` = '智能安防' AND `parent_id` = 0 AND `deleted` = 0
  LIMIT 1
);

-- 2) 创建二级分类【报警主机】（如果不存在）
INSERT INTO iot_product_category (`name`, `parent_id`, `icon`, `description`, `sort`, `tenant_id`)
SELECT '报警主机', @root_category_id, 'bell', '报警主机产品分类', 3, 1
FROM DUAL
WHERE NOT EXISTS (
  SELECT 1 FROM iot_product_category 
  WHERE `name` = '报警主机' AND `parent_id` = @root_category_id AND `deleted` = 0
);

-- 记录报警主机分类ID
SET @alarm_category_id := (
  SELECT id FROM iot_product_category 
  WHERE `name` = '报警主机' AND `parent_id` = @root_category_id AND `deleted` = 0
  LIMIT 1
);

-- 3) 创建【报警主机产品】（如果不存在）
-- 说明：
--  - status: 1（已发布）
--  - device_type: 0（直连设备）
--  - net_type: 2（有线/以太网）
--  - codec_type: 'ALARM_PS600'（PS600协议）
INSERT INTO iot_product (
  `name`, `product_key`, `category_id`, `icon`, `pic_url`, `description`,
  `status`, `device_type`, `net_type`, `codec_type`, `tenant_id`
)
SELECT 
  '报警主机产品', 'ALARM_HOST_PRODUCT', @alarm_category_id, 'ep:bell', NULL,
  'PS600 报警主机', 1, 0, 2, 'ALARM_PS600', 1
FROM DUAL
WHERE NOT EXISTS (
  SELECT 1 FROM iot_product WHERE `product_key` = 'ALARM_HOST_PRODUCT' AND `deleted` = 0
);

-- 确保产品为发布状态
UPDATE iot_product 
SET `status` = 1
WHERE `product_key` = 'ALARM_HOST_PRODUCT' AND `deleted` = 0;

-- 4) 输出结果核对
SET @alarm_product_id := (
  SELECT id FROM iot_product WHERE `product_key` = 'ALARM_HOST_PRODUCT' AND `deleted` = 0 LIMIT 1
);

SELECT 
  @root_category_id     AS root_category_id,
  @alarm_category_id    AS alarm_category_id,
  @alarm_product_id     AS alarm_product_id;

-- 使用说明：
-- 1. 执行本脚本后，会创建“报警主机”分类与“报警主机产品”（如不存在）
-- 2. 记录输出的 @alarm_product_id，用于后端创建设备时设置 productId
-- 3. 如需调整名称/图标/描述/联网方式，可直接 UPDATE iot_product / iot_product_category
