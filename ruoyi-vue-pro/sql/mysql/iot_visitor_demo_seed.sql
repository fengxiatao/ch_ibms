-- =====================================================
-- 访客管理可信演示数据（意向用户演示用）
-- 适用表：iot_visitor_appointment / iot_visitor_abnormal_event
-- 特点：幂等、可重复执行、覆盖多种业务状态
-- =====================================================

SET NAMES utf8mb4;

-- 1) 预约单演示数据（20条）
-- 说明：
-- - 使用固定 ID（900001-900020），避免污染已有真实数据
-- - 执行多次会更新同 ID 记录，不会重复插入
INSERT INTO `iot_visitor_appointment`
(`id`,`name`,`phone`,`type`,`company`,`host`,`host_dept`,`visit_time`,`reason`,`areas`,`id_card`,`car_no`,`remark`,`status`,
 `approval_comment`,`approval_time`,`approver_id`,`sign_in_time`,`sign_out_time`,`current_location`,`rating`,
 `tenant_id`,`creator`,`create_time`,`updater`,`update_time`,`deleted`)
VALUES
(900001,'张晨曦','13810230001','business','华川智能科技有限公司','王海涛','智慧运维中心',DATE_ADD(CURDATE(), INTERVAL 9 HOUR),'智慧园区平台对接评估',JSON_ARRAY('lobby','meeting'),'110101199001015621','沪A8M2K1','首次拜访，需讲解PaaS能力','approved',
 '资料齐全，同意来访',DATE_ADD(CURDATE(), INTERVAL 8 HOUR),1,DATE_ADD(CURDATE(), INTERVAL 9 HOUR),NULL,'A座一层接待区',NULL,
 1,'1',NOW(),'1',NOW(),b'0'),
(900002,'李雨桐','13810230002','vip','江城城建投资集团','刘志远','总经办',DATE_ADD(CURDATE(), INTERVAL 14 HOUR),'项目合作框架签约',JSON_ARRAY('meeting','exhibition'),NULL,'浙B91H23','高层接待，准备会议室与茶歇','approved',
 'VIP接待流程通过',DATE_ADD(CURDATE(), INTERVAL 11 HOUR),1,DATE_ADD(CURDATE(), INTERVAL 13 HOUR),DATE_ADD(CURDATE(), INTERVAL 16 HOUR),'A座18层会议中心',4.80,
 1,'1',NOW(),'1',NOW(),b'0'),
(900003,'赵文斌','13810230003','contractor','安信机电工程有限公司','周建国','设施保障部',DATE_ADD(CURDATE(), INTERVAL 10 HOUR),'空调末端巡检与维保',JSON_ARRAY('plant-room','service-corridor'),NULL,NULL,'需佩戴临时施工证','approved',
 '施工类来访，已通知安保',DATE_ADD(CURDATE(), INTERVAL 8 HOUR),1,DATE_ADD(CURDATE(), INTERVAL 10 HOUR),NULL,'B1机电设备层',NULL,
 1,'1',NOW(),'1',NOW(),b'0'),
(900004,'陈思远','13810230004','interview','个人','孙宁','人力资源部',DATE_ADD(CURDATE(), INTERVAL 15 HOUR),'运维工程师终面',JSON_ARRAY('lobby','hr-office'),'320102199604123517',NULL,'携带作品集纸质版','pending',
 NULL,NULL,NULL,NULL,NULL,NULL,NULL,
 1,'1',NOW(),'1',NOW(),b'0'),
(900005,'黄嘉怡','13810230005','business','深澜信息技术（深圳）有限公司','许明','数字化中心',DATE_ADD(CURDATE(), INTERVAL 1 DAY),'数据中台联合方案交流',JSON_ARRAY('meeting'),NULL,NULL,NULL,'pending',
 NULL,NULL,NULL,NULL,NULL,NULL,NULL,
 1,'1',NOW(),'1',NOW(),b'0'),
(900006,'吴俊凯','13810230006','business','上海观澜物联科技','唐青','安防事业部',DATE_ADD(CURDATE(), INTERVAL -1 DAY) + INTERVAL 10 HOUR,'AI视频联动能力演示',JSON_ARRAY('meeting','control-room'),NULL,'苏E2P8F6','已完成演示','approved',
 '可安排现场演示',DATE_ADD(CURDATE(), INTERVAL -1 DAY) + INTERVAL 8 HOUR,1,DATE_ADD(CURDATE(), INTERVAL -1 DAY) + INTERVAL 10 HOUR,DATE_ADD(CURDATE(), INTERVAL -1 DAY) + INTERVAL 12 HOUR,'监控中心',4.60,
 1,'1',NOW(),'1',NOW(),b'0'),
