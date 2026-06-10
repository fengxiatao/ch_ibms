export interface StreamPlayUrls {
  wsFlvUrl?: string
  webrtcUrl?: string
  wsFmp4Url?: string
  fmp4Url?: string
  flvUrl?: string
  hlsUrl?: string
  rtmpUrl?: string
}

const INTRANET_HOST_REG = /(?:192\.168\.\d+\.\d+|10\.\d+\.\d+\.\d+|172\.(?:1[6-9]|2\d|3[01])\.\d+\.\d+)(?::\d+)?/g
const KNOWN_HOST_REWRITE: Record<string, string> = {
  // 线上历史配置里出现过该拼写错误，导致页面可访问但流地址 DNS 解析失败
  'ibms.gzchanghu.cn': 'ibms.gzchanghui.cn'
}

const resolveConfiguredBaseHostname = () => {
  const rawBaseUrl = String(import.meta.env.VITE_BASE_URL || '').trim()
  if (!rawBaseUrl) {
    return ''
  }
  try {
    return new URL(rawBaseUrl, window.location.origin).hostname
  } catch {
    return ''
  }
}

const isPrivateHostname = (hostname: string) => {
  if (hostname === 'localhost' || hostname === '127.0.0.1') return true
  if (hostname.startsWith('192.168.')) return true
  if (hostname.startsWith('10.')) return true
  if (hostname.startsWith('172.')) {
    const secondOctet = parseInt(hostname.split('.')[1])
    return secondOctet >= 16 && secondOctet <= 31
  }
  return false
}

export const isIntranetAccess = () => {
  const configuredBaseHostname = resolveConfiguredBaseHostname()
  if (configuredBaseHostname && !isPrivateHostname(configuredBaseHostname)) {
    return false
  }
  return isPrivateHostname(window.location.hostname)
}

export const isForceWebrtcEnabled = () => {
  try {
    return new URLSearchParams(window.location.search).get('forceWebrtc') === '1'
  } catch {
    return false
  }
}

export const getDefaultPreferWebrtc = () => {
  // 默认优先 WebRTC（已配置 NATAPP UDP 隧道）
  // 若需回退 FLV 调试，可通过 forceFlv=1 显式禁用 WebRTC
  return !isForceFlvEnabled()
}

export const isForceFlvEnabled = () => {
  try {
    return new URLSearchParams(window.location.search).get('forceFlv') === '1'
  } catch {
    return false
  }
}

/** 计算拉流地址中的「公网 host[:port]」替换目标（私网 IP 段会被替换为此值） */
const resolveStreamPublicAddr = (): string | null => {
  const envHost = (import.meta.env.VITE_STREAM_PUBLIC_HOST || '').trim()
  const envPortRaw = (import.meta.env.VITE_STREAM_PUBLIC_PORT || '').trim()
  const envPort = envPortRaw ? parseInt(envPortRaw, 10) : undefined

  const pageHost = window.location.hostname
  const streamHost = envHost || pageHost

  if ((pageHost === 'localhost' || pageHost === '127.0.0.1') && !envHost) {
    return null
  }
  if ((streamHost === 'localhost' || streamHost === '127.0.0.1') && !envHost) {
    return null
  }

  if (envHost) {
    if (envPort !== undefined && !Number.isNaN(envPort)) {
      return envPort === 80 || envPort === 443 ? streamHost : `${streamHost}:${envPort}`
    }
    return streamHost
  }

  const pagePort = window.location.port ? parseInt(window.location.port, 10) : 80
  return pagePort === 80 || pagePort === 443 ? streamHost : `${streamHost}:${pagePort}`
}

/**
 * 将各协议播放地址的主机名（及可选端口）替换为指定域名，并按当前页面协议校正 ws/http 的加密形态。
 * 用于数字孪生大屏等场景固定走流媒体公网域名（如 ibms.gzchanghui.cn）。
 */
