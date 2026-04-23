-- =============================================
-- IBMS 编码规范字典初始化脚本（MySQL）
-- 来源：IBMS编码规范清单_V2.0.xlsx
-- 可重复执行：已存在时跳过
-- =============================================

INSERT INTO `system_dict_type` (`name`, `type`, `status`, `remark`, `creator`, `create_time`, `updater`, `update_time`, `deleted`, `deleted_time`)
SELECT v.`name`, v.`type`, 0, v.`remark`, 'system', NOW(), 'system', NOW(), b'0', NULL
FROM (
SELECT 'IBMS 专业分组' AS `name`, 'ibms_group' AS `type`, '来源：IBMS编码规范清单_V2.0.xlsx / 专业分组定义' AS `remark`
UNION ALL
SELECT 'IBMS 系统码' AS `name`, 'ibms_system' AS `type`, '来源：IBMS编码规范清单_V2.0.xlsx / 系统码定义' AS `remark`
UNION ALL
SELECT 'IBMS 点位类型码' AS `name`, 'ibms_point_type' AS `type`, '来源：IBMS编码规范清单_V2.0.xlsx / 点位类型码定义' AS `remark`
UNION ALL
SELECT 'IBMS 设备型号码' AS `name`, 'ibms_device_model' AS `type`, '来源：IBMS编码规范清单_V2.0.xlsx / 设备型号码定义' AS `remark`
UNION ALL
SELECT 'IBMS 设备类型码' AS `name`, 'ibms_device_type' AS `type`, '来源：IBMS编码规范清单_V2.0.xlsx / 设备类型码定义' AS `remark`
UNION ALL
SELECT 'IBMS 区域码' AS `name`, 'ibms_region' AS `type`, '来源：IBMS编码规范清单_V2.0.xlsx / 区域码定义' AS `remark`
UNION ALL
SELECT 'IBMS 厂商品牌' AS `name`, 'ibms_brand' AS `type`, '来源：设备编码品牌段，与 ibms_device.brand / 产品 manufacturer 对齐；扩展见 remark JSON' AS `remark`
) v
LEFT JOIN `system_dict_type` t ON t.`type` = v.`type` AND t.`deleted` = b'0'
WHERE t.`id` IS NULL;

