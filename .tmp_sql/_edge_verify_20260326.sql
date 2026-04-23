SELECT
  (SELECT COUNT(*)
   FROM iot_parking_monthly_vehicle
   WHERE tenant_id=1 AND deleted=b'0' AND plate_number IN ('测M00001','测M00002')) AS monthly_cnt,
  (SELECT COUNT(*)
   FROM iot_parking_monthly_recharge
   WHERE tenant_id=1 AND deleted=b'0' AND plate_number='测M00002'
     AND remark='seed:edge_v1 monthly recharge record') AS monthly_recharge_cnt,
  (SELECT COUNT(*)
   FROM iot_parking_free_vehicle
   WHERE tenant_id=1 AND deleted=b'0' AND plate_number='测F00001') AS free_cnt,
  (SELECT COUNT(*)
   FROM iot_parking_blacklist
   WHERE tenant_id=1 AND deleted=b'0' AND plate_number IN ('测BL0001','测BL0002')) AS blacklist_cnt,
  (SELECT COUNT(*)
   FROM iot_parking_present_vehicle
   WHERE tenant_id=1 AND deleted=b'0' AND lot_id=2 AND plate_number IN ('测LT0001','测U00001')) AS present_edge_cnt,
  (SELECT COUNT(*)
   FROM iot_parking_record
   WHERE tenant_id=1 AND deleted=b'0' AND lot_id=2 AND plate_number IN ('测C34567','测U00001')
     AND record_status=1 AND payment_status=0) AS unpaid_record_cnt,
  (SELECT COUNT(*)
   FROM iot_access_event_log
   WHERE tenant_id=1 AND deleted=b'0' AND device_id=114
     AND snapshot_url IN ('https://example.com/access/capture/card_ok.jpg',
                          'https://example.com/access/capture/face_fail.jpg',
                          'https://example.com/access/capture/forced_open.jpg')) AS access_event_cnt,
  (SELECT COUNT(*)
   FROM iot_access_operation_log
   WHERE tenant_id=1 AND deleted=b'0' AND device_id=114
     AND operation_type IN ('open_door','close_door','always_open','always_closed','cancel_always')
     AND JSON_EXTRACT(request_params,'$.source')='seed') AS access_op_cnt;