(900007,'周雅雯','13810230007','business','中安智城研究院','高峰','战略研究部',DATE_ADD(CURDATE(), INTERVAL -2 DAY) + INTERVAL 14 HOUR,'课题合作交流',JSON_ARRAY('meeting'),NULL,NULL,NULL,'rejected',
 '本周领导外出，建议下周重新预约',DATE_ADD(CURDATE(), INTERVAL -2 DAY) + INTERVAL 11 HOUR,1,NULL,NULL,NULL,NULL,
 1,'1',NOW(),'1',NOW(),b'0'),
(900008,'马启航','13810230008','contractor','宏远弱电工程有限公司','钱浩','工程管理部',DATE_ADD(CURDATE(), INTERVAL -3 DAY) + INTERVAL 9 HOUR,'门禁读卡器检修',JSON_ARRAY('service-corridor','access-room'),NULL,NULL,'涉及夜间施工审批','approved',
 '限时作业，注意安全',DATE_ADD(CURDATE(), INTERVAL -3 DAY) + INTERVAL 8 HOUR,1,DATE_ADD(CURDATE(), INTERVAL -3 DAY) + INTERVAL 9 HOUR,DATE_ADD(CURDATE(), INTERVAL -3 DAY) + INTERVAL 18 HOUR,'A座弱电间',4.20,
 1,'1',NOW(),'1',NOW(),b'0'),
(900009,'郑宇轩','13810230009','interview','个人','段可','产品部',DATE_ADD(CURDATE(), INTERVAL -1 DAY) + INTERVAL 14 HOUR,'产品经理二面',JSON_ARRAY('lobby','meeting'),'420106199805063014',NULL,NULL,'cancelled',
 '候选人临时取消',DATE_ADD(CURDATE(), INTERVAL -1 DAY) + INTERVAL 12 HOUR,1,NULL,NULL,NULL,NULL,
 1,'1',NOW(),'1',NOW(),b'0'),
(900010,'唐若曦','13810230010','vip','天辰资本管理有限公司','王海涛','智慧运维中心',DATE_ADD(CURDATE(), INTERVAL 2 DAY) + INTERVAL 10 HOUR,'投资尽调与产品路线评审',JSON_ARRAY('meeting','exhibition'),NULL,'京N7R3Q5','需安排产品总监参与','approved',
 '已协调高层时间',DATE_ADD(CURDATE(), INTERVAL 1 DAY) + INTERVAL 9 HOUR,1,NULL,NULL,NULL,NULL,
 1,'1',NOW(),'1',NOW(),b'0'),
(900011,'姜赫','13810230011','business','星纬云网科技有限公司','刘志远','总经办',DATE_ADD(CURDATE(), INTERVAL 3 DAY) + INTERVAL 11 HOUR,'智慧楼宇联合投标沟通',JSON_ARRAY('meeting'),NULL,NULL,NULL,'pending',
 NULL,NULL,NULL,NULL,NULL,NULL,NULL,
 1,'1',NOW(),'1',NOW(),b'0'),
(900012,'宋佳欣','13810230012','business','鹏程交通设计院','许明','数字化中心',DATE_ADD(CURDATE(), INTERVAL -5 DAY) + INTERVAL 15 HOUR,'停车场云边协同方案交流',JSON_ARRAY('meeting','parking-control'),NULL,'粤B3K5D2',NULL,'approved',
 '同意预约',DATE_ADD(CURDATE(), INTERVAL -5 DAY) + INTERVAL 13 HOUR,1,DATE_ADD(CURDATE(), INTERVAL -5 DAY) + INTERVAL 15 HOUR,DATE_ADD(CURDATE(), INTERVAL -5 DAY) + INTERVAL 17 HOUR,'停车场中控室',4.50,
 1,'1',NOW(),'1',NOW(),b'0'),
(900013,'任博文','13810230013','contractor','金盾安防服务有限公司','唐青','安防事业部',DATE_ADD(CURDATE(), INTERVAL -4 DAY) + INTERVAL 8 HOUR,'消防联动柜巡检',JSON_ARRAY('plant-room','service-corridor'),NULL,NULL,NULL,'approved',
 '按流程执行',DATE_ADD(CURDATE(), INTERVAL -4 DAY) + INTERVAL 7 HOUR,1,DATE_ADD(CURDATE(), INTERVAL -4 DAY) + INTERVAL 8 HOUR,DATE_ADD(CURDATE(), INTERVAL -4 DAY) + INTERVAL 12 HOUR,'消防控制室',4.10,
 1,'1',NOW(),'1',NOW(),b'0'),
