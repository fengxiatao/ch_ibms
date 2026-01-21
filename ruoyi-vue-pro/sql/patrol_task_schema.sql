-- =============================================
-- 巡检任务数据库设计
-- 日期: 2024-11-18
-- 说明: 扩展视频巡更任务表，支持格子内多通道轮播
-- =============================================

-- ========================================
-- 第一部分：表结构说明
-- ========================================

/*
现有表：iot_video_patrol_task
扩展字段：patrol_scenes (JSON)

JSON 数据结构：
{
  "patrolScenes": [
    {
      "sceneId": 1,
      "sceneName": "中山大堂",
      "sceneOrder": 1,
      "gridLayout": "2x2",
      "gridCount": 4,
      "cells": [
        {
          "cellIndex": 0,
          "channels": [
            {
              "channelId": 101,
              "channelCode": "CH_EAST_GATE",
              "channelName": "东大门通道",
              "duration": 10,
              "streamUrl": "rtsp://...",
              "deviceId": 1001,
              "deviceName": "东门摄像头"
            }
          ]
        }
      ]
    }
  ]
}
*/

-- ========================================
-- 第二部分：示例数据
-- ========================================

-- 插入示例巡检计划
INSERT INTO iot_video_patrol_task (
    task_name,
    task_code,
    patrol_scenes,
    schedule_type,
    interval_minutes,
    auto_snapshot,
    auto_recording,
    recording_duration,
    ai_analysis,
    alert_enabled,
    start_date,
    status,
    running_status,
    creator,
    create_time,
    updater,
    update_time,
    deleted
) VALUES (
    '中山大堂巡检',
    'PATROL_ZHONGSHAN_HALL',
    '{
      "patrolScenes": [
        {
          "sceneId": 1,
          "sceneName": "中山大堂",
          "sceneOrder": 1,
          "gridLayout": "2x2",
          "gridCount": 4,
          "cells": [
            {
              "cellIndex": 0,
              "channels": [
                {
                  "channelId": 101,
                  "channelCode": "CH_EAST_GATE",
                  "channelName": "东大门通道",
                  "duration": 10,
                  "streamUrl": "rtsp://192.168.1.100/stream1",
                  "deviceId": 1001,
                  "deviceName": "东门摄像头"
                },
                {
                  "channelId": 102,
                  "channelCode": "CH_SOUTH_GATE",
                  "channelName": "南大门通道",
                  "duration": 8,
                  "streamUrl": "rtsp://192.168.1.101/stream1",
                  "deviceId": 1002,
                  "deviceName": "南门摄像头"
                },
                {
                  "channelId": 103,
                  "channelCode": "CH_WEST_GATE",
                  "channelName": "西大门通道",
                  "duration": 20,
                  "streamUrl": "rtsp://192.168.1.102/stream1",
                  "deviceId": 1003,
                  "deviceName": "西门摄像头"
                },
                {
                  "channelId": 104,
                  "channelCode": "CH_NORTH_GATE",
                  "channelName": "北大门通道",
                  "duration": 18,
                  "streamUrl": "rtsp://192.168.1.103/stream1",
                  "deviceId": 1004,
                  "deviceName": "北门摄像头"
                }
              ]
            },
            {
              "cellIndex": 1,
              "channels": [
                {
                  "channelId": 201,
                  "channelCode": "CH_LIBRARY",
                  "channelName": "图书馆通道",
                  "duration": 10,
                  "streamUrl": "rtsp://192.168.1.110/stream1",
                  "deviceId": 1010,
                  "deviceName": "图书馆摄像头"
                },
                {
                  "channelId": 202,
                  "channelCode": "CH_GYM",
                  "channelName": "篮球馆通道",
                  "duration": 8,
                  "streamUrl": "rtsp://192.168.1.111/stream1",
                  "deviceId": 1011,
                  "deviceName": "篮球馆摄像头"
                }
              ]
            },
            {
              "cellIndex": 2,
              "channels": [
                {
                  "channelId": 301,
                  "channelCode": "CH_STAIR_401",
                  "channelName": "401楼梯通道",
                  "duration": 10,
                  "streamUrl": "rtsp://192.168.1.120/stream1",
                  "deviceId": 1020,
                  "deviceName": "401楼梯摄像头"
                },
                {
                  "channelId": 302,
                  "channelCode": "CH_STAIR_501",
                  "channelName": "501楼梯通道",
                  "duration": 8,
                  "streamUrl": "rtsp://192.168.1.121/stream1",
                  "deviceId": 1021,
                  "deviceName": "501楼梯摄像头"
                },
                {
                  "channelId": 303,
                  "channelCode": "CH_STAIR_601",
                  "channelName": "601楼梯通道",
                  "duration": 20,
                  "streamUrl": "rtsp://192.168.1.122/stream1",
                  "deviceId": 1022,
                  "deviceName": "601楼梯摄像头"
                }
              ]
            },
            {
              "cellIndex": 3,
              "channels": [
                {
                  "channelId": 401,
                  "channelCode": "CH_MEMORIAL_HALL",
                  "channelName": "纪念堂通道",
                  "duration": null,
                  "streamUrl": "rtsp://192.168.1.130/stream1",
                  "deviceId": 1030,
                  "deviceName": "纪念堂摄像头"
                }
              ]
            }
          ]
        }
      ]
    }',
    1,
    60,
    true,
    false,
    30,
    false,
    false,
    CURDATE(),
    1,
    'stopped',
    '1',
    NOW(),
    '1',
    NOW(),
    false
);

-- ========================================
-- 第三部分：查询示例
-- ========================================

-- 查询巡检计划及场景配置
SELECT 
    id,
    task_name,
    task_code,
    patrol_scenes,
    status,
    running_status
FROM iot_video_patrol_task
WHERE deleted = false
ORDER BY create_time DESC;

-- 提取场景信息（MySQL 5.7+）
SELECT 
    id,
    task_name,
    JSON_EXTRACT(patrol_scenes, '$.patrolScenes[0].sceneName') as scene_name,
    JSON_EXTRACT(patrol_scenes, '$.patrolScenes[0].gridLayout') as grid_layout
FROM iot_video_patrol_task
WHERE deleted = false;

-- ========================================
-- 第四部分：数据验证
-- ========================================

-- 验证 JSON 格式
SELECT 
    id,
    task_name,
    JSON_VALID(patrol_scenes) as is_valid_json
FROM iot_video_patrol_task
WHERE deleted = false;

-- 统计场景数量
SELECT 
    id,
    task_name,
    JSON_LENGTH(patrol_scenes, '$.patrolScenes') as scene_count
FROM iot_video_patrol_task
WHERE deleted = false;