export const rewriteStreamPlayUrlsHost = <T extends StreamPlayUrls>(
  urls: T | null | undefined,
  targetHost: string
): T | null => {
  if (!urls) {
    return null
  }
  const raw = String(targetHost || '').trim()
  if (!raw) {
    return { ...urls }
  }
  const withoutScheme = raw.replace(/^https?:\/\//i, '')
  const slash = withoutScheme.indexOf('/')
  const hostPort = (slash >= 0 ? withoutScheme.slice(0, slash) : withoutScheme).trim()
  if (!hostPort) {
    return { ...urls }
  }
  let hostname = hostPort
  let port = ''
  if (hostPort.startsWith('[')) {
    const end = hostPort.indexOf(']')
    if (end > 0) {
      hostname = hostPort.slice(0, end + 1)
      const rest = hostPort.slice(end + 1)
      if (rest.startsWith(':')) {
        port = rest.slice(1)
      }
    }
  } else {
    const colon = hostPort.lastIndexOf(':')
    if (colon > 0 && /^\d+$/.test(hostPort.slice(colon + 1))) {
      hostname = hostPort.slice(0, colon)
      port = hostPort.slice(colon + 1)
    }
  }

  const isHttps =
    typeof window !== 'undefined' && window.location && window.location.protocol === 'https:'

  const alignWsHttpScheme = (url: string) => {
    if (isHttps) {
      return url.replace(/^ws:\/\//i, 'wss://').replace(/^http:\/\//i, 'https://')
    }
    return url.replace(/^wss:\/\//i, 'ws://').replace(/^https:\/\//i, 'http://')
  }

  const replaceOne = (url?: string) => {
    if (!url) {
      return url
    }
    try {
      const u = new URL(url)
      u.hostname = hostname
      if (port) {
        u.port = port
      }
      return alignWsHttpScheme(u.toString())
    } catch {
      return url
    }
  }

  const out = { ...urls } as T
  out.wsFlvUrl = replaceOne(urls.wsFlvUrl)
  out.webrtcUrl = replaceOne(urls.webrtcUrl)
  out.wsFmp4Url = replaceOne(urls.wsFmp4Url)
  out.flvUrl = replaceOne(urls.flvUrl)
  out.fmp4Url = replaceOne(urls.fmp4Url)
  out.hlsUrl = replaceOne(urls.hlsUrl)
  out.rtmpUrl = replaceOne(urls.rtmpUrl)
  return out
}

export const adaptStreamPlayUrls = <T extends StreamPlayUrls>(urls?: T | null): T | null => {
  if (!urls) {
    return null
  }
  if (isIntranetAccess() && !isForceWebrtcEnabled()) {
    return urls
  }

  const adapted = { ...urls }
  const publicAddr = resolveStreamPublicAddr()
  if (!publicAddr) {
    return urls
  }
  const isHttps = window.location.protocol === 'https:'
  const httpProtocol = isHttps ? 'https' : 'http'
  const wsProtocol = isHttps ? 'wss' : 'ws'
  const envRtcHost = (import.meta as any).env?.VITE_ZLM_RTC_HOST as string | undefined
  const envRtcPortRaw = (import.meta as any).env?.VITE_ZLM_RTC_PORT as string | undefined
  const envRtcPort = envRtcPortRaw ? parseInt(envRtcPortRaw) : undefined
  const envSecret = (import.meta as any).env?.VITE_ZLM_SECRET as string | undefined

  const replaceHttpUrl = (url?: string) => {
    if (!url) {
      return url
    }
    let newUrl = url.replace(INTRANET_HOST_REG, publicAddr)
    for (const [wrongHost, correctHost] of Object.entries(KNOWN_HOST_REWRITE)) {
      newUrl = newUrl.replaceAll(wrongHost, correctHost)
    }
    newUrl = newUrl.replace(/^http:/, `${httpProtocol}:`)
    return newUrl
  }

  const replaceWsUrl = (url?: string) => {
    if (!url) {
      return url
    }
    let newUrl = url.replace(INTRANET_HOST_REG, publicAddr)
    for (const [wrongHost, correctHost] of Object.entries(KNOWN_HOST_REWRITE)) {
      newUrl = newUrl.replaceAll(wrongHost, correctHost)
    }
    newUrl = newUrl.replace(/^ws:/, `${wsProtocol}:`)
    return newUrl
  }

  const replaceWebrtcUrl = (url?: string) => {
    if (!url) {
      return url
    }
    let newUrl = replaceHttpUrl(url) || url
    try {
      const uri = new URL(newUrl)
      if (envRtcHost) {
        uri.hostname = envRtcHost
      }
      if (envRtcPort && !Number.isNaN(envRtcPort)) {
        uri.port = String(envRtcPort)
      }
      if (envSecret && !uri.searchParams.get('secret')) {
        uri.searchParams.set('secret', envSecret)
      }
      newUrl = uri.toString()
    } catch {}
    return newUrl
  }

  adapted.wsFlvUrl = replaceWsUrl(urls.wsFlvUrl)
  adapted.webrtcUrl = replaceWebrtcUrl(urls.webrtcUrl)
  adapted.wsFmp4Url = replaceWsUrl(urls.wsFmp4Url)
  adapted.flvUrl = replaceHttpUrl(urls.flvUrl)
  adapted.fmp4Url = replaceHttpUrl(urls.fmp4Url)
  adapted.hlsUrl = replaceHttpUrl(urls.hlsUrl)
  adapted.rtmpUrl = replaceHttpUrl(urls.rtmpUrl)

  return adapted
}

export const pickPreferredPlayUrl = (urls?: StreamPlayUrls | null) => {
  if (!urls) {
    return ''
  }
  return urls.webrtcUrl || urls.wsFlvUrl || urls.hlsUrl || urls.flvUrl || urls.fmp4Url || urls.wsFmp4Url || ''
}

export const pickPreferredProtocol = (urls?: StreamPlayUrls | null) => {
  if (!urls) {
    return '待获取'
  }
  if (urls.webrtcUrl) return 'WebRTC'
  if (urls.wsFlvUrl) return 'WS-FLV'
  if (urls.hlsUrl) return 'HLS'
  if (urls.flvUrl) return 'HTTP-FLV'
  if (urls.fmp4Url) return 'HTTP-FMP4'
  if (urls.wsFmp4Url) return 'WS-FMP4'
  return '待获取'
}