(900014,'于晓曼','13810230014','interview','个人','孙宁','人力资源部',DATE_ADD(CURDATE(), INTERVAL 1 DAY) + INTERVAL 14 HOUR,'前端工程师复试',JSON_ARRAY('lobby','hr-office'),'510105199707283224',NULL,NULL,'approved',
 '复试可安排',DATE_ADD(CURDATE(), INTERVAL 1 DAY) + INTERVAL 10 HOUR,1,NULL,NULL,NULL,NULL,
 1,'1',NOW(),'1',NOW(),b'0'),
(900015,'罗志明','13810230015','business','北斗测绘科技股份有限公司','高峰','战略研究部',DATE_ADD(CURDATE(), INTERVAL -6 DAY) + INTERVAL 10 HOUR,'空间数字孪生合作探讨',JSON_ARRAY('meeting','command-center'),NULL,NULL,NULL,'approved',
 '已通过',DATE_ADD(CURDATE(), INTERVAL -6 DAY) + INTERVAL 9 HOUR,1,DATE_ADD(CURDATE(), INTERVAL -6 DAY) + INTERVAL 10 HOUR,DATE_ADD(CURDATE(), INTERVAL -6 DAY) + INTERVAL 11 HOUR,'智慧指挥中心',4.70,
 1,'1',NOW(),'1',NOW(),b'0'),
(900016,'魏子昂','13810230016','business','青禾能源科技有限公司','周建国','设施保障部',DATE_ADD(CURDATE(), INTERVAL 4 DAY) + INTERVAL 9 HOUR,'能耗优化方案评估',JSON_ARRAY('meeting','plant-room'),NULL,NULL,NULL,'pending',
 NULL,NULL,NULL,NULL,NULL,NULL,NULL,
 1,'1',NOW(),'1',NOW(),b'0'),
(900017,'叶清荷','13810230017','vip','华南产业基金','王海涛','智慧运维中心',DATE_ADD(CURDATE(), INTERVAL -7 DAY) + INTERVAL 15 HOUR,'项目路演回访',JSON_ARRAY('meeting','exhibition'),NULL,NULL,'已签保密协议','approved',
 '同意',DATE_ADD(CURDATE(), INTERVAL -7 DAY) + INTERVAL 12 HOUR,1,DATE_ADD(CURDATE(), INTERVAL -7 DAY) + INTERVAL 15 HOUR,DATE_ADD(CURDATE(), INTERVAL -7 DAY) + INTERVAL 16 HOUR,'A座展厅',4.90,
 1,'1',NOW(),'1',NOW(),b'0'),
(900018,'郭睿','13810230018','contractor','迅达网络维护中心','钱浩','工程管理部',DATE_ADD(CURDATE(), INTERVAL -2 DAY) + INTERVAL 22 HOUR,'夜间网络巡检',JSON_ARRAY('service-corridor','idc-room'),NULL,NULL,'夜间作业白名单','approved',
 '夜间作业已备案',DATE_ADD(CURDATE(), INTERVAL -2 DAY) + INTERVAL 20 HOUR,1,DATE_ADD(CURDATE(), INTERVAL -2 DAY) + INTERVAL 22 HOUR,NULL,'IDC机房外围',NULL,
 1,'1',NOW(),'1',NOW(),b'0'),
(900019,'方芸','13810230019','business','城智运营管理有限公司','刘志远','总经办',DATE_ADD(CURDATE(), INTERVAL 5 DAY) + INTERVAL 10 HOUR,'运营托管服务交流',JSON_ARRAY('meeting'),NULL,NULL,NULL,'pending',
 NULL,NULL,NULL,NULL,NULL,NULL,NULL,
 1,'1',NOW(),'1',NOW(),b'0'),
(900020,'韩敬尧','13810230020','interview','个人','段可','产品部',DATE_ADD(CURDATE(), INTERVAL 2 DAY) + INTERVAL 15 HOUR,'产品运营岗面试',JSON_ARRAY('lobby','meeting'),'330106199909104215',NULL,NULL,'pending',
 NULL,NULL,NULL,NULL,NULL,NULL,NULL,
 1,'1',NOW(),'1',NOW(),b'0')
ON DUPLICATE KEY UPDATE
  `name`=VALUES(`name`),
  `phone`=VALUES(`phone`),
  `type`=VALUES(`type`),
  `company`=VALUES(`company`),
  `host`=VALUES(`host`),
  `host_dept`=VALUES(`host_dept`),
  `visit_time`=VALUES(`visit_time`),
  `reason`=VALUES(`reason`),
  `areas`=VALUES(`areas`),
  `status`=VALUES(`status`),
  `approval_comment`=VALUES(`approval_comment`),
  `approval_time`=VALUES(`approval_time`),
  `approver_id`=VALUES(`approver_id`),
  `sign_in_time`=VALUES(`sign_in_time`),
  `sign_out_time`=VALUES(`sign_out_time`),
  `current_location`=VALUES(`current_location`),
  `rating`=VALUES(`rating`),
  `update_time`=VALUES(`update_time`),
  `deleted`=VALUES(`deleted`);

