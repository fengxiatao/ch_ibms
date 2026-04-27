SET SESSION group_concat_max_len = 1048576;
SET @s := NULL;
SELECT GROUP_CONCAT(
  CONCAT(
    'SELECT ''', TABLE_NAME, ''' AS tbl,',
    'SUM(IF(tenant_id=1,1,0)) AS t1,',
    'SUM(IF(tenant_id=162,1,0)) AS t162,',
    'COUNT(*) AS total ',
    'FROM `', TABLE_NAME, '`'
  ) SEPARATOR ' UNION ALL '
) INTO @s
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = 'ch_ibms'
  AND COLUMN_NAME = 'tenant_id'
  AND (
    TABLE_NAME LIKE 'ibms_%' OR
    TABLE_NAME LIKE 'iot_access_%' OR
    TABLE_NAME LIKE 'iot_visitor_%' OR
    TABLE_NAME LIKE 'iot_parking_%' OR
    TABLE_NAME LIKE 'iot_keding_%' OR
    TABLE_NAME LIKE 'iot_alarm_%' OR
    TABLE_NAME LIKE 'iot_alert_%' OR
    TABLE_NAME LIKE 'iot_camera_%' OR
    TABLE_NAME LIKE 'iot_cloud_defense_%' OR
    TABLE_NAME LIKE 'iot_video_%' OR
    TABLE_NAME LIKE 'iot_epatrol_%' OR
    TABLE_NAME LIKE 'iot_patrol_%' OR
    TABLE_NAME LIKE 'security_%' OR
    TABLE_NAME LIKE 'iot_device_%' OR
    TABLE_NAME LIKE 'iot_ota_%' OR
    TABLE_NAME LIKE 'access_parking_%' OR
    TABLE_NAME IN ('building','area','campus','floor',
                   'iot_thing_model','iot_think_model_function',
                   'iot_product_category','iot_subsystem',
                   'iot_job_type_definition','iot_data_rule','iot_data_sink',
                   'iot_service_config','iot_scheduled_task_config',
                   'iot_rule_scene','iot_scene_rule','iot_ui_component_template')
  )
  AND TABLE_NAME NOT LIKE 'bak_%';

PREPARE stmt FROM @s;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
