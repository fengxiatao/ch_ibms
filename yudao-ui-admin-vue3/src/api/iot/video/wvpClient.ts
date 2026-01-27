/**
 * WVP API 客户端
 * 直接调用 WVP 后端 API，不经过 IBMS 后端
 * 使用原生 fetch，避免 axios baseURL 前缀问题
 * 支持 JWT Token 认证
 */

/**
 * 简单的 MD5 实现（用于 WVP 密码加密）
 */
function md5(string: string): string {
  function md5cycle(x: number[], k: number[]) {
    let a = x[0], b = x[1], c = x[2], d = x[3]
    a = ff(a, b, c, d, k[0], 7, -680876936)
    d = ff(d, a, b, c, k[1], 12, -389564586)
    c = ff(c, d, a, b, k[2], 17, 606105819)
    b = ff(b, c, d, a, k[3], 22, -1044525330)
    a = ff(a, b, c, d, k[4], 7, -176418897)
    d = ff(d, a, b, c, k[5], 12, 1200080426)
    c = ff(c, d, a, b, k[6], 17, -1473231341)
    b = ff(b, c, d, a, k[7], 22, -45705983)
    a = ff(a, b, c, d, k[8], 7, 1770035416)
    d = ff(d, a, b, c, k[9], 12, -1958414417)
    c = ff(c, d, a, b, k[10], 17, -42063)
    b = ff(b, c, d, a, k[11], 22, -1990404162)
    a = ff(a, b, c, d, k[12], 7, 1804603682)
    d = ff(d, a, b, c, k[13], 12, -40341101)
    c = ff(c, d, a, b, k[14], 17, -1502002290)
    b = ff(b, c, d, a, k[15], 22, 1236535329)
    a = gg(a, b, c, d, k[1], 5, -165796510)
    d = gg(d, a, b, c, k[6], 9, -1069501632)
    c = gg(c, d, a, b, k[11], 14, 643717713)
    b = gg(b, c, d, a, k[0], 20, -373897302)
    a = gg(a, b, c, d, k[5], 5, -701558691)
    d = gg(d, a, b, c, k[10], 9, 38016083)
    c = gg(c, d, a, b, k[15], 14, -660478335)
    b = gg(b, c, d, a, k[4], 20, -405537848)
    a = gg(a, b, c, d, k[9], 5, 568446438)
    d = gg(d, a, b, c, k[14], 9, -1019803690)
    c = gg(c, d, a, b, k[3], 14, -187363961)
    b = gg(b, c, d, a, k[8], 20, 1163531501)
    a = gg(a, b, c, d, k[13], 5, -1444681467)
    d = gg(d, a, b, c, k[2], 9, -51403784)
    c = gg(c, d, a, b, k[7], 14, 1735328473)
    b = gg(b, c, d, a, k[12], 20, -1926607734)
    a = hh(a, b, c, d, k[5], 4, -378558)
    d = hh(d, a, b, c, k[8], 11, -2022574463)
    c = hh(c, d, a, b, k[11], 16, 1839030562)
    b = hh(b, c, d, a, k[14], 23, -35309556)
    a = hh(a, b, c, d, k[1], 4, -1530992060)
    d = hh(d, a, b, c, k[4], 11, 1272893353)
    c = hh(c, d, a, b, k[7], 16, -155497632)
    b = hh(b, c, d, a, k[10], 23, -1094730640)
    a = hh(a, b, c, d, k[13], 4, 681279174)
    d = hh(d, a, b, c, k[0], 11, -358537222)
    c = hh(c, d, a, b, k[3], 16, -722521979)
    b = hh(b, c, d, a, k[6], 23, 76029189)
    a = hh(a, b, c, d, k[9], 4, -640364487)
    d = hh(d, a, b, c, k[12], 11, -421815835)
    c = hh(c, d, a, b, k[15], 16, 530742520)
    b = hh(b, c, d, a, k[2], 23, -995338651)
    a = ii(a, b, c, d, k[0], 6, -198630844)
    d = ii(d, a, b, c, k[7], 10, 1126891415)
    c = ii(c, d, a, b, k[14], 15, -1416354905)
    b = ii(b, c, d, a, k[5], 21, -57434055)
    a = ii(a, b, c, d, k[12], 6, 1700485571)
    d = ii(d, a, b, c, k[3], 10, -1894986606)
    c = ii(c, d, a, b, k[10], 15, -1051523)
    b = ii(b, c, d, a, k[1], 21, -2054922799)
    a = ii(a, b, c, d, k[8], 6, 1873313359)
    d = ii(d, a, b, c, k[15], 10, -30611744)
    c = ii(c, d, a, b, k[6], 15, -1560198380)
    b = ii(b, c, d, a, k[13], 21, 1309151649)
    a = ii(a, b, c, d, k[4], 6, -145523070)
    d = ii(d, a, b, c, k[11], 10, -1120210379)
    c = ii(c, d, a, b, k[2], 15, 718787259)
    b = ii(b, c, d, a, k[9], 21, -343485551)
    x[0] = add32(a, x[0])
    x[1] = add32(b, x[1])
    x[2] = add32(c, x[2])
    x[3] = add32(d, x[3])
  }
  function cmn(q: number, a: number, b: number, x: number, s: number, t: number) {
    a = add32(add32(a, q), add32(x, t))
    return add32((a << s) | (a >>> (32 - s)), b)
  }
  function ff(a: number, b: number, c: number, d: number, x: number, s: number, t: number) {
    return cmn((b & c) | ((~b) & d), a, b, x, s, t)
  }
  function gg(a: number, b: number, c: number, d: number, x: number, s: number, t: number) {
    return cmn((b & d) | (c & (~d)), a, b, x, s, t)
  }
  function hh(a: number, b: number, c: number, d: number, x: number, s: number, t: number) {
    return cmn(b ^ c ^ d, a, b, x, s, t)
  }
  function ii(a: number, b: number, c: number, d: number, x: number, s: number, t: number) {
    return cmn(c ^ (b | (~d)), a, b, x, s, t)
  }
  function md51(s: string) {
    const n = s.length
    const state = [1732584193, -271733879, -1732584194, 271733878]
    let i
    for (i = 64; i <= n; i += 64) {
      md5cycle(state, md5blk(s.substring(i - 64, i)))
    }
    s = s.substring(i - 64)
    const tail = [0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0]
    for (i = 0; i < s.length; i++) {
      tail[i >> 2] |= s.charCodeAt(i) << ((i % 4) << 3)
    }
    tail[i >> 2] |= 0x80 << ((i % 4) << 3)
    if (i > 55) {
      md5cycle(state, tail)
      for (i = 0; i < 16; i++) tail[i] = 0
    }
    tail[14] = n * 8
    md5cycle(state, tail)
    return state
  }
  function md5blk(s: string) {
    const md5blks: number[] = []
    for (let i = 0; i < 64; i += 4) {
      md5blks[i >> 2] = s.charCodeAt(i) + (s.charCodeAt(i + 1) << 8) + (s.charCodeAt(i + 2) << 16) + (s.charCodeAt(i + 3) << 24)
    }
    return md5blks
  }
  const hex_chr = '0123456789abcdef'.split('')
  function rhex(n: number) {
    let s = ''
    for (let j = 0; j < 4; j++) {
      s += hex_chr[(n >> (j * 8 + 4)) & 0x0F] + hex_chr[(n >> (j * 8)) & 0x0F]
    }
    return s
  }
  function hex(x: number[]) {
    return x.map(rhex).join('')
  }
  function add32(a: number, b: number) {
    return (a + b) & 0xFFFFFFFF
  }
  return hex(md51(string))
}