-- 2) 异常事件演示数据（6条）
INSERT INTO `iot_visitor_abnormal_event`
(`id`,`appointment_id`,`visitor_name`,`visitor_phone`,`abnormal_type`,`risk_level`,`details`,`event_time`,`current_status`,`handled`,
 `handler_id`,`handle_time`,`handle_result`,`tenant_id`,`creator`,`create_time`,`updater`,`update_time`,`deleted`)
VALUES
(910001,900018,'郭睿','13810230018','overtime','medium','夜间巡检超出预约离场时间 45 分钟',DATE_ADD(CURDATE(), INTERVAL -1 DAY) + INTERVAL 1 HOUR,'仍在IDC外围',b'1',
 1,DATE_ADD(CURDATE(), INTERVAL -1 DAY) + INTERVAL 2 HOUR,'已电话确认并完成离场登记',1,'1',NOW(),'1',NOW(),b'0'),
(910002,900004,'陈思远','13810230004','noshow','low','面试预约已过时段，未签到',DATE_ADD(CURDATE(), INTERVAL -1 HOUR),'未到访',b'0',
 NULL,NULL,NULL,1,'1',NOW(),'1',NOW(),b'0'),
(910003,NULL,'陌生人员','13800000000','unauthorized','high','尝试闯入B1设备层，被门禁拦截',DATE_ADD(CURDATE(), INTERVAL -2 DAY) + INTERVAL 20 HOUR,'门禁拦截点B1-03',b'1',
 1,DATE_ADD(CURDATE(), INTERVAL -2 DAY) + INTERVAL 21 HOUR,'移交安保核查，无进一步风险',1,'1',NOW(),'1',NOW(),b'0'),
(910004,900003,'赵文斌','13810230003','unauthorized','medium','施工人员误入办公区',DATE_ADD(CURDATE(), INTERVAL 11 HOUR),'A座7层办公区',b'0',
 NULL,NULL,NULL,1,'1',NOW(),'1',NOW(),b'0'),
(910005,900010,'唐若曦','13810230010','noshow','medium','VIP来访超时未到，待秘书确认',DATE_ADD(CURDATE(), INTERVAL 2 DAY) + INTERVAL 12 HOUR,'待确认',b'0',
 NULL,NULL,NULL,1,'1',NOW(),'1',NOW(),b'0'),
(910006,900008,'马启航','13810230008','overtime','low','施工离场登记延迟',DATE_ADD(CURDATE(), INTERVAL -3 DAY) + INTERVAL 19 HOUR,'已离场待补录',b'1',
 1,DATE_ADD(CURDATE(), INTERVAL -3 DAY) + INTERVAL 20 HOUR,'已补录签离信息',1,'1',NOW(),'1',NOW(),b'0')
ON DUPLICATE KEY UPDATE
  `appointment_id`=VALUES(`appointment_id`),
  `visitor_name`=VALUES(`visitor_name`),
  `visitor_phone`=VALUES(`visitor_phone`),
  `abnormal_type`=VALUES(`abnormal_type`),
  `risk_level`=VALUES(`risk_level`),
  `details`=VALUES(`details`),
  `event_time`=VALUES(`event_time`),
  `current_status`=VALUES(`current_status`),
  `handled`=VALUES(`handled`),
  `handler_id`=VALUES(`handler_id`),
  `handle_time`=VALUES(`handle_time`),
  `handle_result`=VALUES(`handle_result`),
  `update_time`=VALUES(`update_time`),
  `deleted`=VALUES(`deleted`);

-- 3) 可选核验 SQL（执行后手工查看）
-- SELECT status, COUNT(*) AS cnt FROM iot_visitor_appointment WHERE id BETWEEN 900001 AND 900020 GROUP BY status ORDER BY cnt DESC;
-- SELECT abnormal_type, risk_level, COUNT(*) AS cnt FROM iot_visitor_abnormal_event WHERE id BETWEEN 910001 AND 910006 GROUP BY abnormal_type, risk_level;

-- =====================================================
-- 4) 统计友好演示集（建议用于对外演示）
-- 口径对齐 VisitorOverviewServiceImpl：
-- - 当前在园人数：今天 visit_time + 已签到未签离
-- - 今日访客总数：今天 visit_time
-- - 今日已处理：今天 approval_time 且 status in (approved,rejected)
-- - 近7天趋势：按 sign_in_time 逐日统计
-- =====================================================