INSERT INTO `system_dict_data` (`sort`, `label`, `value`, `dict_type`, `status`, `color_type`, `css_class`, `remark`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT v.`sort`, v.`label`, v.`value`, v.`dict_type`, 0, v.`color_type`, v.`css_class`, v.`remark`, 'system', NOW(), 'system', NOW(), b'0'
FROM (
SELECT 1 AS `sort`, '智慧安防' AS `label`, 'SA' AS `value`, 'ibms_group' AS `dict_type`, '' AS `color_type`, '' AS `css_class`, '{"systems":["VI","AL","GR"],"icon":"fa-shield-alt","color":"blue","desc":"视频监控、入侵报警、电子巡更","systemCount":12}' AS `remark`
UNION ALL
SELECT 2 AS `sort`, '智慧通行' AS `label`, 'ST' AS `value`, 'ibms_group' AS `dict_type`, '' AS `color_type`, '' AS `css_class`, '{"systems":["AC","IC","CA"],"icon":"fa-door-open","color":"purple","desc":"门禁、停车场、对讲","systemCount":8}' AS `remark`
UNION ALL
SELECT 3 AS `sort`, '智慧建筑' AS `label`, 'SB' AS `value`, 'ibms_group' AS `dict_type`, '' AS `color_type`, '' AS `css_class`, '{"systems":["BA","LI","EL"],"icon":"fa-building","color":"cyan","desc":"楼宇自控、照明、电梯","systemCount":10}' AS `remark`
UNION ALL
SELECT 4 AS `sort`, '智慧能源' AS `label`, 'SE' AS `value`, 'ibms_group' AS `dict_type`, '' AS `color_type`, '' AS `css_class`, '{"systems":["EP","EN"],"icon":"fa-bolt","color":"amber","desc":"电力监测、能源管理","systemCount":5}' AS `remark`
UNION ALL
SELECT 5 AS `sort`, '消防安全' AS `label`, 'SF' AS `value`, 'ibms_group' AS `dict_type`, '' AS `color_type`, '' AS `css_class`, '{"systems":["FD","PA"],"icon":"fa-fire-extinguisher","color":"rose","desc":"火灾报警、广播","systemCount":6}' AS `remark`
UNION ALL
SELECT 1 AS `sort`, '视频监控' AS `label`, 'VI' AS `value`, 'ibms_system' AS `dict_type`, '' AS `color_type`, '' AS `css_class`, '{"group":"SA","en":"Video Surveillance","desc":"摄像机/NVR/DVR"}' AS `remark`
UNION ALL
SELECT 2 AS `sort`, '门禁系统' AS `label`, 'AC' AS `value`, 'ibms_system' AS `dict_type`, '' AS `color_type`, '' AS `css_class`, '{"group":"ST","en":"Access Control","desc":"门禁控制器/读卡器"}' AS `remark`
UNION ALL
SELECT 3 AS `sort`, '防盗报警' AS `label`, 'AL' AS `value`, 'ibms_system' AS `dict_type`, '' AS `color_type`, '' AS `css_class`, '{"group":"SA","en":"Alarm System","desc":"报警主机/探测器"}' AS `remark`
UNION ALL
SELECT 4 AS `sort`, '楼宇自控' AS `label`, 'BA' AS `value`, 'ibms_system' AS `dict_type`, '' AS `color_type`, '' AS `css_class`, '{"group":"SB","en":"Building Automation","desc":"DDC/传感器/执行器"}' AS `remark`
UNION ALL
SELECT 5 AS `sort`, '火灾报警' AS `label`, 'FD' AS `value`, 'ibms_system' AS `dict_type`, '' AS `color_type`, '' AS `css_class`, '{"group":"SF","en":"Fire Detection","desc":"火灾报警主机/探测器"}' AS `remark`
UNION ALL
SELECT 6 AS `sort`, '智能照明' AS `label`, 'LI' AS `value`, 'ibms_system' AS `dict_type`, '' AS `color_type`, '' AS `css_class`, '{"group":"SB","en":"Lighting Control","desc":"照明控制箱/灯具"}' AS `remark`
UNION ALL
SELECT 7 AS `sort`, '变配电' AS `label`, 'EP' AS `value`, 'ibms_system' AS `dict_type`, '' AS `color_type`, '' AS `css_class`, '{"group":"SE","en":"Power Distribution","desc":"电力仪表/断路器"}' AS `remark`
UNION ALL
SELECT 8 AS `sort`, '智能对讲' AS `label`, 'IC' AS `value`, 'ibms_system' AS `dict_type`, '' AS `color_type`, '' AS `css_class`, '{"group":"ST","en":"Intercom System","desc":"门口机/管理机"}' AS `remark`
UNION ALL
SELECT 9 AS `sort`, '电梯控制' AS `label`, 'EL' AS `value`, 'ibms_system' AS `dict_type`, '' AS `color_type`, '' AS `css_class`, '{"group":"SB","en":"Elevator Control","desc":"电梯主控器"}' AS `remark`
UNION ALL
SELECT 10 AS `sort`, '能源管理' AS `label`, 'EN' AS `value`, 'ibms_system' AS `dict_type`, '' AS `color_type`, '' AS `css_class`, '{"group":"SE","en":"Energy Management","desc":"电能表/能耗监测"}' AS `remark`
UNION ALL
SELECT 11 AS `sort`, '公共广播' AS `label`, 'PA' AS `value`, 'ibms_system' AS `dict_type`, '' AS `color_type`, '' AS `css_class`, '{"group":"SF","en":"Public Address","desc":"功放/喇叭"}' AS `remark`
UNION ALL
SELECT 12 AS `sort`, '停车场' AS `label`, 'CA' AS `value`, 'ibms_system' AS `dict_type`, '' AS `color_type`, '' AS `css_class`, '{"group":"ST","en":"Car Park","desc":"道闸/摄像机/检测器"}' AS `remark`
UNION ALL
SELECT 13 AS `sort`, '巡更系统' AS `label`, 'GR' AS `value`, 'ibms_system' AS `dict_type`, '' AS `color_type`, '' AS `css_class`, '{"group":"SA","en":"Guard Tour","desc":"巡更棒/巡更点"}' AS `remark`
UNION ALL
SELECT 1 AS `sort`, '视频通道' AS `label`, 'VT' AS `value`, 'ibms_point_type' AS `dict_type`, '' AS `color_type`, '' AS `css_class`, '{"dataType":"视频流","systems":["VI"],"desc":"一路摄像机"}' AS `remark`
UNION ALL
SELECT 2 AS `sort`, '报警输入' AS `label`, 'AI' AS `value`, 'ibms_point_type' AS `dict_type`, '' AS `color_type`, '' AS `css_class`, '{"dataType":"DI","systems":["AL"],"desc":"开关量输入"}' AS `remark`
UNION ALL
SELECT 3 AS `sort`, '报警输出' AS `label`, 'AO' AS `value`, 'ibms_point_type' AS `dict_type`, '' AS `color_type`, '' AS `css_class`, '{"dataType":"DO","systems":["AL"],"desc":"继电器输出"}' AS `remark`
UNION ALL
SELECT 4 AS `sort`, '门禁点' AS `label`, 'DR' AS `value`, 'ibms_point_type' AS `dict_type`, '' AS `color_type`, '' AS `css_class`, '{"dataType":"逻辑点","systems":["AC"],"desc":"一扇门"}' AS `remark`
UNION ALL
SELECT 5 AS `sort`, '数字输入' AS `label`, 'DI' AS `value`, 'ibms_point_type' AS `dict_type`, '' AS `color_type`, '' AS `css_class`, '{"dataType":"DI","systems":["BA","EP","EL"],"desc":"状态信号"}' AS `remark`
UNION ALL
SELECT 6 AS `sort`, '数字输出' AS `label`, 'DO' AS `value`, 'ibms_point_type' AS `dict_type`, '' AS `color_type`, '' AS `css_class`, '{"dataType":"DO","systems":["BA","EP"],"desc":"控制信号"}' AS `remark`
UNION ALL
SELECT 7 AS `sort`, '模拟输入' AS `label`, 'AI_AN' AS `value`, 'ibms_point_type' AS `dict_type`, '' AS `color_type`, '' AS `css_class`, '{"dataType":"AI","systems":["BA","EP","EN"],"desc":"连续量采集"}' AS `remark`
UNION ALL
SELECT 8 AS `sort`, '模拟输出' AS `label`, 'AO_AN' AS `value`, 'ibms_point_type' AS `dict_type`, '' AS `color_type`, '' AS `css_class`, '{"dataType":"AO","systems":["BA","EP"],"desc":"连续量控制"}' AS `remark`
UNION ALL
SELECT 9 AS `sort`, '对讲终端' AS `label`, 'IT' AS `value`, 'ibms_point_type' AS `dict_type`, '' AS `color_type`, '' AS `css_class`, '{"dataType":"音视频","systems":["IC"],"desc":"门口机/分机"}' AS `remark`
UNION ALL
SELECT 10 AS `sort`, '消防点位' AS `label`, 'FP' AS `value`, 'ibms_point_type' AS `dict_type`, '' AS `color_type`, '' AS `css_class`, '{"dataType":"火警/故障","systems":["FD"],"desc":"探测器/模块"}' AS `remark`
UNION ALL
SELECT 11 AS `sort`, '照明回路' AS `label`, 'LT' AS `value`, 'ibms_point_type' AS `dict_type`, '' AS `color_type`, '' AS `css_class`, '{"dataType":"DO/AO","systems":["LI"],"desc":"回路/调光"}' AS `remark`
UNION ALL
SELECT 12 AS `sort`, '电能监测' AS `label`, 'PM' AS `value`, 'ibms_point_type' AS `dict_type`, '' AS `color_type`, '' AS `css_class`, '{"dataType":"AI","systems":["EP","EN"],"desc":"电能计量"}' AS `remark`
UNION ALL
SELECT 13 AS `sort`, '电梯通道' AS `label`, 'EC' AS `value`, 'ibms_point_type' AS `dict_type`, '' AS `color_type`, '' AS `css_class`, '{"dataType":"DI/AI","systems":["EL"],"desc":"状态/报警"}' AS `remark`
UNION ALL
SELECT 14 AS `sort`, '广播通道' AS `label`, 'BC' AS `value`, 'ibms_point_type' AS `dict_type`, '' AS `color_type`, '' AS `css_class`, '{"dataType":"音频","systems":["PA"],"desc":"广播分区"}' AS `remark`
UNION ALL
SELECT 15 AS `sort`, '巡更点' AS `label`, 'GP' AS `value`, 'ibms_point_type' AS `dict_type`, '' AS `color_type`, '' AS `css_class`, '{"dataType":"DI","systems":["GR"],"desc":"巡更签到点"}' AS `remark`
UNION ALL
SELECT 16 AS `sort`, '停车场点' AS `label`, 'CP' AS `value`, 'ibms_point_type' AS `dict_type`, '' AS `color_type`, '' AS `css_class`, '{"dataType":"DI/DO/AI","systems":["CA"],"desc":"车道/车位"}' AS `remark`
UNION ALL
SELECT 1 AS `sort`, '枪机' AS `label`, 'DS' AS `value`, 'ibms_device_model' AS `dict_type`, '' AS `color_type`, '' AS `css_class`, '{"system":"VI","desc":"枪式摄像机"}' AS `remark`
UNION ALL
SELECT 2 AS `sort`, '球机' AS `label`, 'DP' AS `value`, 'ibms_device_model' AS `dict_type`, '' AS `color_type`, '' AS `css_class`, '{"system":"VI","desc":"球型摄像机"}' AS `remark`
UNION ALL
SELECT 3 AS `sort`, '半球摄像机' AS `label`, 'HP' AS `value`, 'ibms_device_model' AS `dict_type`, '' AS `color_type`, '' AS `css_class`, '{"system":"VI","desc":"吸顶安装"}' AS `remark`
UNION ALL
SELECT 4 AS `sort`, 'NVR' AS `label`, 'NV' AS `value`, 'ibms_device_model' AS `dict_type`, '' AS `color_type`, '' AS `css_class`, '{"system":"VI","desc":"网络录像机"}' AS `remark`
UNION ALL
SELECT 5 AS `sort`, 'IC卡读卡器' AS `label`, 'CR' AS `value`, 'ibms_device_model' AS `dict_type`, '' AS `color_type`, '' AS `css_class`, '{"system":"AC","desc":"读取M1/CPU卡"}' AS `remark`
UNION ALL
SELECT 6 AS `sort`, '指纹读卡器' AS `label`, 'FR' AS `value`, 'ibms_device_model' AS `dict_type`, '' AS `color_type`, '' AS `css_class`, '{"system":"AC","desc":"指纹识别"}' AS `remark`
UNION ALL
SELECT 7 AS `sort`, '人脸识别终端' AS `label`, 'LR' AS `value`, 'ibms_device_model' AS `dict_type`, '' AS `color_type`, '' AS `css_class`, '{"system":"AC","desc":"人脸识别"}' AS `remark`
UNION ALL
SELECT 8 AS `sort`, '单门控制器' AS `label`, 'CC' AS `value`, 'ibms_device_model' AS `dict_type`, '' AS `color_type`, '' AS `css_class`, '{"system":"AC","desc":"控制1道门"}' AS `remark`
UNION ALL
SELECT 9 AS `sort`, '双门控制器' AS `label`, 'DC' AS `value`, 'ibms_device_model' AS `dict_type`, '' AS `color_type`, '' AS `css_class`, '{"system":"AC","desc":"控制2道门"}' AS `remark`
UNION ALL
SELECT 10 AS `sort`, '房间温控器' AS `label`, 'TS' AS `value`, 'ibms_device_model' AS `dict_type`, '' AS `color_type`, '' AS `css_class`, '{"system":"BA","desc":"温度设定控制"}' AS `remark`
UNION ALL
SELECT 11 AS `sort`, 'DDC控制器' AS `label`, 'DD' AS `value`, 'ibms_device_model' AS `dict_type`, '' AS `color_type`, '' AS `css_class`, '{"system":"BA","desc":"可编程控制器"}' AS `remark`
UNION ALL
SELECT 1 AS `sort`, '摄像机' AS `label`, 'CAM' AS `value`, 'ibms_device_type' AS `dict_type`, '' AS `color_type`, '' AS `css_class`, '{"desc":"视频采集设备"}' AS `remark`
UNION ALL
SELECT 2 AS `sort`, '读卡器' AS `label`, 'READER' AS `value`, 'ibms_device_type' AS `dict_type`, '' AS `color_type`, '' AS `css_class`, '{"desc":"门禁读卡设备"}' AS `remark`
UNION ALL
SELECT 3 AS `sort`, '传感器' AS `label`, 'SENSOR' AS `value`, 'ibms_device_type' AS `dict_type`, '' AS `color_type`, '' AS `css_class`, '{"desc":"环境传感器"}' AS `remark`
UNION ALL
SELECT 4 AS `sort`, '控制器' AS `label`, 'CONTR' AS `value`, 'ibms_device_type' AS `dict_type`, '' AS `color_type`, '' AS `css_class`, '{"desc":"通用控制器"}' AS `remark`
UNION ALL
SELECT 5 AS `sort`, 'DDC控制器' AS `label`, 'DDC' AS `value`, 'ibms_device_type' AS `dict_type`, '' AS `color_type`, '' AS `css_class`, '{"desc":"可编程控制器"}' AS `remark`
UNION ALL
SELECT 6 AS `sort`, '网络录像机' AS `label`, 'NVR' AS `value`, 'ibms_device_type' AS `dict_type`, '' AS `color_type`, '' AS `css_class`, '{"desc":"视频存储设备"}' AS `remark`
UNION ALL
SELECT 7 AS `sort`, '探测器' AS `label`, 'DETECTOR' AS `value`, 'ibms_device_type' AS `dict_type`, '' AS `color_type`, '' AS `css_class`, '{"desc":"报警探测器"}' AS `remark`
UNION ALL
SELECT 8 AS `sort`, '调光驱动' AS `label`, 'DRIVER' AS `value`, 'ibms_device_type' AS `dict_type`, '' AS `color_type`, '' AS `css_class`, '{"desc":"LED调光驱动"}' AS `remark`
UNION ALL
SELECT 9 AS `sort`, '仪表' AS `label`, 'METER' AS `value`, 'ibms_device_type' AS `dict_type`, '' AS `color_type`, '' AS `css_class`, '{"desc":"电表/水表"}' AS `remark`
UNION ALL
SELECT 10 AS `sort`, '功放' AS `label`, 'AMPLIFIER' AS `value`, 'ibms_device_type' AS `dict_type`, '' AS `color_type`, '' AS `css_class`, '{"desc":"广播功放"}' AS `remark`
UNION ALL
SELECT 1 AS `sort`, '地下1层' AS `label`, 'B01' AS `value`, 'ibms_region' AS `dict_type`, '' AS `color_type`, '' AS `css_class`, '{"regionType":"地下室","example":"停车场/设备房"}' AS `remark`
UNION ALL
SELECT 2 AS `sort`, '地下2层' AS `label`, 'B02' AS `value`, 'ibms_region' AS `dict_type`, '' AS `color_type`, '' AS `css_class`, '{"regionType":"地下室","example":"停车场/设备房"}' AS `remark`
UNION ALL
SELECT 3 AS `sort`, '地下3层' AS `label`, 'B03' AS `value`, 'ibms_region' AS `dict_type`, '' AS `color_type`, '' AS `css_class`, '{"regionType":"地下室","example":"停车场/设备房"}' AS `remark`
UNION ALL
SELECT 4 AS `sort`, '地上1层' AS `label`, 'F01' AS `value`, 'ibms_region' AS `dict_type`, '' AS `color_type`, '' AS `css_class`, '{"regionType":"楼层","example":"大堂/商铺"}' AS `remark`
UNION ALL
SELECT 5 AS `sort`, '地上2层' AS `label`, 'F02' AS `value`, 'ibms_region' AS `dict_type`, '' AS `color_type`, '' AS `css_class`, '{"regionType":"楼层","example":"办公室/会议室"}' AS `remark`
UNION ALL
SELECT 6 AS `sort`, '地上3层' AS `label`, 'F03' AS `value`, 'ibms_region' AS `dict_type`, '' AS `color_type`, '' AS `css_class`, '{"regionType":"楼层","example":"办公室/会议室"}' AS `remark`
UNION ALL
SELECT 7 AS `sort`, '地上4层' AS `label`, 'F04' AS `value`, 'ibms_region' AS `dict_type`, '' AS `color_type`, '' AS `css_class`, '{"regionType":"楼层","example":"办公室/会议室"}' AS `remark`
UNION ALL
SELECT 8 AS `sort`, '地上5层' AS `label`, 'F05' AS `value`, 'ibms_region' AS `dict_type`, '' AS `color_type`, '' AS `css_class`, '{"regionType":"楼层","example":"办公室/会议室"}' AS `remark`
UNION ALL
SELECT 9 AS `sort`, '6-10层' AS `label`, 'F06' AS `value`, 'ibms_region' AS `dict_type`, '' AS `color_type`, '' AS `css_class`, '{"regionType":"楼层","example":"标准层"}' AS `remark`
UNION ALL
SELECT 10 AS `sort`, '11-20层' AS `label`, 'F11' AS `value`, 'ibms_region' AS `dict_type`, '' AS `color_type`, '' AS `css_class`, '{"regionType":"楼层","example":"标准层"}' AS `remark`
UNION ALL
SELECT 11 AS `sort`, '21-30层' AS `label`, 'F21' AS `value`, 'ibms_region' AS `dict_type`, '' AS `color_type`, '' AS `css_class`, '{"regionType":"楼层","example":"标准层"}' AS `remark`
UNION ALL
SELECT 12 AS `sort`, '31-40层' AS `label`, 'F31' AS `value`, 'ibms_region' AS `dict_type`, '' AS `color_type`, '' AS `css_class`, '{"regionType":"楼层","example":"标准层"}' AS `remark`
UNION ALL
SELECT 13 AS `sort`, '停车场' AS `label`, 'PK' AS `value`, 'ibms_region' AS `dict_type`, '' AS `color_type`, '' AS `css_class`, '{"regionType":"车库","example":"车库区域"}' AS `remark`
UNION ALL
SELECT 14 AS `sort`, '大堂' AS `label`, 'LB' AS `value`, 'ibms_region' AS `dict_type`, '' AS `color_type`, '' AS `css_class`, '{"regionType":"公共区域","example":"建筑大堂"}' AS `remark`
UNION ALL
SELECT 15 AS `sort`, '机房' AS `label`, 'FM' AS `value`, 'ibms_region' AS `dict_type`, '' AS `color_type`, '' AS `css_class`, '{"regionType":"设备房","example":"设备机房"}' AS `remark`
UNION ALL
SELECT 16 AS `sort`, '屋面' AS `label`, 'RO' AS `value`, 'ibms_region' AS `dict_type`, '' AS `color_type`, '' AS `css_class`, '{"regionType":"屋面","example":"屋顶区域"}' AS `remark`
UNION ALL
SELECT 17 AS `sort`, '室外' AS `label`, 'OUT' AS `value`, 'ibms_region' AS `dict_type`, '' AS `color_type`, '' AS `css_class`, '{"regionType":"户外","example":"园区/周界"}' AS `remark`
UNION ALL
SELECT 18 AS `sort`, '附属楼' AS `label`, 'AT' AS `value`, 'ibms_region' AS `dict_type`, '' AS `color_type`, '' AS `css_class`, '{"regionType":"附属建筑","example":"辅助建筑"}' AS `remark`
UNION ALL
SELECT 1 AS `sort`, '海康威视' AS `label`, 'HIK' AS `value`, 'ibms_brand' AS `dict_type`, '' AS `color_type`, '' AS `css_class`, '{"fullName":"Hikvision","defaultSdkTcpPort":8000,"desc":"视频监控常用，与设备台账品牌码 HIK 一致"}' AS `remark`
UNION ALL
SELECT 2 AS `sort`, '大华' AS `label`, 'DAH' AS `value`, 'ibms_brand' AS `dict_type`, '' AS `color_type`, '' AS `css_class`, '{"fullName":"Dahua","defaultSdkTcpPort":37777,"desc":"与设备台账品牌码 DAH 一致"}' AS `remark`
UNION ALL
SELECT 3 AS `sort`, '中控熵基' AS `label`, 'ZKT' AS `value`, 'ibms_brand' AS `dict_type`, '' AS `color_type`, '' AS `css_class`, '{"fullName":"ZKTeco","desc":"门禁等，与设备台账品牌码 ZKT 一致"}' AS `remark`
UNION ALL
SELECT 4 AS `sort`, '华为' AS `label`, 'HUA' AS `value`, 'ibms_brand' AS `dict_type`, '' AS `color_type`, '' AS `css_class`, '{"fullName":"Huawei","desc":"与设备台账品牌码 HUA 一致"}' AS `remark`
UNION ALL
SELECT 5 AS `sort`, '江森自控' AS `label`, 'JOH' AS `value`, 'ibms_brand' AS `dict_type`, '' AS `color_type`, '' AS `css_class`, '{"fullName":"Johnson Controls","desc":"楼控等，与设备台账品牌码 JOH 一致"}' AS `remark`
UNION ALL
SELECT 10 AS `sort`, '西门子' AS `label`, 'SIEM' AS `value`, 'ibms_brand' AS `dict_type`, '' AS `color_type`, '' AS `css_class`, '{"fullName":"Siemens","desc":"楼控/消防等产品常用"}' AS `remark`
UNION ALL
SELECT 11 AS `sort`, '霍尼韦尔' AS `label`, 'HON' AS `value`, 'ibms_brand' AS `dict_type`, '' AS `color_type`, '' AS `css_class`, '{"fullName":"Honeywell","desc":"DDC/楼控等"}' AS `remark`
UNION ALL
SELECT 12 AS `sort`, '威胜' AS `label`, 'WASI' AS `value`, 'ibms_brand' AS `dict_type`, '' AS `color_type`, '' AS `css_class`, '{"fullName":"Wasion","desc":"电表等"}' AS `remark`
UNION ALL
SELECT 13 AS `sort`, 'ITC' AS `label`, 'ITC' AS `value`, 'ibms_brand' AS `dict_type`, '' AS `color_type`, '' AS `css_class`, '{"fullName":"ITC","desc":"广播音响等"}' AS `remark`
UNION ALL
SELECT 99 AS `sort`, '其他' AS `label`, 'OTH' AS `value`, 'ibms_brand' AS `dict_type`, '' AS `color_type`, '' AS `css_class`, '{"desc":"未列入码表的厂商，产品型号/描述中补充说明"}' AS `remark`
) v
LEFT JOIN `system_dict_data` d ON d.`dict_type` = v.`dict_type` AND d.`value` = v.`value` AND d.`deleted` = b'0'
WHERE d.`id` IS NULL;