// WVP 后端地址（通过 Vite 代理）
const WVP_API_BASE = '/wvp-api'

// Token 存储 Key
const WVP_TOKEN_KEY = 'wvp_access_token'
const WVP_TOKEN_EXPIRE_KEY = 'wvp_token_expire'

// WVP 默认账号（可通过环境变量配置）
const WVP_USERNAME = import.meta.env.VITE_WVP_USERNAME || 'admin'
const WVP_PASSWORD = import.meta.env.VITE_WVP_PASSWORD || 'admin'

/**
 * WVP API 响应结构
 */
interface WvpResponse<T = any> {
  code: number
  msg?: string
  data?: T
  // 登录响应字段
  accessToken?: string
  username?: string
  serverId?: string
}

// ==================== Token 管理 ====================

/**
 * 获取存储的 Token
 */
function getStoredToken(): string | null {
  const token = localStorage.getItem(WVP_TOKEN_KEY)
  const expire = localStorage.getItem(WVP_TOKEN_EXPIRE_KEY)
  
  // 检查是否过期（提前 5 分钟刷新）
  if (expire && Date.now() > parseInt(expire) - 5 * 60 * 1000) {
    clearToken()
    return null
  }
  
  return token
}

/**
 * 存储 Token
 */
function setStoredToken(token: string, expireSeconds: number = 7200) {
  localStorage.setItem(WVP_TOKEN_KEY, token)
  localStorage.setItem(WVP_TOKEN_EXPIRE_KEY, String(Date.now() + expireSeconds * 1000))
}