-- 先清理本段演示 ID，避免历史残留导致统计偏差
DELETE FROM `iot_visitor_abnormal_event` WHERE id BETWEEN 930100 AND 930199;
DELETE FROM `iot_visitor_appointment` WHERE id BETWEEN 920100 AND 920199;

INSERT INTO `iot_visitor_appointment`
(`id`,`name`,`phone`,`type`,`company`,`host`,`host_dept`,`visit_time`,`reason`,`areas`,`id_card`,`car_no`,`remark`,`status`,
 `approval_comment`,`approval_time`,`approver_id`,`sign_in_time`,`sign_out_time`,`current_location`,`rating`,
 `tenant_id`,`creator`,`create_time`,`updater`,`update_time`,`deleted`)
VALUES
-- D-6
(920101,'演示访客01','13812000101','business','城智科技','王海涛','智慧运维中心',DATE_ADD(CURDATE(),INTERVAL -6 DAY)+INTERVAL 9 HOUR,'商务洽谈',JSON_ARRAY('meeting'),NULL,NULL,NULL,'approved','通过',DATE_ADD(CURDATE(),INTERVAL -6 DAY)+INTERVAL 8 HOUR,1,DATE_ADD(CURDATE(),INTERVAL -6 DAY)+INTERVAL 9 HOUR,DATE_ADD(CURDATE(),INTERVAL -6 DAY)+INTERVAL 10 HOUR,'A座会议室',4.6,1,'1',NOW(),'1',NOW(),b'0'),
(920102,'演示访客02','13812000102','contractor','安保维保','周建国','设施保障部',DATE_ADD(CURDATE(),INTERVAL -6 DAY)+INTERVAL 14 HOUR,'设备维保',JSON_ARRAY('plant-room'),NULL,NULL,NULL,'approved','通过',DATE_ADD(CURDATE(),INTERVAL -6 DAY)+INTERVAL 13 HOUR,1,DATE_ADD(CURDATE(),INTERVAL -6 DAY)+INTERVAL 14 HOUR,DATE_ADD(CURDATE(),INTERVAL -6 DAY)+INTERVAL 16 HOUR,'B1设备层',4.3,1,'1',NOW(),'1',NOW(),b'0'),
-- D-5
(920103,'演示访客03','13812000103','business','启明咨询','刘志远','总经办',DATE_ADD(CURDATE(),INTERVAL -5 DAY)+INTERVAL 10 HOUR,'项目交流',JSON_ARRAY('meeting'),NULL,NULL,NULL,'approved','通过',DATE_ADD(CURDATE(),INTERVAL -5 DAY)+INTERVAL 9 HOUR,1,DATE_ADD(CURDATE(),INTERVAL -5 DAY)+INTERVAL 10 HOUR,DATE_ADD(CURDATE(),INTERVAL -5 DAY)+INTERVAL 11 HOUR,'会议室2',4.8,1,'1',NOW(),'1',NOW(),b'0'),
(920104,'演示访客04','13812000104','interview','个人','孙宁','人力资源部',DATE_ADD(CURDATE(),INTERVAL -5 DAY)+INTERVAL 11 HOUR,'面试',JSON_ARRAY('hr-office'),'320101199610101234',NULL,NULL,'approved','通过',DATE_ADD(CURDATE(),INTERVAL -5 DAY)+INTERVAL 10 HOUR,1,DATE_ADD(CURDATE(),INTERVAL -5 DAY)+INTERVAL 11 HOUR,DATE_ADD(CURDATE(),INTERVAL -5 DAY)+INTERVAL 12 HOUR,'HR面试间',NULL,1,'1',NOW(),'1',NOW(),b'0'),
(920105,'演示访客05','13812000105','business','华测集团','高峰','战略研究部',DATE_ADD(CURDATE(),INTERVAL -5 DAY)+INTERVAL 15 HOUR,'商务洽谈',JSON_ARRAY('meeting'),NULL,NULL,NULL,'rejected','资料不全',DATE_ADD(CURDATE(),INTERVAL -5 DAY)+INTERVAL 14 HOUR,1,NULL,NULL,NULL,NULL,1,'1',NOW(),'1',NOW(),b'0'),
-- D-4
(920106,'演示访客06','13812000106','business','星链物联','王海涛','智慧运维中心',DATE_ADD(CURDATE(),INTERVAL -4 DAY)+INTERVAL 9 HOUR,'商务洽谈',JSON_ARRAY('meeting'),NULL,NULL,NULL,'approved','通过',DATE_ADD(CURDATE(),INTERVAL -4 DAY)+INTERVAL 8 HOUR,1,DATE_ADD(CURDATE(),INTERVAL -4 DAY)+INTERVAL 9 HOUR,DATE_ADD(CURDATE(),INTERVAL -4 DAY)+INTERVAL 10 HOUR,'A座会议室',4.7,1,'1',NOW(),'1',NOW(),b'0'),
(920107,'演示访客07','13812000107','contractor','机电维保中心','周建国','设施保障部',DATE_ADD(CURDATE(),INTERVAL -4 DAY)+INTERVAL 13 HOUR,'设备维保',JSON_ARRAY('plant-room'),NULL,NULL,NULL,'approved','通过',DATE_ADD(CURDATE(),INTERVAL -4 DAY)+INTERVAL 12 HOUR,1,DATE_ADD(CURDATE(),INTERVAL -4 DAY)+INTERVAL 13 HOUR,DATE_ADD(CURDATE(),INTERVAL -4 DAY)+INTERVAL 17 HOUR,'机电层',4.1,1,'1',NOW(),'1',NOW(),b'0'),
-- D-3
(920108,'演示访客08','13812000108','business','城投智运','刘志远','总经办',DATE_ADD(CURDATE(),INTERVAL -3 DAY)+INTERVAL 9 HOUR,'项目交流',JSON_ARRAY('meeting'),NULL,NULL,NULL,'approved','通过',DATE_ADD(CURDATE(),INTERVAL -3 DAY)+INTERVAL 8 HOUR,1,DATE_ADD(CURDATE(),INTERVAL -3 DAY)+INTERVAL 9 HOUR,DATE_ADD(CURDATE(),INTERVAL -3 DAY)+INTERVAL 10 HOUR,'会议室1',4.5,1,'1',NOW(),'1',NOW(),b'0'),
(920109,'演示访客09','13812000109','business','云控科技','许明','数字化中心',DATE_ADD(CURDATE(),INTERVAL -3 DAY)+INTERVAL 10 HOUR,'商务洽谈',JSON_ARRAY('meeting'),NULL,NULL,NULL,'approved','通过',DATE_ADD(CURDATE(),INTERVAL -3 DAY)+INTERVAL 9 HOUR,1,DATE_ADD(CURDATE(),INTERVAL -3 DAY)+INTERVAL 10 HOUR,DATE_ADD(CURDATE(),INTERVAL -3 DAY)+INTERVAL 11 HOUR,'数字中心',4.4,1,'1',NOW(),'1',NOW(),b'0'),
(920110,'演示访客10','13812000110','interview','个人','孙宁','人力资源部',DATE_ADD(CURDATE(),INTERVAL -3 DAY)+INTERVAL 14 HOUR,'面试',JSON_ARRAY('hr-office'),'410101199905051111',NULL,NULL,'approved','通过',DATE_ADD(CURDATE(),INTERVAL -3 DAY)+INTERVAL 13 HOUR,1,DATE_ADD(CURDATE(),INTERVAL -3 DAY)+INTERVAL 14 HOUR,DATE_ADD(CURDATE(),INTERVAL -3 DAY)+INTERVAL 15 HOUR,'HR面试间',NULL,1,'1',NOW(),'1',NOW(),b'0'),
(920111,'演示访客11','13812000111','contractor','联诚机电','周建国','设施保障部',DATE_ADD(CURDATE(),INTERVAL -3 DAY)+INTERVAL 16 HOUR,'设备维保',JSON_ARRAY('plant-room'),NULL,NULL,NULL,'approved','通过',DATE_ADD(CURDATE(),INTERVAL -3 DAY)+INTERVAL 15 HOUR,1,DATE_ADD(CURDATE(),INTERVAL -3 DAY)+INTERVAL 16 HOUR,DATE_ADD(CURDATE(),INTERVAL -3 DAY)+INTERVAL 18 HOUR,'B1设备层',4.2,1,'1',NOW(),'1',NOW(),b'0'),
-- D-2
(920112,'演示访客12','13812000112','business','天行软件','王海涛','智慧运维中心',DATE_ADD(CURDATE(),INTERVAL -2 DAY)+INTERVAL 10 HOUR,'商务洽谈',JSON_ARRAY('meeting'),NULL,NULL,NULL,'approved','通过',DATE_ADD(CURDATE(),INTERVAL -2 DAY)+INTERVAL 9 HOUR,1,DATE_ADD(CURDATE(),INTERVAL -2 DAY)+INTERVAL 10 HOUR,DATE_ADD(CURDATE(),INTERVAL -2 DAY)+INTERVAL 11 HOUR,'A座会议室',4.6,1,'1',NOW(),'1',NOW(),b'0'),
(920113,'演示访客13','13812000113','business','宏景资本','刘志远','总经办',DATE_ADD(CURDATE(),INTERVAL -2 DAY)+INTERVAL 15 HOUR,'项目交流',JSON_ARRAY('meeting'),NULL,NULL,NULL,'approved','通过',DATE_ADD(CURDATE(),INTERVAL -2 DAY)+INTERVAL 14 HOUR,1,DATE_ADD(CURDATE(),INTERVAL -2 DAY)+INTERVAL 15 HOUR,DATE_ADD(CURDATE(),INTERVAL -2 DAY)+INTERVAL 16 HOUR,'会议室2',4.8,1,'1',NOW(),'1',NOW(),b'0'),
-- D-1
(920114,'演示访客14','13812000114','contractor','捷维工程','周建国','设施保障部',DATE_ADD(CURDATE(),INTERVAL -1 DAY)+INTERVAL 10 HOUR,'设备维保',JSON_ARRAY('plant-room'),NULL,NULL,NULL,'approved','通过',DATE_ADD(CURDATE(),INTERVAL -1 DAY)+INTERVAL 9 HOUR,1,DATE_ADD(CURDATE(),INTERVAL -1 DAY)+INTERVAL 10 HOUR,DATE_ADD(CURDATE(),INTERVAL -1 DAY)+INTERVAL 12 HOUR,'机房层',4.2,1,'1',NOW(),'1',NOW(),b'0'),
-- D0（今天，确保统计卡片好看且可解释）
(920115,'演示访客15','13812000115','business','睿达信息','王海涛','智慧运维中心',DATE_ADD(CURDATE(),INTERVAL 9 HOUR),'商务洽谈',JSON_ARRAY('meeting'),NULL,NULL,'今日在访样本','approved','通过',DATE_ADD(CURDATE(),INTERVAL 8 HOUR),1,DATE_ADD(CURDATE(),INTERVAL 9 HOUR),NULL,'A座会议室',NULL,1,'1',NOW(),'1',NOW(),b'0'),
(920116,'演示访客16','13812000116','business','禾木科技','刘志远','总经办',DATE_ADD(CURDATE(),INTERVAL 10 HOUR),'项目交流',JSON_ARRAY('meeting'),NULL,NULL,NULL,'approved','通过',DATE_ADD(CURDATE(),INTERVAL 8 HOUR),1,DATE_ADD(CURDATE(),INTERVAL 10 HOUR),DATE_ADD(CURDATE(),INTERVAL 12 HOUR),'会议室3',4.7,1,'1',NOW(),'1',NOW(),b'0'),
(920117,'演示访客17','13812000117','interview','个人','孙宁','人力资源部',DATE_ADD(CURDATE(),INTERVAL 14 HOUR),'面试',JSON_ARRAY('hr-office'),'110101199911111234',NULL,NULL,'pending',NULL,NULL,NULL,NULL,NULL,NULL,NULL,1,'1',NOW(),'1',NOW(),b'0')
ON DUPLICATE KEY UPDATE
  `name`=VALUES(`name`),`phone`=VALUES(`phone`),`type`=VALUES(`type`),`company`=VALUES(`company`),`host`=VALUES(`host`),`host_dept`=VALUES(`host_dept`),
  `visit_time`=VALUES(`visit_time`),`reason`=VALUES(`reason`),`areas`=VALUES(`areas`),`status`=VALUES(`status`),
  `approval_comment`=VALUES(`approval_comment`),`approval_time`=VALUES(`approval_time`),`approver_id`=VALUES(`approver_id`),
  `sign_in_time`=VALUES(`sign_in_time`),`sign_out_time`=VALUES(`sign_out_time`),`current_location`=VALUES(`current_location`),`rating`=VALUES(`rating`),
  `update_time`=VALUES(`update_time`),`deleted`=VALUES(`deleted`);

