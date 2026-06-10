// Jessibuca 用于播放 ZLMediaKit 的 HTTP-FLV / WS-FLV，支持 H.265 WASM 解码

declare global {
  interface Window {
    Jessibuca?: any
  }
}

let jessibucaLoader: Promise<void> | null = null

const ensureJessibucaLoaded = async () => {
  if (typeof window === 'undefined') {
    throw new Error('当前环境不支持视频播放')
  }
  if (window.Jessibuca) return

  if (!jessibucaLoader) {
    jessibucaLoader = new Promise<void>((resolve, reject) => {
      const existing = document.querySelector<HTMLScriptElement>('script[data-jessibuca="true"]')
      if (existing) {
        existing.addEventListener('load', () => resolve(), { once: true })
        existing.addEventListener('error', () => reject(new Error('Jessibuca 加载失败')), { once: true })
        return
      }

      const script = document.createElement('script')
      script.src = '/jessibuca/jessibuca.js'
      script.async = true
      script.dataset.jessibuca = 'true'
      script.onload = () => resolve()
      script.onerror = () => reject(new Error('Jessibuca 加载失败'))
      document.head.appendChild(script)
    })
  }

  await jessibucaLoader
  if (!window.Jessibuca) {
    throw new Error('Jessibuca 未初始化')
  }
}

// ZLMediaKit 播放地址类型（与后端 PlayUrlRespVO 保持字段一致）
export interface ZlmPlayUrls {
  wsFlvUrl?: string
  flvUrl?: string
  wsFmp4Url?: string
  fmp4Url?: string
  hlsUrl?: string
  /** ZLMediaKit WebRTC API 地址：http(s)://host:port/index/api/webrtc?app=live&stream=xxx&type=play，外网由后端/前端适配公网 host */
  webrtcUrl?: string
}

export type ZlmPlayMode = 'flv' | 'webrtc'

export interface ZlmPlayerInstance {
  mode: ZlmPlayMode
  destroy: () => void | Promise<void>
}

interface PlayLiveParams {
  container: HTMLElement
  urls: ZlmPlayUrls
  preferWebrtc?: boolean
}

const clearJessibucaContainerMark = (container: HTMLElement) => {
  delete container.dataset.jessibuca
  container.removeAttribute('data-jessibuca')
}

/**
 * ZLMediaKit 播放封装
 * - 默认使用 ws-flv/http-flv + Jessibuca WASM，兼容当前摄像机/NVR 的 H.265 流
 * - WebRTC 仅作为显式调试模式；浏览器原生 WebRTC 通常无法播放 H.265
 */