/**
 * 清除 Token
 */
function clearToken() {
  localStorage.removeItem(WVP_TOKEN_KEY)
  localStorage.removeItem(WVP_TOKEN_EXPIRE_KEY)
}

// 登录锁，防止多个请求同时登录
let loginPromise: Promise<string | null> | null = null

/**
 * WVP 登录获取 Token
 */
async function wvpLogin(): Promise<string | null> {
  // 如果正在登录，等待结果
  if (loginPromise) {
    return loginPromise
  }
  
  // 检查已有 token
  const existingToken = getStoredToken()
  if (existingToken) {
    return existingToken
  }
  
  // 开始登录
  loginPromise = (async () => {
    try {
      console.log('[WVP Auth] 正在登录...')
      
      // 密码需要 MD5 加密
      const passwordMd5 = md5(WVP_PASSWORD)
      
      const response = await fetch(
        `${WVP_API_BASE}/user/login?username=${encodeURIComponent(WVP_USERNAME)}&password=${passwordMd5}`,
        {
          method: 'GET',
          headers: {
            'Content-Type': 'application/json'
          }
        }
      )
      
      if (!response.ok) {
        throw new Error(`登录失败: ${response.status} ${response.statusText}`)
      }
      
      const data = await response.json()
      console.log('[WVP Auth] 登录响应:', data)
      
      // WVP 返回格式: { code: 0, data: { accessToken, username, serverId } }
      // 或直接返回: { accessToken, username, serverId }
      const token = data.data?.accessToken || data.accessToken
      
      if (token) {
        setStoredToken(token)
        console.log('[WVP Auth] 登录成功，Token 已保存')
        return token
      }
      
      throw new Error('登录响应中没有 Token')
    } catch (e: any) {
      console.error('[WVP Auth] 登录失败:', e)
      return null
    } finally {
      loginPromise = null
    }
  })()
  
  return loginPromise
}

/**
 * 确保已登录（获取有效 Token）
 */
async function ensureToken(): Promise<string | null> {
  let token = getStoredToken()
  if (!token) {
    token = await wvpLogin()
  }
  return token
}

/**
 * 封装 fetch 请求（带认证）
 */