INSERT INTO `iot_visitor_abnormal_event`
(`id`,`appointment_id`,`visitor_name`,`visitor_phone`,`abnormal_type`,`risk_level`,`details`,`event_time`,`current_status`,`handled`,
 `handler_id`,`handle_time`,`handle_result`,`tenant_id`,`creator`,`create_time`,`updater`,`update_time`,`deleted`)
VALUES
(930101,920111,'演示访客11','13812000111','overtime','medium','维保超时 20 分钟',DATE_ADD(CURDATE(),INTERVAL -3 DAY)+INTERVAL 18 HOUR,'设备层',b'1',1,DATE_ADD(CURDATE(),INTERVAL -3 DAY)+INTERVAL 18 HOUR+INTERVAL 30 MINUTE,'已确认离场',1,'1',NOW(),'1',NOW(),b'0'),
(930102,NULL,'陌生人员A','13800000001','unauthorized','high','尝试进入B1限制区',DATE_ADD(CURDATE(),INTERVAL -2 DAY)+INTERVAL 20 HOUR,'门禁拦截',b'1',1,DATE_ADD(CURDATE(),INTERVAL -2 DAY)+INTERVAL 21 HOUR,'安保处置完成',1,'1',NOW(),'1',NOW(),b'0'),
(930103,920115,'演示访客15','13812000115','overtime','low','会议延时未及时签离',DATE_ADD(CURDATE(),INTERVAL 12 HOUR),'会议室',b'0',NULL,NULL,NULL,1,'1',NOW(),'1',NOW(),b'0'),
(930104,920117,'演示访客17','13812000117','noshow','low','面试预约已过未签到',DATE_ADD(CURDATE(),INTERVAL 17 HOUR),'未到访',b'0',NULL,NULL,NULL,1,'1',NOW(),'1',NOW(),b'0')
ON DUPLICATE KEY UPDATE
  `appointment_id`=VALUES(`appointment_id`),`visitor_name`=VALUES(`visitor_name`),`visitor_phone`=VALUES(`visitor_phone`),
  `abnormal_type`=VALUES(`abnormal_type`),`risk_level`=VALUES(`risk_level`),`details`=VALUES(`details`),`event_time`=VALUES(`event_time`),
  `current_status`=VALUES(`current_status`),`handled`=VALUES(`handled`),`handler_id`=VALUES(`handler_id`),`handle_time`=VALUES(`handle_time`),
  `handle_result`=VALUES(`handle_result`),`update_time`=VALUES(`update_time`),`deleted`=VALUES(`deleted`);

