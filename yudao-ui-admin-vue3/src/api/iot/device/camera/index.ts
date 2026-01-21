import request from '@/config/axios'
import { DeviceVO } from '../device'

// 摄像头设备扩展 VO
export interface CameraDeviceVO extends DeviceVO {
  // 视频流信息
  streamUrl?: string
  snapshot?: string
  
  // 实时属性数据
  realtimeData?: {
    online_status?: boolean
    video_quality?: number
    ptz_position?: {
      pan: number
      tilt: number
      zoom: number
    }
    recording_status?: number
    video_stream_url?: string
    snapshot_url?: string
    device_temperature?: number
    night_vision_mode?: boolean
    motion_detection_enabled?: boolean
  }
  
  // 摄像头配置
  cameraConfig?: {
    brand?: string
    model?: string
    rtsp_url?: string
    http_port?: number
    username?: string
    password?: string
    ptz_support?: boolean
    night_vision?: boolean
  }
}

// 摄像头服务调用参数
export interface CameraServiceCallVO {
  deviceId: number
  serviceIdentifier: string
  params?: any
}

// 云台控制参数
export interface PTZControlParams {
  direction: number // 0:上, 1:下, 2:左, 3:右, 4:放大, 5:缩小, 6:停止
  speed?: number // 1-10
}

// 录像控制参数
export interface RecordControlParams {
  duration?: number // 录像时长（秒）
  quality?: number // 0:标清, 1:高清, 2:超清
}

// 截图参数
export interface SnapshotParams {
  quality?: number // 0:低, 1:中, 2:高
}

// 预置位参数
export interface PresetParams {
  preset_id: number
  preset_name?: string
}

// 摄像头事件 VO
export interface CameraEventVO {
  id: number
  deviceId: number
  deviceName: string
  eventType: string
  eventData: any
  timestamp: Date
  severity: number
}

// 摄像头统计数据 VO
export interface CameraStatsVO {
  total: number
  online: number
  offline: number
  warning: number
  fault: number
  byType?: Record<string, number>
  byLocation?: Record<string, number>
}

// 摄像头 API
export const CameraApi = {
  // 获取所有摄像头设备
  getCameraDevices: async (params?: any) => {
    return await request.get({ 
      url: `/iot/camera/list`, 
      params: {
        pageNo: 1,
        pageSize: 100,
        ...params
      }
    })
  },

  // 获取摄像头详情（包含实时数据）
  getCameraDetail: async (deviceId: number) => {
    return await request.get({ 
      url: `/iot/camera/detail`, 
      params: { deviceId } 
    })
  },

  // 获取摄像头实时属性数据
  getCameraRealtimeData: async (deviceId: number) => {
    return await request.get({ 
      url: `/iot/device/property/get-latest`, 
      params: { deviceId } 
    })
  },

  // 获取摄像头历史属性数据
  getCameraHistoryData: async (deviceId: number, startTime: Date, endTime: Date) => {
    return await request.get({ 
      url: `/iot/device/property/history-list`, 
      params: { 
        deviceId, 
        startTime: startTime.toISOString(),
        endTime: endTime.toISOString()
      } 
    })
  },

  // 调用摄像头服务
  callCameraService: async (data: CameraServiceCallVO) => {
    return await request.post({ 
      url: `/iot/device/message/send`, 
      data: {
        deviceId: data.deviceId,
        method: `thing.service.${data.serviceIdentifier}`,
        params: data.params || {}
      }
    })
  },

  // 云台控制
  controlPTZ: async (deviceId: number, params: PTZControlParams) => {
    return await CameraApi.callCameraService({
      deviceId,
      serviceIdentifier: 'ptz_control',
      params
    })
  },

  // 开始录像
  startRecord: async (deviceId: number, params?: RecordControlParams) => {
    return await CameraApi.callCameraService({
      deviceId,
      serviceIdentifier: 'start_record',
      params: {
        duration: params?.duration || 60,
        quality: params?.quality || 1
      }
    })
  },

  // 停止录像
  stopRecord: async (deviceId: number) => {
    return await CameraApi.callCameraService({
      deviceId,
      serviceIdentifier: 'stop_record',
      params: {}
    })
  },

  // 截图
  takeSnapshot: async (deviceId: number, params?: SnapshotParams) => {
    return await CameraApi.callCameraService({
      deviceId,
      serviceIdentifier: 'take_snapshot',
      params: {
        quality: params?.quality || 1
      }
    })
  },

  // 设置预置位
  setPreset: async (deviceId: number, params: PresetParams) => {
    return await CameraApi.callCameraService({
      deviceId,
      serviceIdentifier: 'set_preset',
      params
    })
  },

  // 转到预置位
  gotoPreset: async (deviceId: number, presetId: number) => {
    return await CameraApi.callCameraService({
      deviceId,
      serviceIdentifier: 'goto_preset',
      params: { preset_id: presetId }
    })
  },

  // 获取摄像头事件列表
  getCameraEvents: async (deviceId: number, eventType?: string, limit: number = 100) => {
    return await request.get({ 
      url: `/iot/device/event/list`, 
      params: { 
        deviceId,
        eventType,
        pageNo: 1,
        pageSize: limit
      } 
    })
  },

  // 获取摄像头统计数据
  getCameraStats: async () => {
    return await request.get({ 
      url: `/iot/camera/stats` 
    })
  },

  // 生成RTSP流地址
  generateRTSPUrl: (camera: CameraDeviceVO): string => {
    const config = camera.cameraConfig
    if (!config) return ''

    // 如果配置中已有RTSP URL，直接返回
    if (config.rtsp_url) {
      return config.rtsp_url
    }

    // 从实时数据中获取
    if (camera.realtimeData?.video_stream_url) {
      return camera.realtimeData.video_stream_url
    }

    // 根据品牌和配置生成
    const { username, password } = config
    const ip = camera.ip
    const rtsp_port = 554

    if (!ip || !username || !password) {
      return ''
    }

    // 海康威视RTSP地址格式
    if (config.brand === 'hikvision') {
      return `rtsp://${username}:${password}@${ip}:${rtsp_port}/Streaming/Channels/101`
    }

    // 大华RTSP地址格式
    if (config.brand === 'dahua') {
      return `rtsp://${username}:${password}@${ip}:${rtsp_port}/cam/realmonitor?channel=1&subtype=0`
    }

    // 通用RTSP地址
    return `rtsp://${username}:${password}@${ip}:${rtsp_port}/stream1`
  },

  // 生成截图URL
  generateSnapshotUrl: (camera: CameraDeviceVO): string => {
    const config = camera.cameraConfig
    if (!config) return ''

    // 从实时数据中获取
    if (camera.realtimeData?.snapshot_url) {
      return camera.realtimeData.snapshot_url
    }

    const { username, password, http_port = 80 } = config
    const ip = camera.ip

    if (!ip || !username || !password) {
      return ''
    }

    // 海康威视截图URL格式
    if (config.brand === 'hikvision') {
      return `http://${ip}:${http_port}/ISAPI/Streaming/channels/101/picture`
    }

    // 大华截图URL格式
    if (config.brand === 'dahua') {
      return `http://${ip}:${http_port}/cgi-bin/snapshot.cgi?channel=1`
    }

    return ''
  }
}



















