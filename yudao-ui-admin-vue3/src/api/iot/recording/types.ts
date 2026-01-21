// 录像时间段分布VO
export interface RecordingTimeSegmentVO {
  channelId: number
  channelName: string
  deviceName: string
  segments: RecordingSegment[]
}

// 录像片段
export interface RecordingSegment {
  startTime: string // 开始时间
  endTime: string   // 结束时间
  hasRecording: boolean // 是否有录像
}

// 录像时间段查询请求
export interface RecordingTimeSegmentReqVO {
  channelIds: number[]
  startTime: string
  endTime: string
  intervalMinutes?: number // 时间间隔（分钟），默认5分钟
}