-- =====================================================
-- 5) 被访人自动对齐门禁人员管理（关键）
-- 说明：
-- - 从 iot_access_person(员工/正常/未删除) 读取真实人员
-- - 关联 iot_access_department 获取部门名称
-- - 覆盖演示预约中的 host / host_dept，确保与门禁人员管理页面一致
-- =====================================================

SET @host_cnt = (
  SELECT COUNT(1)
  FROM `iot_access_person` p
  WHERE p.`deleted` = b'0'
    AND p.`status` = 0
    AND p.`person_type` = 1
    AND p.`person_name` IS NOT NULL
    AND p.`person_name` <> ''
);

WITH host_pool AS (
  SELECT
    p.`id`,
    p.`person_name`,
    COALESCE(d.`dept_name`, '') AS dept_name,
    ROW_NUMBER() OVER (ORDER BY p.`id`) AS rn
  FROM `iot_access_person` p
  LEFT JOIN `iot_access_department` d ON d.`id` = p.`dept_id` AND d.`deleted` = b'0'
  WHERE p.`deleted` = b'0'
    AND p.`status` = 0
    AND p.`person_type` = 1
    AND p.`person_name` IS NOT NULL
    AND p.`person_name` <> ''
)
UPDATE `iot_visitor_appointment` a
JOIN host_pool h
  ON h.rn = 1 + MOD(a.`id` - 900001, @host_cnt)