async function wvpFetch<T = any>(
  url: string,
  options?: RequestInit,
  retry: boolean = true
): Promise<WvpResponse<T>> {
  const token = await ensureToken()
  
  const headers: Record<string, string> = {
    'Content-Type': 'application/json',
    ...options?.headers as Record<string, string>
  }
  
  // 添加认证头
  if (token) {
    headers['access-token'] = token
  }
  
  const response = await fetch(`${WVP_API_BASE}${url}`, {
    ...options,
    headers
  })

  // 处理 401 未授权
  if (response.status === 401 && retry) {
    console.log('[WVP API] Token 无效，重新登录')
    clearToken()
    const newToken = await wvpLogin()
    if (newToken) {
      // 重试请求
      return wvpFetch<T>(url, options, false)
    }
    throw new Error('WVP 认证失败，请检查账号密码')
  }

  if (!response.ok) {
    throw new Error(`WVP API 请求失败: ${response.status} ${response.statusText}`)
  }

  return response.json()
}

/**
 * GET 请求
 */
async function wvpGet<T = any>(url: string, params?: Record<string, any>): Promise<WvpResponse<T>> {
  const searchParams = new URLSearchParams()
  if (params) {
    Object.entries(params).forEach(([key, value]) => {
      if (value !== undefined && value !== null && value !== '') {
        searchParams.append(key, String(value))
      }
    })
  }
  const queryString = searchParams.toString()
  const fullUrl = queryString ? `${url}?${queryString}` : url
  return wvpFetch<T>(fullUrl, { method: 'GET' })
}

// ==================== 对外暴露的认证方法 ====================

/**
 * 手动登录 WVP（可在页面加载时调用）
 */
export async function loginWvp(): Promise<boolean> {
  const token = await wvpLogin()
  return !!token
}

/**
 * 登出 WVP
 */
export function logoutWvp() {
  clearToken()
}

/**
 * 检查 WVP 是否已登录
 */
export function isWvpLoggedIn(): boolean {
  return !!getStoredToken()
}

// ==================== 通道 API ====================

/**
 * WVP 通道信息 - 兼容多种字段名
 */
export interface WvpChannelItem {
  // 主键ID - WVP 可能使用不同字段名
  id?: number
  commonGbChannelId?: number
  
  // 通道ID
  channelId?: string
  gbId?: string
  
  // 设备ID
  deviceId?: string
  gbDeviceId?: string
  gbDeviceDbId?: number
  
  // 名称
  name?: string
  gbName?: string
  
  // 状态
  status?: number
  gbStatus?: string
  
  // 其他
  ptzType?: number
  longitude?: number
  latitude?: number
  hasAudio?: boolean
  channelType?: number
  subCount?: number
}

/**
 * 获取通道列表
 */
export async function getChannelList(params?: {
  page?: number
  count?: number
  query?: string
  online?: boolean
  channelType?: string
}) {
  const res = await wvpGet<{ list: any[]; total: number }>('/common/channel/list', {
    page: params?.page || 1,
    count: params?.count || 100,
    query: params?.query || '',
    online: params?.online,
    channelType: params?.channelType || ''
  })
  
  // 打印原始数据用于调试
  if (res.data?.list?.length > 0) {
    console.log('[WVP API] 通道原始数据示例:', JSON.stringify(res.data.list[0], null, 2))
  }
  
  // 标准化通道数据
  const list = (res.data?.list || []).map(normalizeChannel)
  
  return { list, total: res.data?.total || 0 }
}

/**
 * 标准化通道数据 - 处理不同的字段名
 */
function normalizeChannel(raw: any): WvpChannelItem {
  return {
    // ID: 优先使用 commonGbChannelId，其次 id
    id: raw.commonGbChannelId ?? raw.id ?? raw.channelDbId,
    commonGbChannelId: raw.commonGbChannelId,
    
    // 通道ID
    channelId: raw.channelId ?? raw.gbId ?? raw.commonGbId,
    gbId: raw.gbId ?? raw.commonGbId,
    
    // 设备ID
    deviceId: raw.deviceId ?? raw.gbDeviceId,
    gbDeviceId: raw.gbDeviceId,
    gbDeviceDbId: raw.gbDeviceDbId,
    
    // 名称: 优先使用 gbName
    name: raw.gbName ?? raw.name ?? raw.channelName ?? `通道${raw.channelId || raw.gbId}`,
    gbName: raw.gbName,
    
    // 状态: gbStatus 可能是字符串 "ON"/"OFF" 或数字
    status: raw.gbStatus === 'ON' || raw.gbStatus === 1 || raw.status === 1 ? 1 : 0,
    gbStatus: raw.gbStatus,
    
    // 其他字段
    ptzType: raw.ptzType ?? raw.gbPtzType,
    longitude: raw.longitude ?? raw.gbLongitude,
    latitude: raw.latitude ?? raw.gbLatitude,
    hasAudio: raw.hasAudio ?? raw.gbHasAudio,
    channelType: raw.channelType,
    subCount: raw.subCount
  }
}