export function useZlmPlayer() {
  const createFlvPlayerCore = async (
    container: HTMLElement,
    flvUrl: string
  ): Promise<ZlmPlayerInstance> => {
    await ensureJessibucaLoaded()

    clearJessibucaContainerMark(container)
    container.innerHTML = ''
    const player = new window.Jessibuca({
      container,
      videoBuffer: 0.2,
      isResize: true,
      text: '',
      loadingText: '加载中...',
      useMSE: false,
      useWCS: false,
      debug: false,
      hasAudio: false,
      showBandwidth: false,
      operateBtns: {
        fullscreen: false,
        screenshot: false,
        play: false,
        audio: false,
        record: false
      },
      decoder: '/jessibuca/decoder.js'
    })

    await player.play(flvUrl)

    const destroy = async () => {
      try {
        const destroyResult = player.destroy()
        if (destroyResult && typeof destroyResult.then === 'function') {
          await destroyResult
        }
      } catch {}
      container.innerHTML = ''
      clearJessibucaContainerMark(container)
    }

    return {
      mode: 'flv',
      destroy
    }
  }

  const playFlv = async (container: HTMLElement, flvUrl: string): Promise<ZlmPlayerInstance> => {
    return createFlvPlayerCore(container, flvUrl)
  }

  /**
   * ZLMediaKit WebRTC 播放
   * 流程：创建 RTCPeerConnection(recvonly) -> createOffer -> POST offer 到 webrtcUrl -> setRemoteDescription(answer) -> ontrack 渲染
   * 信令格式见：https://docs.zlmediakit.com/zh/guide/protocol/webrtc/webrtc_signaling_interaction_format.html
   */
  const playWebrtc = async (container: HTMLElement, webrtcUrl: string): Promise<ZlmPlayerInstance> => {
    container.innerHTML = ''
    const videoEl = document.createElement('video')
    videoEl.style.width = '100%'
    videoEl.style.height = '100%'
    videoEl.autoplay = true
    videoEl.muted = true
    videoEl.playsInline = true
    videoEl.setAttribute('playsinline', 'true')
    container.appendChild(videoEl)

    // 旧版验证可用的 WebRTC 播放实现（避免“信令成功但黑屏”）
    const pc = new RTCPeerConnection({
      iceServers: [{ urls: 'stun:stun.l.google.com:19302' }],
      bundlePolicy: 'max-bundle'
    })

    pc.addTransceiver('video', { direction: 'recvonly' })
    pc.addTransceiver('audio', { direction: 'recvonly' })

    // 关键：必须在发起信令/设置 remote desc 之前绑定 ontrack
    // 否则某些浏览器/网络下 track 事件可能在我们绑定前触发，导致“信令成功但一直等不到画面”
    let firstStream: MediaStream | null = null
    pc.ontrack = (e: RTCTrackEvent) => {
      if (firstStream) return
      if (e.streams && e.streams[0]) {
        firstStream = e.streams[0]
        try {
          videoEl.srcObject = firstStream
          videoEl.play().catch(() => {})
        } catch {
          // ignore
        }
      }
    }

    const offer = await pc.createOffer()
    await pc.setLocalDescription(offer)

    const offerSdp = pc.localDescription?.sdp
    if (!offerSdp) {
      pc.close()
      throw new Error('创建 WebRTC offer 失败')
    }

    // 兜底：确保 webrtcUrl 必须包含 type=play（ZLM 会对缺参返回 -300）
    let normalizedWebrtcUrl = webrtcUrl
    try {
      const u = new URL(webrtcUrl)
      if (!u.searchParams.get('type')) {
        u.searchParams.set('type', 'play')
      }
      normalizedWebrtcUrl = u.toString()
    } catch {
      // ignore，交给 fetch 处理
    }

    // 旧版策略：stream not found 自动重试 3 次（给后端/ZLM 拉流留时间）
    const MAX_WEBRTC_RETRIES = 3
    const STREAM_NOT_FOUND_RETRY_DELAY_MS = 2500
    let lastError: Error | null = null

    let answerSdp = ''
    for (let attempt = 1; attempt <= MAX_WEBRTC_RETRIES; attempt++) {
      if (attempt > 1) {
        await new Promise((r) => setTimeout(r, STREAM_NOT_FOUND_RETRY_DELAY_MS))
      }

      const res = await fetch(normalizedWebrtcUrl, {
        method: 'POST',
        headers: { 'Content-Type': 'text/plain;charset=utf-8' },
        body: offerSdp,
        mode: 'cors'
      })

      const responseText = await res.text()

      if (!res.ok) {
        lastError = new Error(`WebRTC 信令请求失败: ${res.status} ${res.statusText}`)
        if (attempt < MAX_WEBRTC_RETRIES) continue
        break
      }

      try {
        const json = JSON.parse(responseText)
        if (json.code === 0 && json.sdp) {
          answerSdp = String(json.sdp)
        } else {
          const msg = String(json.msg || '').toLowerCase()
          if (msg.includes('stream not found') && attempt < MAX_WEBRTC_RETRIES) {
            lastError = new Error(json.msg || 'stream not found')
            continue
          }
          throw new Error(json.msg || `WebRTC 信令错误: code=${json.code}`)
        }
      } catch (e: any) {
        if (responseText.trim().startsWith('v=')) {
          answerSdp = responseText.trim()
        } else if (String(e?.message || '').includes('stream not found') && attempt < MAX_WEBRTC_RETRIES) {
          lastError = e
          continue
        } else {
          lastError = e instanceof Error ? e : new Error(String(e?.message || e))
          if (attempt < MAX_WEBRTC_RETRIES) continue
          break
        }
      }

      if (!answerSdp || !answerSdp.startsWith('v=')) {
        lastError = new Error('无效的 SDP 响应')
        if (attempt < MAX_WEBRTC_RETRIES) continue
        break
      }

      lastError = null
      break
    }

    if (lastError) {
      pc.close()
      throw lastError
    }

    await pc.setRemoteDescription(new RTCSessionDescription({ type: 'answer', sdp: answerSdp }))

    return new Promise<ZlmPlayerInstance>((resolve, reject) => {
      // 旧版兜底：10 秒内没出 track 就认为超时
      const timeout = setTimeout(() => {
        if (!resolved) {
          resolved = true
          pc.close()
          reject(new Error('WebRTC 连接超时'))
        }
      }, 10000)

      let resolved = false

      pc.oniceconnectionstatechange = () => {
        if (pc.iceConnectionState === 'failed' || pc.iceConnectionState === 'closed') {
          clearTimeout(timeout)
          if (!resolved) {
            resolved = true
            pc.close()
            reject(new Error(`WebRTC 连接失败: ${pc.iceConnectionState}`))
          }
        }
      }

      pc.onconnectionstatechange = () => {
        if (pc.connectionState === 'failed') {
          clearTimeout(timeout)
          if (!resolved) {
            resolved = true
            pc.close()
            reject(new Error('WebRTC 连接失败'))
          }
        }
      }

      // 一旦 track 到来即视为成功（旧版行为）
      const successPoll = setInterval(() => {
        if (resolved) {
          clearInterval(successPoll)
          return
        }
        if (firstStream || videoEl.srcObject) {
          clearInterval(successPoll)
          clearTimeout(timeout)
          resolved = true
          resolve({
            mode: 'webrtc',
            destroy: () => {
              try {
                videoEl.srcObject = null
                videoEl.src = ''
              } catch {}
              pc.close()
              container.innerHTML = ''
            }
          })
        }
      }, 100)
    })
  }

  /**
   * 按优先级播放实时流
   * - 优先 WebRTC（若 webrtcUrl 存在且实现成功）
   * - preferWebrtc=false 时才使用 FLV（避免自动回退隐藏 WebRTC 问题）
   */
  const playLive = async (params: PlayLiveParams): Promise<ZlmPlayerInstance> => {
    const { container, urls, preferWebrtc = true } = params

    if (preferWebrtc && urls.webrtcUrl) {
      // 默认策略：优先 WebRTC，失败时不自动回退，直接抛异常让用户看到问题
      return await playWebrtc(container, urls.webrtcUrl)
    }

    // 显式禁用 WebRTC 或 webrtcUrl 为空时才走 FLV
    const flvUrl = urls.wsFlvUrl || urls.flvUrl
    if (flvUrl) {
      return await playFlv(container, flvUrl)
    }

    throw new Error('没有可用的播放地址')
  }

  const stopInstance = async (instance: ZlmPlayerInstance | null | undefined) => {
    if (!instance) return
    try {
      await instance.destroy()
    } catch (e) {
      console.warn('[ZLM] 销毁播放器异常:', e)
    }
  }

  return {
    playLive,
    stopInstance
  }
}

export default useZlmPlayer