SET a.`host` = h.`person_name`,
    a.`host_dept` = h.`dept_name`,
    a.`update_time` = NOW(),
    a.`updater` = '1'
WHERE @host_cnt > 0
  AND a.`id` BETWEEN 900001 AND 900020
  AND a.`deleted` = b'0';

WITH host_pool AS (
  SELECT
    p.`id`,
    p.`person_name`,
    COALESCE(d.`dept_name`, '') AS dept_name,
    ROW_NUMBER() OVER (ORDER BY p.`id`) AS rn
  FROM `iot_access_person` p
  LEFT JOIN `iot_access_department` d ON d.`id` = p.`dept_id` AND d.`deleted` = b'0'
  WHERE p.`deleted` = b'0'
    AND p.`status` = 0
    AND p.`person_type` = 1
    AND p.`person_name` IS NOT NULL
    AND p.`person_name` <> ''
)
UPDATE `iot_visitor_appointment` a
JOIN host_pool h
  ON h.rn = 1 + MOD(a.`id` - 920101, @host_cnt)
SET a.`host` = h.`person_name`,
    a.`host_dept` = h.`dept_name`,
    a.`update_time` = NOW(),
    a.`updater` = '1'
WHERE @host_cnt > 0
  AND a.`id` BETWEEN 920101 AND 920117
  AND a.`deleted` = b'0';

-- 可选核验：演示预约中的被访人是否都能在门禁人员中找到
-- SELECT a.id, a.host, a.host_dept
-- FROM iot_visitor_appointment a
-- LEFT JOIN iot_access_person p ON p.person_name = a.host AND p.deleted = b'0' AND p.status = 0
-- WHERE a.id BETWEEN 920101 AND 920117 AND p.id IS NULL;