/**
 * 获取通道的播放用ID（用于实时播放、云台控制等）
 * WVP 通用 API 使用 commonGbChannelId
 */
export function getChannelPlayId(channel: WvpChannelItem): number | string {
  return channel.commonGbChannelId ?? channel.id ?? channel.channelId ?? 0
}

/**
 * 获取通道的数据库 ID（用于 WVP 回放相关 API）
 * 注意：WVP 后端 API 使用数据库 ID (Integer)，不是国标字符串 ID
 */
export function getChannelGbId(channel: WvpChannelItem): string {
  // WVP API 需要数据库 ID
  return String(channel.gbId || channel.channelId || channel.id || '')
}

/**
 * 获取通道的国标设备 ID（20位数字字符串，用于日志等）
 */
export function getChannelDeviceId(channel: WvpChannelItem): string {
  return channel.gbDeviceId || channel.deviceId || ''
}

/**
 * WVP 流信息
 */
export interface WvpStreamInfo {
  app?: string
  stream?: string
  ip?: string
  flv?: string
  https_flv?: string
  ws_flv?: string
  wss_flv?: string
  fmp4?: string
  https_fmp4?: string
  ws_fmp4?: string
  wss_fmp4?: string
  hls?: string
  https_hls?: string
  rtmp?: string
  rtmps?: string
  rtsp?: string
  rtsps?: string
  rtc?: string
  rtcs?: string
  mediaServerId?: string
  tracks?: any[]
}

/**
 * 播放通道
 */
export async function playChannel(channelId: number | string): Promise<WvpStreamInfo | null> {
  console.log('[WVP API] 请求播放通道:', channelId)
  const res = await wvpGet<WvpStreamInfo>('/common/channel/play', { channelId })
  console.log('[WVP API] 播放返回:', res)
  return res.data || null
}

/**
 * 停止播放
 */
export async function stopChannel(channelId: number | string): Promise<void> {
  await wvpGet('/common/channel/play/stop', { channelId })
}

// ==================== 云台控制 API ====================

/**
 * 云台控制
 * @param channelId 通道ID
 * @param command 命令: up/down/left/right/upleft/upright/downleft/downright/zoomin/zoomout/stop
 * @param speed 速度 1-255
 */
export async function ptzControl(
  channelId: number | string,
  command: string,
  speed: number = 50
): Promise<void> {
  await wvpGet('/common/channel/front-end/ptz', {
    channelId,
    command,
    panSpeed: speed,
    tiltSpeed: speed,
    zoomSpeed: speed
  })
}

/**
 * 预置位信息
 */
export interface WvpPreset {
  presetId: number
  presetName?: string
}

/**
 * 查询预置位
 */
export async function queryPresets(channelId: number | string): Promise<WvpPreset[]> {
  const res = await wvpGet<WvpPreset[]>('/common/channel/front-end/preset/query', { channelId })
  return res.data || []
}

/**
 * 调用预置位
 */
export async function callPreset(channelId: number | string, presetId: number): Promise<void> {
  await wvpGet('/common/channel/front-end/preset/call', { channelId, presetId })
}

/**
 * 添加预置位
 */
export async function addPreset(
  channelId: number | string,
  presetId: number,
  presetName: string
): Promise<void> {
  await wvpGet('/common/channel/front-end/preset/add', { channelId, presetId, presetName })
}

