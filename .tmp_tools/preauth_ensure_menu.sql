INSERT INTO system_menu (
  name, permission, type, sort, parent_id, path, icon, component, component_name,
  status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted
)
SELECT
  CONCAT('AUTO权限:', p.permission) AS name,
  p.permission,
  3 AS type,
  0 AS sort,
  0 AS parent_id,
  '' AS path,
  '' AS icon,
  '' AS component,
  '' AS component_name,
  0 AS status,
  b'1' AS visible,
  b'1' AS keep_alive,
  b'1' AS always_show,
  'mcp-preauth-sync' AS creator,
  NOW() AS create_time,
  'mcp-preauth-sync' AS updater,
  NOW() AS update_time,
  b'0' AS deleted
FROM (
SELECT 'infra:api-access-log:export' AS permission
UNION ALL
SELECT 'infra:api-access-log:query' AS permission
UNION ALL
SELECT 'infra:api-error-log:export' AS permission
UNION ALL
SELECT 'infra:api-error-log:query' AS permission
UNION ALL
SELECT 'infra:api-error-log:update-status' AS permission
UNION ALL
SELECT 'infra:codegen:create' AS permission
UNION ALL
SELECT 'infra:codegen:delete' AS permission
UNION ALL
SELECT 'infra:codegen:download' AS permission
UNION ALL
SELECT 'infra:codegen:preview' AS permission
UNION ALL
SELECT 'infra:codegen:query' AS permission
UNION ALL
SELECT 'infra:codegen:update' AS permission
UNION ALL
SELECT 'infra:config:create' AS permission
UNION ALL
SELECT 'infra:config:delete' AS permission
UNION ALL
SELECT 'infra:config:export' AS permission
UNION ALL
SELECT 'infra:config:query' AS permission
UNION ALL
SELECT 'infra:config:update' AS permission
UNION ALL
SELECT 'infra:data-source-config:create' AS permission
UNION ALL
SELECT 'infra:data-source-config:delete' AS permission
UNION ALL
SELECT 'infra:data-source-config:query' AS permission
UNION ALL
SELECT 'infra:data-source-config:update' AS permission
UNION ALL
SELECT 'infra:demo01-contact:create' AS permission
UNION ALL
SELECT 'infra:demo01-contact:delete' AS permission
UNION ALL
SELECT 'infra:demo01-contact:export' AS permission
UNION ALL
SELECT 'infra:demo01-contact:query' AS permission
UNION ALL
SELECT 'infra:demo01-contact:update' AS permission
UNION ALL
SELECT 'infra:demo02-category:create' AS permission
UNION ALL
SELECT 'infra:demo02-category:delete' AS permission
UNION ALL
SELECT 'infra:demo02-category:export' AS permission
UNION ALL
SELECT 'infra:demo02-category:query' AS permission
UNION ALL
SELECT 'infra:demo02-category:update' AS permission
UNION ALL
SELECT 'infra:demo03-student:create' AS permission
UNION ALL
SELECT 'infra:demo03-student:delete' AS permission
UNION ALL
SELECT 'infra:demo03-student:export' AS permission
UNION ALL
SELECT 'infra:demo03-student:query' AS permission
UNION ALL
SELECT 'infra:demo03-student:update' AS permission
UNION ALL
SELECT 'infra:file-config:create' AS permission
UNION ALL
SELECT 'infra:file-config:delete' AS permission
UNION ALL
SELECT 'infra:file-config:query' AS permission
UNION ALL
SELECT 'infra:file-config:update' AS permission
UNION ALL
SELECT 'infra:file:delete' AS permission
UNION ALL
SELECT 'infra:file:query' AS permission
UNION ALL
SELECT 'infra:job:create' AS permission
UNION ALL
SELECT 'infra:job:delete' AS permission
UNION ALL
SELECT 'infra:job:export' AS permission
UNION ALL
SELECT 'infra:job:query' AS permission
UNION ALL
SELECT 'infra:job:trigger' AS permission
UNION ALL
SELECT 'infra:job:update' AS permission
UNION ALL
SELECT 'infra:redis:get-monitor-info' AS permission
UNION ALL
SELECT 'iot:access-alarm:query' AS permission
UNION ALL
SELECT 'iot:access-alarm:update' AS permission
UNION ALL
SELECT 'iot:access-auth-task:cancel' AS permission
UNION ALL
SELECT 'iot:access-auth-task:dispatch' AS permission
UNION ALL
SELECT 'iot:access-auth-task:query' AS permission
UNION ALL
SELECT 'iot:access-auth-task:retry' AS permission
UNION ALL
SELECT 'iot:access-auth-task:revoke' AS permission
UNION ALL
SELECT 'iot:access-authorization:create' AS permission
UNION ALL
SELECT 'iot:access-authorization:delete' AS permission
UNION ALL
SELECT 'iot:access-authorization:query' AS permission
UNION ALL
SELECT 'iot:access-authorization:update' AS permission
UNION ALL
SELECT 'iot:access-card:add' AS permission
UNION ALL
SELECT 'iot:access-card:clear' AS permission
UNION ALL
SELECT 'iot:access-card:delete' AS permission
UNION ALL
SELECT 'iot:access-card:query' AS permission
UNION ALL
SELECT 'iot:access-card:update' AS permission
UNION ALL
SELECT 'iot:access-channel:control' AS permission
UNION ALL
SELECT 'iot:access-channel:discover' AS permission
UNION ALL
SELECT 'iot:access-channel:query' AS permission
UNION ALL
SELECT 'iot:access-credential:verify' AS permission
UNION ALL
SELECT 'iot:access-department:create' AS permission
UNION ALL
SELECT 'iot:access-department:delete' AS permission
UNION ALL
SELECT 'iot:access-department:query' AS permission
UNION ALL
SELECT 'iot:access-department:update' AS permission
UNION ALL
SELECT 'iot:access-device:activate' AS permission
UNION ALL
SELECT 'iot:access-device:deactivate' AS permission
UNION ALL
SELECT 'iot:access-device:query' AS permission
UNION ALL
SELECT 'iot:access-dispatch:query' AS permission
UNION ALL
SELECT 'iot:access-event:query' AS permission
UNION ALL
SELECT 'iot:access-management:control' AS permission
UNION ALL
SELECT 'iot:access-management:query' AS permission
UNION ALL
SELECT 'iot:access-operation-log:query' AS permission
UNION ALL
SELECT 'iot:access-permission-group:create' AS permission
UNION ALL
SELECT 'iot:access-permission-group:delete' AS permission
UNION ALL
SELECT 'iot:access-permission-group:query' AS permission
UNION ALL
SELECT 'iot:access-permission-group:update' AS permission
UNION ALL
SELECT 'iot:access-person-permission:assign' AS permission
UNION ALL
SELECT 'iot:access-person-permission:query' AS permission
UNION ALL
SELECT 'iot:access-person:create' AS permission
UNION ALL
SELECT 'iot:access-person:delete' AS permission
UNION ALL
SELECT 'iot:access-person:export' AS permission
UNION ALL
SELECT 'iot:access-person:import' AS permission
UNION ALL
SELECT 'iot:access-person:query' AS permission
UNION ALL
SELECT 'iot:access-person:update' AS permission
UNION ALL
SELECT 'iot:access-record:query' AS permission
UNION ALL
SELECT 'iot:access:device-sync' AS permission
UNION ALL
SELECT 'iot:access:query' AS permission
UNION ALL
SELECT 'iot:alarm-event:export' AS permission
UNION ALL
SELECT 'iot:alarm-event:handle' AS permission
UNION ALL
SELECT 'iot:alarm-event:query' AS permission
UNION ALL
SELECT 'iot:alarm-host:arm' AS permission
UNION ALL
SELECT 'iot:alarm-host:clear-alarm' AS permission
UNION ALL
SELECT 'iot:alarm-host:create' AS permission
UNION ALL
SELECT 'iot:alarm-host:delete' AS permission
UNION ALL
SELECT 'iot:alarm-host:query' AS permission
UNION ALL
SELECT 'iot:alarm-host:update' AS permission
UNION ALL
SELECT 'iot:alert-config:create' AS permission
UNION ALL
SELECT 'iot:alert-config:delete' AS permission
UNION ALL
SELECT 'iot:alert-config:query' AS permission
UNION ALL
SELECT 'iot:alert-config:update' AS permission
UNION ALL
SELECT 'iot:alert-record:process' AS permission
UNION ALL
SELECT 'iot:alert-record:query' AS permission
UNION ALL
SELECT 'iot:area:create' AS permission
UNION ALL
SELECT 'iot:area:delete' AS permission
UNION ALL
SELECT 'iot:area:query' AS permission
UNION ALL
SELECT 'iot:area:update' AS permission
UNION ALL
SELECT 'iot:building-bac:control' AS permission
UNION ALL
SELECT 'iot:building-bac:query' AS permission
UNION ALL
SELECT 'iot:building-bac:update' AS permission
UNION ALL
SELECT 'iot:building-energy:create' AS permission
UNION ALL
SELECT 'iot:building-energy:delete' AS permission
UNION ALL
SELECT 'iot:building-energy:query' AS permission
UNION ALL
SELECT 'iot:building-energy:update' AS permission
UNION ALL
SELECT 'iot:building-env:create' AS permission
UNION ALL
SELECT 'iot:building-env:delete' AS permission
UNION ALL
SELECT 'iot:building-env:query' AS permission
UNION ALL
SELECT 'iot:building-env:update' AS permission
UNION ALL
SELECT 'iot:building-lighting:control' AS permission
UNION ALL
SELECT 'iot:building-lighting:query' AS permission
UNION ALL
SELECT 'iot:building-lighting:update' AS permission
UNION ALL
SELECT 'iot:building:create' AS permission
UNION ALL
SELECT 'iot:building:delete' AS permission
UNION ALL
SELECT 'iot:building:query' AS permission
UNION ALL
SELECT 'iot:building:update' AS permission
UNION ALL
SELECT 'iot:camera:create' AS permission
UNION ALL
SELECT 'iot:camera:cruise:control' AS permission
UNION ALL
SELECT 'iot:camera:cruise:create' AS permission
UNION ALL
SELECT 'iot:camera:cruise:delete' AS permission
UNION ALL
SELECT 'iot:camera:cruise:query' AS permission
UNION ALL
SELECT 'iot:camera:cruise:update' AS permission
UNION ALL
SELECT 'iot:camera:delete' AS permission
UNION ALL
SELECT 'iot:camera:preset:create' AS permission
UNION ALL
SELECT 'iot:camera:preset:delete' AS permission
UNION ALL
SELECT 'iot:camera:preset:query' AS permission
UNION ALL
SELECT 'iot:camera:preset:update' AS permission
UNION ALL
SELECT 'iot:camera:query' AS permission
UNION ALL
SELECT 'iot:camera:recording' AS permission
UNION ALL
SELECT 'iot:camera:snapshot' AS permission
UNION ALL
SELECT 'iot:camera:test' AS permission
UNION ALL
SELECT 'iot:camera:update' AS permission
UNION ALL
SELECT 'iot:campus:create' AS permission
UNION ALL
SELECT 'iot:campus:delete' AS permission
UNION ALL
SELECT 'iot:campus:query' AS permission
UNION ALL
SELECT 'iot:campus:update' AS permission
UNION ALL
SELECT 'iot:changhui-alarm:create' AS permission
UNION ALL
SELECT 'iot:changhui-alarm:delete' AS permission
UNION ALL
SELECT 'iot:changhui-alarm:query' AS permission
UNION ALL
SELECT 'iot:changhui-alarm:update' AS permission
UNION ALL
SELECT 'iot:changhui-control:delete' AS permission
UNION ALL
SELECT 'iot:changhui-control:operate' AS permission
UNION ALL
SELECT 'iot:changhui-control:query' AS permission
UNION ALL
SELECT 'iot:changhui-data:create' AS permission
UNION ALL
SELECT 'iot:changhui-data:delete' AS permission
UNION ALL
SELECT 'iot:changhui-data:export' AS permission
UNION ALL
SELECT 'iot:changhui-data:query' AS permission
UNION ALL
SELECT 'iot:changhui-device:create' AS permission
UNION ALL
SELECT 'iot:changhui-device:delete' AS permission
UNION ALL
SELECT 'iot:changhui-device:query' AS permission
UNION ALL
SELECT 'iot:changhui-device:update' AS permission
UNION ALL
SELECT 'iot:changhui-upgrade:create' AS permission
UNION ALL
SELECT 'iot:changhui-upgrade:delete' AS permission
UNION ALL
SELECT 'iot:changhui-upgrade:query' AS permission
UNION ALL
SELECT 'iot:changhui-upgrade:update' AS permission
UNION ALL
SELECT 'iot:data-rule:create' AS permission
UNION ALL
SELECT 'iot:data-rule:delete' AS permission
UNION ALL
SELECT 'iot:data-rule:query' AS permission
UNION ALL
SELECT 'iot:data-rule:update' AS permission
UNION ALL
SELECT 'iot:data-sink:create' AS permission
UNION ALL
SELECT 'iot:data-sink:delete' AS permission
UNION ALL
SELECT 'iot:data-sink:query' AS permission
UNION ALL
SELECT 'iot:data-sink:update' AS permission
UNION ALL
SELECT 'iot:device:create' AS permission
UNION ALL
SELECT 'iot:device:delete' AS permission
UNION ALL
SELECT 'iot:device:message-end' AS permission
UNION ALL
SELECT 'iot:device:message-query' AS permission
UNION ALL
SELECT 'iot:device:property-query' AS permission
UNION ALL
SELECT 'iot:device:query' AS permission
UNION ALL
SELECT 'iot:device:update' AS permission
UNION ALL
SELECT 'iot:door-group:create' AS permission
UNION ALL
SELECT 'iot:door-group:delete' AS permission
UNION ALL
SELECT 'iot:door-group:query' AS permission
UNION ALL
SELECT 'iot:door-group:update' AS permission
UNION ALL
SELECT 'iot:door-post:create' AS permission
UNION ALL
SELECT 'iot:door-post:delete' AS permission
UNION ALL
SELECT 'iot:door-post:query' AS permission
UNION ALL
SELECT 'iot:door-post:update' AS permission
UNION ALL
SELECT 'iot:epatrol-person:create' AS permission
UNION ALL
SELECT 'iot:epatrol-person:delete' AS permission
UNION ALL
SELECT 'iot:epatrol-person:query' AS permission
UNION ALL
SELECT 'iot:epatrol-person:update' AS permission
UNION ALL
SELECT 'iot:epatrol-plan:create' AS permission
UNION ALL
SELECT 'iot:epatrol-plan:delete' AS permission
UNION ALL
SELECT 'iot:epatrol-plan:query' AS permission
UNION ALL
SELECT 'iot:epatrol-plan:update' AS permission
UNION ALL
SELECT 'iot:epatrol-point:create' AS permission
UNION ALL
SELECT 'iot:epatrol-point:delete' AS permission
UNION ALL
SELECT 'iot:epatrol-point:query' AS permission
UNION ALL
SELECT 'iot:epatrol-point:update' AS permission
UNION ALL
SELECT 'iot:epatrol-route:create' AS permission
UNION ALL
SELECT 'iot:epatrol-route:delete' AS permission
UNION ALL
SELECT 'iot:epatrol-route:query' AS permission
UNION ALL
SELECT 'iot:epatrol-route:update' AS permission
UNION ALL
SELECT 'iot:epatrol-task:query' AS permission
UNION ALL
SELECT 'iot:epatrol-task:submit' AS permission
UNION ALL
SELECT 'iot:floor:create' AS permission
UNION ALL
SELECT 'iot:floor:delete' AS permission
UNION ALL
SELECT 'iot:floor:query' AS permission
UNION ALL
SELECT 'iot:floor:update' AS permission
UNION ALL
SELECT 'iot:gis:query' AS permission
UNION ALL
SELECT 'iot:ibms-channel:create' AS permission
UNION ALL
SELECT 'iot:ibms-channel:delete' AS permission
UNION ALL
SELECT 'iot:ibms-channel:export' AS permission
UNION ALL
SELECT 'iot:ibms-channel:query' AS permission
UNION ALL
SELECT 'iot:ibms-channel:update' AS permission
UNION ALL
SELECT 'iot:ibms-device:create' AS permission
UNION ALL
SELECT 'iot:ibms-device:delete' AS permission
UNION ALL
SELECT 'iot:ibms-device:export' AS permission
UNION ALL
SELECT 'iot:ibms-device:query' AS permission
UNION ALL
SELECT 'iot:ibms-device:update' AS permission
UNION ALL
SELECT 'iot:ibms-product:create' AS permission
UNION ALL
SELECT 'iot:ibms-product:delete' AS permission
UNION ALL
SELECT 'iot:ibms-product:export' AS permission
UNION ALL
SELECT 'iot:ibms-product:query' AS permission
UNION ALL
SELECT 'iot:ibms-product:update' AS permission
UNION ALL
SELECT 'iot:ibms-space:create' AS permission
UNION ALL
SELECT 'iot:ibms-space:delete' AS permission
UNION ALL
SELECT 'iot:ibms-space:export' AS permission
UNION ALL
SELECT 'iot:ibms-space:query' AS permission
UNION ALL
SELECT 'iot:ibms-space:update' AS permission
UNION ALL
SELECT 'iot:ota-firmware:create' AS permission
UNION ALL
SELECT 'iot:ota-firmware:query' AS permission
UNION ALL
SELECT 'iot:ota-firmware:update' AS permission
UNION ALL
SELECT 'iot:ota-task-record:cancel' AS permission
UNION ALL
SELECT 'iot:ota-task-record:query' AS permission
UNION ALL
SELECT 'iot:parking:blacklist:create' AS permission
UNION ALL
SELECT 'iot:parking:blacklist:delete' AS permission
UNION ALL
SELECT 'iot:parking:blacklist:query' AS permission
UNION ALL
SELECT 'iot:parking:blacklist:release' AS permission
UNION ALL
SELECT 'iot:parking:blacklist:update' AS permission
UNION ALL
SELECT 'iot:parking:charge-rule-apply:create' AS permission
UNION ALL
SELECT 'iot:parking:charge-rule-apply:delete' AS permission
UNION ALL
SELECT 'iot:parking:charge-rule-apply:query' AS permission
UNION ALL
SELECT 'iot:parking:charge-rule-apply:update' AS permission
UNION ALL
SELECT 'iot:parking:charge-rule:create' AS permission
UNION ALL
SELECT 'iot:parking:charge-rule:delete' AS permission
UNION ALL
SELECT 'iot:parking:charge-rule:query' AS permission
UNION ALL
SELECT 'iot:parking:charge-rule:update' AS permission
UNION ALL
SELECT 'iot:parking:free-vehicle:create' AS permission
UNION ALL
SELECT 'iot:parking:free-vehicle:delete' AS permission
UNION ALL
SELECT 'iot:parking:free-vehicle:query' AS permission
UNION ALL
SELECT 'iot:parking:free-vehicle:update' AS permission
UNION ALL
SELECT 'iot:parking:gate:create' AS permission
UNION ALL
SELECT 'iot:parking:gate:delete' AS permission
UNION ALL
SELECT 'iot:parking:gate:query' AS permission
UNION ALL
SELECT 'iot:parking:gate:update' AS permission
UNION ALL
SELECT 'iot:parking:lane:create' AS permission
UNION ALL
SELECT 'iot:parking:lane:delete' AS permission
UNION ALL
SELECT 'iot:parking:lane:query' AS permission
UNION ALL
SELECT 'iot:parking:lane:update' AS permission
UNION ALL
SELECT 'iot:parking:lot:create' AS permission
UNION ALL
SELECT 'iot:parking:lot:delete' AS permission
UNION ALL
SELECT 'iot:parking:lot:query' AS permission
UNION ALL
SELECT 'iot:parking:lot:update' AS permission
UNION ALL
SELECT 'iot:parking:monthly-vehicle:create' AS permission
UNION ALL
SELECT 'iot:parking:monthly-vehicle:delete' AS permission
UNION ALL
SELECT 'iot:parking:monthly-vehicle:query' AS permission
UNION ALL
SELECT 'iot:parking:monthly-vehicle:recharge' AS permission
UNION ALL
SELECT 'iot:parking:monthly-vehicle:update' AS permission
UNION ALL
SELECT 'iot:parking:pass-rule:create' AS permission
UNION ALL
SELECT 'iot:parking:pass-rule:delete' AS permission
UNION ALL
SELECT 'iot:parking:pass-rule:query' AS permission
UNION ALL
SELECT 'iot:parking:pass-rule:update' AS permission
UNION ALL
SELECT 'iot:parking:present-vehicle:force-exit' AS permission
UNION ALL
SELECT 'iot:parking:present-vehicle:query' AS permission
UNION ALL
SELECT 'iot:parking:recharge-record:query' AS permission
UNION ALL
SELECT 'iot:parking:record:entry' AS permission
UNION ALL
SELECT 'iot:parking:record:exit' AS permission
UNION ALL
SELECT 'iot:parking:record:manual-pay' AS permission
UNION ALL
SELECT 'iot:parking:record:query' AS permission
UNION ALL
SELECT 'iot:parking:refund:apply' AS permission
UNION ALL
SELECT 'iot:parking:refund:close' AS permission
UNION ALL
SELECT 'iot:parking:refund:execute' AS permission
UNION ALL
SELECT 'iot:parking:refund:query' AS permission
UNION ALL
SELECT 'iot:parking:system:query' AS permission
UNION ALL
SELECT 'iot:parking:system:update' AS permission
UNION ALL
SELECT 'iot:patrol-plan:create' AS permission
UNION ALL
SELECT 'iot:patrol-plan:delete' AS permission
UNION ALL
SELECT 'iot:patrol-plan:pause' AS permission
UNION ALL
SELECT 'iot:patrol-plan:query' AS permission
UNION ALL
SELECT 'iot:patrol-plan:start' AS permission
UNION ALL
SELECT 'iot:patrol-plan:stop' AS permission
UNION ALL
SELECT 'iot:patrol-plan:update' AS permission
UNION ALL
SELECT 'iot:patrol-record:query' AS permission
UNION ALL
SELECT 'iot:patrol-record:update' AS permission
UNION ALL
SELECT 'iot:patrol-scene:create' AS permission
UNION ALL
SELECT 'iot:patrol-scene:delete' AS permission
UNION ALL
SELECT 'iot:patrol-scene:query' AS permission
UNION ALL
SELECT 'iot:patrol-scene:update' AS permission
UNION ALL
SELECT 'iot:patrol-task:create' AS permission
UNION ALL
SELECT 'iot:patrol-task:delete' AS permission
UNION ALL
SELECT 'iot:patrol-task:pause' AS permission
UNION ALL
SELECT 'iot:patrol-task:query' AS permission
UNION ALL
SELECT 'iot:patrol-task:start' AS permission
UNION ALL
SELECT 'iot:patrol-task:stop' AS permission
UNION ALL
SELECT 'iot:patrol-task:update' AS permission
UNION ALL
SELECT 'iot:product-category:create' AS permission
UNION ALL
SELECT 'iot:product-category:delete' AS permission
UNION ALL
SELECT 'iot:product-category:query' AS permission
UNION ALL
SELECT 'iot:product-category:update' AS permission
UNION ALL
SELECT 'iot:product:query' AS permission
UNION ALL
SELECT 'iot:product:update' AS permission
UNION ALL
SELECT 'iot:scene-rule:create' AS permission
UNION ALL
SELECT 'iot:scene-rule:delete' AS permission
UNION ALL
SELECT 'iot:scene-rule:query' AS permission
UNION ALL
SELECT 'iot:scene-rule:update' AS permission
UNION ALL
SELECT 'iot:task-config:query' AS permission
UNION ALL
SELECT 'iot:task:config:create' AS permission
UNION ALL
SELECT 'iot:task:config:delete' AS permission
UNION ALL
SELECT 'iot:task:config:execute' AS permission
UNION ALL
SELECT 'iot:task:config:query' AS permission
UNION ALL
SELECT 'iot:task:config:update' AS permission
UNION ALL
SELECT 'iot:task:monitor:query' AS permission
UNION ALL
SELECT 'iot:thing-model:create' AS permission
UNION ALL
SELECT 'iot:thing-model:delete' AS permission
UNION ALL
SELECT 'iot:thing-model:query' AS permission
UNION ALL
SELECT 'iot:thing-model:update' AS permission
UNION ALL
SELECT 'iot:video-inspection-task:create' AS permission
UNION ALL
SELECT 'iot:video-inspection-task:delete' AS permission
UNION ALL
SELECT 'iot:video-inspection-task:query' AS permission
UNION ALL
SELECT 'iot:video-inspection-task:update' AS permission
UNION ALL
SELECT 'iot:video-patrol-schedule:create' AS permission
UNION ALL
SELECT 'iot:video-patrol-schedule:delete' AS permission
UNION ALL
SELECT 'iot:video-patrol-schedule:query' AS permission
UNION ALL
SELECT 'iot:video-patrol-schedule:update' AS permission
UNION ALL
SELECT 'security:visitor:approve' AS permission
UNION ALL
SELECT 'security:visitor:create' AS permission
UNION ALL
SELECT 'security:visitor:delete' AS permission
UNION ALL
SELECT 'security:visitor:query' AS permission
UNION ALL
SELECT 'security:visitor:sign-out' AS permission
UNION ALL
SELECT 'security:visitor:update' AS permission
UNION ALL
SELECT 'system:dept:create' AS permission
UNION ALL
SELECT 'system:dept:delete' AS permission
UNION ALL
SELECT 'system:dept:query' AS permission
UNION ALL
SELECT 'system:dept:update' AS permission
UNION ALL
SELECT 'system:dict:create' AS permission
UNION ALL
SELECT 'system:dict:delete' AS permission
UNION ALL
SELECT 'system:dict:export' AS permission
UNION ALL
SELECT 'system:dict:query' AS permission
UNION ALL
SELECT 'system:dict:update' AS permission
UNION ALL
SELECT 'system:login-log:export' AS permission
UNION ALL
SELECT 'system:login-log:query' AS permission
UNION ALL
SELECT 'system:mail-account:create' AS permission
UNION ALL
SELECT 'system:mail-account:delete' AS permission
UNION ALL
SELECT 'system:mail-account:query' AS permission
UNION ALL
SELECT 'system:mail-account:update' AS permission
UNION ALL
SELECT 'system:mail-log:query' AS permission
UNION ALL
SELECT 'system:mail-template:create' AS permission
UNION ALL
SELECT 'system:mail-template:delete' AS permission
UNION ALL
SELECT 'system:mail-template:query' AS permission
UNION ALL
SELECT 'system:mail-template:send-mail' AS permission
UNION ALL
SELECT 'system:mail-template:update' AS permission
UNION ALL
SELECT 'system:menu:create' AS permission
UNION ALL
SELECT 'system:menu:delete' AS permission
UNION ALL
SELECT 'system:menu:query' AS permission
UNION ALL
SELECT 'system:menu:update' AS permission
UNION ALL
SELECT 'system:notice:create' AS permission
UNION ALL
SELECT 'system:notice:delete' AS permission
UNION ALL
SELECT 'system:notice:query' AS permission
UNION ALL
SELECT 'system:notice:update' AS permission
UNION ALL
SELECT 'system:notify-message:query' AS permission
UNION ALL
SELECT 'system:notify-template:create' AS permission
UNION ALL
SELECT 'system:notify-template:delete' AS permission
UNION ALL
SELECT 'system:notify-template:query' AS permission
UNION ALL
SELECT 'system:notify-template:send-notify' AS permission
UNION ALL
SELECT 'system:notify-template:update' AS permission
UNION ALL
SELECT 'system:oauth2-client:create' AS permission
UNION ALL
SELECT 'system:oauth2-client:delete' AS permission
UNION ALL
SELECT 'system:oauth2-client:query' AS permission
UNION ALL
SELECT 'system:oauth2-client:update' AS permission
UNION ALL
SELECT 'system:oauth2-token:delete' AS permission
UNION ALL
SELECT 'system:oauth2-token:page' AS permission
UNION ALL
SELECT 'system:operate-log:export' AS permission
UNION ALL
SELECT 'system:operate-log:query' AS permission
UNION ALL
SELECT 'system:permission:assign-role-data-scope' AS permission
UNION ALL
SELECT 'system:permission:assign-role-menu' AS permission
UNION ALL
SELECT 'system:permission:assign-user-role' AS permission
UNION ALL
SELECT 'system:post:create' AS permission
UNION ALL
SELECT 'system:post:delete' AS permission
UNION ALL
SELECT 'system:post:export' AS permission
UNION ALL
SELECT 'system:post:query' AS permission
UNION ALL
SELECT 'system:post:update' AS permission
UNION ALL
SELECT 'system:role:create' AS permission
UNION ALL
SELECT 'system:role:delete' AS permission
UNION ALL
SELECT 'system:role:export' AS permission
UNION ALL
SELECT 'system:role:query' AS permission
UNION ALL
SELECT 'system:role:update' AS permission
UNION ALL
SELECT 'system:sms-channel:create' AS permission
UNION ALL
SELECT 'system:sms-channel:delete' AS permission
UNION ALL
SELECT 'system:sms-channel:query' AS permission
UNION ALL
SELECT 'system:sms-channel:update' AS permission
UNION ALL
SELECT 'system:sms-log:export' AS permission
UNION ALL
SELECT 'system:sms-log:query' AS permission
UNION ALL
SELECT 'system:sms-template:create' AS permission
UNION ALL
SELECT 'system:sms-template:delete' AS permission
UNION ALL
SELECT 'system:sms-template:export' AS permission
UNION ALL
SELECT 'system:sms-template:query' AS permission
UNION ALL
SELECT 'system:sms-template:send-sms' AS permission
UNION ALL
SELECT 'system:sms-template:update' AS permission
UNION ALL
SELECT 'system:social-client:create' AS permission
UNION ALL
SELECT 'system:social-client:delete' AS permission
UNION ALL
SELECT 'system:social-client:query' AS permission
UNION ALL
SELECT 'system:social-client:update' AS permission
UNION ALL
SELECT 'system:social-user:query' AS permission
UNION ALL
SELECT 'system:tenant-package:create' AS permission
UNION ALL
SELECT 'system:tenant-package:delete' AS permission
UNION ALL
SELECT 'system:tenant-package:query' AS permission
UNION ALL
SELECT 'system:tenant-package:update' AS permission
UNION ALL
SELECT 'system:tenant:create' AS permission
UNION ALL
SELECT 'system:tenant:delete' AS permission
UNION ALL
SELECT 'system:tenant:export' AS permission
UNION ALL
SELECT 'system:tenant:query' AS permission
UNION ALL
SELECT 'system:tenant:update' AS permission
UNION ALL
SELECT 'system:user:create' AS permission
UNION ALL
SELECT 'system:user:delete' AS permission
UNION ALL
SELECT 'system:user:export' AS permission
UNION ALL
SELECT 'system:user:import' AS permission
UNION ALL
SELECT 'system:user:query' AS permission
UNION ALL
SELECT 'system:user:update' AS permission
UNION ALL
SELECT 'system:user:update-password' AS permission
) p
WHERE NOT EXISTS (
  SELECT 1 FROM system_menu m
  WHERE m.deleted = b'0' AND m.status = 0 AND m.permission = p.permission
);