/**
 * 删除预置位
 */
export async function deletePreset(channelId: number | string, presetId: number): Promise<void> {
  await wvpGet('/common/channel/front-end/preset/delete', { channelId, presetId })
}

// ==================== 录像回放 API ====================

/**
 * 录像片段
 */
export interface WvpRecordItem {
  startTime: string
  endTime: string
  name?: string
  filePath?: string
}

/**
 * 查询录像列表
 * 注意：此接口使用 gbId（国标ID），不是 commonGbChannelId
 */
export async function queryRecords(
  channelId: string,
  startTime: string,
  endTime: string
): Promise<WvpRecordItem[]> {
  console.log('[WVP API] 查询录像, channelId:', channelId, 'startTime:', startTime, 'endTime:', endTime)
  const res = await wvpGet<WvpRecordItem[]>('/common/channel/playback/query', {
    channelId,
    startTime,
    endTime
  })
  console.log('[WVP API] 录像查询返回:', res)
  // WVP 返回的 data 直接就是数组
  const records = Array.isArray(res.data) ? res.data : (res.data as any)?.recordList || []
  return records
}

/**
 * 开始回放
 * 注意：此接口使用 gbId（国标ID）
 */
export async function startPlayback(
  channelId: string,
  startTime: string,
  endTime: string
): Promise<WvpStreamInfo | null> {
  console.log('[WVP API] 开始回放, channelId:', channelId, 'startTime:', startTime, 'endTime:', endTime)
  const res = await wvpGet<WvpStreamInfo>('/common/channel/playback', {
    channelId,
    startTime,
    endTime
  })
  console.log('[WVP API] 回放返回:', res)
  return res.data || null
}

/**
 * 停止回放
 * 注意：此接口使用 gbId（国标ID）
 */
export async function stopPlayback(channelId: string, stream: string): Promise<void> {
  await wvpGet('/common/channel/playback/stop', { channelId, stream })
}

/**
 * 回放倍速
 * 注意：此接口使用 gbId（国标ID）
 */
export async function setPlaybackSpeed(
  channelId: string,
  stream: string,
  speed: number
): Promise<void> {
  console.log('[WVP API] 设置倍速请求:', { channelId, stream, speed })
  const res = await wvpGet('/common/channel/playback/speed', { channelId, stream, speed })
  console.log('[WVP API] 设置倍速响应:', res)
}

/**
 * 回放暂停
 * 注意：此接口使用 gbId（国标ID）
 */
export async function pausePlayback(channelId: string, stream: string): Promise<void> {
  await wvpGet('/common/channel/playback/pause', { channelId, stream })
}

/**
 * 回放恢复
 * 注意：此接口使用 gbId（国标ID）
 */
export async function resumePlayback(channelId: string, stream: string): Promise<void> {
  await wvpGet('/common/channel/playback/resume', { channelId, stream })
}

/**
 * 回放seek
 * 注意：此接口使用 gbId（国标ID）
 */
export async function seekPlayback(
  channelId: string,
  stream: string,
  seekTime: string
): Promise<void> {
  await wvpGet('/common/channel/playback/seek', { channelId, stream, seekTime })
}

// ==================== 设备 API ====================

/**
 * WVP 设备信息
 */
export interface WvpDevice {
  deviceId: string
  name: string
  manufacturer?: string
  model?: string
  online?: boolean
  channelCount?: number
  ip?: string
  port?: number
}

/**
 * 获取设备列表
 */
export async function getDeviceList(params?: {
  page?: number
  count?: number
  query?: string
  online?: boolean
}) {
  const res = await wvpGet<{ list: WvpDevice[]; total: number }>('/device/query/devices', {
    page: params?.page || 1,
    count: params?.count || 100,
    query: params?.query || '',
    online: params?.online
  })
  return res.data
}

/**
 * 同步设备通道
 */
export async function syncDevice(deviceId: string): Promise<void> {
  await wvpGet(`/device/query/devices/${deviceId}/sync`)
}
